package jnu.econovation.ecoknockbecentral.airquality.event

import jnu.econovation.ecoknockbecentral.airquality.service.AirQualityHistorySettingService
import jnu.econovation.ecoknockbecentral.member.event.MemberCreatedEvent
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class MemberCreatedEventAirQualityHistorySettingListener(
    private val service: AirQualityHistorySettingService,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun initializeSetting(event: MemberCreatedEvent) {
        try {
            service.initialize(event.memberId)
        } catch (e: Exception) {
            logger.error(e) { "신규 회원 공기질 과거 시계열 설정 초기화 실패: memberId=${event.memberId}" }
        }
    }
}
