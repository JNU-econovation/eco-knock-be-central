package jnu.econovation.ecoknockbecentral.member.repository

import jnu.econovation.ecoknockbecentral.member.model.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MemberRepository : JpaRepository<Member, Long>