package com.studycoachai.security;

import org.springframework.security.oauth2.jwt.Jwt;

public final class CurrentUser {
    private CurrentUser() {
    }

    public static Long id(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null) {
            throw new IllegalArgumentException("Authenticated user is required.");
        }
        return Long.valueOf(jwt.getSubject());
    }
}
