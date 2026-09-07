package jnu.econovation.ecoknockbecentral.airquality.readmodel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@SuperBuilder
public abstract class AirQualityAggregate {
    @Id
    @Column(nullable = false)
    private Instant bucketStart;

    @Column(nullable = false)
    private Instant bucketEnd;

    @Column(nullable = false)
    private Long sumPm25;

    @Column(nullable = false)
    private Integer maxPm25;

    @Column(nullable = false)
    private Integer minPm25;

    @Column(nullable = false)
    private Double sumHumidity;

    @Column(nullable = false)
    private Double sumTemperature;

    @Column(nullable = false)
    private Double sumEco2;

    @Column(nullable = false)
    private Double sumBvoc;

    @Column(nullable = false)
    private Long sampleCount;

    public AirQualityAggregate replace(
            Long sumPm25,
            Integer maxPm25,
            Integer minPm25,
            Double sumHumidity,
            Double sumTemperature,
            Double sumEco2,
            Double sumBvoc,
            Long sampleCount
    ) {
        this.sumPm25 = sumPm25;
        this.maxPm25 = maxPm25;
        this.minPm25 = minPm25;
        this.sumHumidity = sumHumidity;
        this.sumTemperature = sumTemperature;
        this.sumEco2 = sumEco2;
        this.sumBvoc = sumBvoc;
        this.sampleCount = sampleCount;

        return this;
    }
}
