package com.studycoachai.ai;

import com.studycoachai.dto.AiAdvicePromptData;
import com.studycoachai.dto.AiAdviceResponse;

public interface AiClient {
    AiAdviceResponse generateDailyAdvice(AiAdvicePromptData promptData);
}
