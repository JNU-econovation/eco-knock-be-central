package jnu.econovation.ecoknockbecentral.airquality.model.entity;

import jakarta.persistence.*;
import jnu.econovation.ecoknockbecentral.airquality.model.vo.AirQualityResolution;
import jnu.econovation.ecoknockbecentral.common.model.entity.BaseEntity;
import jnu.econovation.ecoknockbecentral.member.model.entity.Member;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AirQualityHistorySetting extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AirQualityResolution resolution;

    @Builder
    AirQualityHistorySetting(Member member, AirQualityResolution resolution) {
        this.member = member;
        this.resolution = resolution;
    }

    public void changeResolution(AirQualityResolution resolution) {
        this.resolution = resolution;
    }
}
