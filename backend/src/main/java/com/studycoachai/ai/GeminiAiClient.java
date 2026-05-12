package com.studycoachai.ai;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studycoachai.dto.AiAdvicePromptData;
import com.studycoachai.dto.AiAdviceResponse;
import com.studycoachai.dto.AiAdviceTaskResponse;
import com.studycoachai.dto.AiAdviceWeakPointResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class GeminiAiClient implements AiClient {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public GeminiAiClient(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${app.ai.gemini.api-key:}") String apiKey,
            @Value("${app.ai.gemini.model:gemini-1.5-flash}") String model,
            @Value("${app.ai.gemini.connect-timeout-ms:5000}") int connectTimeoutMs,
            @Value("${app.ai.gemini.read-timeout-ms:30000}") int readTimeoutMs
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);
        this.restClient = restClientBuilder
                .baseUrl("https://generativelanguage.googleapis.com")
                .requestFactory(requestFactory)
                .build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AiAdviceResponse generateDailyAdvice(AiAdvicePromptData promptData) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY is not set on the backend.");
        }

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", buildPrompt(promptData)))
                )),
                "generationConfig", Map.of(
                        "temperature", 0.4,
                        "responseMimeType", "application/json"
                )
        );

        try {
            Map<String, Object> response = restClient.post()
                    .uri("/v1beta/models/{model}:generateContent?key={apiKey}", model, apiKey)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            String rawResponse = extractText(response);
            ParsedAdvice parsedAdvice = parseAdvice(rawResponse);
            return new AiAdviceResponse(
                    null,
                    null,
                    promptData.today(),
                    parsedAdvice.summary(),
                    parsedAdvice.tasks(),
                    parsedAdvice.weakPoints(),
                    parsedAdvice.overallAdvice(),
                    rawResponse,
                    null
            );
        } catch (RestClientResponseException ex) {
            throw new IllegalStateException("Gemini API returned an error: " + ex.getStatusCode() + " " + ex.getResponseBodyAsString(), ex);
        } catch (ResourceAccessException ex) {
            if (isTimeout(ex)) {
                throw new IllegalStateException("Gemini API request timed out.", ex);
            }
            throw new IllegalStateException("Gemini API request failed: " + ex.getMessage(), ex);
        }
    }

    private String buildPrompt(AiAdvicePromptData data) {
        return """
                あなたは資格・技術学習のコーチです。
                以下の学習データをもとに、今日の勉強メニューと苦手分野への改善アドバイスを日本語で生成してください。
                出力は説明文やMarkdownを含めず、必ずJSONのみを返してください。

                返却JSON形式:
                {
                  "summary": "今日はネットワークとデータベースを重点的に学習しましょう。",
                  "tasks": [
                    {
                      "title": "サブネット計算の復習",
                      "minutes": 30,
                      "reason": "直近の正答率が低いため"
                    }
                  ],
                  "weakPoints": [
                    {
                      "field": "ネットワーク",
                      "advice": "IPアドレスとサブネットマスクの計算問題を重点的に復習してください。"
                    }
                  ],
                  "overallAdvice": "新しい範囲を広げるより、直近で間違えた分野の復習を優先しましょう。"
                }

                ルール:
                - tasks は1〜5件
                - minutes は15〜90分
                - 学習対象の目標日が近いもの、期限切れタスク、苦手分野を優先
                - データが少ない場合は、未完了タスクと目標日から無理のない提案にする

                今日: %s
                学習対象一覧: %s
                目標日までの日数: %s
                今日の未完了タスク: %s
                期限切れタスク: %s
                直近7日間の学習ログ: %s
                分野別正答率: %s
                苦手分野: %s
                直近の演習ログ: %s
                """.formatted(
                data.today(),
                writeJson(data.studyTargets()),
                writeJson(data.targetDaysLeft()),
                writeJson(data.todayTasks()),
                writeJson(data.overdueTasks()),
                writeJson(data.recentSevenDayStudyLogs()),
                writeJson(data.fieldAccuracies()),
                writeJson(data.weakFields()),
                writeJson(data.recentQuestionLogs())
        );
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<String, Object> response) {
        if (response == null) {
            throw new IllegalStateException("Gemini API returned an empty response.");
        }
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        if (candidates == null || candidates.isEmpty()) {
            throw new IllegalStateException("Gemini API returned no candidates.");
        }
        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        List<Map<String, Object>> parts = content == null ? null : (List<Map<String, Object>>) content.get("parts");
        if (parts == null || parts.isEmpty() || parts.get(0).get("text") == null) {
            throw new IllegalStateException("Gemini API returned no text content.");
        }
        return parts.get(0).get("text").toString();
    }

    private ParsedAdvice parseAdvice(String rawResponse) {
        String json = extractJson(rawResponse);
        try {
            ParsedAdvice parsedAdvice = objectMapper.readValue(json, ParsedAdvice.class);
            if (isBlank(parsedAdvice.summary()) || isBlank(parsedAdvice.overallAdvice())) {
                throw new IllegalStateException("Gemini response JSON is missing required fields.");
            }
            return parsedAdvice.withSafeLists();
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Gemini response was not valid JSON.", ex);
        }
    }

    private String extractJson(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new IllegalStateException("Gemini response was empty.");
        }
        String trimmed = rawResponse.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(?:json)?", "").replaceFirst("```$", "").trim();
        }
        int firstBrace = trimmed.indexOf('{');
        int lastBrace = trimmed.lastIndexOf('}');
        if (firstBrace < 0 || lastBrace <= firstBrace) {
            throw new IllegalStateException("Gemini response did not contain a JSON object.");
        }
        return trimmed.substring(firstBrace, lastBrace + 1);
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not serialize prompt data for Gemini.", ex);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean isTimeout(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SocketTimeoutException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private record ParsedAdvice(
            String summary,
            List<AiAdviceTaskResponse> tasks,
            List<AiAdviceWeakPointResponse> weakPoints,
            String overallAdvice
    ) {
        private ParsedAdvice withSafeLists() {
            return new ParsedAdvice(
                    summary,
                    tasks == null ? List.of() : tasks,
                    weakPoints == null ? List.of() : weakPoints,
                    overallAdvice
            );
        }
    }
}
