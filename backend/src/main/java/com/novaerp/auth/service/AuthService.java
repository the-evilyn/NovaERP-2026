package com.novaerp.auth.service;

import com.novaerp.auth.dto.*;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    UserResponse register(RegisterRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(String refreshToken);
    UserResponse getCurrentUser(String username);
}
