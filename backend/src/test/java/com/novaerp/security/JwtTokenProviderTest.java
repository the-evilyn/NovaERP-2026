package com.novaerp.security;

import com.novaerp.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private final String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(secret, 3600000, 7200000, "NovaERP-Test");
    }

    @Test
    void testGenerateAndValidateToken() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "salma",
                "Password@123",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        String token = tokenProvider.generateToken(auth, 1L);

        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals("salma", tokenProvider.getUsernameFromToken(token));
        assertEquals(1L, tokenProvider.getUserIdFromToken(token));
    }

    @Test
    void testInvalidToken() {
        assertFalse(tokenProvider.validateToken("invalid.jwt.token"));
    }
}
