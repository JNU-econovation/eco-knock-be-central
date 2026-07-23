package jnu.econovation.ecoknockbecentral.ai.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jnu.econovation.ecoknockbecentral.ai.dto.aiserver.response.AIServerResponse
import jnu.econovation.ecoknockbecentral.ai.dto.client.request.AIChatRequest
import jnu.econovation.ecoknockbecentral.ai.service.AIService
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse.success
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.BAD_DATA_SYNTAX_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.BAD_DATA_SYNTAX_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.UNAUTHORIZED_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.UNAUTHORIZED_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.INTERNAL_SERVER_ERROR_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.INTERNAL_SERVER_ERROR_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.ACCESS_TOKEN_SECURITY_SCHEME_NAME
import jnu.econovation.ecoknockbecentral.common.security.dto.EcoKnockUserDetails
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/ai/chat")
@Tag(name = "AI Chat", description = "AI 채팅 API")
@SecurityRequirement(name = ACCESS_TOKEN_SECURITY_SCHEME_NAME)
class AIController(
    private val service: AIService,
) {
    @PostMapping(
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @Operation(
        summary = "AI 채팅",
        description = "이전 대화 최대 20쌍과 현재 질문을 AI 서버로 전달하고 답변을 반환합니다. 채팅 히스토리 저장 실패는 답변 반환에 영향을 주지 않습니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "AI 답변 반환"),
            ApiResponse(
                responseCode = "400",
                description = "요청 본문 문법 오류",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = [ExampleObject(
                        name = BAD_DATA_SYNTAX_EXAMPLE_NAME,
                        ref = BAD_DATA_SYNTAX_EXAMPLE_REF,
                    )],
                )],
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 필요",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = [ExampleObject(
                        name = UNAUTHORIZED_EXAMPLE_NAME,
                        ref = UNAUTHORIZED_EXAMPLE_REF,
                    )],
                )],
            ),
            ApiResponse(
                responseCode = "500",
                description = "AI 서버 통신 또는 내부 처리 오류",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = [ExampleObject(
                        name = INTERNAL_SERVER_ERROR_EXAMPLE_NAME,
                        ref = INTERNAL_SERVER_ERROR_EXAMPLE_REF,
                    )],
                )],
            ),
        ],
    )
    fun chat(
        @AuthenticationPrincipal userDetails: EcoKnockUserDetails,
        @RequestBody request: AIChatRequest,
    ): ResponseEntity<CommonResponse<AIServerResponse>> {
        val response = service.chat(userDetails.memberInfo, request)
        return ok(success(response))
    }
}
