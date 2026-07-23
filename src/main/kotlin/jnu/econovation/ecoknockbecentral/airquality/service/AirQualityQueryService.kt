package jnu.econovation.ecoknockbecentral.airquality.service

import jnu.econovation.ecoknockbecentral.airquality.dto.internal.AirQualityViewDTO
import jnu.econovation.ecoknockbecentral.airquality.dto.internal.GetTimeseriesDTO
import jnu.econovation.ecoknockbecentral.airquality.dto.internal.GetTimeseriesHistoryDTO
import jnu.econovation.ecoknockbecentral.airquality.model.vo.AirQualityResolution
import jnu.econovation.ecoknockbecentral.airquality.dto.internal.AirQualityTimeseriesPointDTO
import jnu.econovation.ecoknockbecentral.airquality.dto.rest.response.GetAirQualityResponse
import jnu.econovation.ecoknockbecentral.airquality.exception.BadFromToException
import jnu.econovation.ecoknockbecentral.airquality.readmodel.entity.AirQualityView
import jnu.econovation.ecoknockbecentral.airquality.repository.*
import jnu.econovation.ecoknockbecentral.airquality.usecase.QueryAirQualityUseCase
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant

@Service
@Transactional(readOnly = true)
class AirQualityQueryService(
    private val oneMinuteRepository: AirQuality1mViewRepository,
    private val fiveMinuteRepository: AirQuality5mViewRepository,
    private val fifteenMinuteRepository: AirQuality15mViewRepository,
    private val oneHourRepository: AirQuality1hViewRepository,
    private val fourHourRepository: AirQuality4hViewRepository,
    private val oneDayRepository: AirQuality1dViewRepository,
) : QueryAirQualityUseCase {
    override fun queryAirQuality(): GetAirQualityResponse {
        val now = Instant.now()
        val airQualities = buildList {
            addAll(oneMinuteRepository.findBuckets(now.minus(Duration.ofHours(1)), now).map(AirQualityViewDTO::from))
            addAll(fiveMinuteRepository.findBuckets(now.minus(Duration.ofHours(6)), now).map(AirQualityViewDTO::from))
            addAll(fifteenMinuteRepository.findBuckets(now.minus(Duration.ofDays(1)), now).map(AirQualityViewDTO::from))
            addAll(oneHourRepository.findBuckets(now.minus(Duration.ofDays(7)), now).map(AirQualityViewDTO::from))
            addAll(fourHourRepository.findBuckets(now.minus(Duration.ofDays(30)), now).map(AirQualityViewDTO::from))
            addAll(oneDayRepository.findBuckets(now.minus(Duration.ofDays(365)), now).map(AirQualityViewDTO::from))
        }

        return GetAirQualityResponse(airQualities = airQualities)
    }

    override fun queryAirQualityTimeseries(dto: GetTimeseriesDTO): Slice<AirQualityTimeseriesPointDTO> {
        val from = dto.from
        val to = dto.to
        val resolution = dto.resolution

        if (!from.isBefore(to)) {
            throw BadFromToException()
        }

        return SliceImpl(
            findBuckets(resolution, from, to)
                .map(AirQualityTimeseriesPointDTO::from)
        )
    }

    override fun queryAirQualityTimeseriesHistory(dto: GetTimeseriesHistoryDTO): Slice<AirQualityTimeseriesPointDTO> {
        val limit = dto.limit
        val before = dto.before
        val resolution = dto.resolution

        val pageable = PageRequest.of(0, limit + 1)
        val buckets = findPreviousBuckets(resolution, before, pageable)
        val hasNext = buckets.size > limit

        val points = buckets
            .take(limit)
            .asReversed()
            .map(AirQualityTimeseriesPointDTO::from)

        return SliceImpl(points, PageRequest.of(0, limit), hasNext)
    }

    private fun findBuckets(
        resolution: AirQualityResolution,
        from: Instant,
        to: Instant,
    ): List<AirQualityView> {
        return when (resolution) {
            AirQualityResolution.ONE_MINUTE -> oneMinuteRepository.findBuckets(from, to)
            AirQualityResolution.FIVE_MINUTES -> fiveMinuteRepository.findBuckets(from, to)
            AirQualityResolution.FIFTEEN_MINUTES -> fifteenMinuteRepository.findBuckets(from, to)
            AirQualityResolution.ONE_HOUR -> oneHourRepository.findBuckets(from, to)
            AirQualityResolution.FOUR_HOURS -> fourHourRepository.findBuckets(from, to)
            AirQualityResolution.ONE_DAY -> oneDayRepository.findBuckets(from, to)
        }
    }

    private fun findPreviousBuckets(
        resolution: AirQualityResolution,
        before: Instant,
        pageable: PageRequest,
    ): List<AirQualityView> {
        return when (resolution) {
            AirQualityResolution.ONE_MINUTE -> oneMinuteRepository.findPreviousBuckets(before, pageable)
            AirQualityResolution.FIVE_MINUTES -> fiveMinuteRepository.findPreviousBuckets(before, pageable)
            AirQualityResolution.FIFTEEN_MINUTES -> fifteenMinuteRepository.findPreviousBuckets(before, pageable)
            AirQualityResolution.ONE_HOUR -> oneHourRepository.findPreviousBuckets(before, pageable)
            AirQualityResolution.FOUR_HOURS -> fourHourRepository.findPreviousBuckets(before, pageable)
            AirQualityResolution.ONE_DAY -> oneDayRepository.findPreviousBuckets(before, pageable)
        }
    }
}
