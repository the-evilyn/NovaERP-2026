package com.novaerp.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    @JsonProperty("token")
    private String token;

    @JsonProperty("accessToken")
    private String accessToken;

    @JsonProperty("refreshToken")
    private String refreshToken;

    @Builder.Default
    @JsonProperty("tokenType")
    private String tokenType = "Bearer";

    @JsonProperty("expiresIn")
    private Long expiresIn;

    @JsonProperty("user")
    private UserResponse user;

    public static AuthResponse of(String token, String refreshToken, Long expiresIn, UserResponse user) {
        return AuthResponse.builder()
                .token(token)
                .accessToken(token)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(user)
                .build();
    }
}
