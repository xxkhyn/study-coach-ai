package com.studycoachai.ai;

import com.studycoachai.dto.AiAdvicePromptData;
import com.studycoachai.dto.AiAdviceResponse;
import com.studycoachai.dto.AiQuestionGenerationResult;
import com.studycoachai.dto.QuestionGenerationPromptData;

public interface AiClient {
    AiAdviceResponse generateDailyAdvice(AiAdvicePromptData promptData);

    AiQuestionGenerationResult generateQuestions(QuestionGenerationPromptData promptData);
}
