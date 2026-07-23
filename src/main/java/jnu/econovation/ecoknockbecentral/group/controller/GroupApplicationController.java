package jnu.econovation.ecoknockbecentral.group.controller;

import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.ACCESS_TOKEN_SECURITY_SCHEME_NAME;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.EMPTY_SUCCESS_EXAMPLE_NAME;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.EMPTY_SUCCESS_EXAMPLE_REF;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_ACCESS_DENIED_EXAMPLE_NAME;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_ACCESS_DENIED_EXAMPLE_REF;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_APPLICATION_ALREADY_PENDING_EXAMPLE_NAME;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_APPLICATION_ALREADY_PENDING_EXAMPLE_REF;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_APPLICATION_ALREADY_PROCESSED_EXAMPLE_NAME;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_APPLICATION_ALREADY_PROCESSED_EXAMPLE_REF;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_APPLICATION_CONTENT_INVALID_EXAMPLE_NAME;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_APPLICATION_CONTENT_INVALID_EXAMPLE_REF;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_APPLICATION_NOT_FOUND_EXAMPLE_NAME;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_APPLICATION_NOT_FOUND_EXAMPLE_REF;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_APPLICANT_ALREADY_MEMBER_EXAMPLE_NAME;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_APPLICANT_ALREADY_MEMBER_EXAMPLE_REF;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_CAPACITY_REACHED_EXAMPLE_NAME;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_CAPACITY_REACHED_EXAMPLE_REF;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_LEADER_PERMISSION_REQUIRED_EXAMPLE_NAME;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_LEADER_PERMISSION_REQUIRED_EXAMPLE_REF;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_MEMBER_ALREADY_EXISTS_EXAMPLE_NAME;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_MEMBER_ALREADY_EXISTS_EXAMPLE_REF;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_NOT_FOUND_EXAMPLE_NAME;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_NOT_FOUND_EXAMPLE_REF;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_RECRUITMENT_CLOSED_EXAMPLE_NAME;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_RECRUITMENT_CLOSED_EXAMPLE_REF;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.INVALID_INPUT_VALUE_EXAMPLE_NAME;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.INVALID_INPUT_VALUE_EXAMPLE_REF;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.UNAUTHORIZED_EXAMPLE_NAME;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.UNAUTHORIZED_EXAMPLE_REF;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse;
import jnu.econovation.ecoknockbecentral.common.security.dto.EcoKnockUserDetails;
import jnu.econovation.ecoknockbecentral.group.dto.request.CreateGroupApplicationRequest;
import jnu.econovation.ecoknockbecentral.group.dto.response.GroupApplicationResponse;
import jnu.econovation.ecoknockbecentral.group.service.GroupApplicationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/groups/{groupId}/applications")
@Tag(name = "Group Application", description = "그룹 지원서 API")
@SecurityRequirement(name = ACCESS_TOKEN_SECURITY_SCHEME_NAME)
public class GroupApplicationController {

    private final GroupApplicationService groupApplicationService;

    public GroupApplicationController(GroupApplicationService groupApplicationService) {
        this.groupApplicationService = groupApplicationService;
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "그룹 지원",
            description = "모집 중이며 정원이 남은 그룹에 PENDING 지원서를 생성합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "지원 성공",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = EMPTY_SUCCESS_EXAMPLE_NAME,
                                            ref = EMPTY_SUCCESS_EXAMPLE_REF
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "지원 내용 형식 오류 (COMMON_400_001)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = INVALID_INPUT_VALUE_EXAMPLE_NAME, ref = INVALID_INPUT_VALUE_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = UNAUTHORIZED_EXAMPLE_NAME, ref = UNAUTHORIZED_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "403", description = "GUEST는 지원할 수 없음 (SECURITY_403_001)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class))),
                    @ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음 (GROUP_404_001)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = GROUP_NOT_FOUND_EXAMPLE_NAME, ref = GROUP_NOT_FOUND_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "409", description = "PENDING 지원서 중복 (GROUP_409_003)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = GROUP_APPLICATION_ALREADY_PENDING_EXAMPLE_NAME, ref = GROUP_APPLICATION_ALREADY_PENDING_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "422", description = "이미 그룹원이거나 모집할 수 없음 (GROUP_422_002, GROUP_422_003, GROUP_422_005)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = {
                            @ExampleObject(name = GROUP_RECRUITMENT_CLOSED_EXAMPLE_NAME, ref = GROUP_RECRUITMENT_CLOSED_EXAMPLE_REF),
                            @ExampleObject(name = GROUP_APPLICATION_CONTENT_INVALID_EXAMPLE_NAME, ref = GROUP_APPLICATION_CONTENT_INVALID_EXAMPLE_REF),
                            @ExampleObject(name = GROUP_APPLICANT_ALREADY_MEMBER_EXAMPLE_NAME, ref = GROUP_APPLICANT_ALREADY_MEMBER_EXAMPLE_REF)
                    }))
            }
    )
    public ResponseEntity<CommonResponse<Void>> create(
            @PathVariable Long groupId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal EcoKnockUserDetails userDetails,
            @Valid @RequestBody CreateGroupApplicationRequest request
    ) {
        groupApplicationService.create(
                groupId,
                userDetails.memberInfo().getId(),
                request.content()
        );
        return ResponseEntity.ok(CommonResponse.emptySuccess());
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "PENDING 지원서 목록 조회",
            description = "그룹원 또는 ADMIN이 현재 처리 대기 중인 지원서를 신청 순서로 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "지원서 목록 조회 성공", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "400", description = "groupId 형식 오류 (COMMON_400_001)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = INVALID_INPUT_VALUE_EXAMPLE_NAME, ref = INVALID_INPUT_VALUE_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = UNAUTHORIZED_EXAMPLE_NAME, ref = UNAUTHORIZED_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "403", description = "그룹원 또는 ADMIN 권한 없음 (GROUP_403_001)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = GROUP_ACCESS_DENIED_EXAMPLE_NAME, ref = GROUP_ACCESS_DENIED_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음 (GROUP_404_001)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = GROUP_NOT_FOUND_EXAMPLE_NAME, ref = GROUP_NOT_FOUND_EXAMPLE_REF)))
            }
    )
    public ResponseEntity<CommonResponse<List<GroupApplicationResponse>>> getPendingApplications(
            @PathVariable Long groupId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal EcoKnockUserDetails userDetails
    ) {
        return ResponseEntity.ok(CommonResponse.success(
                groupApplicationService.getPendingApplications(
                        groupId,
                        userDetails.memberInfo().getId()
                )
        ));
    }

    @GetMapping(value = "/{applicationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "PENDING 지원서 상세 조회",
            description = "그룹원 또는 ADMIN이 해당 그룹의 처리 대기 중인 지원서를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "지원서 조회 성공", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "400", description = "경로 ID 형식 오류 (COMMON_400_001)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = INVALID_INPUT_VALUE_EXAMPLE_NAME, ref = INVALID_INPUT_VALUE_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = UNAUTHORIZED_EXAMPLE_NAME, ref = UNAUTHORIZED_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "403", description = "그룹원 또는 ADMIN 권한 없음 (GROUP_403_001)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = GROUP_ACCESS_DENIED_EXAMPLE_NAME, ref = GROUP_ACCESS_DENIED_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "404", description = "그룹 또는 PENDING 지원서를 찾을 수 없음 (GROUP_404_001, GROUP_404_003)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = {
                            @ExampleObject(name = GROUP_NOT_FOUND_EXAMPLE_NAME, ref = GROUP_NOT_FOUND_EXAMPLE_REF),
                            @ExampleObject(name = GROUP_APPLICATION_NOT_FOUND_EXAMPLE_NAME, ref = GROUP_APPLICATION_NOT_FOUND_EXAMPLE_REF)
                    }))
            }
    )
    public ResponseEntity<CommonResponse<GroupApplicationResponse>> getPendingApplication(
            @PathVariable Long groupId,
            @PathVariable Long applicationId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal EcoKnockUserDetails userDetails
    ) {
        return ResponseEntity.ok(CommonResponse.success(
                groupApplicationService.getPendingApplication(
                        groupId,
                        applicationId,
                        userDetails.memberInfo().getId()
                )
        ));
    }

    @PutMapping(value = "/{applicationId}/accept", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "그룹 지원 수락",
            description = "그룹장 또는 ADMIN이 PENDING 지원서를 수락하고 지원자를 그룹원으로 추가합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "지원 수락 성공", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = EMPTY_SUCCESS_EXAMPLE_NAME, ref = EMPTY_SUCCESS_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "400", description = "경로 ID 형식 오류 (COMMON_400_001)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = INVALID_INPUT_VALUE_EXAMPLE_NAME, ref = INVALID_INPUT_VALUE_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = UNAUTHORIZED_EXAMPLE_NAME, ref = UNAUTHORIZED_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "403", description = "그룹장 또는 ADMIN 권한 없음 (GROUP_403_002)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = GROUP_LEADER_PERMISSION_REQUIRED_EXAMPLE_NAME, ref = GROUP_LEADER_PERMISSION_REQUIRED_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "404", description = "그룹 또는 지원서를 찾을 수 없음 (GROUP_404_001, GROUP_404_003)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = {
                            @ExampleObject(name = GROUP_NOT_FOUND_EXAMPLE_NAME, ref = GROUP_NOT_FOUND_EXAMPLE_REF),
                            @ExampleObject(name = GROUP_APPLICATION_NOT_FOUND_EXAMPLE_NAME, ref = GROUP_APPLICATION_NOT_FOUND_EXAMPLE_REF)
                    })),
                    @ApiResponse(responseCode = "409", description = "이미 처리됨, 이미 그룹원이거나 정원 도달 (GROUP_409_002, GROUP_409_004, GROUP_409_007)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = {
                            @ExampleObject(name = GROUP_MEMBER_ALREADY_EXISTS_EXAMPLE_NAME, ref = GROUP_MEMBER_ALREADY_EXISTS_EXAMPLE_REF),
                            @ExampleObject(name = GROUP_APPLICATION_ALREADY_PROCESSED_EXAMPLE_NAME, ref = GROUP_APPLICATION_ALREADY_PROCESSED_EXAMPLE_REF),
                            @ExampleObject(name = GROUP_CAPACITY_REACHED_EXAMPLE_NAME, ref = GROUP_CAPACITY_REACHED_EXAMPLE_REF)
                    }))
            }
    )
    public ResponseEntity<CommonResponse<Void>> accept(
            @PathVariable Long groupId,
            @PathVariable Long applicationId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal EcoKnockUserDetails userDetails
    ) {
        groupApplicationService.accept(
                groupId,
                applicationId,
                userDetails.memberInfo().getId()
        );
        return ResponseEntity.ok(CommonResponse.emptySuccess());
    }

    @PutMapping(value = "/{applicationId}/reject", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "그룹 지원 거절",
            description = "그룹장 또는 ADMIN이 PENDING 지원서를 거절합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "지원 거절 성공", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = EMPTY_SUCCESS_EXAMPLE_NAME, ref = EMPTY_SUCCESS_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "400", description = "경로 ID 형식 오류 (COMMON_400_001)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = INVALID_INPUT_VALUE_EXAMPLE_NAME, ref = INVALID_INPUT_VALUE_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = UNAUTHORIZED_EXAMPLE_NAME, ref = UNAUTHORIZED_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "403", description = "그룹장 또는 ADMIN 권한 없음 (GROUP_403_002)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = GROUP_LEADER_PERMISSION_REQUIRED_EXAMPLE_NAME, ref = GROUP_LEADER_PERMISSION_REQUIRED_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "404", description = "그룹 또는 지원서를 찾을 수 없음 (GROUP_404_001, GROUP_404_003)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = {
                            @ExampleObject(name = GROUP_NOT_FOUND_EXAMPLE_NAME, ref = GROUP_NOT_FOUND_EXAMPLE_REF),
                            @ExampleObject(name = GROUP_APPLICATION_NOT_FOUND_EXAMPLE_NAME, ref = GROUP_APPLICATION_NOT_FOUND_EXAMPLE_REF)
                    })),
                    @ApiResponse(responseCode = "409", description = "이미 처리된 지원서 (GROUP_409_004)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = GROUP_APPLICATION_ALREADY_PROCESSED_EXAMPLE_NAME, ref = GROUP_APPLICATION_ALREADY_PROCESSED_EXAMPLE_REF)))
            }
    )
    public ResponseEntity<CommonResponse<Void>> reject(
            @PathVariable Long groupId,
            @PathVariable Long applicationId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal EcoKnockUserDetails userDetails
    ) {
        groupApplicationService.reject(
                groupId,
                applicationId,
                userDetails.memberInfo().getId()
        );
        return ResponseEntity.ok(CommonResponse.emptySuccess());
    }
}
