package jnu.econovation.ecoknockbecentral.group.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import jnu.econovation.ecoknockbecentral.common.model.entity.BaseEntity;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupType;
import jnu.econovation.ecoknockbecentral.group.model.vo.RecruitmentMode;
import jnu.econovation.ecoknockbecentral.group.model.vo.RecruitmentStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "groups")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Group extends BaseEntity {

    public static final int MIN_NAME_LENGTH = 1;
    public static final int MAX_NAME_LENGTH = 15;
    public static final int MIN_DESCRIPTION_LENGTH = 1;
    public static final int MAX_DESCRIPTION_LENGTH = 100;
    public static final int MIN_CAPACITY = 1;
    public static final int MAX_CAPACITY = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = MAX_NAME_LENGTH)
    private String name;

    @Column(nullable = false, length = MAX_DESCRIPTION_LENGTH)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private GroupType type;

    @Column(nullable = false)
    private int capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RecruitmentMode recruitmentMode;

    private Instant recruitmentStartAt;

    private Instant recruitmentEndAt;

    private Group(
            String name,
            String description,
            GroupType type,
            int capacity,
            RecruitmentMode recruitmentMode,
            Instant recruitmentStartAt,
            Instant recruitmentEndAt
    ) {
        this.name = requireTrimmedLength(name, MIN_NAME_LENGTH, MAX_NAME_LENGTH, "Group name");
        this.description = requireTrimmedLength(
                description,
                MIN_DESCRIPTION_LENGTH,
                MAX_DESCRIPTION_LENGTH,
                "Group description"
        );
        this.type = Objects.requireNonNull(type, "Group type must not be null");
        this.capacity = requireCapacity(capacity);
        setRecruitment(recruitmentMode, recruitmentStartAt, recruitmentEndAt);
    }

    public static Group create(
            String name,
            String description,
            GroupType type,
            int capacity,
            RecruitmentMode recruitmentMode,
            Instant recruitmentStartAt,
            Instant recruitmentEndAt
    ) {
        return new Group(
                name,
                description,
                type,
                capacity,
                recruitmentMode,
                recruitmentStartAt,
                recruitmentEndAt
        );
    }

    public void updateName(String name) {
        this.name = requireTrimmedLength(name, MIN_NAME_LENGTH, MAX_NAME_LENGTH, "Group name");
    }

    public void updateDetails(String description, GroupType type, int capacity) {
        String validatedDescription = requireTrimmedLength(
                description,
                MIN_DESCRIPTION_LENGTH,
                MAX_DESCRIPTION_LENGTH,
                "Group description"
        );
        GroupType validatedType = Objects.requireNonNull(type, "Group type must not be null");
        int validatedCapacity = requireCapacity(capacity);

        this.description = validatedDescription;
        this.type = validatedType;
        this.capacity = validatedCapacity;
    }

    public void updateRecruitment(
            RecruitmentMode recruitmentMode,
            Instant recruitmentStartAt,
            Instant recruitmentEndAt
    ) {
        setRecruitment(recruitmentMode, recruitmentStartAt, recruitmentEndAt);
    }

    public RecruitmentStatus getRecruitmentStatus(int memberCount, Instant now) {
        if (memberCount < 0) {
            throw new IllegalArgumentException("Member count must not be negative");
        }
        Objects.requireNonNull(now, "Current time must not be null");

        if (memberCount >= capacity) {
            return RecruitmentStatus.CLOSED;
        }
        if (recruitmentMode == RecruitmentMode.ALWAYS) {
            return RecruitmentStatus.ALWAYS_RECRUITING;
        }
        if (now.isBefore(recruitmentStartAt)) {
            return RecruitmentStatus.UPCOMING;
        }
        if (now.isAfter(recruitmentEndAt)) {
            return RecruitmentStatus.CLOSED;
        }
        return RecruitmentStatus.RECRUITING;
    }

    private void setRecruitment(
            RecruitmentMode recruitmentMode,
            Instant recruitmentStartAt,
            Instant recruitmentEndAt
    ) {
        RecruitmentMode validatedMode = Objects.requireNonNull(
                recruitmentMode,
                "Recruitment mode must not be null"
        );
        if (validatedMode == RecruitmentMode.ALWAYS) {
            if (recruitmentStartAt != null || recruitmentEndAt != null) {
                throw new IllegalArgumentException(
                        "Always recruiting group must not have a recruitment period"
                );
            }
        } else {
            if (recruitmentStartAt == null || recruitmentEndAt == null) {
                throw new IllegalArgumentException(
                        "Period recruiting group must have a recruitment period"
                );
            }
            if (recruitmentStartAt.isAfter(recruitmentEndAt)) {
                throw new IllegalArgumentException(
                        "Recruitment start must not be after recruitment end"
                );
            }
        }

        this.recruitmentMode = validatedMode;
        this.recruitmentStartAt = recruitmentStartAt;
        this.recruitmentEndAt = recruitmentEndAt;
    }

    private static int requireCapacity(int capacity) {
        if (capacity < MIN_CAPACITY || capacity > MAX_CAPACITY) {
            throw new IllegalArgumentException(
                    "Group capacity must be between " + MIN_CAPACITY + " and " + MAX_CAPACITY
            );
        }
        return capacity;
    }

    private static String requireTrimmedLength(
            String value,
            int minLength,
            int maxLength,
            String fieldName
    ) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        String trimmed = value.trim();
        if (trimmed.length() < minLength || trimmed.length() > maxLength) {
            throw new IllegalArgumentException(
                    fieldName + " length must be between " + minLength + " and " + maxLength
            );
        }
        return trimmed;
    }
}
