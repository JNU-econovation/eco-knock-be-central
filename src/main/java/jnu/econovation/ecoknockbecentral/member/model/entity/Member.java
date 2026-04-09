package jnu.econovation.ecoknockbecentral.member.model.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jnu.econovation.ecoknockbecentral.common.converter.StringEncryptConverter;
import jnu.econovation.ecoknockbecentral.common.model.entity.BaseEntity;
import jnu.econovation.ecoknockbecentral.member.model.vo.ActiveStatus;
import jnu.econovation.ecoknockbecentral.member.model.vo.Cohort;
import jnu.econovation.ecoknockbecentral.member.model.vo.Oauth2Provider;
import jnu.econovation.ecoknockbecentral.member.model.vo.Role;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "cohort", nullable = false))
    private Cohort cohort;

    @Column(nullable = false)
    @Convert(converter = StringEncryptConverter.class)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ActiveStatus status;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Oauth2Provider provider;

    @Builder
    Member(
            Cohort cohort,
            String name,
            ActiveStatus status,
            Oauth2Provider provider
    ) {
        this.role = Role.USER;
        this.cohort = cohort;
        this.name = name;
        this.status = status;
        this.provider = provider;
    }

    public void promoteToAdmin() {
        this.role = Role.ADMIN;
    }
}