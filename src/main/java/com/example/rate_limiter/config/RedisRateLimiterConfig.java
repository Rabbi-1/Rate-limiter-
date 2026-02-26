package com.example.rate_limiter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.List;

@Configuration
public class RedisRateLimiterConfig {

    /**
     * Returns: [allowed(1/0), remainingTokensInt, resetEpochSeconds]
     */
    @Bean
    public DefaultRedisScript<List> tokenBucketScript() {
        String lua = """
            -- KEYS[1] = bucket key
            -- ARGV[1] = nowMs
            -- ARGV[2] = capacityMicros
            -- ARGV[3] = refillPerSecondMicros
            -- ARGV[4] = ttlSeconds

            local key = KEYS[1]

            local nowMs = tonumber(ARGV[1])
            local capacity = tonumber(ARGV[2])
            local refillPerSec = tonumber(ARGV[3])
            local ttlSeconds = tonumber(ARGV[4])

            local TOKEN = 1000000 -- 1 token in micro-tokens

            -- read existing state
            local tokens = tonumber(redis.call('HGET', key, 'tokens'))
            local lastTs = tonumber(redis.call('HGET', key, 'ts'))

            if tokens == nil then
              tokens = capacity
            end

            if lastTs == nil then
              lastTs = nowMs
            end

            -- refill
            local deltaMs = nowMs - lastTs
            if deltaMs < 0 then
              deltaMs = 0
            end

            -- addedMicros = deltaMs * refillPerSec / 1000
            local added = math.floor(deltaMs * refillPerSec / 1000)
            if added > 0 then
              tokens = tokens + added
              if tokens > capacity then
                tokens = capacity
              end
              lastTs = nowMs
            end

            local allowed = 0
            if tokens >= TOKEN then
              allowed = 1
              tokens = tokens - TOKEN
            end

            -- compute remaining whole tokens
            local remainingTokens = math.floor(tokens / TOKEN)

            -- compute reset time (when at least 1 token will be available)
            local nowSec = math.floor(nowMs / 1000)
            local resetSec = nowSec

            if allowed == 0 then
              if refillPerSec <= 0 then
                resetSec = nowSec + 31536000 -- 1 year, basically "never"
              else
                local deficit = TOKEN - tokens
                -- waitMs = ceil(deficit * 1000 / refillPerSec)
                local waitMs = math.floor((deficit * 1000 + refillPerSec - 1) / refillPerSec)
                resetSec = math.floor((nowMs + waitMs) / 1000)
              end
            end

            -- write back
            redis.call('HSET', key, 'tokens', tokens)
            redis.call('HSET', key, 'ts', lastTs)
            redis.call('EXPIRE', key, ttlSeconds)

            return {allowed, remainingTokens, resetSec}
            """;

        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setScriptText(lua);
        script.setResultType(List.class);
        return script;
    }
}
