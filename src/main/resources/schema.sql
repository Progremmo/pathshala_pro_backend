-- ============================================================
-- PathshalaPro - Complete PostgreSQL Schema
-- Database: pathshalapro_db
-- Version: 1.0.0
-- ============================================================

-- CREATE DATABASE pathshalapro_db;


-- ---- Subscription Plans ----
CREATE TABLE subscription_plans (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL UNIQUE,
    description     TEXT,
    price_monthly   DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    price_annually  DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    max_students    INT,
    max_teachers    INT,
    max_classes     INT,
    storage_gb      INT DEFAULT 5,
    features        JSONB,
    is_active       BOOLEAN NOT NULL DEFAULT 1,
    is_deleted      BOOLEAN NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      BIGINT,
    updated_by      BIGINT
);

-- ---- Schools (Tenants) ----
CREATE TABLE schools (
    id                   BIGSERIAL PRIMARY KEY,
    name                 VARCHAR(200) NOT NULL,
    code                 VARCHAR(50) NOT NULL UNIQUE,
    address              TEXT,
    city                 VARCHAR(100),
    state                VARCHAR(100),
    pincode              VARCHAR(10),
    phone                VARCHAR(20),
    email                VARCHAR(150),
    website              VARCHAR(200),
    logo_url             VARCHAR(500),
    is_active            BOOLEAN NOT NULL DEFAULT 1,
    subscription_status  VARCHAR(50) NOT NULL DEFAULT 'TRIAL',
    is_deleted           BOOLEAN NOT NULL DEFAULT 0,
    created_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP,
    created_by           BIGINT,
    updated_by           BIGINT
);

-- ---- School Subscriptions ----
CREATE TABLE school_subscriptions (
    id                      BIGSERIAL PRIMARY KEY,
    school_id               BIGINT NOT NULL UNIQUE,
    plan_id                 BIGINT NOT NULL,
    status                  VARCHAR(50) NOT NULL DEFAULT 'TRIAL',
    start_date              DATE NOT NULL,
    end_date                DATE NOT NULL,
    trial_end_date          DATE,
    billing_cycle           VARCHAR(50) NOT NULL DEFAULT 'MONTHLY',
    amount_paid             DECIMAL(10,2),
    razorpay_subscription_id VARCHAR(100),
    auto_renew              BOOLEAN NOT NULL DEFAULT 1,
    notes                   TEXT,
    is_deleted              BOOLEAN NOT NULL DEFAULT 0,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP,
    created_by              BIGINT,
    updated_by              BIGINT,
    FOREIGN KEY (school_id) REFERENCES schools(id),
    FOREIGN KEY (plan_id) REFERENCES subscription_plans(id)
);

-- ---- Roles ----
CREATE TABLE roles (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    is_deleted  BOOLEAN NOT NULL DEFAULT 0,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  BIGINT,
    updated_by  BIGINT
);

-- ---- Users ----
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    phone           VARCHAR(20),
    profile_pic_url VARCHAR(500),
    is_active       BOOLEAN NOT NULL DEFAULT 1,
    is_email_verified BOOLEAN NOT NULL DEFAULT 0,
    employee_id     VARCHAR(50),
    admission_no    VARCHAR(50),
    gender          VARCHAR(10),
    date_of_birth   DATE,
    address         TEXT,
    qualification   VARCHAR(200),
    joining_date    DATE,
    school_id       BIGINT,
    class_room_id   BIGINT,
    parent_id       BIGINT,
    is_deleted      BOOLEAN NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      BIGINT,
    updated_by      BIGINT
);

-- ---- User Roles (Many-to-Many) ----
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- ---- ClassRooms ----
CREATE TABLE class_rooms (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(100) NOT NULL,
    section          VARCHAR(10),
    grade            VARCHAR(20) NOT NULL,
    academic_year    VARCHAR(20) NOT NULL,
    capacity         INT,
    room_number      VARCHAR(20),
    school_id        BIGINT NOT NULL,
    class_teacher_id BIGINT,
    is_deleted       BOOLEAN NOT NULL DEFAULT 0,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP,
    created_by       BIGINT,
    updated_by       BIGINT,
    FOREIGN KEY (school_id) REFERENCES schools(id),
    FOREIGN KEY (class_teacher_id) REFERENCES users(id)
);

-- ---- Subjects ----
CREATE TABLE subjects (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(150) NOT NULL,
    code         VARCHAR(20) NOT NULL,
    description  TEXT,
    grade        VARCHAR(20),
    credit_hours INT,
    school_id    BIGINT NOT NULL,
    is_deleted   BOOLEAN NOT NULL DEFAULT 0,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP,
    created_by   BIGINT,
    updated_by   BIGINT,
    FOREIGN KEY (school_id) REFERENCES schools(id)
);

-- ---- Timetables ----
CREATE TABLE timetables (
    id            BIGSERIAL PRIMARY KEY,
    day_of_week   VARCHAR(50) NOT NULL,
    start_time    TIME NOT NULL,
    end_time      TIME NOT NULL,
    period_number INT,
    academic_year VARCHAR(20) NOT NULL,
    school_id     BIGINT NOT NULL,
    class_room_id BIGINT NOT NULL,
    subject_id    BIGINT NOT NULL,
    teacher_id    BIGINT NOT NULL,
    is_deleted    BOOLEAN NOT NULL DEFAULT 0,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP,
    created_by    BIGINT,
    updated_by    BIGINT,
    FOREIGN KEY (school_id) REFERENCES schools(id),
    FOREIGN KEY (class_room_id) REFERENCES class_rooms(id),
    FOREIGN KEY (subject_id) REFERENCES subjects(id),
    FOREIGN KEY (teacher_id) REFERENCES users(id)
);

-- ---- Attendance ----
CREATE TABLE attendances (
    id              BIGSERIAL PRIMARY KEY,
    attendance_date DATE NOT NULL,
    status          VARCHAR(50) NOT NULL,
    remarks         VARCHAR(500),
    school_id       BIGINT NOT NULL,
    student_id      BIGINT NOT NULL,
    class_room_id   BIGINT NOT NULL,
    subject_id      BIGINT,
    marked_by       BIGINT,
    is_deleted      BOOLEAN NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      BIGINT,
    updated_by      BIGINT,
    CONSTRAINT uk_student_date UNIQUE (student_id, attendance_date),
    FOREIGN KEY (school_id) REFERENCES schools(id),
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (class_room_id) REFERENCES class_rooms(id),
    FOREIGN KEY (subject_id) REFERENCES subjects(id)
);

-- ---- Exams ----
CREATE TABLE exams (
    id                 BIGSERIAL PRIMARY KEY,
    name               VARCHAR(200) NOT NULL,
    exam_type          VARCHAR(50) NOT NULL,
    exam_date          DATE NOT NULL,
    start_time         TIME,
    duration_minutes   INT,
    total_marks        DOUBLE PRECISION NOT NULL,
    passing_marks      DOUBLE PRECISION NOT NULL,
    academic_year      VARCHAR(20) NOT NULL,
    instructions       TEXT,
    is_result_published BOOLEAN NOT NULL DEFAULT 0,
    school_id          BIGINT NOT NULL,
    class_room_id      BIGINT NOT NULL,
    subject_id         BIGINT NOT NULL,
    is_deleted         BOOLEAN NOT NULL DEFAULT 0,
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP,
    created_by         BIGINT,
    updated_by         BIGINT,
    FOREIGN KEY (school_id) REFERENCES schools(id),
    FOREIGN KEY (class_room_id) REFERENCES class_rooms(id),
    FOREIGN KEY (subject_id) REFERENCES subjects(id)
);

-- ---- Marks ----
CREATE TABLE marks (
    id              BIGSERIAL PRIMARY KEY,
    marks_obtained  DOUBLE PRECISION NOT NULL,
    grade           VARCHAR(5),
    remarks         VARCHAR(500),
    is_absent       BOOLEAN NOT NULL DEFAULT 0,
    exam_id         BIGINT NOT NULL,
    student_id      BIGINT NOT NULL,
    school_id       BIGINT NOT NULL,
    entered_by      BIGINT,
    is_deleted      BOOLEAN NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      BIGINT,
    updated_by      BIGINT,
    CONSTRAINT uk_exam_student UNIQUE (exam_id, student_id),
    FOREIGN KEY (exam_id) REFERENCES exams(id),
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (school_id) REFERENCES schools(id),
    FOREIGN KEY (entered_by) REFERENCES users(id)
);

-- ---- Notes ----
CREATE TABLE notes (
    id           BIGSERIAL PRIMARY KEY,
    title        VARCHAR(255) NOT NULL,
    description  TEXT,
    content_url  VARCHAR(500) NOT NULL,
    content_type VARCHAR(50),
    grade        VARCHAR(20),
    academic_year VARCHAR(20),
    is_visible   BOOLEAN NOT NULL DEFAULT 1,
    school_id    BIGINT NOT NULL,
    subject_id   BIGINT NOT NULL,
    uploaded_by  BIGINT NOT NULL,
    is_deleted   BOOLEAN NOT NULL DEFAULT 0,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP,
    created_by   BIGINT,
    updated_by   BIGINT,
    FOREIGN KEY (school_id) REFERENCES schools(id),
    FOREIGN KEY (subject_id) REFERENCES subjects(id),
    FOREIGN KEY (uploaded_by) REFERENCES users(id)
);

-- ---- Fee Structures ----
CREATE TABLE fee_structures (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(200) NOT NULL,
    fee_type     VARCHAR(50) NOT NULL,
    amount       DECIMAL(10,2) NOT NULL,
    frequency    VARCHAR(20) NOT NULL,
    grade        VARCHAR(20),
    academic_year VARCHAR(20) NOT NULL,
    description  TEXT,
    due_day      INT,
    school_id    BIGINT NOT NULL,
    is_deleted   BOOLEAN NOT NULL DEFAULT 0,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP,
    created_by   BIGINT,
    updated_by   BIGINT,
    FOREIGN KEY (school_id) REFERENCES schools(id)
);

-- ---- Fee Invoices ----
CREATE TABLE fee_invoices (
    id              BIGSERIAL PRIMARY KEY,
    invoice_number  VARCHAR(50) NOT NULL UNIQUE,
    total_amount    DECIMAL(10,2) NOT NULL,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    fine_amount     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    net_amount      DECIMAL(10,2) NOT NULL,
    paid_amount     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    payment_status  VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    due_date        DATE NOT NULL,
    period_month    INT,
    period_year     INT,
    academic_year   VARCHAR(20),
    remarks         TEXT,
    school_id       BIGINT NOT NULL,
    student_id      BIGINT NOT NULL,
    fee_structure_id BIGINT NOT NULL,
    is_deleted      BOOLEAN NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      BIGINT,
    updated_by      BIGINT,
    FOREIGN KEY (school_id) REFERENCES schools(id),
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (fee_structure_id) REFERENCES fee_structures(id)
);

-- ---- Payments (Razorpay) ----
CREATE TABLE payments (
    id                  BIGSERIAL PRIMARY KEY,
    amount              DECIMAL(10,2) NOT NULL,
    currency            VARCHAR(5) NOT NULL DEFAULT 'INR',
    status              VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    razorpay_order_id   VARCHAR(100),
    razorpay_payment_id VARCHAR(100),
    razorpay_signature  VARCHAR(500),
    payment_method      VARCHAR(50),
    payment_date        TIMESTAMP,
    failure_reason      TEXT,
    receipt_number      VARCHAR(100),
    notes               TEXT,
    school_id           BIGINT NOT NULL,
    fee_invoice_id      BIGINT NOT NULL,
    paid_by             BIGINT NOT NULL,
    is_deleted          BOOLEAN NOT NULL DEFAULT 0,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP,
    created_by          BIGINT,
    updated_by          BIGINT,
    FOREIGN KEY (school_id) REFERENCES schools(id),
    FOREIGN KEY (fee_invoice_id) REFERENCES fee_invoices(id),
    FOREIGN KEY (paid_by) REFERENCES users(id)
);

-- ---- Online Classes ----
CREATE TABLE online_classes (
    id                 BIGSERIAL PRIMARY KEY,
    title              VARCHAR(255) NOT NULL,
    description        TEXT,
    meeting_link       VARCHAR(500),
    meeting_id         VARCHAR(100),
    meeting_password   VARCHAR(100),
    platform           VARCHAR(50),
    scheduled_at       TIMESTAMP NOT NULL,
    duration_minutes   INT,
    is_recurring       BOOLEAN NOT NULL DEFAULT 0,
    recurrence_pattern VARCHAR(50),
    status             VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    recording_url      VARCHAR(500),
    school_id          BIGINT NOT NULL,
    class_room_id      BIGINT NOT NULL,
    subject_id         BIGINT,
    teacher_id         BIGINT NOT NULL,
    is_deleted         BOOLEAN NOT NULL DEFAULT 0,
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP,
    created_by         BIGINT,
    updated_by         BIGINT,
    FOREIGN KEY (school_id) REFERENCES schools(id),
    FOREIGN KEY (class_room_id) REFERENCES class_rooms(id),
    FOREIGN KEY (subject_id) REFERENCES subjects(id),
    FOREIGN KEY (teacher_id) REFERENCES users(id)
);

-- ---- Notifications ----
CREATE TABLE notifications (
    id                BIGSERIAL PRIMARY KEY,
    title             VARCHAR(255) NOT NULL,
    message           TEXT NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    is_read           BOOLEAN NOT NULL DEFAULT 0,
    read_at           TIMESTAMP,
    scheduled_at      TIMESTAMP,
    sent_at           TIMESTAMP,
    is_sent           BOOLEAN NOT NULL DEFAULT 0,
    reference_id      BIGINT,
    reference_type    VARCHAR(50),
    school_id         BIGINT NOT NULL,
    recipient_id      BIGINT,
    sender_id         BIGINT,
    is_deleted        BOOLEAN NOT NULL DEFAULT 0,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP,
    created_by        BIGINT,
    updated_by        BIGINT,
    FOREIGN KEY (school_id) REFERENCES schools(id),
    FOREIGN KEY (recipient_id) REFERENCES users(id),
    FOREIGN KEY (sender_id) REFERENCES users(id)
);

-- ---- Announcements ----
CREATE TABLE announcements (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    content         TEXT NOT NULL,
    target_audience VARCHAR(50) DEFAULT 'ALL',
    target_grade    VARCHAR(20),
    is_pinned       BOOLEAN NOT NULL DEFAULT 0,
    published_at    TIMESTAMP,
    expires_at      TIMESTAMP,
    attachment_url  VARCHAR(500),
    school_id       BIGINT NOT NULL,
    created_by_user BIGINT NOT NULL,
    is_deleted      BOOLEAN NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      BIGINT,
    updated_by      BIGINT,
    FOREIGN KEY (school_id) REFERENCES schools(id),
    FOREIGN KEY (created_by_user) REFERENCES users(id)
);

-- ============================================================
-- Foreign Key Additions (deferred to avoid ordering issues)
-- ============================================================
ALTER TABLE users
    ADD CONSTRAINT fk_user_school FOREIGN KEY (school_id) REFERENCES schools(id),
    ADD CONSTRAINT fk_user_classroom FOREIGN KEY (class_room_id) REFERENCES class_rooms(id),
    ADD CONSTRAINT fk_user_parent FOREIGN KEY (parent_id) REFERENCES users(id);

ALTER TABLE class_rooms
    ADD CONSTRAINT fk_class_school FOREIGN KEY (school_id) REFERENCES schools(id);


-- ---- Indexes ----
CREATE INDEX idx_school_code ON schools (code);
CREATE INDEX idx_school_active ON schools (is_active);
CREATE INDEX idx_user_email ON users (email);
CREATE INDEX idx_user_school ON users (school_id);
CREATE INDEX idx_user_class ON users (class_room_id);
CREATE INDEX idx_classroom_school ON class_rooms (school_id);
CREATE INDEX idx_subject_school ON subjects (school_id);
CREATE INDEX idx_tt_school ON timetables (school_id);
CREATE INDEX idx_tt_classroom ON timetables (class_room_id);
CREATE INDEX idx_tt_teacher ON timetables (teacher_id);
CREATE INDEX idx_att_school ON attendances (school_id);
CREATE INDEX idx_att_student ON attendances (student_id);
CREATE INDEX idx_att_date ON attendances (attendance_date);
CREATE INDEX idx_exam_school ON exams (school_id);
CREATE INDEX idx_exam_class ON exams (class_room_id);
CREATE INDEX idx_marks_exam ON marks (exam_id);
CREATE INDEX idx_marks_student ON marks (student_id);
CREATE INDEX idx_notes_school ON notes (school_id);
CREATE INDEX idx_notes_subject ON notes (subject_id);
CREATE INDEX idx_fee_structure_school ON fee_structures (school_id);
CREATE INDEX idx_invoice_school ON fee_invoices (school_id);
CREATE INDEX idx_invoice_student ON fee_invoices (student_id);
CREATE INDEX idx_invoice_number ON fee_invoices (invoice_number);
CREATE INDEX idx_payment_school ON payments (school_id);
CREATE INDEX idx_payment_invoice ON payments (fee_invoice_id);
CREATE INDEX idx_payment_razorpay_order ON payments (razorpay_order_id);
CREATE INDEX idx_oc_school ON online_classes (school_id);
CREATE INDEX idx_oc_teacher ON online_classes (teacher_id);
CREATE INDEX idx_notif_school ON notifications (school_id);
CREATE INDEX idx_notif_recipient ON notifications (recipient_id);
CREATE INDEX idx_ann_school ON announcements (school_id);