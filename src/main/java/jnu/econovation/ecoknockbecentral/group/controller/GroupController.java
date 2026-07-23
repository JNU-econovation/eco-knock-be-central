package jnu.econovation.ecoknockbecentral.group.controller;

import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.ACCESS_TOKEN_SECURITY_SCHEME_NAME;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.EMPTY_SUCCESS_EXAMPLE_NAME;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.EMPTY_SUCCESS_EXAMPLE_REF;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_CAPACITY_INVALID_EXAMPLE_NAME;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_CAPACITY_INVALID_EXAMPLE_REF;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_LEADER_CANNOT_BE_REMOVED_EXAMPLE_NAME;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_LEADER_CANNOT_BE_REMOVED_EXAMPLE_REF;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_LEADER_NOT_CHANGED_EXAMPLE_NAME;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_LEADER_NOT_CHANGED_EXAMPLE_REF;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_LEADER_PERMISSION_REQUIRED_EXAMPLE_NAME;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_LEADER_PERMISSION_REQUIRED_EXAMPLE_REF;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_MEMBER_NOT_FOUND_EXAMPLE_NAME;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_MEMBER_NOT_FOUND_EXAMPLE_REF;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_NAME_DUPLICATED_EXAMPLE_NAME;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_NAME_DUPLICATED_EXAMPLE_REF;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_NEW_LEADER_NOT_MEMBER_EXAMPLE_NAME;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_NEW_LEADER_NOT_MEMBER_EXAMPLE_REF;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_NOT_FOUND_EXAMPLE_NAME;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_NOT_FOUND_EXAMPLE_REF;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_RECRUITMENT_PERIOD_INVALID_EXAMPLE_NAME;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GROUP_RECRUITMENT_PERIOD_INVALID_EXAMPLE_REF;
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
import jnu.econovation.ecoknockbecentral.group.dto.request.BrowseGroupsRequest;
import jnu.econovation.ecoknockbecentral.group.dto.request.CreateGroupRequest;
import jnu.econovation.ecoknockbecentral.group.dto.request.ChangeGroupLeaderRequest;
import jnu.econovation.ecoknockbecentral.group.dto.request.UpdateGroupDetailRequest;
import jnu.econovation.ecoknockbecentral.group.dto.request.UpdateGroupNameRequest;
import jnu.econovation.ecoknockbecentral.group.dto.request.UpdateGroupRecruitmentRequest;
import jnu.econovation.ecoknockbecentral.group.dto.response.BrowseGroupResponse;
import jnu.econovation.ecoknockbecentral.group.dto.response.CreateGroupResponse;
import jnu.econovation.ecoknockbecentral.group.dto.response.GroupDetailResponse;
import jnu.econovation.ecoknockbecentral.group.dto.response.MyGroupResponse;
import jnu.econovation.ecoknockbecentral.group.dto.response.ManageGroupMemberResponse;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupSort;
import jnu.econovation.ecoknockbecentral.group.service.GroupService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/groups")
@Tag(name = "Group", description = "그룹 생성과 조회 API")
@SecurityRequirement(name = ACCESS_TOKEN_SECURITY_SCHEME_NAME)
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "그룹 생성",
            description = "인증 회원이 그룹을 생성하고 그룹장으로 가입합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "그룹 생성 성공", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "400", description = "요청 형식 또는 필드 검증 실패 (COMMON_400_001)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class))),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = UNAUTHORIZED_EXAMPLE_NAME, ref = UNAUTHORIZED_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "403", description = "GUEST는 그룹을 생성할 수 없음 (SECURITY_403_001)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class))),
                    @ApiResponse(responseCode = "409", description = "그룹명 중복 (GROUP_409_001)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class))),
                    @ApiResponse(responseCode = "422", description = "정원 또는 모집 기간 의미 검증 실패 (GROUP_422_001, GROUP_422_004)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class)))
            }
    )
    public ResponseEntity<CommonResponse<CreateGroupResponse>> create(
            @Parameter(hidden = true)
            @AuthenticationPrincipal EcoKnockUserDetails userDetails,
            @Valid @RequestBody CreateGroupRequest request
    ) {
        return ResponseEntity.ok(CommonResponse.success(
                groupService.create(userDetails.memberInfo().getId(), request)
        ));
    }

    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "내 그룹 목록 조회",
            description = "인증된 USER 또는 ADMIN이 가입한 그룹을 그룹명 오름차순으로 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "내 그룹 목록 조회 성공", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = UNAUTHORIZED_EXAMPLE_NAME, ref = UNAUTHORIZED_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "403", description = "현재 보안 정책에서 GUEST는 접근할 수 없음 (SECURITY_403_001)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class)))
            }
    )
    public ResponseEntity<CommonResponse<List<MyGroupResponse>>> getMyGroups(
            @Parameter(hidden = true)
            @AuthenticationPrincipal EcoKnockUserDetails userDetails
    ) {
        return ResponseEntity.ok(CommonResponse.success(
                groupService.getMyGroups(userDetails.memberInfo().getId())
        ));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "그룹 둘러보기",
            description = "그룹별 현재 인원, 그룹장과 계산된 모집 상태를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "그룹 목록 조회 성공", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "400", description = "정렬 값 형식 오류 (COMMON_400_001)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class))),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = UNAUTHORIZED_EXAMPLE_NAME, ref = UNAUTHORIZED_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "403", description = "현재 보안 정책에서 GUEST는 접근할 수 없음 (SECURITY_403_001)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class)))
            }
    )
    public ResponseEntity<CommonResponse<List<BrowseGroupResponse>>> browse(
            @Parameter(description = "true이면 CLOSED 그룹만 제외", example = "false", schema = @Schema(defaultValue = "false"))
            @RequestParam(defaultValue = "false") boolean excludeClosed,
            @Parameter(description = "정렬 방식", example = "NAME_ASC", schema = @Schema(defaultValue = "NAME_ASC", allowableValues = {"NAME_ASC", "NAME_DESC", "RECENT", "DEADLINE_ASC"}))
            @RequestParam(defaultValue = "NAME_ASC") GroupSort sort
    ) {
        return ResponseEntity.ok(CommonResponse.success(
                groupService.browse(new BrowseGroupsRequest(excludeClosed, sort))
        ));
    }

    @GetMapping(value = "/{groupId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "그룹 상세 조회",
            description = "그룹 설정, 그룹장, 전체 그룹원과 호출 회원의 가입·그룹장 여부를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "그룹 상세 조회 성공", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "400", description = "groupId 형식 오류 (COMMON_400_001)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class))),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = UNAUTHORIZED_EXAMPLE_NAME, ref = UNAUTHORIZED_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "403", description = "현재 보안 정책에서 GUEST는 접근할 수 없음 (SECURITY_403_001)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class))),
                    @ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음 (GROUP_404_001)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class)))
            }
    )
    public ResponseEntity<CommonResponse<GroupDetailResponse>> getDetail(
            @PathVariable Long groupId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal EcoKnockUserDetails userDetails
    ) {
        return ResponseEntity.ok(CommonResponse.success(
                groupService.getDetail(groupId, userDetails.memberInfo().getId())
        ));
    }

    @PutMapping(
            value = "/{groupId}/name",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "그룹명 수정",
            description = "그룹장 또는 ADMIN이 그룹명을 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "그룹명 수정 성공", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = EMPTY_SUCCESS_EXAMPLE_NAME, ref = EMPTY_SUCCESS_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "400", description = "요청 형식 또는 필드 검증 실패 (COMMON_400_001)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = INVALID_INPUT_VALUE_EXAMPLE_NAME, ref = INVALID_INPUT_VALUE_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = UNAUTHORIZED_EXAMPLE_NAME, ref = UNAUTHORIZED_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "403", description = "그룹장 권한 없음 (GROUP_403_002)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = GROUP_LEADER_PERMISSION_REQUIRED_EXAMPLE_NAME, ref = GROUP_LEADER_PERMISSION_REQUIRED_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음 (GROUP_404_001)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = GROUP_NOT_FOUND_EXAMPLE_NAME, ref = GROUP_NOT_FOUND_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "409", description = "그룹명 중복 (GROUP_409_001)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = GROUP_NAME_DUPLICATED_EXAMPLE_NAME, ref = GROUP_NAME_DUPLICATED_EXAMPLE_REF)))
            }
    )
    public ResponseEntity<CommonResponse<Void>> updateName(
            @PathVariable Long groupId,
            @Parameter(hidden = true) @AuthenticationPrincipal EcoKnockUserDetails userDetails,
            @Valid @RequestBody UpdateGroupNameRequest request
    ) {
        groupService.updateName(groupId, userDetails.memberInfo().getId(), request);
        return ResponseEntity.ok(CommonResponse.emptySuccess());
    }

    @PutMapping(
            value = "/{groupId}/detail",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "그룹 상세 설정 수정",
            description = "그룹장 또는 ADMIN이 유형, 소개와 정원을 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "상세 설정 수정 성공", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = EMPTY_SUCCESS_EXAMPLE_NAME, ref = EMPTY_SUCCESS_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "400", description = "요청 형식 또는 필드 검증 실패 (COMMON_400_001)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = INVALID_INPUT_VALUE_EXAMPLE_NAME, ref = INVALID_INPUT_VALUE_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = UNAUTHORIZED_EXAMPLE_NAME, ref = UNAUTHORIZED_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "403", description = "그룹장 권한 없음 (GROUP_403_002)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = GROUP_LEADER_PERMISSION_REQUIRED_EXAMPLE_NAME, ref = GROUP_LEADER_PERMISSION_REQUIRED_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음 (GROUP_404_001)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = GROUP_NOT_FOUND_EXAMPLE_NAME, ref = GROUP_NOT_FOUND_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "422", description = "현재 인원보다 작은 정원 (GROUP_422_004)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = GROUP_CAPACITY_INVALID_EXAMPLE_NAME, ref = GROUP_CAPACITY_INVALID_EXAMPLE_REF)))
            }
    )
    public ResponseEntity<CommonResponse<Void>> updateDetails(
            @PathVariable Long groupId,
            @Parameter(hidden = true) @AuthenticationPrincipal EcoKnockUserDetails userDetails,
            @Valid @RequestBody UpdateGroupDetailRequest request
    ) {
        groupService.updateDetails(groupId, userDetails.memberInfo().getId(), request);
        return ResponseEntity.ok(CommonResponse.emptySuccess());
    }

    @PutMapping(
            value = "/{groupId}/recruitment",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "그룹 모집 설정 수정",
            description = "그룹장 또는 ADMIN이 모집 방식과 기간을 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "모집 설정 수정 성공", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = EMPTY_SUCCESS_EXAMPLE_NAME, ref = EMPTY_SUCCESS_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "400", description = "요청 형식 또는 필드 검증 실패 (COMMON_400_001)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = INVALID_INPUT_VALUE_EXAMPLE_NAME, ref = INVALID_INPUT_VALUE_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = UNAUTHORIZED_EXAMPLE_NAME, ref = UNAUTHORIZED_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "403", description = "그룹장 권한 없음 (GROUP_403_002)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = GROUP_LEADER_PERMISSION_REQUIRED_EXAMPLE_NAME, ref = GROUP_LEADER_PERMISSION_REQUIRED_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음 (GROUP_404_001)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = GROUP_NOT_FOUND_EXAMPLE_NAME, ref = GROUP_NOT_FOUND_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "422", description = "잘못된 모집 기간 (GROUP_422_001)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = GROUP_RECRUITMENT_PERIOD_INVALID_EXAMPLE_NAME, ref = GROUP_RECRUITMENT_PERIOD_INVALID_EXAMPLE_REF)))
            }
    )
    public ResponseEntity<CommonResponse<Void>> updateRecruitment(
            @PathVariable Long groupId,
            @Parameter(hidden = true) @AuthenticationPrincipal EcoKnockUserDetails userDetails,
            @Valid @RequestBody UpdateGroupRecruitmentRequest request
    ) {
        groupService.updateRecruitment(groupId, userDetails.memberInfo().getId(), request);
        return ResponseEntity.ok(CommonResponse.emptySuccess());
    }

    @GetMapping(
            value = "/{groupId}/members/management",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "관리용 그룹원 목록 조회",
            description = "그룹장 또는 ADMIN이 그룹원 식별자와 역할을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "관리용 그룹원 목록 조회 성공", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = UNAUTHORIZED_EXAMPLE_NAME, ref = UNAUTHORIZED_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "403", description = "그룹장 권한 없음 (GROUP_403_002)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = GROUP_LEADER_PERMISSION_REQUIRED_EXAMPLE_NAME, ref = GROUP_LEADER_PERMISSION_REQUIRED_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음 (GROUP_404_001)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = GROUP_NOT_FOUND_EXAMPLE_NAME, ref = GROUP_NOT_FOUND_EXAMPLE_REF)))
            }
    )
    public ResponseEntity<CommonResponse<List<ManageGroupMemberResponse>>> getMembersForManagement(
            @PathVariable Long groupId,
            @Parameter(hidden = true) @AuthenticationPrincipal EcoKnockUserDetails userDetails
    ) {
        return ResponseEntity.ok(CommonResponse.success(
                groupService.getMembersForManagement(
                        groupId,
                        userDetails.memberInfo().getId()
                )
        ));
    }

    @DeleteMapping(
            value = "/{groupId}/members/{memberId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "그룹원 제거",
            description = "그룹장 또는 ADMIN이 일반 그룹원을 제거합니다. 현재 그룹장은 제거할 수 없습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "그룹원 제거 성공", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = EMPTY_SUCCESS_EXAMPLE_NAME, ref = EMPTY_SUCCESS_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = UNAUTHORIZED_EXAMPLE_NAME, ref = UNAUTHORIZED_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "403", description = "그룹장 권한 없음 (GROUP_403_002)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = GROUP_LEADER_PERMISSION_REQUIRED_EXAMPLE_NAME, ref = GROUP_LEADER_PERMISSION_REQUIRED_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "404", description = "그룹 또는 그룹원을 찾을 수 없음 (GROUP_404_001, GROUP_404_002)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = {
                            @ExampleObject(name = GROUP_NOT_FOUND_EXAMPLE_NAME, ref = GROUP_NOT_FOUND_EXAMPLE_REF),
                            @ExampleObject(name = GROUP_MEMBER_NOT_FOUND_EXAMPLE_NAME, ref = GROUP_MEMBER_NOT_FOUND_EXAMPLE_REF)
                    })),
                    @ApiResponse(responseCode = "409", description = "현재 그룹장은 제거할 수 없음 (GROUP_409_005)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = GROUP_LEADER_CANNOT_BE_REMOVED_EXAMPLE_NAME, ref = GROUP_LEADER_CANNOT_BE_REMOVED_EXAMPLE_REF)))
            }
    )
    public ResponseEntity<CommonResponse<Void>> removeMember(
            @PathVariable Long groupId,
            @PathVariable Long memberId,
            @Parameter(hidden = true) @AuthenticationPrincipal EcoKnockUserDetails userDetails
    ) {
        groupService.removeMember(groupId, userDetails.memberInfo().getId(), memberId);
        return ResponseEntity.ok(CommonResponse.emptySuccess());
    }

    @PutMapping(
            value = "/{groupId}/leader",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "그룹장 위임",
            description = "그룹장 또는 ADMIN이 기존 그룹원에게 그룹장을 위임합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "그룹장 위임 성공", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = EMPTY_SUCCESS_EXAMPLE_NAME, ref = EMPTY_SUCCESS_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "400", description = "요청 형식 또는 필드 검증 실패 (COMMON_400_001)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = INVALID_INPUT_VALUE_EXAMPLE_NAME, ref = INVALID_INPUT_VALUE_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = UNAUTHORIZED_EXAMPLE_NAME, ref = UNAUTHORIZED_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "403", description = "그룹장 권한 없음 (GROUP_403_002)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = GROUP_LEADER_PERMISSION_REQUIRED_EXAMPLE_NAME, ref = GROUP_LEADER_PERMISSION_REQUIRED_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음 (GROUP_404_001)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = GROUP_NOT_FOUND_EXAMPLE_NAME, ref = GROUP_NOT_FOUND_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "409", description = "현재 그룹장과 동일함 (GROUP_409_006)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = GROUP_LEADER_NOT_CHANGED_EXAMPLE_NAME, ref = GROUP_LEADER_NOT_CHANGED_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "422", description = "새 그룹장이 기존 그룹원이 아님 (GROUP_422_006)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = GROUP_NEW_LEADER_NOT_MEMBER_EXAMPLE_NAME, ref = GROUP_NEW_LEADER_NOT_MEMBER_EXAMPLE_REF)))
            }
    )
    public ResponseEntity<CommonResponse<Void>> changeLeader(
            @PathVariable Long groupId,
            @Parameter(hidden = true) @AuthenticationPrincipal EcoKnockUserDetails userDetails,
            @Valid @RequestBody ChangeGroupLeaderRequest request
    ) {
        groupService.changeLeader(
                groupId,
                userDetails.memberInfo().getId(),
                request.memberId()
        );
        return ResponseEntity.ok(CommonResponse.emptySuccess());
    }

    @DeleteMapping(value = "/{groupId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "그룹 삭제",
            description = "그룹장 또는 ADMIN이 그룹을 즉시 삭제합니다. 그룹원과 지원서도 함께 삭제됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "그룹 삭제 성공", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = EMPTY_SUCCESS_EXAMPLE_NAME, ref = EMPTY_SUCCESS_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = UNAUTHORIZED_EXAMPLE_NAME, ref = UNAUTHORIZED_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "403", description = "그룹장 권한 없음 (GROUP_403_002)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = GROUP_LEADER_PERMISSION_REQUIRED_EXAMPLE_NAME, ref = GROUP_LEADER_PERMISSION_REQUIRED_EXAMPLE_REF))),
                    @ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음 (GROUP_404_001)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(name = GROUP_NOT_FOUND_EXAMPLE_NAME, ref = GROUP_NOT_FOUND_EXAMPLE_REF)))
            }
    )
    public ResponseEntity<CommonResponse<Void>> delete(
            @PathVariable Long groupId,
            @Parameter(hidden = true) @AuthenticationPrincipal EcoKnockUserDetails userDetails
    ) {
        groupService.delete(groupId, userDetails.memberInfo().getId());
        return ResponseEntity.ok(CommonResponse.emptySuccess());
    }
}
