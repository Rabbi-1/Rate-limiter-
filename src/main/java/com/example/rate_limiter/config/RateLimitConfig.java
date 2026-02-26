package com.example.rate_limiter.config;

import com.example.rate_limiter.RateLimitRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfig {

    @Bean
    public RateLimitRule defaultRule() {
        // 10 requests per 10 seconds
        return new RateLimitRule("default", 10, 1.0);
    }
}