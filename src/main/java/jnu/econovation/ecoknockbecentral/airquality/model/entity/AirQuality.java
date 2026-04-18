package jnu.econovation.ecoknockbecentral.airquality.model.entity;

import jakarta.persistence.*;
import jnu.econovation.ecoknockbecentral.airquality.dto.RawAirPurifierDTO;
import jnu.econovation.ecoknockbecentral.airquality.dto.RawSensorDTO;
import jnu.econovation.ecoknockbecentral.common.model.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AirQuality extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant sensorMeasuredAt;

    @Column(nullable = false)
    private Instant airPurifierMeasuredAt;

    @Column(nullable = false)
    private Integer pm25;

    @Column(nullable = false)
    private Double humidity;

    @Column(nullable = false)
    private Double temperature;

    @Column(nullable = false)
    private Double estimatedEco2PPM;

    @Column(nullable = false)
    private Double estimatedBvocPPM;

    @Column(nullable = false)
    private Integer accuracy;

    // 아래는 기록용 필드들 (로깅 및 디버깅 할때 용이)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = true)
    private RawSensorDTO rawSensor;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = true)
    private RawAirPurifierDTO rawAirPurifier;

    @Builder
    AirQuality(
            Instant sensorMeasuredAt,
            Instant airPurifierMeasuredAt,
            Integer pm25,
            Double humidity,
            Double temperature,
            Double estimatedEco2PPM,
            Double estimatedBvocPPM,
            Integer accuracy,
            RawSensorDTO rawSensor,
            RawAirPurifierDTO rawAirPurifier
    ) {
        this.sensorMeasuredAt = sensorMeasuredAt;
        this.airPurifierMeasuredAt = airPurifierMeasuredAt;
        this.pm25 = pm25;
        this.humidity = humidity;
        this.temperature = temperature;
        this.estimatedEco2PPM = estimatedEco2PPM;
        this.estimatedBvocPPM = estimatedBvocPPM;
        this.accuracy = accuracy;
        this.rawSensor = rawSensor;
        this.rawAirPurifier = rawAirPurifier;
    }
}
