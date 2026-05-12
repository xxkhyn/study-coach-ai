package com.studycoachai.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "question_logs")
public class QuestionLog {
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

    @Column(length = 80)
    private String field;

    @Column(nullable = false)
    private LocalDate practicedDate;

    @Column(nullable = false)
    private Integer solvedCount;

    @Column(nullable = false)
    private Integer correctCount;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal accuracyRate;

    @Column(length = 1000)
    private String memo;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    protected QuestionLog() {
    }

    public QuestionLog(Long userId, Long studyTargetId, String field, LocalDate practicedDate, Integer solvedCount, Integer correctCount, String memo) {
        this.userId = userId;
        this.studyTargetId = studyTargetId;
        this.field = field;
        this.practicedDate = practicedDate;
        this.solvedCount = solvedCount;
        this.correctCount = correctCount;
        this.accuracyRate = calculateAccuracy(solvedCount, correctCount);
        this.memo = memo;
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    private BigDecimal calculateAccuracy(Integer solvedCount, Integer correctCount) {
        if (solvedCount == null || solvedCount == 0 || correctCount == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(correctCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(solvedCount), 2, RoundingMode.HALF_UP);
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

    public String getField() {
        return field;
    }

    public LocalDate getPracticedDate() {
        return practicedDate;
    }

    public Integer getSolvedCount() {
        return solvedCount;
    }

    public Integer getCorrectCount() {
        return correctCount;
    }

    public BigDecimal getAccuracyRate() {
        return accuracyRate;
    }

    public String getMemo() {
        return memo;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
