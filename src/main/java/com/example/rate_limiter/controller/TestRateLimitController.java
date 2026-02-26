package com.example.rate_limiter.controller;


import com.example.rate_limiter.ClientKeyResolver;
import com.example.rate_limiter.RateLimitDecision;
import com.example.rate_limiter.RateLimitRule;
import com.example.rate_limiter.RedisTokenBucketRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestRateLimitController {

    private final ClientKeyResolver clientKeyResolver;
    private final RateLimitRule rule;
    private final RedisTokenBucketRateLimiter limiter;

    public TestRateLimitController(
            ClientKeyResolver clientKeyResolver,
            RateLimitRule rule,
            RedisTokenBucketRateLimiter limiter
    ) {
        this.clientKeyResolver = clientKeyResolver;
        this.rule = rule;
        this.limiter = limiter;
    }

    @GetMapping("/test-limit")
    public RateLimitDecision test(HttpServletRequest request) {
        String clientKey = clientKeyResolver.resolveClientKey(request);
        return limiter.check(clientKey, rule);
    }
}