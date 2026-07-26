package com.example.springbootdemo.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    private static final String RATE_LIMIT_PREFIX = "limiter:";
    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT =
            new DefaultRedisScript<>(
                    "redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, tonumber(ARGV[2]) - tonumber(ARGV[3])); "
                            + "if (redis.call('ZCARD', KEYS[1]) >= tonumber(ARGV[1])) then "
                            + "return 0; "
                            + "end; "
                            + "redis.call('ZADD', KEYS[1], ARGV[2], ARGV[2]); "
                            + "redis.call('PEXPIRE', KEYS[1], ARGV[3]); "
                            + "return 1;",
                    Long.class);

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setCacheObject(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public Object getCacheObject(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public boolean isAllow(String source, int limit, long period, TimeUnit unit) {
        String key = RATE_LIMIT_PREFIX + source;
        long now = System.currentTimeMillis();
        long periodMillis = TimeUnit.MILLISECONDS.convert(period, unit);
        Long result = redisTemplate.execute(
                RATE_LIMIT_SCRIPT,
                Collections.singletonList(key),
                limit,
                now,
                periodMillis);
        return result != null && result == 1L;
    }

    public void deleteRateLimit(String source) {
        delete(RATE_LIMIT_PREFIX + source);
    }
}
