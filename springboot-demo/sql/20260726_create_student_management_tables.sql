CREATE TABLE IF NOT EXISTS school_class (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '班级 ID',
    class_code VARCHAR(32) NOT NULL COMMENT '班级编码',
    class_name VARCHAR(100) NOT NULL COMMENT '班级名称',
    gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        COMMENT '创建时间',
    gmt_modify DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0
        COMMENT '逻辑删除：0 正常，1 删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_school_class_class_code (class_code)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='班级';

CREATE TABLE IF NOT EXISTS course (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '课程 ID',
    course_code VARCHAR(32) NOT NULL COMMENT '课程编码',
    course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
    gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        COMMENT '创建时间',
    gmt_modify DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0
        COMMENT '逻辑删除：0 正常，1 删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_course_course_code (course_code)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='课程';

CREATE TABLE IF NOT EXISTS student (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '学生 ID',
    student_no VARCHAR(32) NOT NULL COMMENT '学号',
    name VARCHAR(50) NOT NULL COMMENT '学生姓名',
    gender TINYINT NOT NULL DEFAULT 0
        COMMENT '性别：0 未知，1 男，2 女',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    class_id BIGINT NOT NULL COMMENT '所属班级 ID',
    gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        COMMENT '创建时间',
    gmt_modify DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0
        COMMENT '逻辑删除：0 正常，1 删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_student_no (student_no),
    KEY idx_student_class_deleted (class_id, deleted),
    CONSTRAINT fk_student_school_class
        FOREIGN KEY (class_id) REFERENCES school_class (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='学生';

CREATE TABLE IF NOT EXISTS student_course (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '学生课程关系 ID',
    student_id BIGINT NOT NULL COMMENT '学生 ID',
    course_id BIGINT NOT NULL COMMENT '课程 ID',
    gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_course_student_course (student_id, course_id),
    KEY idx_student_course_course_id (course_id),
    CONSTRAINT fk_student_course_student
        FOREIGN KEY (student_id) REFERENCES student (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT fk_student_course_course
        FOREIGN KEY (course_id) REFERENCES course (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='学生课程关联';
