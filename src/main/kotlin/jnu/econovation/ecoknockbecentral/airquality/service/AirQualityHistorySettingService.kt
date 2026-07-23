package jnu.econovation.ecoknockbecentral.airquality.service

import jnu.econovation.ecoknockbecentral.airquality.dto.rest.request.UpdateAirQualityHistorySettingRequest
import jnu.econovation.ecoknockbecentral.airquality.dto.rest.response.GetAirQualityHistorySettingResponse
import jnu.econovation.ecoknockbecentral.airquality.model.entity.AirQualityHistorySetting
import jnu.econovation.ecoknockbecentral.airquality.model.vo.AirQualityResolution
import jnu.econovation.ecoknockbecentral.airquality.repository.AirQualityHistorySettingRepository
import jnu.econovation.ecoknockbecentral.common.exception.server.InternalServerException
import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO
import jnu.econovation.ecoknockbecentral.member.service.MemberService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class AirQualityHistorySettingService(
    private val memberService: MemberService,
    private val repository: AirQualityHistorySettingRepository,
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun initialize(memberId: Long) {
        if (repository.existsByMemberId(memberId)) {
            return
        }

        val member = memberService.getEntityOrThrow(memberId)
        repository.save(
            AirQualityHistorySetting.builder()
                .member(member)
                .resolution(AirQualityResolution.FIFTEEN_MINUTES)
                .build()
        )
    }

    @Transactional(readOnly = true)
    fun get(memberInfo: MemberInfoDTO): GetAirQualityHistorySettingResponse {
        val setting = repository.findByMemberId(memberInfo.id)
            ?: throw InternalServerException(
                IllegalStateException("id가 ${memberInfo.id}인 공기질 과거 시계열 설정을 찾을 수 없음.")
            )
        return GetAirQualityHistorySettingResponse(
            resolution = setting.resolution
        )
    }

    @Transactional
    fun update(memberInfo: MemberInfoDTO, request: UpdateAirQualityHistorySettingRequest) {
        val setting = repository.findByMemberId(memberInfo.id)
            ?: throw InternalServerException(
                IllegalStateException("id가 ${memberInfo.id}인 공기질 과거 시계열 설정을 찾을 수 없음.")
            )
        setting.changeResolution(request.resolution)
    }
}
