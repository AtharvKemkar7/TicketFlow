package com.helixdesk.dto;

import com.helixdesk.entity.UserAccount;
import com.helixdesk.enums.AccountStatus;
import com.helixdesk.enums.RoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public final class UserDtos {
    private UserDtos() {
    }

    public record UserResponse(
            Long id,
            String email,
            String firstName,
            String lastName,
            String fullName,
            RoleType role,
            AccountStatus status,
            String department
    ) {
        public static UserResponse from(UserAccount user) {
            return new UserResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.fullName(),
                    user.getRole(),
                    user.getStatus(),
                    user.getDepartment()
            );
        }
    }

    public record CreateUserRequest(
            @Email @NotBlank String email,
            @NotBlank String password,
            @NotBlank String firstName,
            @NotBlank String lastName,
            RoleType role,
            String department
    ) {
    }

    public record UpdateUserRequest(
            String firstName,
            String lastName,
            RoleType role,
            AccountStatus status,
            String department
    ) {
    }
}
