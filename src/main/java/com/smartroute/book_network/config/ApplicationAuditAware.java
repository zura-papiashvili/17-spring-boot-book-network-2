package com.smartroute.book_network.config;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.smartroute.book_network.user.User;

public class ApplicationAuditAware implements AuditorAware<Integer> {

    @Override
    public Optional<Integer> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        User userPrincipal = (User) authentication.getPrincipal();

        return Optional.ofNullable(userPrincipal.getId());

    }

}
