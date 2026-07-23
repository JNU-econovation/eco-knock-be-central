package jnu.econovation.ecoknockbecentral.ai.controller

import jnu.econovation.ecoknockbecentral.ai.dto.client.request.AIChatRequest
import jnu.econovation.ecoknockbecentral.common.security.dto.EcoKnockUserDetails
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody

class AIControllerTest {
    @Test
    fun `AI 채팅은 JSON 요청 본문을 받는다`() {
        val chat = AIController::class.java.getDeclaredMethod(
            "chat",
            EcoKnockUserDetails::class.java,
            AIChatRequest::class.java,
        )
        val postMapping = chat.getAnnotation(PostMapping::class.java)

        assertThat(postMapping.consumes).containsExactly(MediaType.APPLICATION_JSON_VALUE)
        assertThat(postMapping.produces).containsExactly(MediaType.APPLICATION_JSON_VALUE)
        assertThat(chat.parameters[1].isAnnotationPresent(RequestBody::class.java)).isTrue()
    }

    @Test
    fun `AI 채팅 과거 기록 조회는 history GET 엔드포인트다`() {
        val history = AIController::class.java.getDeclaredMethod(
            "history",
            Class.forName("jnu.econovation.ecoknockbecentral.ai.dto.rest.request.GetAIChatHistoryRequest"),
            EcoKnockUserDetails::class.java,
        )
        val getMapping = history.getAnnotation(GetMapping::class.java)

        assertThat(getMapping.value).containsExactly("/history")
        assertThat(getMapping.produces).containsExactly(MediaType.APPLICATION_JSON_VALUE)
    }
}
