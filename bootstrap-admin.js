require("dotenv").config();
const bcrypt = require("bcryptjs");
const { Pool } = require("pg");

async function main() {
  const { ADMIN_ID, ADMIN_EMAIL, ADMIN_NAME, ADMIN_PASSWORD, DATABASE_URL } = process.env;
  if (!ADMIN_ID || !ADMIN_EMAIL || !ADMIN_NAME || !ADMIN_PASSWORD || !DATABASE_URL) {
    throw new Error("Set ADMIN_ID, ADMIN_EMAIL, ADMIN_NAME, ADMIN_PASSWORD and DATABASE_URL in your local .env first.");
  }
  if (ADMIN_PASSWORD.length < 12 || ADMIN_PASSWORD === "CHANGE_ME_TO_A_LONG_UNIQUE_PASSWORD") {
    throw new Error("Choose a unique admin password of at least 12 characters.");
  }
  if (ADMIN_ID.startsWith("CHANGE_ME")) throw new Error("Replace the example ADMIN_ID first.");
  const pool = new Pool({ connectionString: DATABASE_URL });
  try {
    const passwordHash = await bcrypt.hash(ADMIN_PASSWORD, 12);
    const result = await pool.query(
      `INSERT INTO users(full_name,email,role,college_id,password_hash)
       VALUES($1,$2,'admin',$3,$4)
       ON CONFLICT (college_id) DO UPDATE SET
         full_name=EXCLUDED.full_name,
         email=EXCLUDED.email,
         password_hash=EXCLUDED.password_hash,
         role='admin'
       RETURNING id,full_name,email,role,college_id`,
      [ADMIN_NAME, ADMIN_EMAIL.toLowerCase(), ADMIN_ID, passwordHash]
    );
    console.log("Admin account provisioned:", result.rows[0]);
    console.log("The password is stored as a bcrypt hash, not plain text.");
  } finally {
    await pool.end();
  }
}
main().catch((err) => { console.error(err.message); process.exit(1); });
