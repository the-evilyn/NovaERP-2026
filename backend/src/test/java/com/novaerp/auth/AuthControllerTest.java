package com.novaerp.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novaerp.auth.controller.AuthController;
import com.novaerp.auth.dto.*;
import com.novaerp.auth.service.AuthService;
import com.novaerp.security.jwt.CustomAccessDeniedHandler;
import com.novaerp.security.jwt.JwtAuthenticationEntryPoint;
import com.novaerp.security.jwt.JwtAuthenticationFilter;
import com.novaerp.security.jwt.JwtTokenProvider;
import com.novaerp.security.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void testLoginEndpoint() throws Exception {
        LoginRequest request = new LoginRequest("salma", "Password@123");
        UserResponse user = UserResponse.builder().id(1L).username("salma").email("salma@novaerp.ma").role("ADMIN").build();
        AuthResponse authResponse = AuthResponse.of("mock_jwt", "mock_refresh", 86400L, user);

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock_jwt"))
                .andExpect(jsonPath("$.user.username").value("salma"));
    }

    @Test
    void testRegisterEndpoint() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .email("new@novaerp.ma")
                .password("Password@123")
                .firstName("New")
                .lastName("User")
                .build();

        UserResponse user = UserResponse.builder().id(2L).username("newuser").email("new@novaerp.ma").role("EMPLOYEE").build();
        when(authService.register(any(RegisterRequest.class))).thenReturn(user);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.username").value("newuser"))
                .andExpect(jsonPath("$.success").value(true));
    }
}
