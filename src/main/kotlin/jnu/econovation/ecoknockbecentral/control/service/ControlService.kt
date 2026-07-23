package jnu.econovation.ecoknockbecentral.control.service

import jnu.econovation.ecoknockbecentral.airquality.command.AutoControlAirPurifierCommand
import jnu.econovation.ecoknockbecentral.airquality.dto.internal.AirQualityTimeseriesPointDTO
import jnu.econovation.ecoknockbecentral.airquality.dto.internal.GetTimeseriesDTO
import jnu.econovation.ecoknockbecentral.airquality.dto.grpc.request.Power
import jnu.econovation.ecoknockbecentral.airquality.dto.grpc.request.Power.OFF
import jnu.econovation.ecoknockbecentral.airquality.dto.grpc.request.Power.ON
import jnu.econovation.ecoknockbecentral.airquality.dto.internal.Quality.NORMAL
import jnu.econovation.ecoknockbecentral.airquality.model.vo.AirQualityResolution.ONE_MINUTE
import jnu.econovation.ecoknockbecentral.airquality.usecase.QueryAirQualityUseCase
import jnu.econovation.ecoknockbecentral.control.dto.ControlSettingDTO
import jnu.econovation.ecoknockbecentral.control.model.entity.ControlActionLog
import jnu.econovation.ecoknockbecentral.control.model.vo.ControlDecision
import jnu.econovation.ecoknockbecentral.control.model.vo.ControlDecision.TURN_OFF
import jnu.econovation.ecoknockbecentral.control.model.vo.ControlDecision.TURN_ON
import jnu.econovation.ecoknockbecentral.control.repository.ControlActionLogRepository
import jnu.econovation.ecoknockbecentral.common.metrics.ApplicationMetrics
import jnu.econovation.ecoknockbecentral.grpc.client.airpurifier.AirPurifierGrpcClient
import jnu.econovation.ecoknockbecentral.light.repository.LightRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class ControlService(
    private val airPurifierGrpcClient: AirPurifierGrpcClient,
    private val lightRepository: LightRepository,
    private val queryAirQualityUseCase: QueryAirQualityUseCase,
    private val actionLogRepository: ControlActionLogRepository,
    private val controlSettingService: ControlSettingService,
    private val metrics: ApplicationMetrics,
) {
    companion object {
        private val logger = KotlinLogging.logger {}

        private const val MIN_SAMPLE_COVERAGE = 0.8
        private val LIGHT_FRESHNESS_THRESHOLD = Duration.ofMinutes(2)
    }

    @Transactional
    fun autoControlAirPurifier(command: AutoControlAirPurifierCommand) {
        val setting: ControlSettingDTO = controlSettingService.getSetting()
        if (!setting.enabled) return

        val now = Instant.now()

        if (isCoolTime(now, setting)) return

        val isOn = command.isOn
        val airQualityTo = now.truncatedTo(ChronoUnit.MINUTES)
        val airQuality = getRecentAirQuality(setting, airQualityTo)

        when {
            isOn && isDark(now, setting) -> {
                setPowerAndLog(power = OFF, decision = TURN_OFF, now = now, reason = "어두워요")
            }

            !isOn && isBright(now, setting) && isBadAirQuality(airQuality, setting, airQualityTo) -> {
                setPowerAndLog(power = ON, decision = TURN_ON, now = now, reason = "밝고 공기가 더러워요")
            }

            isOn && isCleanAirQuality(airQuality, setting, airQualityTo) -> {
                setPowerAndLog(power = OFF, decision = TURN_OFF, now = now, reason = "공기가 깨끗해졌어요")
            }
        }
    }

    @Suppress("DuplicatedCode")
    private fun isDark(
        now: Instant,
        setting: ControlSettingDTO,
    ): Boolean {
        val duration = Duration.ofMinutes(setting.darkDetectionTimeThreshold.toLong())
        val from = now.minus(duration)
        val report = lightRepository.findReport(from = from, to = now)
        val maxLux = report.maxLux ?: return false
        val sampleCount = report.sampleCount

        when {
            !isFresh(report.measuredAt, now) -> return false
            !hasEnoughSamples(sampleCount, duration) -> return false
            maxLux > setting.darkLuxThreshold -> return false
        }

        return true
    }

    @Suppress("DuplicatedCode")
    private fun isBright(
        now: Instant,
        setting: ControlSettingDTO,
    ): Boolean {
        val duration = Duration.ofMinutes(setting.brightDetectionTimeThreshold.toLong())
        val from = now.minus(duration)
        val report = lightRepository.findReport(from = from, to = now)
        val avgLux = report.avgLux ?: return false
        val sampleCount = report.sampleCount

        when {
            !isFresh(report.measuredAt, now) -> return false
            !hasEnoughSamples(sampleCount, duration) -> return false
            avgLux < setting.brightLuxThreshold -> return false
        }

        return true
    }

    private fun isCoolTime(
        now: Instant,
        setting: ControlSettingDTO,
    ): Boolean {
        val lastActionAt = actionLogRepository
            .findFirstByOrderByActedAtDesc()
            ?.actedAt
            ?: return false

        val actionableTime = lastActionAt.plus(Duration.ofMinutes(setting.cooldownMinutes.toLong()))

        return actionableTime.isAfter(now)
    }

    private fun hasEnoughSamples(
        sampleCount: Long,
        countDuration: Duration,
    ): Boolean {
        return sampleCount >= countDuration.toSeconds() * MIN_SAMPLE_COVERAGE
    }

    private fun isFresh(
        measuredAt: Instant?,
        now: Instant,
    ): Boolean {
        return measuredAt != null && !measuredAt.plus(LIGHT_FRESHNESS_THRESHOLD).isBefore(now)
    }

    private fun getRecentAirQuality(
        setting: ControlSettingDTO,
        to: Instant,
    ): List<AirQualityTimeseriesPointDTO> {
        val from = to.minus(Duration.ofMinutes(setting.airQualityDetectionTimeThreshold.toLong()))
        val timeseries = GetTimeseriesDTO(
            resolution = ONE_MINUTE,
            from = from,
            to = to,
        )

        return queryAirQualityUseCase
            .queryAirQualityTimeseries(dto = timeseries)
            .content
    }

    private fun isBadAirQuality(
        airQualities: List<AirQualityTimeseriesPointDTO>,
        setting: ControlSettingDTO,
        expectedTo: Instant,
    ): Boolean {
        val validAirQualities = validRecentAirQualities(airQualities, setting, expectedTo)
            .takeIf { it.isNotEmpty() }
            ?: return false

        val badCount = validAirQualities.count {
            it.pm25Quality.isWorseThan(NORMAL) || it.gasQuality.isWorseThan(NORMAL)
        }
        val badRatio = badCount.toDouble() / validAirQualities.size * 100

        return badRatio >= setting.badAirQualityRatioThreshold
    }

    private fun isCleanAirQuality(
        airQualities: List<AirQualityTimeseriesPointDTO>,
        setting: ControlSettingDTO,
        expectedTo: Instant,
    ): Boolean {
        val validAirQualities = validRecentAirQualities(airQualities, setting, expectedTo)
            .takeIf { it.isNotEmpty() }
            ?: return false

        val cleanCount = validAirQualities.count {
            it.pm25Quality.isBetterThan(NORMAL) && it.gasQuality.isBetterThan(NORMAL)
        }
        val cleanRatio = cleanCount.toDouble() / validAirQualities.size * 100

        return cleanRatio >= setting.cleanAirQualityRatioThreshold
    }

    private fun validRecentAirQualities(
        airQualities: List<AirQualityTimeseriesPointDTO>,
        setting: ControlSettingDTO,
        expectedTo: Instant,
    ): List<AirQualityTimeseriesPointDTO> {
        if (airQualities.lastOrNull()?.end?.toInstant() != expectedTo) {
            return emptyList()
        }

        val minimumValidCount = setting.airQualityDetectionTimeThreshold * MIN_SAMPLE_COVERAGE
        val validAirQualities = airQualities.filter { hasEnoughSamples(it.sampleCount, Duration.ofMinutes(1)) }

        return if (validAirQualities.size >= minimumValidCount) {
            validAirQualities
        } else {
            emptyList()
        }
    }

    private fun setPowerAndLog(
        power: Power,
        decision: ControlDecision,
        now: Instant,
        reason: String,
    ) {
        runCatching {
            metrics.recordAutoControlAction(decision.name.lowercase()) {
                airPurifierGrpcClient.setAirPurifierPower(power)
            }
        }.onSuccess {
            actionLogRepository.save(
                ControlActionLog.builder()
                    .actedAt(now)
                    .decision(decision)
                    .reason(reason)
                    .build()
            )
            logger.info { "공기청정기 자동제어 실행: decision=$decision reason=$reason" }
        }.onFailure {
            logger.error(it) { "공기청정기 자동제어 명령 실패: decision=$decision reason=$reason" }
        }
    }
}
