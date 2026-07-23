CREATE DATABASE IF NOT EXISTS springboot_demo
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE springboot_demo;

CREATE TABLE IF NOT EXISTS demo_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户 ID',
    username VARCHAR(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
    display_name VARCHAR(100) NOT NULL COMMENT '展示名称',
    gmt_modify DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常，1 删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_demo_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='脚手架示例用户';
