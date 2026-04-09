package jnu.econovation.ecoknockbecentral.member.model.vo;

import jakarta.persistence.Embeddable;
import jnu.econovation.ecoknockbecentral.common.exception.client.BadDataMeaningException;

@Embeddable
public record Cohort(int value) {

    public Cohort {
        validate(value);
    }

    private void validate(int value) {
        if (value < 1) {
            throw new BadDataMeaningException("기수는 1보다 작을 수 없습니다.");
        }
    }
}