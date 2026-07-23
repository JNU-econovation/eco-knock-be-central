package jnu.econovation.ecoknockbecentral.group.model.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import jnu.econovation.ecoknockbecentral.group.model.vo.GroupMemberRole;
import jnu.econovation.ecoknockbecentral.member.model.entity.Member;
import org.junit.jupiter.api.Test;

class GroupMemberTest {

    private final Group group = mock(Group.class);
    private final Member member = mock(Member.class);

    @Test
    void createsLeaderAndMemberRoles() {
        assertThat(GroupMember.leader(group, member).getRole())
                .isEqualTo(GroupMemberRole.LEADER);
        assertThat(GroupMember.member(group, member).getRole())
                .isEqualTo(GroupMemberRole.MEMBER);
    }

    @Test
    void changesMemberRole() {
        GroupMember groupMember = GroupMember.member(group, member);

        groupMember.promoteToLeader();
        assertThat(groupMember.getRole()).isEqualTo(GroupMemberRole.LEADER);

        groupMember.demoteToMember();
        assertThat(groupMember.getRole()).isEqualTo(GroupMemberRole.MEMBER);
    }

    @Test
    void requiresGroupAndMember() {
        assertThatThrownBy(() -> GroupMember.member(null, member))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> GroupMember.member(group, null))
                .isInstanceOf(NullPointerException.class);
    }
}
