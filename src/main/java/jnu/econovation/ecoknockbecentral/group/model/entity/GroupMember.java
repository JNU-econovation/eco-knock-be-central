package jnu.econovation.ecoknockbecentral.group.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;
import jnu.econovation.ecoknockbecentral.common.model.entity.BaseEntity;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupMemberRole;
import jnu.econovation.ecoknockbecentral.member.model.entity.Member;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "group_member",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_group_member_group_member",
                columnNames = {"group_id", "member_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private GroupMemberRole role;

    private GroupMember(Group group, Member member, GroupMemberRole role) {
        this.group = Objects.requireNonNull(group, "Group must not be null");
        this.member = Objects.requireNonNull(member, "Member must not be null");
        this.role = Objects.requireNonNull(role, "Group member role must not be null");
    }

    public static GroupMember leader(Group group, Member member) {
        return new GroupMember(group, member, GroupMemberRole.LEADER);
    }

    public static GroupMember member(Group group, Member member) {
        return new GroupMember(group, member, GroupMemberRole.MEMBER);
    }

    public void promoteToLeader() {
        this.role = GroupMemberRole.LEADER;
    }

    public void demoteToMember() {
        this.role = GroupMemberRole.MEMBER;
    }
}
