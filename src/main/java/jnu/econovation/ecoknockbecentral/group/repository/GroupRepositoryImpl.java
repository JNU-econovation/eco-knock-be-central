package jnu.econovation.ecoknockbecentral.group.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import jnu.econovation.ecoknockbecentral.group.model.entity.QGroup;
import jnu.econovation.ecoknockbecentral.group.model.entity.QGroupMember;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupMemberRole;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupSort;
import jnu.econovation.ecoknockbecentral.group.model.vo.RecruitmentMode;
import org.springframework.stereotype.Repository;

@Repository
public class GroupRepositoryImpl implements GroupRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public GroupRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public List<GroupBrowseRow> findAllForBrowse(
            boolean excludeClosed,
            GroupSort sort,
            Instant now,
            Long requesterId
    ) {
        return findAllSummaries(excludeClosed, sort, now, requesterId, false);
    }

    @Override
    public List<GroupBrowseRow> findAllForMember(Long memberId, Instant now) {
        return findAllSummaries(false, GroupSort.NAME_ASC, now, memberId, true);
    }

    private List<GroupBrowseRow> findAllSummaries(
            boolean excludeClosed,
            GroupSort sort,
            Instant now,
            Long requesterId,
            boolean onlyMember
    ) {
        QGroup group = QGroup.group;
        QGroupMember member = new QGroupMember("summaryMember");
        QGroupMember leader = new QGroupMember("summaryLeader");
        QGroupMember requester = new QGroupMember("summaryRequester");

        NumberExpression<Long> memberCount = member.id.count();
        BooleanExpression capacityAvailable = memberCount.lt(group.capacity.longValue());
        BooleanExpression periodNotEnded = group.recruitmentMode.eq(RecruitmentMode.ALWAYS)
                .or(group.recruitmentEndAt.goe(now));
        NumberExpression<Integer> requesterMembership = new CaseBuilder()
                .when(requester.id.isNotNull()).then(1)
                .otherwise(0);
        NumberExpression<Integer> requesterLeadership = new CaseBuilder()
                .when(requester.role.eq(GroupMemberRole.LEADER)).then(1)
                .otherwise(0);

        return queryFactory
                .select(Projections.constructor(
                        GroupBrowseRow.class,
                        group,
                        memberCount,
                        leader.member.name,
                        requesterMembership.eq(1),
                        requesterLeadership.eq(1)
                ))
                .from(group)
                .leftJoin(member).on(member.group.eq(group))
                .leftJoin(leader).on(
                        leader.group.eq(group)
                                .and(leader.role.eq(GroupMemberRole.LEADER))
                )
                .leftJoin(requester).on(
                        requester.group.eq(group)
                                .and(requester.member.id.eq(requesterId))
                )
                .where(
                        excludeClosed ? periodNotEnded : null,
                        onlyMember ? requester.id.isNotNull() : null
                )
                .groupBy(group, leader.member.name, requester.id, requester.role)
                .having(excludeClosed ? capacityAvailable : null)
                .orderBy(orderSpecifiers(sort, group, memberCount, now))
                .fetch();
    }

    private OrderSpecifier<?>[] orderSpecifiers(
            GroupSort sort,
            QGroup group,
            NumberExpression<Long> memberCount,
            Instant now
    ) {
        return switch (sort) {
            case NAME_ASC -> new OrderSpecifier<?>[]{group.name.asc(), group.id.asc()};
            case NAME_DESC -> new OrderSpecifier<?>[]{group.name.desc(), group.id.asc()};
            case RECENT -> new OrderSpecifier<?>[]{group.createdAt.desc(), group.id.asc()};
            case DEADLINE_ASC -> deadlineOrder(group, memberCount, now);
        };
    }

    private OrderSpecifier<?>[] deadlineOrder(
            QGroup group,
            NumberExpression<Long> memberCount,
            Instant now
    ) {
        BooleanExpression full = memberCount.goe(group.capacity.longValue());
        BooleanExpression recruitingPeriod = group.recruitmentMode.eq(RecruitmentMode.PERIOD)
                .and(group.recruitmentStartAt.loe(now))
                .and(group.recruitmentEndAt.goe(now))
                .and(full.not());
        BooleanExpression upcoming = group.recruitmentMode.eq(RecruitmentMode.PERIOD)
                .and(group.recruitmentStartAt.gt(now))
                .and(full.not());
        BooleanExpression always = group.recruitmentMode.eq(RecruitmentMode.ALWAYS)
                .and(full.not());

        NumberExpression<Integer> category = new CaseBuilder()
                .when(recruitingPeriod).then(0)
                .when(upcoming).then(1)
                .when(always).then(2)
                .otherwise(3);

        return new OrderSpecifier<?>[]{
                category.asc(),
                new CaseBuilder()
                        .when(recruitingPeriod).then(group.recruitmentEndAt)
                        .otherwise((Instant) null)
                        .asc(),
                new CaseBuilder()
                        .when(upcoming).then(group.recruitmentStartAt)
                        .otherwise((Instant) null)
                        .asc(),
                group.id.asc()
        };
    }
}
