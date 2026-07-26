CREATE DATABASE IF NOT EXISTS springboot_demo
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE springboot_demo;

CREATE TABLE IF NOT EXISTS sys_admin_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '管理员 ID',
    username VARCHAR(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录用户名',
    display_name VARCHAR(100) NOT NULL COMMENT '展示名称',
    password_hash VARCHAR(100) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT 'BCrypt 密码摘要',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1 启用，0 禁用',
    failed_login_count INT NOT NULL DEFAULT 0 COMMENT '失败窗口内连续登录失败次数',
    last_failed_login_time DATETIME(3) DEFAULT NULL COMMENT '最近登录失败时间',
    locked_until DATETIME(3) DEFAULT NULL COMMENT '账号锁定截止时间',
    last_login_ip VARCHAR(45) DEFAULT NULL COMMENT '最后登录 IP',
    last_login_time DATETIME(3) DEFAULT NULL COMMENT '最后登录时间',
    gmt_modify DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    gmt_create DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除，1 为删除，0 为正常',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_admin_user_username (username),
    KEY idx_sys_admin_user_status_deleted (status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统管理员';

CREATE TABLE IF NOT EXISTS sys_auth_token (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '会话 ID',
    user_id BIGINT NOT NULL COMMENT '管理员 ID',
    token VARCHAR(255) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT '' COMMENT '用户校验 Token',
    system_type TINYINT NOT NULL COMMENT '系统类型：1 PC，2 APP，其他取值与 bjsm-cloud 一致',
    gmt_modify DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    gmt_create DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除，1 为删除，0 为正常',
    PRIMARY KEY (id),
    KEY idx_sys_auth_token_user_id (user_id),
    KEY idx_sys_auth_token_user_system_deleted (user_id, system_type, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员登录会话';
