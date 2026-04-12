package com.acuityspace.mantle.web.controller;

import com.acuityspace.mantle.service.AuthService;
import com.acuityspace.mantle.service.InviteService;
import com.acuityspace.mantle.web.dto.AuthResponse;
import com.acuityspace.mantle.web.dto.InviteDetailResponse;
import com.acuityspace.mantle.web.dto.LoginRequest;
import com.acuityspace.mantle.web.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final InviteService inviteService;

    public AuthController(AuthService authService, InviteService inviteService) {
        this.authService = authService;
        this.inviteService = inviteService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@RequestBody @Valid RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest request, HttpServletResponse response) {
        return authService.login(request, response);
    }

    @PostMapping("/logout")
    public Map<String, String> logout(HttpServletResponse response) {
        authService.logout(response);
        return Map.of("message", "Logged out");
    }

    @GetMapping("/invite/{inviteId}")
    public InviteDetailResponse getInviteDetail(@PathVariable UUID inviteId) {
        return inviteService.getInviteDetail(inviteId);
    }
}
