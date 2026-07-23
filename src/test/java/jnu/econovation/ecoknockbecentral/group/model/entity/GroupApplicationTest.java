package jnu.econovation.ecoknockbecentral.group.model.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import jnu.econovation.ecoknockbecentral.group.model.vo.GroupApplicationStatus;
import jnu.econovation.ecoknockbecentral.member.model.entity.Member;
import org.junit.jupiter.api.Test;

class GroupApplicationTest {

    private final Group group = mock(Group.class);
    private final Member applicant = mock(Member.class);

    @Test
    void createsPendingApplicationWithTrimmedContent() {
        GroupApplication application = GroupApplication.pending(
                group,
                applicant,
                "  지원합니다.  "
        );

        assertThat(application.getContent()).isEqualTo("지원합니다.");
        assertThat(application.getStatus()).isEqualTo(GroupApplicationStatus.PENDING);
    }

    @Test
    void rejectsInvalidContentLength() {
        assertThatThrownBy(() -> GroupApplication.pending(group, applicant, " "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> GroupApplication.pending(group, applicant, "가".repeat(21)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void acceptsOrRejectsOnlyPendingApplication() {
        GroupApplication accepted = GroupApplication.pending(group, applicant, "지원");
        accepted.accept();
        assertThat(accepted.getStatus()).isEqualTo(GroupApplicationStatus.ACCEPTED);
        assertThatThrownBy(accepted::reject).isInstanceOf(IllegalStateException.class);

        GroupApplication rejected = GroupApplication.pending(group, applicant, "지원");
        rejected.reject();
        assertThat(rejected.getStatus()).isEqualTo(GroupApplicationStatus.REJECTED);
        assertThatThrownBy(rejected::accept).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requiresGroupAndApplicant() {
        assertThatThrownBy(() -> GroupApplication.pending(null, applicant, "지원"))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> GroupApplication.pending(group, null, "지원"))
                .isInstanceOf(NullPointerException.class);
    }
}
