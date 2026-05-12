package com.studycoachai.entity;

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
@Table(name = "study_logs")
public class StudyLog {
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
    private LocalDate studiedDate;

    @Column(nullable = false)
    private Integer minutes;

    @Column(length = 1000)
    private String memo;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    protected StudyLog() {
    }

    public StudyLog(Long userId, Long studyTargetId, String field, LocalDate studiedDate, Integer minutes, String memo) {
        this.userId = userId;
        this.studyTargetId = studyTargetId;
        this.field = field;
        this.studiedDate = studiedDate;
        this.minutes = minutes;
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

    public void update(Long studyTargetId, String field, LocalDate studiedDate, Integer minutes, String memo) {
        this.studyTargetId = studyTargetId;
        this.field = field;
        this.studiedDate = studiedDate;
        this.minutes = minutes;
        this.memo = memo;
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

    public LocalDate getStudiedDate() {
        return studiedDate;
    }

    public Integer getMinutes() {
        return minutes;
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
