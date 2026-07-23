package jnu.econovation.ecoknockbecentral.ai.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jnu.econovation.ecoknockbecentral.ai.dto.aiserver.response.AIServerResponse
import jnu.econovation.ecoknockbecentral.ai.dto.client.request.AIChatRequest
import jnu.econovation.ecoknockbecentral.ai.dto.rest.request.GetAIChatHistoryRequest
import jnu.econovation.ecoknockbecentral.ai.dto.rest.response.AIChatHistoryPageResponse
import jnu.econovation.ecoknockbecentral.ai.service.AIService
import jnu.econovation.ecoknockbecentral.ai.service.internal.AIChatHistoryService
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse.success
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.BAD_DATA_SYNTAX_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.BAD_DATA_SYNTAX_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.UNAUTHORIZED_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.UNAUTHORIZED_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.INTERNAL_SERVER_ERROR_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.INTERNAL_SERVER_ERROR_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.AI_CHAT_HISTORY_LIMIT_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.AI_CHAT_HISTORY_LIMIT_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.ACCESS_TOKEN_SECURITY_SCHEME_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.FORBIDDEN_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.FORBIDDEN_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.security.dto.EcoKnockUserDetails
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/ai/chat")
@Tag(name = "AI Chat", description = "AI 채팅 API")
@SecurityRequirement(name = ACCESS_TOKEN_SECURITY_SCHEME_NAME)
class AIController(
    private val service: AIService,
    private val historyService: AIChatHistoryService,
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

    @GetMapping(
        "/history",
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @Operation(
        summary = "AI 채팅 과거 기록 조회",
        description = "현재 회원의 AI 채팅 기록을 최신순으로 조회합니다. 다음 페이지가 있으면 nextBefore를 before 파라미터로 전달하세요.",
        parameters = [
            Parameter(
                name = "limit",
                `in` = ParameterIn.QUERY,
                required = false,
                description = "페이지 크기. 1 이상 50 이하",
                schema = Schema(type = "integer", format = "int32", minimum = "1", maximum = "50"),
                example = "20",
            ),
            Parameter(
                name = "before",
                `in` = ParameterIn.QUERY,
                required = false,
                description = "이 시각 이전의 기록부터 조회하는 커서",
                schema = Schema(type = "string", format = "date-time"),
                example = "2026-07-23T12:30:00Z",
            ),
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "채팅 기록 조회 성공"),
            ApiResponse(
                responseCode = "400",
                description = "limit 또는 before 파라미터 오류",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = [
                        ExampleObject(
                            name = AI_CHAT_HISTORY_LIMIT_EXAMPLE_NAME,
                            ref = AI_CHAT_HISTORY_LIMIT_EXAMPLE_REF,
                        ),
                        ExampleObject(
                            name = BAD_DATA_SYNTAX_EXAMPLE_NAME,
                            ref = BAD_DATA_SYNTAX_EXAMPLE_REF,
                        ),
                    ],
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
                responseCode = "403",
                description = "게스트 또는 권한이 없는 회원의 접근",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = [ExampleObject(
                        name = FORBIDDEN_EXAMPLE_NAME,
                        ref = FORBIDDEN_EXAMPLE_REF,
                    )],
                )],
            ),
            ApiResponse(
                responseCode = "500",
                description = "채팅 이력 내부 처리 오류",
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
    fun history(
        @Parameter(hidden = true)
        request: GetAIChatHistoryRequest,
        @AuthenticationPrincipal userDetails: EcoKnockUserDetails,
    ): ResponseEntity<CommonResponse<AIChatHistoryPageResponse>> {
        val response = historyService.getChatHistoryPage(
            memberInfo = userDetails.memberInfo,
            limit = request.limit,
            before = request.before,
        )
        return ok(success(response))
    }
}
