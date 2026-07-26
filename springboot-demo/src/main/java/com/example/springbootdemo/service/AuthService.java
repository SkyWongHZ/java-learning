package com.example.springbootdemo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springbootdemo.config.AuthProperties;
import com.example.springbootdemo.dao.inter.AdminUserDao;
import com.example.springbootdemo.enums.BaseStatusCodeEnum;
import com.example.springbootdemo.exception.BaseException;
import com.example.springbootdemo.model.domain.AdminUserDO;
import com.example.springbootdemo.model.dto.IssuedTokenDTO;
import com.example.springbootdemo.model.form.LoginForm;
import com.example.springbootdemo.model.vo.CurrentUserVO;
import com.example.springbootdemo.model.vo.LoginVO;
import com.example.springbootdemo.web.auth.AuthContext;
import com.example.springbootdemo.web.auth.CurrentUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    private final AdminUserDao adminUserDao;
    private final AuthTokenService authTokenService;
    private final PasswordEncoder passwordEncoder;
    private final AuthProperties authProperties;
    private final RedisService redisService;
    private final String dummyPasswordHash;

    public AuthService(
            AdminUserDao adminUserDao,
            AuthTokenService authTokenService,
            PasswordEncoder passwordEncoder,
            AuthProperties authProperties,
            RedisService redisService) {
        this.adminUserDao = adminUserDao;
        this.authTokenService = authTokenService;
        this.passwordEncoder = passwordEncoder;
        this.authProperties = authProperties;
        this.redisService = redisService;
        this.dummyPasswordHash = passwordEncoder.encode("invalid-password-placeholder");
    }

    @Transactional(noRollbackFor = BaseException.class)
    public LoginVO login(LoginForm form, String clientIp, int systemType) {
        String username = form.getUsername().trim();
        String failureKey = loginFailureKey(systemType, username);
        String lockKey = loginLockKey(failureKey);
        if (redisService.exists(lockKey)) {
            throw new BaseException(BaseStatusCodeEnum.TOO_MANY_PASSWORD_ERRORS);
        }

        AdminUserDO user = adminUserDao.getOne(new LambdaQueryWrapper<AdminUserDO>()
                .eq(AdminUserDO::getUsername, username)
                .last("LIMIT 1 FOR UPDATE"));

        if (user == null || !Integer.valueOf(1).equals(user.getStatus())) {
            passwordEncoder.matches(form.getPassword(), dummyPasswordHash);
            recordFailedLogin(failureKey, lockKey);
            throw new BaseException(BaseStatusCodeEnum.ACCOUNT_OR_PASSWORD_ERROR);
        }

        LocalDateTime now = LocalDateTime.now();
        if (!passwordEncoder.matches(form.getPassword(), user.getPasswordHash())) {
            recordFailedLogin(failureKey, lockKey);
            throw new BaseException(BaseStatusCodeEnum.ACCOUNT_OR_PASSWORD_ERROR);
        }

        redisService.deleteRateLimit(failureKey);
        redisService.delete(lockKey);
        user.setLastLoginIp(clientIp);
        user.setLastLoginTime(now);
        user.setGmtModify(now);
        adminUserDao.updateById(user);

        IssuedTokenDTO issuedToken = authTokenService.issueToken(user.getId(), systemType);
        return new LoginVO(
                issuedToken.getToken(),
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                systemType,
                issuedToken.getExpiresAt());
    }

    public CurrentUserVO currentUser() {
        return CurrentUserVO.from(AuthContext.requireCurrentUser());
    }

    public boolean logout() {
        CurrentUser currentUser = AuthContext.requireCurrentUser();
        return authTokenService.revoke(
                currentUser.getId(),
                currentUser.getSystemType(),
                currentUser.getToken());
    }

    private String loginFailureKey(int systemType, String username) {
        return String.format("sleep_login_error:%s:%s", systemType, username);
    }

    private String loginLockKey(String failureKey) {
        return String.format("sleep_login_error_lock:%s", failureKey);
    }

    private void recordFailedLogin(String failureKey, String lockKey) {
        boolean allowed = redisService.isAllow(
                failureKey,
                authProperties.getMaxFailedAttempts() - 1,
                authProperties.getFailureWindowMinutes(),
                TimeUnit.MINUTES);
        if (!allowed) {
            redisService.setCacheObject(
                    lockKey,
                    Boolean.TRUE,
                    authProperties.getLockMinutes(),
                    TimeUnit.MINUTES);
        }
    }
}
