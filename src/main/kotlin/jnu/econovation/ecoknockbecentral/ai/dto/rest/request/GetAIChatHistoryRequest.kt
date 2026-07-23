package jnu.econovation.ecoknockbecentral.ai.dto.rest.request

import io.swagger.v3.oas.annotations.media.Schema
import jnu.econovation.ecoknockbecentral.ai.exception.BadAIChatHistoryLimitException
import java.time.Instant

data class GetAIChatHistoryRequest(
    @field:Schema(description = "페이지 크기. 1 이상 50 이하", minimum = "1", maximum = "50", example = "20")
    val limit: Int = 20,
    @field:Schema(description = "이 시각 이전의 기록부터 조회하는 커서", example = "2026-07-23T12:30:00Z")
    val before: Instant? = null,
) {
    init {
        if (limit !in MIN_LIMIT..MAX_LIMIT) {
            throw BadAIChatHistoryLimitException()
        }
    }

    companion object {
        const val MIN_LIMIT = 1
        const val MAX_LIMIT = 50
    }
}
