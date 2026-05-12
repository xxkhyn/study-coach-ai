package com.studycoachai.dto;

public record AuthResponse(
        String token,
        UserResponse user
) {
}
