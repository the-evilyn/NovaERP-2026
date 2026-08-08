package com.novaerp.auth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.novaerp.role.entity.Role;
import com.novaerp.role.entity.RoleName;
import com.novaerp.user.entity.User;
import com.novaerp.user.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String role;
    private Set<String> roles;
    private boolean active;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static UserResponse fromEntity(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());

        String primaryRole = "USER";
        if (roleNames.contains(RoleName.ROLE_ADMIN.name())) {
            primaryRole = "ADMIN";
        } else if (roleNames.contains(RoleName.ROLE_MANAGER.name())) {
            primaryRole = "MANAGER";
        } else if (roleNames.contains(RoleName.ROLE_EMPLOYEE.name())) {
            primaryRole = "EMPLOYEE";
        }

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername() != null ? user.getUsername() : user.getEmail())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(primaryRole)
                .roles(roleNames)
                .active(UserStatus.ACTIVE.equals(user.getStatus()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
