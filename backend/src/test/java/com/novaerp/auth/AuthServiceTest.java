package com.novaerp.auth;

import com.novaerp.auth.dto.LoginRequest;
import com.novaerp.auth.dto.RegisterRequest;
import com.novaerp.auth.dto.UserResponse;
import com.novaerp.auth.dto.AuthResponse;
import com.novaerp.auth.service.AuthServiceImpl;
import com.novaerp.exception.ResourceAlreadyExistsException;
import com.novaerp.exception.UnauthorizedException;
import com.novaerp.role.entity.Role;
import com.novaerp.role.entity.RoleName;
import com.novaerp.role.repository.RoleRepository;
import com.novaerp.security.entity.RefreshToken;
import com.novaerp.security.jwt.JwtTokenProvider;
import com.novaerp.security.repository.RefreshTokenRepository;
import com.novaerp.user.entity.User;
import com.novaerp.user.entity.UserStatus;
import com.novaerp.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;
    private Role sampleRole;

    @BeforeEach
    void setUp() {
        sampleRole = Role.builder().id(1L).name(RoleName.ROLE_ADMIN).build();
        sampleUser = User.builder()
                .id(1L)
                .username("salma")
                .email("salma@novaerp.ma")
                .password("encoded_pass")
                .firstName("Salma")
                .lastName("Architect")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(sampleRole))
                .build();
    }

    @Test
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest("salma", "Password@123");
        Authentication auth = mock(Authentication.class);

        when(userRepository.findByEmailOrUsername("salma")).thenReturn(Optional.of(sampleUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(tokenProvider.generateToken(any(), anyLong())).thenReturn("mock_access_token");
        when(tokenProvider.getRefreshTokenExpirationMs()).thenReturn(604800000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> {
            RefreshToken t = i.getArgument(0);
            t.setId(10L);
            return t;
        });

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock_access_token", response.getToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("salma", response.getUser().getUsername());
    }

    @Test
    void testLoginInactiveUserFails() {
        sampleUser.setStatus(UserStatus.INACTIVE);
        when(userRepository.findByEmailOrUsername("salma")).thenReturn(Optional.of(sampleUser));

        LoginRequest request = new LoginRequest("salma", "Password@123");
        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void testRegisterSuccess() {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .email("new@novaerp.ma")
                .password("Secret@123")
                .firstName("New")
                .lastName("User")
                .build();

        when(userRepository.existsByEmail("new@novaerp.ma")).thenReturn(false);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_EMPLOYEE)).thenReturn(Optional.of(sampleRole));
        when(passwordEncoder.encode(any())).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(5L);
            return u;
        });

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("newuser", response.getUsername());
        assertEquals("new@novaerp.ma", response.getEmail());
    }

    @Test
    void testRegisterDuplicateEmailThrowsException() {
        RegisterRequest request = RegisterRequest.builder()
                .username("salma")
                .email("salma@novaerp.ma")
                .build();

        when(userRepository.existsByEmail("salma@novaerp.ma")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> authService.register(request));
    }
}
