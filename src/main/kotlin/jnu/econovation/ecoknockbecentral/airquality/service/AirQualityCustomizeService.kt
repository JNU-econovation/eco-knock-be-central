package jnu.econovation.ecoknockbecentral.airquality.service

import jnu.econovation.ecoknockbecentral.airquality.dto.rest.request.UpdateAirQualityHistorySettingRequest
import jnu.econovation.ecoknockbecentral.airquality.dto.rest.response.GetAirQualityHistorySettingResponse
import jnu.econovation.ecoknockbecentral.airquality.model.entity.AirQualityHistorySetting
import jnu.econovation.ecoknockbecentral.airquality.model.vo.AirQualityResolution
import jnu.econovation.ecoknockbecentral.airquality.repository.AirQualityCustomizeRepository
import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO
import jnu.econovation.ecoknockbecentral.member.service.MemberService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class AirQualityCustomizeService(
    private val memberService: MemberService,
    private val repository: AirQualityCustomizeRepository,
) {
    companion object {
        private val DEFAULT_RESOLUTION = AirQualityResolution.FIFTEEN_MINUTES
    }

    //실패해도 상관 없음
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun initialize(memberId: Long): AirQualityHistorySetting {
        repository.findByMemberId(memberId)?.let { return it }

        val member = memberService.getEntityOrThrow(memberId)

        val newSetting = AirQualityHistorySetting.builder()
            .member(member)
            .resolution(DEFAULT_RESOLUTION)
            .build()

        return repository.save(newSetting)
    }

    //lazy init 가능
    @Transactional
    fun getOrInit(memberInfo: MemberInfoDTO): GetAirQualityHistorySettingResponse {
        val setting = repository.findByMemberId(memberInfo.id)
            ?: initialize(memberInfo.id)

        return GetAirQualityHistorySettingResponse.from(setting)
    }

    //lazy init 가능
    @Transactional
    fun update(memberInfo: MemberInfoDTO, request: UpdateAirQualityHistorySettingRequest) {
        val setting = repository.findByMemberId(memberInfo.id)
            ?: initialize(memberInfo.id)

        setting.changeResolution(request.resolution)
    }
}
