package com.studycoachai.dto;

public record QuestionAnswerResponse(
        Long attemptId,
        Long questionId,
        Integer selectedIndex,
        Integer answerIndex,
        boolean correct,
        String explanation
) {
}
