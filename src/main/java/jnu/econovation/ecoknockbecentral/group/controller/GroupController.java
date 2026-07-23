package jnu.econovation.ecoknockbecentral.group.controller;

import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.ACCESS_TOKEN_SECURITY_SCHEME_NAME;
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
import jnu.econovation.ecoknockbecentral.group.dto.response.BrowseGroupResponse;
import jnu.econovation.ecoknockbecentral.group.dto.response.CreateGroupResponse;
import jnu.econovation.ecoknockbecentral.group.dto.response.GroupDetailResponse;
import jnu.econovation.ecoknockbecentral.group.dto.response.MyGroupResponse;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupSort;
import jnu.econovation.ecoknockbecentral.group.service.GroupService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
}
