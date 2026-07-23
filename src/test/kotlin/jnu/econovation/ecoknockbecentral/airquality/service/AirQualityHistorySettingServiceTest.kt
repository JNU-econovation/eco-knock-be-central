package jnu.econovation.ecoknockbecentral.airquality.service

import jnu.econovation.ecoknockbecentral.airquality.dto.rest.request.UpdateAirQualityHistorySettingRequest
import jnu.econovation.ecoknockbecentral.airquality.model.entity.AirQualityHistorySetting
import jnu.econovation.ecoknockbecentral.airquality.model.vo.AirQualityResolution
import jnu.econovation.ecoknockbecentral.airquality.repository.AirQualityHistorySettingRepository
import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO
import jnu.econovation.ecoknockbecentral.member.model.entity.Member
import jnu.econovation.ecoknockbecentral.member.model.vo.Role
import jnu.econovation.ecoknockbecentral.member.service.MemberService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AirQualityHistorySettingServiceTest {
    private val memberService = mock<MemberService>()
    private val repository = mock<AirQualityHistorySettingRepository>()
    private val service = AirQualityHistorySettingService(memberService, repository)
    private val memberInfo = MemberInfoDTO(id = 1L, ssoMemberId = null, role = Role.USER, cohort = null, name = "테스트", status = null)

    @Test
    fun initializesFifteenMinuteDefaultWhenSettingDoesNotExist() {
        val member = mock<Member>()
        whenever(repository.existsByMemberId(memberInfo.id)).thenReturn(false)
        whenever(memberService.getEntityOrThrow(memberInfo.id)).thenReturn(member)

        service.initialize(memberInfo.id)

        val settingCaptor = argumentCaptor<AirQualityHistorySetting>()
        verify(repository).save(settingCaptor.capture())
        assertThat(settingCaptor.firstValue.resolution).isEqualTo(AirQualityResolution.FIFTEEN_MINUTES)
    }

    @Test
    fun updatesSavedResolution() {
        val setting = AirQualityHistorySetting.builder()
            .member(mock())
            .resolution(AirQualityResolution.FIFTEEN_MINUTES)
            .build()
        whenever(repository.findByMemberId(memberInfo.id)).thenReturn(setting)

        service.update(memberInfo, UpdateAirQualityHistorySettingRequest(AirQualityResolution.ONE_HOUR))

        assertThat(setting.resolution).isEqualTo(AirQualityResolution.ONE_HOUR)
    }

    @Test
    fun returnsRestResolutionCodeForStoredSetting() {
        val setting = AirQualityHistorySetting.builder()
            .member(mock())
            .resolution(AirQualityResolution.FIFTEEN_MINUTES)
            .build()
        whenever(repository.findByMemberId(memberInfo.id)).thenReturn(setting)

        val response = service.get(memberInfo)

        assertThat(response.resolution).isEqualTo(AirQualityResolution.FIFTEEN_MINUTES)
    }
}
