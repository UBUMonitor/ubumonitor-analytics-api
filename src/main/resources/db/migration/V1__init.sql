-- 1. Primero tablas sin FK (USERS, COURSES, ROLES)
-- =========================

CREATE TABLE USERS
(
  ID           INT PRIMARY KEY,
  FULL_NAME    VARCHAR(255),
  FIRST_NAME   VARCHAR(255),
  LAST_NAME    VARCHAR(255),
  EMAIL        VARCHAR(255),
  FIRST_ACCESS TIMESTAMP WITH TIME ZONE,
  LAST_ACCESS  TIMESTAMP WITH TIME ZONE,
  CREATED_AT   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UPDATED_AT   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ROLES
(
  ID         INT PRIMARY KEY,
  NAME       VARCHAR(255),
  CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UPDATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE COURSES
(
  ID                INT PRIMARY KEY,
  FULL_NAME         VARCHAR(255),
  SHORT_NAME        VARCHAR(255),
  SHOW_GRADES       BOOLEAN,
  ENABLE_COMPLETION BOOLEAN,
  START_DATE        TIMESTAMP WITH TIME ZONE,
  END_DATE          TIMESTAMP WITH TIME ZONE,
  TIME_MODIFIED     TIMESTAMP WITH TIME ZONE,
  CREATED_AT        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UPDATED_AT        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Dependientes directas
-- =========================

CREATE TABLE GROUPS
(
  ID          INT PRIMARY KEY,
  NAME        VARCHAR(255),
  DESCRIPTION CLOB,
  CREATED_AT  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UPDATED_AT  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Tablas intermedias (many-to-many)
-- =========================

CREATE TABLE USERS_COURSES
(
  USER_ID            INT NOT NULL,
  COURSE_ID          INT NOT NULL,
  LAST_COURSE_ACCESS TIMESTAMP WITH TIME ZONE,
  IS_FAVOURITE       BOOLEAN,
  ACTIVE             BOOLEAN,
  CREATED_AT         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UPDATED_AT         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (USER_ID, COURSE_ID),

  CONSTRAINT FK_UC_USER FOREIGN KEY (USER_ID) REFERENCES USERS (ID) ON DELETE CASCADE,
  CONSTRAINT FK_UC_COURSE FOREIGN KEY (COURSE_ID) REFERENCES COURSES (ID) ON DELETE CASCADE
);

CREATE TABLE USERS_GROUPS
(
  USER_ID    INT NOT NULL,
  GROUP_ID   INT NOT NULL,
  COURSE_ID  INT NOT NULL,
  ACTIVE     BOOLEAN,
  CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UPDATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (USER_ID, GROUP_ID, COURSE_ID),

  CONSTRAINT FK_UG_USER FOREIGN KEY (USER_ID) REFERENCES USERS (ID) ON DELETE CASCADE,
  CONSTRAINT FK_UG_GROUP FOREIGN KEY (GROUP_ID) REFERENCES GROUPS (ID) ON DELETE CASCADE,
  CONSTRAINT FK_UG_COURSE FOREIGN KEY (COURSE_ID) REFERENCES COURSES (ID) ON DELETE CASCADE
);

CREATE TABLE USERS_ROLES
(
  ROLE_ID    INT NOT NULL,
  USER_ID    INT NOT NULL,
  COURSE_ID  INT NOT NULL,
  ACTIVE     BOOLEAN,
  CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UPDATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (USER_ID, ROLE_ID, COURSE_ID),

  CONSTRAINT FK_UR_USER FOREIGN KEY (USER_ID) REFERENCES USERS (ID) ON DELETE CASCADE,
  CONSTRAINT FK_UR_ROLE FOREIGN KEY (ROLE_ID) REFERENCES ROLES (ID) ON DELETE CASCADE,
  CONSTRAINT FK_UR_COURSE FOREIGN KEY (COURSE_ID) REFERENCES COURSES (ID) ON DELETE CASCADE
);

-- 4. Estructura jerárquica
-- =========================

CREATE TABLE SECTIONS
(
  ID         INT PRIMARY KEY,
  COURSE_ID  INT NOT NULL,
  NAME       VARCHAR(255),
  SUMMARY    CLOB,
  POSITION   INT,
  VISIBLE    BOOLEAN,
  ACTIVE     BOOLEAN,
  CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UPDATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT FK_SECTIONS_COURSE FOREIGN KEY (COURSE_ID) REFERENCES COURSES (ID)
);

CREATE TABLE MODULES
(
  ID          INT PRIMARY KEY,
  SECTION_ID  INT NOT NULL,
  NAME        VARCHAR(255),
  MOD_NAME    VARCHAR(50),
  URL         VARCHAR(2048),
  DESCRIPTION CLOB,
  VISIBLE     BOOLEAN,
  POSITION    INT,
  ACTIVE      BOOLEAN,
  CREATED_AT  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UPDATED_AT  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT FK_MODULES_SECTION FOREIGN KEY (SECTION_ID) REFERENCES SECTIONS (ID)
);

-- 5. SITES
-- =========================

CREATE TABLE SITES
(
  ID             INT PRIMARY KEY,
  HOST           VARCHAR(2048),
  SITE_NAME      VARCHAR(255),
  VERSION_NUMBER VARCHAR(100),
  TYPE_OF_LOGIN  VARCHAR(100),
  LAUNCH_URL     VARCHAR(2048),
  USER_ID        INT,
  USER_NAME      VARCHAR(255),
  FULL_NAME      VARCHAR(255),
  FIRST_NAME     VARCHAR(255),
  LAST_NAME      VARCHAR(255),
  USER_IMAGE_URL VARCHAR(2048),
  CREATED_AT     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UPDATED_AT     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. Tablas finales
-- =========================

CREATE TABLE USERS_IMAGES
(
  USER_ID      INT PRIMARY KEY,
  IMAGE_DATA   BLOB         NOT NULL,
  IMAGE_HASH   VARCHAR(64)  NOT NULL,
  CONTENT_TYPE VARCHAR(100) NOT NULL,
  CREATED_AT   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UPDATED_AT   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT FK_UI_USER FOREIGN KEY (USER_ID) REFERENCES USERS (ID) ON DELETE CASCADE
);

CREATE TABLE LOGS_COMPONENTS
(
  ID   TINYINT AUTO_INCREMENT PRIMARY KEY,
  NAME VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE LOGS_EVENTS
(
  ID   SMALLINT AUTO_INCREMENT PRIMARY KEY,
  NAME VARCHAR(255) NOT NULL,
  CONSTRAINT UQ_EVENT UNIQUE (NAME)
);

CREATE TABLE LOGS_ORIGINS
(
  ID   TINYINT AUTO_INCREMENT PRIMARY KEY,
  NAME VARCHAR(20) UNIQUE NOT NULL
);

-- =========================
-- LOGS
-- =========================

CREATE SEQUENCE LOGS_SEQ START WITH 1 INCREMENT BY 1;

CREATE TABLE LOGS
(
  ID           INT DEFAULT NEXT VALUE FOR LOGS_SEQ PRIMARY KEY,
  TIMESTAMP    TIMESTAMP NOT NULL,
  USER_ID      INT       NULL,
  COURSE_ID    INT       NOT NULL,
  COMPONENT_ID TINYINT   NOT NULL,
  EVENT_ID     SMALLINT  NOT NULL,
  MODULE_ID    INT       NULL,
  ORIGIN_ID    TINYINT   NOT NULL,
  IP_ADDRESS   VARCHAR(45) NULL


  -- CONSTRAINT FK_LOG_COMPONENT FOREIGN KEY (COMPONENT_ID) REFERENCES LOGS_COMPONENTS (ID),
  -- CONSTRAINT FK_LOG_EVENT FOREIGN KEY (EVENT_ID) REFERENCES LOGS_EVENTS (ID),
  -- CONSTRAINT FK_LOG_ORIGIN FOREIGN KEY (ORIGIN_ID) REFERENCES LOGS_ORIGINS (ID),
  -- CONSTRAINT FK_LOG_COURSE FOREIGN KEY (COURSE_ID) REFERENCES COURSES (ID)
);

-- 7. INDEXES
-- =========================

CREATE INDEX IDX_USER_COURSE_USER_ID ON USERS_COURSES (USER_ID);
CREATE INDEX IDX_USER_COURSE_COURSE_ID ON USERS_COURSES (COURSE_ID);

CREATE INDEX IDX_LOGS_COURSE_TIMESTAMP ON LOGS (COURSE_ID, USER_ID, TIMESTAMP);




-- 9. INSERTS INICIALES
-- =========================
INSERT INTO LOGS_ORIGINS (name)
values ('web'),
       ('cli'),
       ('ws'),
       ('scheduled'),
       ('restore'),
       ('backup');


INSERT INTO LOGS_COMPONENTS (name)
VALUES ('Activity report'),
       ('Assignment'),
       ('BigBlueButton'),
       ('Book'),
       ('Book printing'),
       ('Chat'),
       ('Choice'),
       ('Database'),
       ('Excel spreadsheet'),
       ('External tool'),
       ('Feedback'),
       ('File'),
       ('File submissions'),
       ('Folder'),
       ('Forum'),
       ('Glossary'),
       ('Grade history'),
       ('Grader report'),
       ('Guía Docente'),
       ('H5P'),
       ('H5P Package'),
       ('Kaltura Video Resource'),
       ('Live logs'),
       ('Logs'),
       ('Online text submissions'),
       ('OpenDocument spreadsheet'),
       ('Overview report'),
       ('Page'),
       ('Question bank'),
       ('Quiz'),
       ('Recycle bin'),
       ('SCORM package'),
       ('SMOWL'),
       ('Single view'),
       ('Submission comments'),
       ('Survey'),
       ('System'),
       ('Tab display'),
       ('URL'),
       ('User report'),
       ('User tours'),
       ('Workshop');

-- Events: 157
INSERT INTO LOGS_EVENTS (name)
VALUES ('A file has been uploaded.'),
       ('A submission has been submitted.'),
       ('All the submissions are being downloaded.'),
       ('An online text has been uploaded.'),
       ('Assignment override created'),
       ('Assignment override deleted'),
       ('Assignment override updated'),
       ('Badge listing viewed'),
       ('Batch set workflow state viewed.'),
       ('Book printed'),
       ('Calendar event created'),
       ('Calendar event deleted'),
       ('Calendar event updated'),
       ('Calendar subscription updated'),
       ('Chapter updated'),
       ('Chapter viewed'),
       ('Choice answer added'),
       ('Choice answer deleted'),
       ('Choice report downloaded'),
       ('Choice report viewed'),
       ('Comment created'),
       ('Comment deleted'),
       ('Content viewed'),
       ('Course activities overview page viewed'),
       ('Course activity completion updated'),
       ('Course backup created'),
       ('Course created'),
       ('Course module created'),
       ('Course module deleted'),
       ('Course module instance list viewed'),
       ('Course module updated'),
       ('Course module viewed'),
       ('Course reset ended'),
       ('Course reset started'),
       ('Course restored'),
       ('Course searched'),
       ('Course section created'),
       ('Course section deleted'),
       ('Course section updated'),
       ('Course summary viewed'),
       ('Course updated'),
       ('Course user report viewed'),
       ('Course viewed'),
       ('Discussion created'),
       ('Discussion deleted'),
       ('Discussion subscription created'),
       ('Discussion viewed'),
       ('Enrolment instance created'),
       ('Enrolment instance updated'),
       ('Entry has been viewed'),
       ('Event created block instance'),
       ('Event deleted block instance'),
       ('Feedback viewed'),
       ('Field created'),
       ('Field updated'),
       ('Folder updated'),
       ('Grade deleted'),
       ('Grade history report viewed'),
       ('Grade item created'),
       ('Grade item deleted'),
       ('Grade item updated'),
       ('Grade overview report viewed'),
       ('Grade single view report viewed.'),
       ('Grade user report viewed'),
       ('Grader report viewed'),
       ('Grading form viewed'),
       ('Grading table viewed'),
       ('Group created'),
       ('Group member added'),
       ('Group member removed'),
       ('Guia docente viewed'),
       ('H5P content viewed'),
       ('Item created'),
       ('Item deleted'),
       ('Live log report viewed'),
       ('Log report viewed'),
       ('OpenDocument grade exported'),
       ('Outline report viewed'),
       ('Page break created'),
       ('Page break deleted'),
       ('Phase switched'),
       ('Post created'),
       ('Post deleted'),
       ('Post updated'),
       ('Question category created'),
       ('Question category deleted'),
       ('Question category updated'),
       ('Question category viewed'),
       ('Question created'),
       ('Question deleted'),
       ('Question manually graded'),
       ('Question moved'),
       ('Question updated'),
       ('Question viewed'),
       ('Questions exported'),
       ('Questions imported'),
       ('Quiz attempt abandoned'),
       ('Quiz attempt auto-saved'),
       ('Quiz attempt deleted'),
       ('Quiz attempt graded'),
       ('Quiz attempt preview started'),
       ('Quiz attempt reviewed'),
       ('Quiz attempt started'),
       ('Quiz attempt submitted'),
       ('Quiz attempt summary viewed'),
       ('Quiz attempt time limit exceeded'),
       ('Quiz attempt updated'),
       ('Quiz attempt viewed'),
       ('Quiz edit page viewed'),
       ('Quiz re-paginated'),
       ('Quiz report viewed'),
       ('Read tracking disabled'),
       ('Read tracking enabled'),
       ('Recent activity viewed'),
       ('Record created'),
       ('Record deleted'),
       ('Record updated'),
       ('Remove submission confirmation viewed.'),
       ('Role assigned'),
       ('Role unassigned'),
       ('Sco launched'),
       ('Section shuffle updated'),
       ('Section viewed'),
       ('Slot created'),
       ('Slot deleted'),
       ('Slot mark updated'),
       ('Slot moved'),
       ('Some content has been posted.'),
       ('Step shown'),
       ('Submission confirmation form viewed.'),
       ('Submission created.'),
       ('Submission form viewed.'),
       ('Submission removed.'),
       ('Submission updated.'),
       ('Submitted SCORM status'),
       ('Subscribers viewed'),
       ('Subscription created'),
       ('Tag added to an item'),
       ('Template updated'),
       ('Templates viewed'),
       ('The state of the workflow has been updated.'),
       ('The status of the submission has been updated.'),
       ('The status of the submission has been viewed.'),
       ('The submission has been graded.'),
       ('The user has accepted the statement of the submission.'),
       ('Tour ended'),
       ('Tour reset'),
       ('Tour started'),
       ('User enrolled in course'),
       ('User graded'),
       ('User list viewed'),
       ('User log report viewed'),
       ('User profile viewed'),
       ('User unenrolled from course'),
       ('Video resource viewed'),
       ('XLS grade exported'),
       ('Zip archive of folder downloaded');
