package jnu.econovation.ecoknockbecentral.ai.dto.client.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "AI 채팅 요청")
data class AIChatRequest(
    @field:Schema(description = "AI에게 전달할 질문", example = "분리배출 방법을 알려줘")
    val question: String,
)
