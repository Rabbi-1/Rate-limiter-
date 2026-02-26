package com.example.rate_limiter.controller;

import com.example.rate_limiter.ClientKeyResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WhoAmIController {
    private final ClientKeyResolver clientKeyResolver;

    public WhoAmIController(ClientKeyResolver clientKeyResolver) {
        this.clientKeyResolver = clientKeyResolver;
    }

    @GetMapping("/whoami")
    public String whoAmI(HttpServletRequest request) {
        return clientKeyResolver.resolveClientKey(request);
    }
}
