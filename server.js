require("dotenv").config();
const express = require("express");
const helmet = require("helmet");
const cors = require("cors");
const rateLimit = require("express-rate-limit");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const nodemailer = require("nodemailer");
const { Pool } = require("pg");

const app = express();
app.use(helmet());
app.use(cors({ origin: process.env.APP_ORIGIN || "http://localhost:3000" }));
app.use(express.json({ limit: "1mb" }));
app.use("/auth", rateLimit({ windowMs: 15 * 60 * 1000, limit: 20 }));
const pool = new Pool({ connectionString: process.env.DATABASE_URL });

function normalizeEmail(email) {
  return String(email || "").trim().toLowerCase();
}
function validRole(role) {
  return ["student", "teacher", "parent"].includes(role);
}
function makeToken(user) {
  if (!process.env.JWT_SECRET || process.env.JWT_SECRET.length < 32) {
    throw new Error("JWT_SECRET must be configured with at least 32 characters");
  }
  return jwt.sign(
    { sub: String(user.id), role: user.role, email: user.email },
    process.env.JWT_SECRET,
    { expiresIn: "2h", issuer: "azharix-api", audience: "azharix-app" }
  );
}
function authRequired(req, res, next) {
  try {
    const header = req.headers.authorization || "";
    const token = header.startsWith("Bearer ") ? header.slice(7) : "";
    req.auth = jwt.verify(token, process.env.JWT_SECRET, {
      issuer: "azharix-api", audience: "azharix-app"
    });
    next();
  } catch {
    res.status(401).json({ error: "براہ کرم دوبارہ لاگ اِن کریں۔" });
  }
}
function mailer() {
  const required = ["SMTP_HOST", "SMTP_USER", "SMTP_PASS", "MAIL_FROM"];
  if (required.some((key) => !process.env[key])) {
    throw new Error("SMTP settings are missing. Configure backend/.env first.");
  }
  return nodemailer.createTransport({
    host: process.env.SMTP_HOST,
    port: Number(process.env.SMTP_PORT || 587),
    secure: String(process.env.SMTP_SECURE).toLowerCase() === "true",
    auth: { user: process.env.SMTP_USER, pass: process.env.SMTP_PASS }
  });
}

app.get("/health", (_req, res) => res.json({ status: "ok", service: "AZHARIX API" }));

// Sends a six-digit OTP. The code is stored as a bcrypt hash and expires.
app.post("/auth/request-otp", async (req, res) => {
  try {
    const email = normalizeEmail(req.body.email);
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      return res.status(400).json({ error: "درست ای میل ایڈریس درج کریں۔" });
    }
    const otp = String(require("crypto").randomInt(0, 1000000)).padStart(6, "0");
    const otpHash = await bcrypt.hash(otp, 10);
    const ttl = Math.max(5, Math.min(15, Number(process.env.OTP_TTL_MINUTES || 10)));
    await pool.query(
      "INSERT INTO email_otps(email, otp_hash, expires_at) VALUES($1,$2,NOW() + ($3 || ' minutes')::interval)",
      [email, otpHash, ttl]
    );
    await mailer().sendMail({
      from: process.env.MAIL_FROM,
      to: email,
      subject: "Your AZHARIX verification code",
      text: `Your AZHARIX verification code is ${otp}. It expires in ${ttl} minutes. Do not share this code.`
    });
    res.json({ message: "اگر ای میل درست ہے تو تصدیقی کوڈ بھیج دیا گیا ہے۔", expiresInMinutes: ttl });
  } catch (err) {
    console.error("OTP request failed:", err.message);
    res.status(503).json({ error: "ای میل سروس ابھی تیار نہیں۔ سرور کی SMTP سیٹنگ چیک کریں۔" });
  }
});

app.post("/auth/verify-otp", async (req, res) => {
  const email = normalizeEmail(req.body.email);
  const otp = String(req.body.otp || "").trim();
  if (!/^[0-9]{6}$/.test(otp)) return res.status(400).json({ error: "چھ ہندسوں کا OTP درج کریں۔" });
  const client = await pool.connect();
  try {
    await client.query("BEGIN");
    const result = await client.query(
      `SELECT id, otp_hash, attempts FROM email_otps
       WHERE email=$1 AND verified_at IS NULL AND expires_at > NOW()
       ORDER BY created_at DESC LIMIT 1 FOR UPDATE`,
      [email]
    );
    if (!result.rowCount) {
      await client.query("ROLLBACK");
      return res.status(400).json({ error: "کوڈ ختم ہو گیا ہے یا موجود نہیں۔ نیا OTP حاصل کریں۔" });
    }
    const row = result.rows[0];
    if (row.attempts >= 5) {
      await client.query("ROLLBACK");
      return res.status(429).json({ error: "زیادہ کوششیں ہو چکی ہیں۔ نیا OTP حاصل کریں۔" });
    }
    const matches = await bcrypt.compare(otp, row.otp_hash);
    if (!matches) {
      await client.query("UPDATE email_otps SET attempts=attempts+1 WHERE id=$1", [row.id]);
      await client.query("COMMIT");
      return res.status(400).json({ error: "OTP درست نہیں ہے۔" });
    }
    await client.query("UPDATE email_otps SET verified_at=NOW() WHERE id=$1", [row.id]);
    await client.query("COMMIT");
    const verificationToken = jwt.sign(
      { email, purpose: "registration-email-verified" },
      process.env.JWT_SECRET,
      { expiresIn: "15m", issuer: "azharix-api", audience: "azharix-registration" }
    );
    res.json({ message: "ای میل کی تصدیق مکمل ہوگئی۔", verificationToken });
  } catch (err) {
    await client.query("ROLLBACK");
    console.error(err);
    res.status(500).json({ error: "سرور میں مسئلہ پیش آیا۔" });
  } finally {
    client.release();
  }
});

// Registration requires a college-issued ID that exists, matches the selected role,
// is active, and has not already been claimed. Parent-child access is admin-approved.
app.post("/auth/register", async (req, res) => {
  const { fullName, email: rawEmail, role, collegeId, password, photoUrl, verificationToken } = req.body || {};
  const email = normalizeEmail(rawEmail);
  if (!String(fullName || "").trim() || String(fullName).trim().length > 100) {
    return res.status(400).json({ error: "پورا نام درج کریں۔" });
  }
  if (!validRole(role)) return res.status(400).json({ error: "درست کردار منتخب کریں۔" });
  if (!collegeId || String(collegeId).trim().length < 3) {
    return res.status(400).json({ error: "کالج کی جاری کردہ آئی ڈی ضروری ہے۔" });
  }
  if (typeof password !== "string" || password.length < 8) {
    return res.status(400).json({ error: "پاس ورڈ کم از کم 8 حروف کا ہونا چاہیے۔" });
  }
  let verified;
  try {
    verified = jwt.verify(verificationToken, process.env.JWT_SECRET, {
      issuer: "azharix-api", audience: "azharix-registration"
    });
  } catch {
    return res.status(400).json({ error: "پہلے ای میل OTP کی تصدیق کریں۔" });
  }
  if (verified.purpose !== "registration-email-verified" || verified.email !== email) {
    return res.status(400).json({ error: "ای میل تصدیقی ٹوکن درست نہیں۔" });
  }

  const client = await pool.connect();
  try {
    await client.query("BEGIN");
    const idResult = await client.query(
      "SELECT id FROM college_ids WHERE college_id=$1 AND role=$2 AND active=TRUE AND claimed_by IS NULL FOR UPDATE",
      [String(collegeId).trim(), role]
    );
    if (!idResult.rowCount) {
      await client.query("ROLLBACK");
      return res.status(403).json({ error: "یہ کالج آئی ڈی غیر فعال، غلط یا پہلے استعمال ہو چکی ہے۔ کالج انتظامیہ سے رابطہ کریں۔" });
    }
    const passwordHash = await bcrypt.hash(password, 12);
    const created = await client.query(
      `INSERT INTO users(full_name,email,role,college_id,password_hash,photo_url)
       VALUES($1,$2,$3,$4,$5,$6)
       RETURNING id,full_name,email,role,college_id,photo_url`,
      [String(fullName).trim(), email, role, String(collegeId).trim(), passwordHash, photoUrl || null]
    );
    await client.query("UPDATE college_ids SET claimed_by=$1 WHERE id=$2", [created.rows[0].id, idResult.rows[0].id]);
    await client.query("COMMIT");
    res.status(201).json({ user: created.rows[0], token: makeToken(created.rows[0]) });
  } catch (err) {
    await client.query("ROLLBACK");
    if (err.code === "23505") return res.status(409).json({ error: "یہ ای میل یا آئی ڈی پہلے سے استعمال ہو رہی ہے۔" });
    console.error(err);
    res.status(500).json({ error: "اکاؤنٹ نہیں بن سکا۔" });
  } finally {
    client.release();
  }
});


// Login using email or college-issued ID and password.
app.post("/auth/login", async (req, res) => {
  const identifier = String(req.body.identifier || "").trim();
  const password = req.body.password;
  if (!identifier || typeof password !== "string") {
    return res.status(400).json({ error: "ID/email اور پاس ورڈ درج کریں۔" });
  }
  try {
    const result = await pool.query(
      `SELECT id,full_name,email,role,college_id,password_hash,photo_url
       FROM users WHERE LOWER(email)=LOWER($1) OR LOWER(college_id)=LOWER($1)
       LIMIT 1`,
      [identifier]
    );
    if (!result.rowCount) return res.status(401).json({ error: "لاگ اِن کی تفصیلات درست نہیں۔" });
    const user = result.rows[0];
    const matches = await bcrypt.compare(password, user.password_hash);
    if (!matches) return res.status(401).json({ error: "لاگ اِن کی تفصیلات درست نہیں۔" });
    const token = makeToken(user);
    delete user.password_hash;
    res.json({ user, token });
  } catch (err) {
    console.error("Login failed:", err.message);
    res.status(500).json({ error: "لاگ اِن نہیں ہو سکا۔" });
  }
});

app.get("/me", authRequired, async (req, res) => {
  try {
    const result = await pool.query(
      "SELECT id,full_name,email,role,college_id,photo_url,created_at FROM users WHERE id=$1",
      [req.auth.sub]
    );
    if (!result.rowCount) return res.status(404).json({ error: "اکاؤنٹ نہیں ملا۔" });
    res.json({ user: result.rows[0] });
  } catch {
    res.status(500).json({ error: "ریکارڈ حاصل نہیں ہو سکا۔" });
  }
});

app.get("/student/:id/summary", authRequired, async (req, res) => {
  // Students can view their own data; parents need an approved link; staff/admin
  // permissions should be expanded carefully before production deployment.
  const targetId = String(req.params.id);
  try {
    let allowed = req.auth.role === "admin" || (req.auth.role === "student" && req.auth.sub === targetId);
    if (req.auth.role === "parent") {
      const link = await pool.query(
        "SELECT 1 FROM parent_student_links WHERE parent_user_id=$1 AND student_user_id=$2 AND approved_at IS NOT NULL",
        [req.auth.sub, targetId]
      );
      allowed = link.rowCount > 0;
    }
    if (!allowed) return res.status(403).json({ error: "اس طالب علم کا ریکارڈ دیکھنے کی اجازت نہیں۔" });
    const [attendance, fees, results] = await Promise.all([
      pool.query("SELECT attendance_date,status FROM attendance WHERE student_user_id=$1 ORDER BY attendance_date DESC LIMIT 60", [targetId]),
      pool.query("SELECT description,amount_pkr,due_date,status,paid_at FROM fees WHERE student_user_id=$1 ORDER BY due_date DESC NULLS LAST", [targetId]),
      pool.query("SELECT session,month,subject,total_marks,obtained_marks FROM results WHERE student_user_id=$1 AND published=TRUE ORDER BY session DESC,month DESC,subject", [targetId])
    ]);
    res.json({ attendance: attendance.rows, fees: fees.rows, results: results.rows });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: "ریکارڈ حاصل نہیں ہو سکا۔" });
  }
});

const port = Number(process.env.PORT || 4000);
app.listen(port, () => console.log(`AZHARIX API listening on port ${port}`));
