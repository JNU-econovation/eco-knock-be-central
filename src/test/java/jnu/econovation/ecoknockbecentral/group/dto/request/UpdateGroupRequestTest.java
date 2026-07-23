package jnu.econovation.ecoknockbecentral.group.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupType;
import org.junit.jupiter.api.Test;

class UpdateGroupRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void trimsAndValidatesName() {
        UpdateGroupNameRequest valid = new UpdateGroupNameRequest(" 새 이름 ");
        UpdateGroupNameRequest blank = new UpdateGroupNameRequest("   ");
        UpdateGroupNameRequest tooLong = new UpdateGroupNameRequest("가".repeat(16));

        assertThat(valid.name()).isEqualTo("새 이름");
        assertThat(validator.validate(valid)).isEmpty();
        assertThat(validator.validate(blank)).isNotEmpty();
        assertThat(validator.validate(tooLong)).isNotEmpty();
    }

    @Test
    void trimsAndValidatesDetail() {
        UpdateGroupDetailRequest valid =
                new UpdateGroupDetailRequest(GroupType.STUDY, " 소개 ", 50);
        UpdateGroupDetailRequest tooSmall =
                new UpdateGroupDetailRequest(GroupType.STUDY, "소개", 0);

        assertThat(valid.introduction()).isEqualTo("소개");
        assertThat(validator.validate(valid)).isEmpty();
        assertThat(validator.validate(tooSmall)).isNotEmpty();
    }

    @Test
    void validatesLeaderMemberId() {
        assertThat(validator.validate(new ChangeGroupLeaderRequest(1L))).isEmpty();
        assertThat(validator.validate(new ChangeGroupLeaderRequest(0L))).isNotEmpty();
    }
}
