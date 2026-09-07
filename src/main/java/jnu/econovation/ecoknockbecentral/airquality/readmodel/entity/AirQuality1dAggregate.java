package jnu.econovation.ecoknockbecentral.airquality.readmodel.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "air_quality_1d_aggregate")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@SuperBuilder
public class AirQuality1dAggregate extends AirQualityAggregate {
}
