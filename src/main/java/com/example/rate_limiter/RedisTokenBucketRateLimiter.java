package com.example.rate_limiter;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class RedisTokenBucketRateLimiter {

    private static final long TOKEN_MICROS = 1_000_000L;

    private final StringRedisTemplate redis;
    private final DefaultRedisScript<List> tokenBucketScript;

    public RedisTokenBucketRateLimiter(
            StringRedisTemplate redis,
            DefaultRedisScript<List> tokenBucketScript
    ) {
        this.redis = redis;
        this.tokenBucketScript = tokenBucketScript;
    }

    public RateLimitDecision check(String clientKey, RateLimitRule rule) {
        String redisKey = "rl:" + rule.ruleId() + ":" + clientKey;

        long nowMs = Instant.now().toEpochMilli();

        long capacityMicros = rule.capacity() * TOKEN_MICROS;
        long refillPerSecondMicros = (long) Math.floor(rule.refillTokensPerSecond() * TOKEN_MICROS);

        // delete idle buckets after 1 hour
        long ttlSeconds = 3600;

        List<?> out = redis.execute(
                tokenBucketScript,
                List.of(redisKey),
                String.valueOf(nowMs),
                String.valueOf(capacityMicros),
                String.valueOf(refillPerSecondMicros),
                String.valueOf(ttlSeconds)
        );

        if (out == null || out.size() < 3) {
            // If Redis returns weird output, fail closed (block) to be safe
            return RateLimitDecision.blocked(0, Instant.now().getEpochSecond());
        }

        long allowed = toLong(out.get(0));
        long remaining = toLong(out.get(1));
        long resetEpochSec = toLong(out.get(2));

        if (allowed == 1) {
            return RateLimitDecision.allowed(remaining, resetEpochSec);
        }
        return RateLimitDecision.blocked(remaining, resetEpochSec);
    }

    private long toLong(Object x) {
        // Redis/Lua often returns Long; sometimes String.
        if (x instanceof Long v) return v;
        if (x instanceof Integer v) return v.longValue();
        return Long.parseLong(String.valueOf(x));
    }
}