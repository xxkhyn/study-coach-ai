package com.studycoachai.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "question_generation_logs")
public class QuestionGenerationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(name = "study_target_id", nullable = false)
    private Long studyTargetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_target_id", insertable = false, updatable = false)
    private StudyTarget studyTarget;

    @Column(nullable = false, length = 80)
    private String examType;

    @Column(nullable = false, length = 80)
    private String field;

    @Column(nullable = false, length = 40)
    private String difficulty;

    @Column(nullable = false)
    private Integer count;

    @Column(nullable = false, columnDefinition = "text")
    private String prompt;

    @Column(nullable = false, columnDefinition = "text")
    private String rawResponse;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    protected QuestionGenerationLog() {
    }

    public QuestionGenerationLog(
            Long userId,
            Long studyTargetId,
            String examType,
            String field,
            String difficulty,
            Integer count,
            String prompt,
            String rawResponse
    ) {
        this.userId = userId;
        this.studyTargetId = studyTargetId;
        this.examType = examType;
        this.field = field;
        this.difficulty = difficulty;
        this.count = count;
        this.prompt = prompt;
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

    public Long getStudyTargetId() {
        return studyTargetId;
    }

    public StudyTarget getStudyTarget() {
        return studyTarget;
    }

    public String getExamType() {
        return examType;
    }

    public String getField() {
        return field;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public Integer getCount() {
        return count;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
