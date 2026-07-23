package jnu.econovation.ecoknockbecentral.group.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import jnu.econovation.ecoknockbecentral.group.model.entity.GroupMember;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    long countByGroupId(Long groupId);

    boolean existsByGroupIdAndMemberId(Long groupId, Long memberId);

    boolean existsByMemberIdAndRole(Long memberId, GroupMemberRole role);

    Optional<GroupMember> findByGroupIdAndMemberId(Long groupId, Long memberId);

    List<GroupMember> findAllByGroupId(Long groupId);

    @Query("""
            SELECT groupMember
            FROM GroupMember groupMember
            JOIN FETCH groupMember.group
            WHERE groupMember.member.id = :memberId
            ORDER BY groupMember.group.name ASC, groupMember.group.id ASC
            """)
    List<GroupMember> findAllByMemberIdWithGroup(@Param("memberId") Long memberId);

    @Query("""
            SELECT groupMember
            FROM GroupMember groupMember
            JOIN FETCH groupMember.member
            WHERE groupMember.group.id = :groupId
            ORDER BY groupMember.member.name ASC, groupMember.member.id ASC
            """)
    List<GroupMember> findAllByGroupIdWithMember(@Param("groupId") Long groupId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT groupMember
            FROM GroupMember groupMember
            WHERE groupMember.group.id = :groupId
              AND groupMember.member.id = :memberId
            """)
    Optional<GroupMember> findByGroupIdAndMemberIdForUpdate(
            @Param("groupId") Long groupId,
            @Param("memberId") Long memberId
    );
}
