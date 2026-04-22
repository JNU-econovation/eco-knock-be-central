package jnu.econovation.ecoknockbecentral.airquality.readmodel.entity;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.Instant;

@Immutable
@Getter
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AirQualityView {
    @Id
    private Instant bucketStart;

    private Instant bucketEnd;

    private Double avgPm25;

    private Integer maxPm25;

    private Integer minPm25;

    private Double avgHumidity;

    private Double avgTemperature;

    private Double avgEco2;

    private Double avgBvoc;

    private Long sampleCount;
}
