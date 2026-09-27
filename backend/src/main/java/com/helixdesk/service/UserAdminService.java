package com.helixdesk.service;

import com.helixdesk.dto.UserDtos.CreateUserRequest;
import com.helixdesk.dto.UserDtos.UpdateUserRequest;
import com.helixdesk.dto.UserDtos.UserResponse;
import com.helixdesk.entity.UserAccount;
import com.helixdesk.enums.AccountStatus;
import com.helixdesk.enums.RoleType;
import com.helixdesk.exception.BadRequestException;
import com.helixdesk.exception.NotFoundException;
import com.helixdesk.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserAdminService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAdminService(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> all() {
        return userAccountRepository.findAll().stream().map(UserResponse::from).toList();
    }

    public UserResponse get(Long id) {
        return UserResponse.from(require(id));
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userAccountRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BadRequestException("Email already registered");
        }
        UserAccount user = new UserAccount();
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setDepartment(request.department());
        user.setRole(request.role() == null ? RoleType.USER : request.role());
        user.setStatus(AccountStatus.ACTIVE);
        return UserResponse.from(userAccountRepository.save(user));
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        UserAccount user = require(id);
        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }
        if (request.status() != null) {
            user.setStatus(request.status());
        }
        if (request.department() != null) {
            user.setDepartment(request.department());
        }
        return UserResponse.from(user);
    }

    private UserAccount require(Long id) {
        return userAccountRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }
}
