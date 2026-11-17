-- 1. Tablas sin FK
-- =====================================================================

CREATE TABLE users
(
    id           BIGINT PRIMARY KEY,
    full_name    TEXT,
    first_name   TEXT,
    last_name    TEXT,
    email        TEXT,
    first_access BIGINT,
    last_access  BIGINT,
    created_at   BIGINT DEFAULT 0 NOT NULL,
    updated_at   BIGINT DEFAULT 0 NOT NULL
);

CREATE TABLE roles
(
    id         BIGINT PRIMARY KEY,
    name       TEXT,
    created_at BIGINT DEFAULT 0 NOT NULL,
    updated_at BIGINT DEFAULT 0 NOT NULL
);

CREATE TABLE courses
(
    id                BIGINT PRIMARY KEY,
    full_name         TEXT,
    short_name        TEXT,
    show_grades       BOOLEAN NOT NULL DEFAULT FALSE,
    enable_completion BOOLEAN NOT NULL DEFAULT FALSE,
    start_date        BIGINT,
    end_date          BIGINT,
    time_modified     BIGINT,
    last_sync         BIGINT,
    created_at        BIGINT           DEFAULT 0 NOT NULL,
    updated_at        BIGINT           DEFAULT 0 NOT NULL,

    CONSTRAINT ck_courses_dates CHECK (end_date IS NULL OR start_date IS NULL OR end_date >= start_date)
);

-- 2. Dependientes directas
-- =========================

CREATE TABLE course_groups
(
    id          BIGINT PRIMARY KEY,
    course_id   BIGINT           NOT NULL,
    name        TEXT,
    description TEXT,
    created_at  BIGINT DEFAULT 0 NOT NULL,
    updated_at  BIGINT DEFAULT 0 NOT NULL,

    CONSTRAINT uq_groups_id_course UNIQUE (id, course_id)
);

CREATE INDEX idx_groups_course_id ON course_groups (course_id);

-- 3. Tablas intermedias (many-to-many)
-- =========================
CREATE TABLE grades
(
    id         BIGINT PRIMARY KEY,
    parent_id  BIGINT,
    category_id BIGINT UNIQUE,
    parent_category_id BIGINT,
    course_id  BIGINT           NOT NULL,
    name       TEXT,
    item_type  TEXT,
    module_id  BIGINT,
    position   INTEGER,
    grade_min  NUMERIC,
    grade_max  NUMERIC,
    active     BOOLEAN          NOT NULL DEFAULT TRUE,
    created_at BIGINT DEFAULT 0 NOT NULL,
    updated_at BIGINT DEFAULT 0 NOT NULL,

    CONSTRAINT ck_grades_range CHECK (grade_min IS NULL OR grade_max IS NULL OR grade_max >= grade_min)
);

CREATE INDEX idx_grades_course_id ON grades (course_id);
CREATE INDEX idx_grades_parent_id ON grades (parent_id);


CREATE TABLE forums
(
    id                     BIGINT PRIMARY KEY,
    course_id              BIGINT  NOT NULL,
    module_id              BIGINT  NOT NULL, -- cmid / coursemodule -> modules.id (sin FK)
    name                   TEXT,
    intro                  TEXT,
    type                   TEXT,
    visible                BOOLEAN NOT NULL DEFAULT TRUE,
    group_mode             INTEGER,
    grouping_id            BIGINT,
    due_date               BIGINT,
    cutoff_date            BIGINT,
    assessed               INTEGER,
    scale                  INTEGER,
    grade_forum            INTEGER,
    max_bytes              BIGINT,
    max_attachments        INTEGER,
    force_subscribe        INTEGER,
    tracking_type          INTEGER,
    warn_after             BIGINT,
    block_after            BIGINT,
    block_period           BIGINT,
    lock_discussion_after  BIGINT,
    completion_discussions INTEGER,
    completion_replies     INTEGER,
    completion_posts       INTEGER,
    time_modified          BIGINT,
    active                 BOOLEAN NOT NULL DEFAULT TRUE,
    created_at             BIGINT           DEFAULT 0 NOT NULL,
    updated_at             BIGINT           DEFAULT 0 NOT NULL,

    CONSTRAINT uq_forums_module UNIQUE (module_id)
);

CREATE INDEX idx_forums_course_id ON forums (course_id);

CREATE TABLE forums_discussions
(
    id            BIGINT PRIMARY KEY,          -- campo "discussion" (no el "id", que es el del primer post)
    forum_id      BIGINT  NOT NULL,
    name          TEXT,
    group_id      BIGINT  NOT NULL DEFAULT -1, -- -1 = todos los participantes
    user_id       BIGINT,                      -- autor
    time_start    BIGINT,
    time_end      BIGINT,
    time_modified BIGINT,
    pinned        BOOLEAN NOT NULL DEFAULT FALSE,
    locked        BOOLEAN NOT NULL DEFAULT FALSE,
    active        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    BIGINT           DEFAULT 0 NOT NULL,
    updated_at    BIGINT           DEFAULT 0 NOT NULL
);

CREATE INDEX idx_fd_forum_id ON forums_discussions (forum_id);
CREATE INDEX idx_fd_user_id ON forums_discussions (user_id);

CREATE TABLE forums_posts
(
    id               BIGINT PRIMARY KEY,
    discussion_id    BIGINT  NOT NULL,
    parent_id        BIGINT, -- NULL = primer post de la discusion
    user_id          BIGINT,
    subject          TEXT,
    message          TEXT,
    message_format   INTEGER,
    has_attachment   BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted       BOOLEAN NOT NULL DEFAULT FALSE,
    is_private_reply BOOLEAN NOT NULL DEFAULT FALSE,
    time_created     BIGINT,
    time_modified    BIGINT,
    active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at       BIGINT           DEFAULT 0 NOT NULL,
    updated_at       BIGINT           DEFAULT 0 NOT NULL
);

CREATE INDEX idx_fp_discussion_id ON forums_posts (discussion_id);
CREATE INDEX idx_fp_parent_id ON forums_posts (parent_id);
CREATE INDEX idx_fp_user_id ON forums_posts (user_id);

CREATE TABLE users_courses
(
    user_id            BIGINT  NOT NULL,
    course_id          BIGINT  NOT NULL,
    last_course_access BIGINT,
    is_favourite       BOOLEAN NOT NULL DEFAULT FALSE,
    active             BOOLEAN NOT NULL DEFAULT TRUE,
    created_at         BIGINT           DEFAULT 0 NOT NULL,
    updated_at         BIGINT           DEFAULT 0 NOT NULL,

    PRIMARY KEY (user_id, course_id)

);
CREATE INDEX idx_uc_course_id ON users_courses (course_id);

CREATE TABLE users_course_groups
(
    user_id    BIGINT           NOT NULL,
    course_id  BIGINT           NOT NULL,
    group_id   BIGINT           NOT NULL,
    active     BOOLEAN          NOT NULL DEFAULT TRUE,
    created_at BIGINT DEFAULT 0 NOT NULL,
    updated_at BIGINT DEFAULT 0 NOT NULL,

    PRIMARY KEY (user_id, group_id, course_id)

);

CREATE INDEX idx_ug_group_id ON users_course_groups (group_id);
CREATE INDEX idx_ug_course_id ON users_course_groups (course_id);
CREATE INDEX idx_ug_user_id ON users_course_groups (user_id);

CREATE TABLE users_roles
(
    role_id    BIGINT           NOT NULL,
    user_id    BIGINT           NOT NULL,
    course_id  BIGINT           NOT NULL,
    active     BOOLEAN          NOT NULL DEFAULT TRUE,
    created_at BIGINT DEFAULT 0 NOT NULL,
    updated_at BIGINT DEFAULT 0 NOT NULL,

    PRIMARY KEY (user_id, role_id, course_id)

);

CREATE INDEX idx_ur_course_id ON users_roles (course_id);
CREATE INDEX idx_ur_role_id ON users_roles (role_id);


CREATE TABLE users_grades
(
    grade_id   BIGINT           NOT NULL,
    user_id    BIGINT           NOT NULL,
    grade      NUMERIC,
    percentage NUMERIC,
    active     BOOLEAN          NOT NULL DEFAULT TRUE,
    created_at BIGINT DEFAULT 0 NOT NULL,
    updated_at BIGINT DEFAULT 0 NOT NULL,

    PRIMARY KEY (grade_id, user_id)

);

CREATE INDEX idx_ugr_user_id ON users_grades (user_id);

-- 4. Estructura jerarquica
-- =========================

CREATE TABLE sections
(
    id         BIGINT PRIMARY KEY,
    course_id  BIGINT  NOT NULL,
    name       TEXT,
    summary    TEXT,
    position   INTEGER,
    visible    BOOLEAN NOT NULL DEFAULT TRUE,
    active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at BIGINT           DEFAULT 0 NOT NULL,
    updated_at BIGINT           DEFAULT 0 NOT NULL
);

CREATE INDEX idx_sections_course_id ON sections (course_id);

CREATE TABLE modules
(
    id          BIGINT PRIMARY KEY,
    section_id  BIGINT  NOT NULL,
    name        TEXT,
    mod_name    TEXT,
    instance    BIGINT,
    url         TEXT,
    description TEXT,
    visible     BOOLEAN NOT NULL DEFAULT TRUE,
    position    INTEGER,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  BIGINT           DEFAULT 0 NOT NULL,
    updated_at  BIGINT           DEFAULT 0 NOT NULL
);

CREATE INDEX idx_modules_section_id ON modules (section_id);

CREATE TABLE users_activities
(
    module_id       BIGINT  NOT NULL,
    user_id         BIGINT  NOT NULL,
    course_id       BIGINT  NOT NULL,
    state           TEXT    NOT NULL,
    time_completed  BIGINT,
    tracking        TEXT    NOT NULL,
    override_by     BIGINT,
    value_used      BOOLEAN NOT NULL DEFAULT FALSE,
    is_tracked_user BOOLEAN NOT NULL,
    user_visible    BOOLEAN NOT NULL,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      BIGINT           DEFAULT 0 NOT NULL,
    updated_at      BIGINT           DEFAULT 0 NOT NULL,
    PRIMARY KEY (module_id, user_id)
);

CREATE TABLE assignment_submissions
(
    submission_id  BIGINT  NOT NULL,
    assignment_id  BIGINT  NOT NULL,
    course_id      BIGINT  NOT NULL,
    user_id        BIGINT  NOT NULL,
    attempt_number INTEGER NOT NULL,
    time_created   BIGINT,
    time_modified  BIGINT,
    time_started   BIGINT,
    status         TEXT    NOT NULL,
    latest         BOOLEAN,
    grading_status TEXT,
    active         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at     BIGINT  NOT NULL DEFAULT 0,
    updated_at     BIGINT  NOT NULL DEFAULT 0,

    PRIMARY KEY (submission_id, user_id)
);

CREATE TABLE course_assignments
(
    id                            BIGINT  PRIMARY KEY,
    course_id                     BIGINT  NOT NULL,
    module_id                     BIGINT  NOT NULL,
    name                          TEXT    NOT NULL,
    intro                         TEXT,
    time_modified                 BIGINT,
    due_date                      BIGINT,
    submissions_from_date         BIGINT,
    cutoff_date                   BIGINT,
    grading_due_date              BIGINT,
    active                        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at                    BIGINT  NOT NULL DEFAULT 0,
    updated_at                    BIGINT  NOT NULL DEFAULT 0
);

CREATE INDEX idx_course_assignments_course_id
    ON course_assignments (course_id);

CREATE INDEX idx_assignment_submissions_course_id
    ON assignment_submissions (course_id);
CREATE INDEX idx_assignment_submissions_assignment_user
    ON assignment_submissions (assignment_id, user_id);

-- 5. SITES
-- =========================

CREATE TABLE sites
(
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id        BIGINT           NOT NULL,
    user_name      TEXT,
    host           TEXT,
    site_name      TEXT,
    version_number TEXT,
    release        TEXT,
    version        TEXT,
    time_zone      TEXT,
    created_at     BIGINT DEFAULT 0 NOT NULL,
    updated_at     BIGINT DEFAULT 0 NOT NULL
);

CREATE TABLE actual_user
(
    id                  BIGINT PRIMARY KEY,
    user_name           TEXT,
    full_name           TEXT,
    first_name          TEXT,
    last_name           TEXT,
    initials            TEXT,
    email               TEXT,
    user_image_url      TEXT,
    time_zone           TEXT,
    is_server_time_zone BOOLEAN DEFAULT FALSE NOT NULL,
    lang                TEXT,
    created_at          BIGINT  DEFAULT 0     NOT NULL,
    updated_at          BIGINT  DEFAULT 0     NOT NULL,
    CONSTRAINT uq_actual_user_user_name UNIQUE (user_name)
);
-- 6. Tablas finales
-- =========================

CREATE TABLE users_images
(
    user_id      BIGINT PRIMARY KEY,
    image_data   BLOB             NOT NULL,
    image_hash   TEXT             NOT NULL CHECK (length(image_hash) = 64), -- SHA-256 en hexadecimal
    content_type TEXT             NOT NULL,
    created_at   BIGINT DEFAULT 0 NOT NULL,
    updated_at   BIGINT DEFAULT 0 NOT NULL
);

CREATE TABLE calendars_events
(
    id              BIGINT PRIMARY KEY,
    name            TEXT             NOT NULL,
    description     TEXT,
    course_id       BIGINT           NOT NULL,
    category_id     BIGINT,
    group_id        BIGINT,
    user_id         BIGINT           NOT NULL,
    repeat_id       BIGINT,
    module_name     TEXT,
    instance        BIGINT,
    event_type      TEXT             NOT NULL,
    time_start      BIGINT           NOT NULL,
    time_duration   BIGINT           NOT NULL,
    visible         INTEGER          NOT NULL,
    sequence        INTEGER          NOT NULL,
    time_modified   BIGINT           NOT NULL,
    subscription_id BIGINT,
    uuid            TEXT,
    created_at      BIGINT DEFAULT 0 NOT NULL,
    updated_at      BIGINT DEFAULT 0 NOT NULL
);

CREATE INDEX idx_calendars_events_course_id ON calendars_events (course_id);
CREATE INDEX idx_calendars_events_time_start ON calendars_events (time_start);

CREATE TABLE logs_components
(
    id   SMALLINT PRIMARY KEY,
    code TEXT NOT NULL UNIQUE, -- solo ASCII
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE logs_events
(
    id   SMALLINT PRIMARY KEY,
    code TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL -- no es unico: Moodle repite nombres entre eventos distintos
);

CREATE TABLE logs_origins
(
    id   TINYINT PRIMARY KEY, -- IDs fijos: logs.origin_id se valida con CHECK
    code TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL
);

-- Un patron GROK por fila. Un mismo (componente, evento) puede tener varias
-- filas (una por patron); basta con que case una. Sin patron: pattern NULL.
CREATE TABLE logs_components_events
(
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    component_id SMALLINT NOT NULL,
    event_id     SMALLINT NOT NULL,
    pattern      TEXT,

    CONSTRAINT uq_lce_component_event_pattern UNIQUE (component_id, event_id, pattern),
    CONSTRAINT fk_lce_component FOREIGN KEY (component_id) REFERENCES logs_components (id),
    CONSTRAINT fk_lce_event FOREIGN KEY (event_id) REFERENCES logs_events (id)
);

CREATE INDEX idx_lce_event_id ON logs_components_events (event_id);

-- =====================================================================
-- LOGS
-- =====================================================================

CREATE TABLE logs
(
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    logged_at    BIGINT   NOT NULL,
    user_id      BIGINT NULL,
    course_id    BIGINT   NOT NULL,
    component_id SMALLINT NOT NULL,
    event_id     SMALLINT NOT NULL,
    module_id    BIGINT NULL,
    origin_id    TINYINT  NOT NULL,
    ip_address   TEXT NULL


    -- Se mantienen SIN FK por rendimiento en la tabla grande:
    --   course_id -> courses(id), user_id -> users(id), module_id -> modules(id)
);
CREATE INDEX idx_logs_course_user_time ON logs (course_id, user_id, logged_at);
CREATE INDEX idx_logs_course_time ON logs (course_id, logged_at);
CREATE INDEX idx_logs_course_component_event ON logs (course_id, component_id, event_id);

CREATE TRIGGER trg_users_set_timestamps
    AFTER INSERT
    ON users
    FOR EACH ROW WHEN NEW.created_at = 0 OR NEW.updated_at = 0
BEGIN
    UPDATE users
    SET created_at = CASE WHEN NEW.created_at = 0 THEN unixepoch() ELSE NEW.created_at END,
        updated_at = CASE WHEN NEW.updated_at = 0 THEN unixepoch() ELSE NEW.updated_at END
    WHERE rowid = NEW.rowid;
END;

CREATE TRIGGER trg_roles_set_timestamps
    AFTER INSERT
    ON roles
    FOR EACH ROW WHEN NEW.created_at = 0 OR NEW.updated_at = 0
BEGIN
    UPDATE roles
    SET created_at = CASE WHEN NEW.created_at = 0 THEN unixepoch() ELSE NEW.created_at END,
        updated_at = CASE WHEN NEW.updated_at = 0 THEN unixepoch() ELSE NEW.updated_at END
    WHERE rowid = NEW.rowid;
END;

CREATE TRIGGER trg_courses_set_timestamps
    AFTER INSERT
    ON courses
    FOR EACH ROW WHEN NEW.created_at = 0 OR NEW.updated_at = 0
BEGIN
    UPDATE courses
    SET created_at = CASE WHEN NEW.created_at = 0 THEN unixepoch() ELSE NEW.created_at END,
        updated_at = CASE WHEN NEW.updated_at = 0 THEN unixepoch() ELSE NEW.updated_at END
    WHERE rowid = NEW.rowid;
END;

CREATE TRIGGER trg_groups_set_timestamps
    AFTER INSERT
    ON course_groups
    FOR EACH ROW WHEN NEW.created_at = 0 OR NEW.updated_at = 0
BEGIN
    UPDATE course_groups
    SET created_at = CASE WHEN NEW.created_at = 0 THEN unixepoch() ELSE NEW.created_at END,
        updated_at = CASE WHEN NEW.updated_at = 0 THEN unixepoch() ELSE NEW.updated_at END
    WHERE rowid = NEW.rowid;
END;

CREATE TRIGGER trg_grades_set_timestamps
    AFTER INSERT
    ON grades
    FOR EACH ROW WHEN NEW.created_at = 0 OR NEW.updated_at = 0
BEGIN
    UPDATE grades
    SET created_at = CASE WHEN NEW.created_at = 0 THEN unixepoch() ELSE NEW.created_at END,
        updated_at = CASE WHEN NEW.updated_at = 0 THEN unixepoch() ELSE NEW.updated_at END
    WHERE rowid = NEW.rowid;
END;

CREATE TRIGGER trg_users_courses_set_timestamps
    AFTER INSERT
    ON users_courses
    FOR EACH ROW WHEN NEW.created_at = 0 OR NEW.updated_at = 0
BEGIN
    UPDATE users_courses
    SET created_at = CASE WHEN NEW.created_at = 0 THEN unixepoch() ELSE NEW.created_at END,
        updated_at = CASE WHEN NEW.updated_at = 0 THEN unixepoch() ELSE NEW.updated_at END
    WHERE rowid = NEW.rowid;
END;

CREATE TRIGGER trg_users_groups_set_timestamps
    AFTER INSERT
    ON users_course_groups
    FOR EACH ROW WHEN NEW.created_at = 0 OR NEW.updated_at = 0
BEGIN
    UPDATE users_course_groups
    SET created_at = CASE WHEN NEW.created_at = 0 THEN unixepoch() ELSE NEW.created_at END,
        updated_at = CASE WHEN NEW.updated_at = 0 THEN unixepoch() ELSE NEW.updated_at END
    WHERE rowid = NEW.rowid;
END;

CREATE TRIGGER trg_users_roles_set_timestamps
    AFTER INSERT
    ON users_roles
    FOR EACH ROW WHEN NEW.created_at = 0 OR NEW.updated_at = 0
BEGIN
    UPDATE users_roles
    SET created_at = CASE WHEN NEW.created_at = 0 THEN unixepoch() ELSE NEW.created_at END,
        updated_at = CASE WHEN NEW.updated_at = 0 THEN unixepoch() ELSE NEW.updated_at END
    WHERE rowid = NEW.rowid;
END;

CREATE TRIGGER trg_users_grades_set_timestamps
    AFTER INSERT
    ON users_grades
    FOR EACH ROW WHEN NEW.created_at = 0 OR NEW.updated_at = 0
BEGIN
    UPDATE users_grades
    SET created_at = CASE WHEN NEW.created_at = 0 THEN unixepoch() ELSE NEW.created_at END,
        updated_at = CASE WHEN NEW.updated_at = 0 THEN unixepoch() ELSE NEW.updated_at END
    WHERE rowid = NEW.rowid;
END;

CREATE TRIGGER trg_sections_set_timestamps
    AFTER INSERT
    ON sections
    FOR EACH ROW WHEN NEW.created_at = 0 OR NEW.updated_at = 0
BEGIN
    UPDATE sections
    SET created_at = CASE WHEN NEW.created_at = 0 THEN unixepoch() ELSE NEW.created_at END,
        updated_at = CASE WHEN NEW.updated_at = 0 THEN unixepoch() ELSE NEW.updated_at END
    WHERE rowid = NEW.rowid;
END;

CREATE TRIGGER trg_modules_set_timestamps
    AFTER INSERT
    ON modules
    FOR EACH ROW WHEN NEW.created_at = 0 OR NEW.updated_at = 0
BEGIN
    UPDATE modules
    SET created_at = CASE WHEN NEW.created_at = 0 THEN unixepoch() ELSE NEW.created_at END,
        updated_at = CASE WHEN NEW.updated_at = 0 THEN unixepoch() ELSE NEW.updated_at END
    WHERE rowid = NEW.rowid;
END;

CREATE TRIGGER trg_users_activities_set_timestamps
    AFTER INSERT
    ON users_activities
    FOR EACH ROW WHEN NEW.created_at = 0 OR NEW.updated_at = 0
BEGIN
    UPDATE users_activities
    SET created_at = CASE WHEN NEW.created_at = 0 THEN unixepoch() ELSE NEW.created_at END,
        updated_at = CASE WHEN NEW.updated_at = 0 THEN unixepoch() ELSE NEW.updated_at END
    WHERE rowid = NEW.rowid;
END;

CREATE TRIGGER trg_sites_set_timestamps
    AFTER INSERT
    ON sites
    FOR EACH ROW WHEN NEW.created_at = 0 OR NEW.updated_at = 0
BEGIN
    UPDATE sites
    SET created_at = CASE WHEN NEW.created_at = 0 THEN unixepoch() ELSE NEW.created_at END,
        updated_at = CASE WHEN NEW.updated_at = 0 THEN unixepoch() ELSE NEW.updated_at END
    WHERE rowid = NEW.rowid;
END;

CREATE TRIGGER trg_actual_user_set_timestamps
    AFTER INSERT
    ON actual_user
    FOR EACH ROW WHEN NEW.created_at = 0 OR NEW.updated_at = 0
BEGIN
    UPDATE actual_user
    SET created_at = CASE WHEN NEW.created_at = 0 THEN unixepoch() ELSE NEW.created_at END,
        updated_at = CASE WHEN NEW.updated_at = 0 THEN unixepoch() ELSE NEW.updated_at END
    WHERE rowid = NEW.rowid;
END;

CREATE TRIGGER trg_users_images_set_timestamps
    AFTER INSERT
    ON users_images
    FOR EACH ROW WHEN NEW.created_at = 0 OR NEW.updated_at = 0
BEGIN
    UPDATE users_images
    SET created_at = CASE WHEN NEW.created_at = 0 THEN unixepoch() ELSE NEW.created_at END,
        updated_at = CASE WHEN NEW.updated_at = 0 THEN unixepoch() ELSE NEW.updated_at END
    WHERE rowid = NEW.rowid;
END;

CREATE TRIGGER trg_forums_set_timestamps
    AFTER INSERT
    ON forums
    FOR EACH ROW WHEN NEW.created_at = 0 OR NEW.updated_at = 0
BEGIN
    UPDATE forums
    SET created_at = CASE WHEN NEW.created_at = 0 THEN unixepoch() ELSE NEW.created_at END,
        updated_at = CASE WHEN NEW.updated_at = 0 THEN unixepoch() ELSE NEW.updated_at END
    WHERE rowid = NEW.rowid;
END;

CREATE TRIGGER trg_forums_discussions_set_timestamps
    AFTER INSERT
    ON forums_discussions
    FOR EACH ROW WHEN NEW.created_at = 0 OR NEW.updated_at = 0
BEGIN
    UPDATE forums_discussions
    SET created_at = CASE WHEN NEW.created_at = 0 THEN unixepoch() ELSE NEW.created_at END,
        updated_at = CASE WHEN NEW.updated_at = 0 THEN unixepoch() ELSE NEW.updated_at END
    WHERE rowid = NEW.rowid;
END;

CREATE TRIGGER trg_forums_posts_set_timestamps
    AFTER INSERT
    ON forums_posts
    FOR EACH ROW WHEN NEW.created_at = 0 OR NEW.updated_at = 0
BEGIN
    UPDATE forums_posts
    SET created_at = CASE WHEN NEW.created_at = 0 THEN unixepoch() ELSE NEW.created_at END,
        updated_at = CASE WHEN NEW.updated_at = 0 THEN unixepoch() ELSE NEW.updated_at END
    WHERE rowid = NEW.rowid;
END;

CREATE TRIGGER trg_calendars_events_set_timestamps
    AFTER INSERT
    ON calendars_events
    FOR EACH ROW WHEN NEW.created_at = 0 OR NEW.updated_at = 0
BEGIN
    UPDATE calendars_events
    SET created_at = CASE WHEN NEW.created_at = 0 THEN unixepoch() ELSE NEW.created_at END,
        updated_at = CASE WHEN NEW.updated_at = 0 THEN unixepoch() ELSE NEW.updated_at END
    WHERE rowid = NEW.rowid;
END;
