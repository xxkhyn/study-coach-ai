package com.studycoachai.dto;

import java.time.OffsetDateTime;

import com.studycoachai.entity.User;

public record UserResponse(
        Long id,
        String username,
        String email,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
