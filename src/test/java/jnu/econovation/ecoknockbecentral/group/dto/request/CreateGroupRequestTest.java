package jnu.econovation.ecoknockbecentral.group.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupType;
import jnu.econovation.ecoknockbecentral.group.model.vo.RecruitmentMode;
import org.junit.jupiter.api.Test;

class CreateGroupRequestTest {

    @Test
    void trimsTextBeforeBeanValidation() {
        var request = new CreateGroupRequest(
                GroupType.STUDY, "  그룹  ", "  소개  ", 10,
                RecruitmentMode.ALWAYS, null, null
        );

        assertThat(request.name()).isEqualTo("그룹");
        assertThat(request.introduction()).isEqualTo("소개");
        assertThat(Validation.buildDefaultValidatorFactory().getValidator().validate(request))
                .isEmpty();
    }

    @Test
    void validatesLengthsCapacityAndRequiredFields() {
        var request = new CreateGroupRequest(
                null, " ", " ", null, null, null, null
        );

        assertThat(Validation.buildDefaultValidatorFactory().getValidator().validate(request))
                .hasSize(5);
    }
}
