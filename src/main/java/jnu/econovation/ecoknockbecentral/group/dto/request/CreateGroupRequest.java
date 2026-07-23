package jnu.econovation.ecoknockbecentral.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.ZonedDateTime;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupType;
import jnu.econovation.ecoknockbecentral.group.model.vo.RecruitmentMode;

public record CreateGroupRequest(
        @NotNull
        @Schema(description = "그룹 유형", example = "STUDY")
        GroupType type,

        @NotBlank
        @Size(max = 15)
        @Schema(description = "그룹명. 앞뒤 공백 제거 후 1~15자", example = "백엔드 스터디")
        String name,

        @NotBlank
        @Size(max = 100)
        @Schema(description = "그룹 소개. 앞뒤 공백 제거 후 1~100자", example = "함께 백엔드를 공부합니다.")
        String introduction,

        @Schema(description = "그룹장을 포함한 정원", example = "10", minimum = "1", maximum = "50")
        @NotNull
        Integer capacity,

        @NotNull
        @Schema(description = "모집 방식", example = "PERIOD")
        RecruitmentMode recruitmentMode,

        @Schema(description = "기간제 모집 시작 시각. 날짜만 입력하는 화면은 서울 기준 00:00:00을 사용", example = "2026-07-24T00:00:00+09:00")
        ZonedDateTime recruitmentStartAt,

        @Schema(description = "기간제 모집 종료 시각. 날짜만 입력하는 화면은 서울 기준 23:59:59.999999를 사용", example = "2026-07-31T23:59:59.999999+09:00")
        ZonedDateTime recruitmentEndAt
) {
    public CreateGroupRequest {
        name = name == null ? null : name.trim();
        introduction = introduction == null ? null : introduction.trim();
    }
}
