package com.novaerp.auth.service;

import com.novaerp.auth.dto.*;
import com.novaerp.exception.BadRequestException;
import com.novaerp.exception.ResourceAlreadyExistsException;
import com.novaerp.exception.ResourceNotFoundException;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Attempting login for user: {}", request.getUsername());

        // Validate user existence and status
        User user = userRepository.findByEmailOrUsername(request.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid username/email or password"));

        if (!user.isActive()) {
            throw new UnauthorizedException("User account is inactive or suspended");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            String accessToken = tokenProvider.generateToken(authentication, user.getId());
            RefreshToken refreshToken = createRefreshToken(user);

            UserResponse userResponse = UserResponse.fromEntity(user);
            return AuthResponse.of(accessToken, refreshToken.getToken(), 86400L, userResponse);
        } catch (BadCredentialsException ex) {
            log.warn("Failed authentication attempt for: {}", request.getUsername());
            throw new UnauthorizedException("Invalid username/email or password");
        }
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.info("Registering new user: {} ({})", request.getUsername(), request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException("User with username " + request.getUsername() + " already exists");
        }

        Set<Role> roles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (RoleName roleName : request.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Role " + roleName + " not found"));
                roles.add(role);
            }
        } else {
            Role defaultRole = roleRepository.findByName(RoleName.ROLE_EMPLOYEE)
                    .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_EMPLOYEE, "Default Employee Role")));
            roles.add(defaultRole);
        }

        User user = User.builder()
                .username(request.getUsername().trim().toLowerCase())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .phone(request.getPhone())
                .status(UserStatus.ACTIVE)
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);
        return UserResponse.fromEntity(savedUser);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String requestToken = request.getRefreshToken();

        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestToken)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (refreshToken.isRevoked() || refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new UnauthorizedException("Refresh token is expired or revoked. Please log in again.");
        }

        User user = refreshToken.getUser();
        if (!user.isActive()) {
            throw new UnauthorizedException("User account is inactive or suspended");
        }

        List<String> roleNames = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toList());

        String newAccessToken = tokenProvider.generateTokenFromUser(
                user.getId(),
                user.getUsername() != null ? user.getUsername() : user.getEmail(),
                roleNames
        );

        // Rotate Refresh Token
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(tokenProvider.getRefreshTokenExpirationMs()));
        RefreshToken updatedToken = refreshTokenRepository.save(refreshToken);

        UserResponse userResponse = UserResponse.fromEntity(user);
        return AuthResponse.of(newAccessToken, updatedToken.getToken(), 86400L, userResponse);
    }

    @Override
    @Transactional
    public void logout(String refreshTokenStr) {
        if (refreshTokenStr != null && !refreshTokenStr.isBlank()) {
            refreshTokenRepository.findByToken(refreshTokenStr).ifPresent(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
            });
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByEmailOrUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with identifier: " + username));
        return UserResponse.fromEntity(user);
    }

    private RefreshToken createRefreshToken(User user) {
        // Revoke prior active refresh tokens
        refreshTokenRepository.revokeAllUserTokens(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(tokenProvider.getRefreshTokenExpirationMs()))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }
}
