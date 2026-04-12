package com.acuityspace.mantle.service;

import com.acuityspace.mantle.web.dto.AuthResponse;
import com.acuityspace.mantle.web.dto.LoginRequest;
import com.acuityspace.mantle.web.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request, HttpServletResponse response);

    void logout(HttpServletResponse response);
}
