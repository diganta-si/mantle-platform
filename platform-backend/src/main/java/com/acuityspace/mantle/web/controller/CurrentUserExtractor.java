package com.acuityspace.mantle.web.controller;

import com.acuityspace.mantle.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentUserExtractor {

    private final JwtService jwtService;

    public CurrentUserExtractor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public UUID extractUserId(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("mantle-token".equals(cookie.getName())) {
                    return jwtService.extractUserId(cookie.getValue());
                }
            }
        }
        throw new IllegalStateException("No authentication token found in request");
    }
}
