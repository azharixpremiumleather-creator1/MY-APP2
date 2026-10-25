CREATE TABLE IF NOT EXISTS college_ids (
  id BIGSERIAL PRIMARY KEY,
  role TEXT NOT NULL CHECK (role IN ('student','teacher','parent')),
  college_id TEXT NOT NULL UNIQUE,
  display_name TEXT,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  claimed_by BIGINT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS email_otps (
  id BIGSERIAL PRIMARY KEY,
  email TEXT NOT NULL,
  otp_hash TEXT NOT NULL,
  expires_at TIMESTAMPTZ NOT NULL,
  attempts INTEGER NOT NULL DEFAULT 0,
  verified_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS email_otps_email_idx ON email_otps(email);

CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL PRIMARY KEY,
  full_name TEXT NOT NULL,
  email TEXT NOT NULL UNIQUE,
  role TEXT NOT NULL CHECK (role IN ('student','teacher','parent','admin')),
  college_id TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  photo_url TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS attendance (
  id BIGSERIAL PRIMARY KEY,
  student_user_id BIGINT NOT NULL REFERENCES users(id),
  attendance_date DATE NOT NULL,
  status TEXT NOT NULL CHECK (status IN ('present','absent','leave')),
  recorded_by BIGINT REFERENCES users(id),
  UNIQUE(student_user_id, attendance_date)
);

CREATE TABLE IF NOT EXISTS results (
  id BIGSERIAL PRIMARY KEY,
  student_user_id BIGINT NOT NULL REFERENCES users(id),
  session TEXT NOT NULL,
  month TEXT NOT NULL,
  subject TEXT NOT NULL,
  total_marks NUMERIC(7,2) NOT NULL CHECK (total_marks > 0),
  obtained_marks NUMERIC(7,2) NOT NULL CHECK (obtained_marks >= 0 AND obtained_marks <= total_marks),
  published BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS fees (
  id BIGSERIAL PRIMARY KEY,
  student_user_id BIGINT NOT NULL REFERENCES users(id),
  description TEXT NOT NULL,
  amount_pkr NUMERIC(12,2) NOT NULL CHECK (amount_pkr >= 0),
  due_date DATE,
  status TEXT NOT NULL DEFAULT 'unpaid' CHECK (status IN ('unpaid','paid','waived')),
  paid_at TIMESTAMPTZ
);

-- Parent-child links must be explicitly created/approved by an administrator.
CREATE TABLE IF NOT EXISTS parent_student_links (
  parent_user_id BIGINT NOT NULL REFERENCES users(id),
  student_user_id BIGINT NOT NULL REFERENCES users(id),
  approved_by BIGINT REFERENCES users(id),
  approved_at TIMESTAMPTZ,
  PRIMARY KEY(parent_user_id, student_user_id)
);
