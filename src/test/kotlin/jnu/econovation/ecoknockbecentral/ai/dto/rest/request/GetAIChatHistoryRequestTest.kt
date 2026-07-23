package jnu.econovation.ecoknockbecentral.ai.dto.rest.request

import jnu.econovation.ecoknockbecentral.ai.exception.BadAIChatHistoryLimitException
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class GetAIChatHistoryRequestTest {
    @Test
    fun `limit은 1 미만이면 거부한다`() {
        assertThatThrownBy { GetAIChatHistoryRequest(limit = 0) }
            .isInstanceOf(BadAIChatHistoryLimitException::class.java)
    }

    @Test
    fun `limit은 50 초과이면 거부한다`() {
        assertThatThrownBy { GetAIChatHistoryRequest(limit = 51) }
            .isInstanceOf(BadAIChatHistoryLimitException::class.java)
    }
}
