package com.helixdesk.security;

import com.helixdesk.entity.UserAccount;
import com.helixdesk.exception.ForbiddenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    public UserAccount requireUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserDetails details)) {
            throw new ForbiddenException("Authentication required");
        }
        return details.getUser();
    }
}
