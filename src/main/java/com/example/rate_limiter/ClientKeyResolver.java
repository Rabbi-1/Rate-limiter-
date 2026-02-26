package com.example.rate_limiter;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class ClientKeyResolver {
    public String resolveClientKey(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            // X-Forwarded-For can be: "clientIP, proxy1, proxy2"
            String first = xff.split(",")[0].trim();
            if (!first.isBlank()) {
                return "ip:" + first;
            }
        }

        String remoteAddr = request.getRemoteAddr();
        return "ip:" + remoteAddr;
    }
}
