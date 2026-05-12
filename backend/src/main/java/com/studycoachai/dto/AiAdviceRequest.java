package com.studycoachai.dto;

public record AiAdviceRequest(
        Boolean force
) {
    public boolean shouldForce() {
        return Boolean.TRUE.equals(force);
    }
}
