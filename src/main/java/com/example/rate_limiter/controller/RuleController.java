package com.example.rate_limiter.controller;

import com.example.rate_limiter.RateLimitRule;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RuleController {

    private final RateLimitRule rule;

    public RuleController(RateLimitRule rule) {
        this.rule = rule;
    }

    @GetMapping("/rule")
    public RateLimitRule rule() {
        return rule;
    }
}