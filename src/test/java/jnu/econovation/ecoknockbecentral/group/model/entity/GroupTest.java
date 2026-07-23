package jnu.econovation.ecoknockbecentral.group.model.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupType;
import jnu.econovation.ecoknockbecentral.group.model.vo.RecruitmentMode;
import jnu.econovation.ecoknockbecentral.group.model.vo.RecruitmentStatus;
import org.junit.jupiter.api.Test;

class GroupTest {

    private static final Instant START_AT = Instant.parse("2026-07-24T09:00:00Z");
    private static final Instant END_AT = Instant.parse("2026-07-31T09:00:00Z");

    @Test
    void trimsGroupNameAndDescription() {
        Group group = Group.create(
                "  알고리즘  ",
                "  함께 공부합니다.  ",
                GroupType.STUDY,
                10,
                RecruitmentMode.ALWAYS,
                null,
                null
        );

        assertThat(group.getName()).isEqualTo("알고리즘");
        assertThat(group.getDescription()).isEqualTo("함께 공부합니다.");
    }

    @Test
    void rejectsInvalidNameAndDescriptionLengths() {
        assertThatThrownBy(() -> Group.create(
                " ",
                "소개",
                GroupType.STUDY,
                10,
                RecruitmentMode.ALWAYS,
                null,
                null
        )).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> Group.create(
                "1234567890123456",
                "소개",
                GroupType.STUDY,
                10,
                RecruitmentMode.ALWAYS,
                null,
                null
        )).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> Group.create(
                "그룹",
                " ".repeat(2),
                GroupType.STUDY,
                10,
                RecruitmentMode.ALWAYS,
                null,
                null
        )).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> Group.create(
                "그룹",
                "가".repeat(101),
                GroupType.STUDY,
                10,
                RecruitmentMode.ALWAYS,
                null,
                null
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsCapacityOutsideAllowedRange() {
        assertThatThrownBy(() -> alwaysRecruitingGroup(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> alwaysRecruitingGroup(51))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requiresRecruitmentPeriodOnlyForPeriodMode() {
        assertThatThrownBy(() -> Group.create(
                "그룹",
                "소개",
                GroupType.DEPARTMENT,
                10,
                RecruitmentMode.ALWAYS,
                START_AT,
                END_AT
        )).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> Group.create(
                "그룹",
                "소개",
                GroupType.DEPARTMENT,
                10,
                RecruitmentMode.PERIOD,
                null,
                null
        )).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> Group.create(
                "그룹",
                "소개",
                GroupType.DEPARTMENT,
                10,
                RecruitmentMode.PERIOD,
                END_AT,
                START_AT
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void preservesAlwaysRecruitmentWhenPeriodUpdateFails() {
        Group group = alwaysRecruitingGroup(10);

        assertThatThrownBy(() -> group.updateRecruitment(
                RecruitmentMode.PERIOD,
                null,
                null
        )).isInstanceOf(IllegalArgumentException.class);

        assertThat(group.getRecruitmentMode()).isEqualTo(RecruitmentMode.ALWAYS);
        assertThat(group.getRecruitmentStartAt()).isNull();
        assertThat(group.getRecruitmentEndAt()).isNull();
    }

    @Test
    void preservesPeriodRecruitmentWhenAlwaysUpdateFails() {
        Group group = periodRecruitingGroup();

        assertThatThrownBy(() -> group.updateRecruitment(
                RecruitmentMode.ALWAYS,
                START_AT,
                END_AT
        )).isInstanceOf(IllegalArgumentException.class);

        assertThat(group.getRecruitmentMode()).isEqualTo(RecruitmentMode.PERIOD);
        assertThat(group.getRecruitmentStartAt()).isEqualTo(START_AT);
        assertThat(group.getRecruitmentEndAt()).isEqualTo(END_AT);
    }

    @Test
    void preservesDetailsWhenDetailsUpdateFails() {
        Group group = alwaysRecruitingGroup(10);

        assertThatThrownBy(() -> group.updateDetails(
                "변경된 소개",
                GroupType.DEPARTMENT,
                51
        )).isInstanceOf(IllegalArgumentException.class);

        assertThat(group.getDescription()).isEqualTo("소개");
        assertThat(group.getType()).isEqualTo(GroupType.STUDY);
        assertThat(group.getCapacity()).isEqualTo(10);
    }

    @Test
    void returnsUpcomingBeforeRecruitmentStart() {
        Group group = periodRecruitingGroup();

        assertThat(group.getRecruitmentStatus(1, START_AT.minusNanos(1)))
                .isEqualTo(RecruitmentStatus.UPCOMING);
    }

    @Test
    void includesRecruitmentStartAndEndInRecruitingPeriod() {
        Group group = periodRecruitingGroup();

        assertThat(group.getRecruitmentStatus(1, START_AT))
                .isEqualTo(RecruitmentStatus.RECRUITING);
        assertThat(group.getRecruitmentStatus(1, END_AT))
                .isEqualTo(RecruitmentStatus.RECRUITING);
    }

    @Test
    void closesAfterRecruitmentEnd() {
        Group group = periodRecruitingGroup();

        assertThat(group.getRecruitmentStatus(1, END_AT.plusNanos(1)))
                .isEqualTo(RecruitmentStatus.CLOSED);
    }

    @Test
    void returnsAlwaysRecruitingForAlwaysMode() {
        Group group = alwaysRecruitingGroup(10);

        assertThat(group.getRecruitmentStatus(1, START_AT))
                .isEqualTo(RecruitmentStatus.ALWAYS_RECRUITING);
    }

    @Test
    void closesWhenCapacityIsReachedRegardlessOfRecruitmentMode() {
        Group group = alwaysRecruitingGroup(2);

        assertThat(group.getRecruitmentStatus(2, START_AT))
                .isEqualTo(RecruitmentStatus.CLOSED);
        assertThat(group.getRecruitmentStatus(3, START_AT))
                .isEqualTo(RecruitmentStatus.CLOSED);
    }

    private Group alwaysRecruitingGroup(int capacity) {
        return Group.create(
                "그룹",
                "소개",
                GroupType.STUDY,
                capacity,
                RecruitmentMode.ALWAYS,
                null,
                null
        );
    }

    private Group periodRecruitingGroup() {
        return Group.create(
                "그룹",
                "소개",
                GroupType.STUDY,
                10,
                RecruitmentMode.PERIOD,
                START_AT,
                END_AT
        );
    }
}
