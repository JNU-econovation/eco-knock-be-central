package jnu.econovation.ecoknockbecentral.member.dto

import jnu.econovation.ecoknockbecentral.member.model.entity.Member
import jnu.econovation.ecoknockbecentral.member.model.vo.ActiveStatus
import jnu.econovation.ecoknockbecentral.member.model.vo.Cohort
import jnu.econovation.ecoknockbecentral.member.model.vo.Oauth2Provider
import jnu.econovation.ecoknockbecentral.member.model.vo.Role

data class MemberInfoDTO(
    val id: Long,
    val role: Role,
    val cohort: Cohort,
    val name: String,
    val status: ActiveStatus,
    val oauth2Provider: Oauth2Provider
) {
    companion object {
        fun from(member: Member): MemberInfoDTO {
            return MemberInfoDTO(
                id = member.id,
                role = member.role,
                cohort = member.cohort,
                name = member.name,
                status = member.status,
                oauth2Provider = member.provider
            )
        }
    }
}