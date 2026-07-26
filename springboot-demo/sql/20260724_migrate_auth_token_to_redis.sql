USE springboot_demo;

ALTER TABLE sys_auth_token
    DROP INDEX uk_sys_auth_token_hash,
    DROP INDEX idx_sys_auth_token_user_deleted,
    DROP INDEX idx_sys_auth_token_expire_deleted,
    ADD COLUMN token VARCHAR(255) CHARACTER SET ascii COLLATE ascii_bin
        NOT NULL DEFAULT '' COMMENT '用户校验 Token' AFTER user_id,
    ADD COLUMN system_type TINYINT NOT NULL DEFAULT 1
        COMMENT '系统类型：1 PC，2 APP，其他取值与 bjsm-cloud 一致' AFTER token,
    DROP COLUMN token_hash,
    DROP COLUMN expires_at,
    DROP COLUMN last_access_time,
    ADD KEY idx_sys_auth_token_user_id (user_id),
    ADD KEY idx_sys_auth_token_user_system_deleted (user_id, system_type, deleted);
