package jnu.econovation.ecoknockbecentral.group.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class CreateGroupApplicationRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsContentFromOneToTwentyCharacters() {
        assertThat(validator.validate(new CreateGroupApplicationRequest("가"))).isEmpty();
        assertThat(validator.validate(new CreateGroupApplicationRequest("가".repeat(20)))).isEmpty();
        CreateGroupApplicationRequest trimmed = new CreateGroupApplicationRequest("  지원  ");
        assertThat(validator.validate(trimmed)).isEmpty();
        assertThat(trimmed.content()).isEqualTo("지원");
    }

    @Test
    void rejectsBlankOrOverTwentyCharacters() {
        assertThat(validator.validate(new CreateGroupApplicationRequest("   "))).isNotEmpty();
        assertThat(validator.validate(new CreateGroupApplicationRequest("가".repeat(21)))).isNotEmpty();
    }
}
