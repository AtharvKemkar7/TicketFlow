package com.helixdesk.service;

import com.helixdesk.dto.AuthDtos.AuthResponse;
import com.helixdesk.dto.AuthDtos.LoginRequest;
import com.helixdesk.dto.AuthDtos.RegisterRequest;
import com.helixdesk.entity.UserAccount;
import com.helixdesk.enums.AccountStatus;
import com.helixdesk.enums.AuditAction;
import com.helixdesk.enums.RoleType;
import com.helixdesk.exception.BadRequestException;
import com.helixdesk.repository.UserAccountRepository;
import com.helixdesk.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuditService auditService;

    public AuthService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            AuditService auditService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.auditService = auditService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userAccountRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BadRequestException("Email already registered");
        }
        UserAccount user = new UserAccount();
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setDepartment(request.department());
        user.setRole(RoleType.USER);
        user.setStatus(AccountStatus.ACTIVE);
        userAccountRepository.save(user);
        return toAuth(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password()));
        UserAccount user = userAccountRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));
        auditService.record(user, null, AuditAction.LOGIN, null, user.getEmail(), null);
        return toAuth(user);
    }

    private AuthResponse toAuth(UserAccount user) {
        return new AuthResponse(jwtService.generateToken(user), user.getId(), user.getEmail(), user.fullName(), user.getRole());
    }
}
