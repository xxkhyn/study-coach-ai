package com.studycoachai.entity;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "ai_advice_logs")
public class AiAdviceLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDate adviceDate;

    @Column(nullable = false, length = 1000)
    private String summary;

    @Lob
    @Column(nullable = false)
    private String tasksJson;

    @Lob
    @Column(nullable = false)
    private String weakPointsJson;

    @Column(nullable = false, length = 2000)
    private String overallAdvice;

    @Lob
    @Column(nullable = false)
    private String rawResponse;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    protected AiAdviceLog() {
    }

    public AiAdviceLog(
            Long userId,
            LocalDate adviceDate,
            String summary,
            String tasksJson,
            String weakPointsJson,
            String overallAdvice,
            String rawResponse
    ) {
        this.userId = userId;
        this.adviceDate = adviceDate;
        this.summary = summary;
        this.tasksJson = tasksJson;
        this.weakPointsJson = weakPointsJson;
        this.overallAdvice = overallAdvice;
        this.rawResponse = rawResponse;
    }

    @PrePersist
    void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public LocalDate getAdviceDate() {
        return adviceDate;
    }

    public String getSummary() {
        return summary;
    }

    public String getTasksJson() {
        return tasksJson;
    }

    public String getWeakPointsJson() {
        return weakPointsJson;
    }

    public String getOverallAdvice() {
        return overallAdvice;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
