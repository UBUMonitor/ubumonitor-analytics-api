CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    full_name TEXT,
    first_name TEXT,
    last_name TEXT,
    email TEXT,
    first_access BIGINT,
    last_access BIGINT,
    created_at BIGINT DEFAULT 0 NOT NULL,
    updated_at BIGINT DEFAULT 0 NOT NULL
);

CREATE TABLE roles (
    id BIGINT PRIMARY KEY,
    name TEXT,
    created_at BIGINT DEFAULT 0 NOT NULL,
    updated_at BIGINT DEFAULT 0 NOT NULL
);

CREATE TABLE courses (
    id BIGINT PRIMARY KEY,
    full_name TEXT,
    short_name TEXT,
    show_grades BOOLEAN NOT NULL DEFAULT FALSE,
    enable_completion BOOLEAN NOT NULL DEFAULT FALSE,
    start_date BIGINT,
    end_date BIGINT,
    time_modified BIGINT,
    last_sync BIGINT,
    created_at BIGINT DEFAULT 0 NOT NULL,
    updated_at BIGINT DEFAULT 0 NOT NULL,
    CONSTRAINT ck_courses_dates
        CHECK (end_date IS NULL OR start_date IS NULL OR end_date >= start_date)
);

CREATE TABLE course_groups (
    id BIGINT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    name TEXT,
    description TEXT,
    created_at BIGINT DEFAULT 0 NOT NULL,
    updated_at BIGINT DEFAULT 0 NOT NULL,
    CONSTRAINT uq_groups_id_course UNIQUE (id, course_id)
);

CREATE INDEX idx_groups_course_id ON course_groups (course_id);

CREATE TABLE users_courses (
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    last_course_access BIGINT,
    is_favourite BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at BIGINT DEFAULT 0 NOT NULL,
    updated_at BIGINT DEFAULT 0 NOT NULL,
    PRIMARY KEY (user_id, course_id)
);

CREATE INDEX idx_uc_course_id ON users_courses (course_id);

CREATE TABLE users_course_groups (
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at BIGINT DEFAULT 0 NOT NULL,
    updated_at BIGINT DEFAULT 0 NOT NULL,
    PRIMARY KEY (user_id, group_id, course_id)
);

CREATE INDEX idx_ug_group_id ON users_course_groups (group_id);
CREATE INDEX idx_ug_course_id ON users_course_groups (course_id);
CREATE INDEX idx_ug_user_id ON users_course_groups (user_id);

CREATE TABLE users_roles (
    role_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at BIGINT DEFAULT 0 NOT NULL,
    updated_at BIGINT DEFAULT 0 NOT NULL,
    PRIMARY KEY (user_id, role_id, course_id)
);

CREATE INDEX idx_ur_course_id ON users_roles (course_id);
CREATE INDEX idx_ur_role_id ON users_roles (role_id);
