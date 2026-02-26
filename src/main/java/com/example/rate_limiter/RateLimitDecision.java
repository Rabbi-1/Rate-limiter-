package com.example.rate_limiter;


public record RateLimitDecision(
        boolean allowed,
        long remaining,
        long resetEpochSeconds
) {
    public static RateLimitDecision allowed(long remaining, long resetEpochSeconds) {
        return new RateLimitDecision(true, remaining, resetEpochSeconds);
    }

    public static RateLimitDecision blocked(long remaining, long resetEpochSeconds) {
        return new RateLimitDecision(false, remaining, resetEpochSeconds);
    }
}