package jnu.econovation.ecoknockbecentral.ai.dto.rest.response

import io.swagger.v3.oas.annotations.media.Schema
import jnu.econovation.ecoknockbecentral.ai.model.entity.AIChatHistory
import jnu.econovation.ecoknockbecentral.common.exception.server.InternalServerException
import java.time.Instant

data class AIChatHistoryPageResponse(
    @field:Schema(description = "최신순으로 정렬된 채팅 이력")
    val items: List<AIChatHistoryResponse>,
    @field:Schema(description = "다음 페이지 존재 여부")
    val hasNext: Boolean,
    @field:Schema(description = "다음 요청에 전달할 커서", nullable = true)
    val nextBefore: Instant?,
)

data class AIChatHistoryResponse(
    @field:Schema(description = "채팅 이력 ID", example = "101")
    val id: Long,
    @field:Schema(description = "사용자 질문", example = "분리배출 방법을 알려줘")
    val question: String,
    @field:Schema(description = "AI 답변", example = "내용...")
    val answer: String,
    @field:Schema(description = "생성 시각", example = "2026-07-23T12:30:00Z")
    val createdAt: Instant,
) {
    companion object {
        fun from(history: AIChatHistory): AIChatHistoryResponse {
            return AIChatHistoryResponse(
                id = history.id,
                question = history.question,
                answer = history.answer,
                createdAt = history.createdAt
                    ?: throw InternalServerException(IllegalStateException("AI 채팅 이력의 createdAt이 null입니다.")),
            )
        }
    }
}
