package com.example.rate_limiter;

public record RateLimitRule(
        String ruleId,
        long capacity,
        double refillTokensPerSecond
) {}

//ruleId lets us support multiple rules later (per endpoint, per user tier, etc.)
//capacity is max tokens
//refillTokensPerSecond controls steady rate