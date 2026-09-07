package jnu.econovation.ecoknockbecentral.airquality.repository

import jnu.econovation.ecoknockbecentral.airquality.readmodel.entity.*
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.NoRepositoryBean
import java.time.Instant

@NoRepositoryBean
interface AirQualityAggregateRepository<T : AirQualityAggregate> :
    JpaRepository<T, Instant> {

    @Query(
        """
        select a
        from #{#entityName} a
        where a.bucketStart >= :from
          and a.bucketStart < :to
        order by a.bucketStart
        """
    )
    fun findBuckets(from: Instant, to: Instant): List<T>

//    @Query(
//        """
//            select a
//            from #{#entityName} a
//            where a.bucketEnd >= :from
//            and a.bucketEnd < :to
//            order by a.bucketStart
//        """
//    )
//    fun findForAggregation(from: Instant, to: Instant): List<T>

    @Query(
        """
        select a
        from #{#entityName} a
        where a.bucketStart < :before
        order by a.bucketStart desc
        """
    )
    fun findPreviousBuckets(before: Instant, pageable: Pageable): List<T>
}

interface AirQuality1mAggregateRepository : AirQualityAggregateRepository<AirQuality1mAggregate>

interface AirQuality5mAggregateRepository : AirQualityAggregateRepository<AirQuality5mAggregate>

interface AirQuality15mAggregateRepository : AirQualityAggregateRepository<AirQuality15mAggregate>

interface AirQuality1hAggregateRepository : AirQualityAggregateRepository<AirQuality1hAggregate>

interface AirQuality4hAggregateRepository : AirQualityAggregateRepository<AirQuality4hAggregate>

interface AirQuality1dAggregateRepository : AirQualityAggregateRepository<AirQuality1dAggregate>
