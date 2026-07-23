package jnu.econovation.ecoknockbecentral.airquality.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jnu.econovation.ecoknockbecentral.airquality.dto.rest.request.UpdateAirQualityHistorySettingRequest
import jnu.econovation.ecoknockbecentral.airquality.dto.rest.response.GetAirQualityHistorySettingResponse
import jnu.econovation.ecoknockbecentral.airquality.service.AirQualityHistorySettingService
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse.emptySuccess
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse.success
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.ACCESS_TOKEN_SECURITY_SCHEME_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.BAD_DATA_SYNTAX_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.BAD_DATA_SYNTAX_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.EMPTY_SUCCESS_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.EMPTY_SUCCESS_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.UNAUTHORIZED_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.UNAUTHORIZED_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.security.dto.EcoKnockUserDetails
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/air-quality/timeseries/history/default")
@Tag(name = "Air Quality", description = "공기질 시계열 API")
@SecurityRequirement(name = ACCESS_TOKEN_SECURITY_SCHEME_NAME)
class AirQualityHistorySettingController(
    private val service: AirQualityHistorySettingService,
) {
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "공기질 과거 시계열 기본값 조회",
        description = "현재 로그인한 회원 또는 게스트의 과거 시계열 해상도 기본값을 조회합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(
                responseCode = "401",
                description = "인증 필요",
                content = [Content(
                    mediaType = "application/json",
                    examples = [ExampleObject(
                        name = UNAUTHORIZED_EXAMPLE_NAME,
                        ref = UNAUTHORIZED_EXAMPLE_REF
                    )]
                )]
            )
        ]
    )
    fun get(@AuthenticationPrincipal userDetails: EcoKnockUserDetails): ResponseEntity<CommonResponse<GetAirQualityHistorySettingResponse>> =
        ResponseEntity.ok(success(service.get(userDetails.memberInfo)))

    @PutMapping(
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Operation(
        summary = "공기질 과거 시계열 기본값 저장",
        description = "현재 로그인한 회원 또는 게스트의 과거 시계열 해상도 기본값을 저장합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "저장 성공",
                content = [Content(
                    mediaType = "application/json",
                    examples = [ExampleObject(
                        name = EMPTY_SUCCESS_EXAMPLE_NAME,
                        ref = EMPTY_SUCCESS_EXAMPLE_REF
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "요청 본문 문법 오류",
                content = [Content(
                    mediaType = "application/json",
                    examples = [ExampleObject(
                        name = BAD_DATA_SYNTAX_EXAMPLE_NAME,
                        ref = BAD_DATA_SYNTAX_EXAMPLE_REF
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 필요",
                content = [Content(
                    mediaType = "application/json",
                    examples = [ExampleObject(
                        name = UNAUTHORIZED_EXAMPLE_NAME,
                        ref = UNAUTHORIZED_EXAMPLE_REF
                    )]
                )]
            )
        ]
    )
    fun update(
        @AuthenticationPrincipal userDetails: EcoKnockUserDetails,
        @RequestBody request: UpdateAirQualityHistorySettingRequest
    ): ResponseEntity<CommonResponse<Void>> {
        service.update(userDetails.memberInfo, request)
        return ResponseEntity.ok(emptySuccess())
    }
}
