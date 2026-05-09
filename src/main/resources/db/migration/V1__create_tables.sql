-- 1. Primero tablas sin FK (users, courses, roles)
-- 2. Luego dependientes directas (groups, sections, modules)
-- 3. Luego tablas intermedias (many-to-many)
-- 4. Luego tablas finales (logs, images)
-- 5. Índices siempre al final


-- 1. Primero tablas sin FK (users, courses, roles)
-- =========================
-- USERS
-- =========================
CREATE TABLE users
(
  id           INT PRIMARY KEY,
  full_name    VARCHAR(255),
  first_name   VARCHAR(255),
  last_name    VARCHAR(255),
  email        VARCHAR(255),
  first_access TIMESTAMP WITH TIME ZONE,
  last_access  TIMESTAMP WITH TIME ZONE,
  created_at   TIMESTAMP NOT NULL,
  updated_at   TIMESTAMP NOT NULL
);

-- =========================
-- ROLES
-- =========================
CREATE TABLE roles
(
  id         INT PRIMARY KEY,
  name       VARCHAR(255),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);

-- =========================
-- COURSES
-- =========================
CREATE TABLE courses
(
  id                INT PRIMARY KEY,
  full_name         VARCHAR(255),
  short_name        VARCHAR(100),
  show_grades       BOOLEAN,
  enable_completion BOOLEAN,
  start_date        TIMESTAMP WITH TIME ZONE,
  end_date          TIMESTAMP WITH TIME ZONE,
  time_modified     TIMESTAMP WITH TIME ZONE,
  created_at        TIMESTAMP NOT NULL,
  updated_at        TIMESTAMP NOT NULL
);
-- 2. Luego dependientes directas (groups, sections, modules)
-- =========================
-- GROUPS
-- =========================
CREATE TABLE groups
(
  id         INT PRIMARY KEY,
  course_id  INT       NOT NULL,
  name       VARCHAR(255),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,

  CONSTRAINT fk_group_course FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE CASCADE
);

-- 3. Luego tablas intermedias (many-to-many)

-- =========================
-- USERS_COURSES
-- =========================
CREATE TABLE users_courses
(
  user_id            INT       NOT NULL,
  course_id          INT       NOT NULL,
  last_course_access TIMESTAMP WITH TIME ZONE,
  active             BOOLEAN,
  created_at         TIMESTAMP NOT NULL,
  updated_at         TIMESTAMP NOT NULL,

  PRIMARY KEY (user_id, course_id),

  CONSTRAINT fk_user_course_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_user_course_course FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE CASCADE
);

-- =========================
-- USERS_GROUPS
-- =========================
CREATE TABLE users_groups
(
  user_id    INT       NOT NULL,
  group_id   INT       NOT NULL,
  course_id  INT       NOT NULL,
  active     BOOLEAN,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,

  PRIMARY KEY (user_id, group_id, course_id),

  CONSTRAINT fk_users_groups_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_users_groups_group FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE CASCADE,
  CONSTRAINT fk_users_groups_course FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE CASCADE
);

-- =========================
-- USERS_ROLES
-- =========================
CREATE TABLE users_roles
(
  role_id    INT       NOT NULL,
  user_id    INT       NOT NULL,
  course_id  INT       NOT NULL,
  active     BOOLEAN,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,

  PRIMARY KEY (user_id, role_id, course_id),

  CONSTRAINT fk_users_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_users_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
  CONSTRAINT fk_users_roles_course FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE CASCADE
);


-- =========================
-- SECTIONS
-- =========================
CREATE TABLE sections
(
  id         INT PRIMARY KEY,
  course_id  INT       NOT NULL,
  name       VARCHAR(255),
  summary    CLOB,
  position   INT,
  visible    BOOLEAN,
  active     BOOLEAN,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_sections_course FOREIGN KEY (course_id) REFERENCES courses (id)
);

-- =========================
-- MODULES
-- =========================
CREATE TABLE modules
(
  id          INT PRIMARY KEY,
  section_id  INT,
  course_id   INT       NOT NULL,
  name        VARCHAR(255),
  mod_name    VARCHAR(50),
  url         VARCHAR(2048),
  description CLOB,
  visible     BOOLEAN,
  active     BOOLEAN,
  created_at  TIMESTAMP NOT NULL,
  updated_at  TIMESTAMP NOT NULL,
  CONSTRAINT fk_modules_section FOREIGN KEY (section_id) REFERENCES sections (id),

  CONSTRAINT fk_modules_course FOREIGN KEY (course_id) REFERENCES courses (id)
);

-- =========================
-- SITES
-- =========================
CREATE TABLE sites
(
  id             INT PRIMARY KEY,
  host           VARCHAR(2048),
  site_name      VARCHAR(255),
  version_number VARCHAR(100),
  type_of_login  VARCHAR(100),
  launch_url     VARCHAR(2048),
  user_id        INT,
  user_name      VARCHAR(255),
  full_name      VARCHAR(255),
  first_name     VARCHAR(255),
  last_name      VARCHAR(255),
  user_image_url VARCHAR(2048),
  created_at     TIMESTAMP NOT NULL,
  updated_at     TIMESTAMP NOT NULL
);

-- 4. Luego tablas finales (logs, images)

-- =========================
-- USERS_IMAGES
-- =========================
CREATE TABLE users_images
(
  user_id    INT PRIMARY KEY,
  image_data BLOB        NOT NULL,
  image_hash VARCHAR(64) NOT NULL,
  created_at TIMESTAMP   NOT NULL,
  updated_at TIMESTAMP   NOT NULL,
  CONSTRAINT fk_user_images_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- =========================
-- LOGS
-- =========================
CREATE TABLE logs
(
  id           INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  timestamp    INT     NOT NULL,
  user_id      INT,
  course_id    INT     NOT NULL,
  component_id TINYINT NOT NULL,
  event_id     TINYINT NOT NULL,
  moduleId     INT,

  CONSTRAINT fk_log_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_log_course FOREIGN KEY (course_id) REFERENCES courses (id),
  CONSTRAINT fk_log_module FOREIGN KEY (module_id) REFERENCES modules (id)
);

-- 5. Índices siempre al final
-- =========================
-- INDEXES
-- =========================

CREATE INDEX idx_user_course_user_id ON users_courses (user_id);
CREATE INDEX idx_user_course_course_id ON users_courses (course_id);

CREATE INDEX idx_logs_user_id ON logs (user_id);
CREATE INDEX idx_logs_course_id ON logs (course_id);
CREATE INDEX idx_logs_course_module_id ON logs (course_module_id);
CREATE INDEX idx_logs_timestamp ON logs (timestamp);
CREATE INDEX idx_logs_user_timestamp ON logs (user_id, timestamp);
