package com.example.springbootdemo.config;

import cn.hutool.core.util.StrUtil;
import com.example.springbootdemo.dao.inter.AdminUserDao;
import com.example.springbootdemo.model.domain.AdminUserDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
public class AdminUserBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserBootstrap.class);

    private final AdminUserDao adminUserDao;
    private final PasswordEncoder passwordEncoder;
    private final AuthProperties authProperties;

    public AdminUserBootstrap(
            AdminUserDao adminUserDao,
            PasswordEncoder passwordEncoder,
            AuthProperties authProperties) {
        this.adminUserDao = adminUserDao;
        this.passwordEncoder = passwordEncoder;
        this.authProperties = authProperties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String username = StrUtil.trim(authProperties.getBootstrapUsername());
        String password = authProperties.getBootstrapPassword();
        boolean hasUsername = StrUtil.isNotBlank(username);
        boolean hasPassword = StrUtil.isNotBlank(password);

        if (adminUserDao.count() > 0) {
            log.info("Administrator bootstrap skipped because an administrator already exists");
            return;
        }
        if (!hasUsername && !hasPassword) {
            log.warn("No administrator exists. Configure AUTH_BOOTSTRAP_ADMIN_USERNAME "
                    + "and AUTH_BOOTSTRAP_ADMIN_PASSWORD for the first startup.");
            return;
        }
        if (!hasUsername || !hasPassword) {
            throw new IllegalStateException(
                    "AUTH_BOOTSTRAP_ADMIN_USERNAME and AUTH_BOOTSTRAP_ADMIN_PASSWORD must be set together");
        }
        validateBootstrapCredentials(username, password);

        LocalDateTime now = LocalDateTime.now();
        AdminUserDO user = new AdminUserDO();
        user.setUsername(username);
        user.setDisplayName(defaultDisplayName(username));
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setStatus(1);
        user.setFailedLoginCount(0);
        user.setGmtCreate(now);
        user.setGmtModify(now);
        user.setDeleted(0);
        adminUserDao.save(user);
        log.info("Bootstrapped the initial administrator account username={}", username);
    }

    private void validateBootstrapCredentials(String username, String password) {
        if (username.length() < 2 || username.length() > 50) {
            throw new IllegalStateException("Bootstrap administrator username must contain 2 to 50 characters");
        }
        if (password.length() < 8 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException(
                    "Bootstrap administrator password must contain at least 8 characters and at most 72 UTF-8 bytes");
        }
        int characterTypes = 0;
        characterTypes += password.matches(".*[a-z].*") ? 1 : 0;
        characterTypes += password.matches(".*[A-Z].*") ? 1 : 0;
        characterTypes += password.matches(".*\\d.*") ? 1 : 0;
        characterTypes += password.matches(".*[^A-Za-z0-9].*") ? 1 : 0;
        if (characterTypes < 3) {
            throw new IllegalStateException(
                    "Bootstrap administrator password must contain at least three character types");
        }
    }

    private String defaultDisplayName(String username) {
        String displayName = StrUtil.trim(authProperties.getBootstrapDisplayName());
        displayName = StrUtil.isBlank(displayName) ? username : displayName;
        if (displayName.length() > 100) {
            throw new IllegalStateException(
                    "Bootstrap administrator display name cannot exceed 100 characters");
        }
        return displayName;
    }
}
