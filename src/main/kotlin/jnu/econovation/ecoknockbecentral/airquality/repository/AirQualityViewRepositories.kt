package jnu.econovation.ecoknockbecentral.airquality.repository

import jnu.econovation.ecoknockbecentral.airquality.readmodel.entity.*
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.query.Param
import java.time.Instant

@NoRepositoryBean
interface AirQualityViewRepository<T : AirQualityView> : JpaRepository<T, Instant> {
    @Query(
        """
        select v
        from #{#entityName} v
        where v.bucketStart >= :from
          and v.bucketStart < :to
        order by v.bucketStart
        """
    )
    fun findBuckets(
        @Param("from") from: Instant,
        @Param("to") to: Instant,
    ): List<T>

    @Query(
        """
        select v
        from #{#entityName} v
        where v.bucketStart < :before
        order by v.bucketStart desc
        """
    )
    fun findPreviousBuckets(
        @Param("before") before: Instant,
        pageable: Pageable,
    ): List<T>
}

interface AirQuality1mViewRepository : AirQualityViewRepository<AirQuality1mView>
interface AirQuality5mViewRepository : AirQualityViewRepository<AirQuality5mView>
interface AirQuality15mViewRepository : AirQualityViewRepository<AirQuality15mView>
interface AirQuality1hViewRepository : AirQualityViewRepository<AirQuality1hView>
interface AirQuality4hViewRepository : AirQualityViewRepository<AirQuality4hView>
interface AirQuality1dViewRepository : AirQualityViewRepository<AirQuality1dView>
