package com.example.springbootdemo.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springbootdemo.config.AuthProperties;
import com.example.springbootdemo.dao.inter.AdminUserDao;
import com.example.springbootdemo.dao.inter.AuthTokenDao;
import com.example.springbootdemo.enums.BaseStatusCodeEnum;
import com.example.springbootdemo.exception.BaseException;
import com.example.springbootdemo.model.domain.AdminUserDO;
import com.example.springbootdemo.model.domain.AuthTokenDO;
import com.example.springbootdemo.model.dto.IssuedTokenDTO;
import com.example.springbootdemo.web.auth.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AuthTokenService {

    private static final int MAX_TOKEN_LENGTH = 256;

    private final AuthTokenDao authTokenDao;
    private final AdminUserDao adminUserDao;
    private final AuthProperties authProperties;
    private final RedisService redisService;

    public AuthTokenService(
            AuthTokenDao authTokenDao,
            AdminUserDao adminUserDao,
            AuthProperties authProperties,
            RedisService redisService) {
        this.authTokenDao = authTokenDao;
        this.adminUserDao = adminUserDao;
        this.authProperties = authProperties;
        this.redisService = redisService;
    }

    @Transactional
    public IssuedTokenDTO issueToken(long userId, int systemType) {
        List<AuthTokenDO> existingTokens = authTokenDao.list(new LambdaQueryWrapper<AuthTokenDO>()
                .eq(AuthTokenDO::getUserId, userId)
                .eq(AuthTokenDO::getSystemType, systemType));
        if (CollUtil.isNotEmpty(existingTokens)) {
            for (AuthTokenDO existingToken : existingTokens) {
                redisService.delete(existingToken.getToken());
                authTokenDao.removeById(existingToken.getId());
            }
        }

        String rawToken = generateToken();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusDays(authProperties.getTokenTtlDays());
        redisService.setCacheObject(
                rawToken,
                userId,
                authProperties.getTokenTtlDays(),
                TimeUnit.DAYS);

        AuthTokenDO token = new AuthTokenDO();
        token.setUserId(userId);
        token.setToken(rawToken);
        token.setSystemType(systemType);
        token.setGmtCreate(now);
        token.setGmtModify(now);
        token.setDeleted(0);
        authTokenDao.save(token);

        return new IssuedTokenDTO(rawToken, expiresAt);
    }

    @Transactional(noRollbackFor = BaseException.class)
    public CurrentUser authenticate(String rawToken, int systemType) {
        if (StrUtil.isBlank(rawToken) || rawToken.length() > MAX_TOKEN_LENGTH) {
            throw new BaseException(BaseStatusCodeEnum.USER_NOT_LOGGED_IN);
        }

        AuthTokenDO token = authTokenDao.getOne(new LambdaQueryWrapper<AuthTokenDO>()
                .eq(AuthTokenDO::getToken, rawToken)
                .eq(AuthTokenDO::getSystemType, systemType)
                .last("LIMIT 1"));
        if (token == null) {
            throw new BaseException(BaseStatusCodeEnum.USER_NOT_LOGGED_IN);
        }

        Long cachedUserId = toLong(redisService.getCacheObject(rawToken));
        if (cachedUserId == null || !cachedUserId.equals(token.getUserId())) {
            throw new BaseException(BaseStatusCodeEnum.LOGIN_EXPIRED);
        }

        AdminUserDO user = adminUserDao.getById(cachedUserId);
        if (user == null || !Integer.valueOf(1).equals(user.getStatus())) {
            throw new BaseException(BaseStatusCodeEnum.LOGIN_EXPIRED);
        }

        redisService.setCacheObject(
                rawToken,
                cachedUserId,
                authProperties.getTokenTtlDays(),
                TimeUnit.DAYS);

        return new CurrentUser(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                rawToken,
                systemType);
    }

    @Transactional
    public boolean revoke(long userId, int systemType, String rawToken) {
        if (StrUtil.isBlank(rawToken) || rawToken.length() > MAX_TOKEN_LENGTH) {
            return true;
        }
        AuthTokenDO token = authTokenDao.getOne(new LambdaQueryWrapper<AuthTokenDO>()
                .eq(AuthTokenDO::getUserId, userId)
                .eq(AuthTokenDO::getSystemType, systemType)
                .eq(AuthTokenDO::getToken, rawToken)
                .last("LIMIT 1"));
        if (token != null) {
            redisService.delete(token.getToken());
            authTokenDao.removeById(token.getId());
        }
        return true;
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private Long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.valueOf((String) value);
            } catch (NumberFormatException exception) {
                return null;
            }
        }
        return null;
    }
}
