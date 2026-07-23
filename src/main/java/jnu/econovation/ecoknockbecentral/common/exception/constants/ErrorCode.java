package jnu.econovation.ecoknockbecentral.common.exception.constants;

import jnu.econovation.ecoknockbecentral.airquality.model.vo.AirQualityResolution;
import jnu.econovation.ecoknockbecentral.airquality.dto.rest.request.GetTimeseriesHistoryRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // --- AUTH ---
    BAD_ACCESS_TOKEN(Domain.AUTH, HttpStatus.UNAUTHORIZED, 1, "Access Token이 올바르지 않습니다."),
    BAD_REFRESH_TOKEN(Domain.AUTH, HttpStatus.UNAUTHORIZED, 2, "Refresh Token이 올바르지 않습니다."),
    GUEST_LOGIN_RATE_LIMIT_EXCEEDED(Domain.AUTH, HttpStatus.TOO_MANY_REQUESTS, 3, "게스트 로그인 요청이 너무 많습니다."),

    // --- COMMON ---
    INVALID_INPUT_VALUE(Domain.COMMON, HttpStatus.BAD_REQUEST, 1, "유효하지 않은 입력 값입니다."),
    BAD_DATA_SYNTAX(Domain.COMMON, HttpStatus.BAD_REQUEST, 2, "%s"),
    BAD_DATA_MEANING(Domain.COMMON, HttpStatus.UNPROCESSABLE_CONTENT, 1, "%s"),

    INTERNAL_SERVER_ERROR(Domain.COMMON, HttpStatus.INTERNAL_SERVER_ERROR, 1, "서버 내부 오류입니다."),

    // --- SSO ---
    INVALID_REDIRECT_URI(Domain.SSO, HttpStatus.BAD_REQUEST, 1, "유효하지 않은 Redirect URI 입니다."),
    BAD_SSO_TOKEN(Domain.SSO, HttpStatus.UNAUTHORIZED, 1, "SSO 토큰이 올바르지 않습니다."),

    // --- SECURITY ---
    ENCRYPTION_ERROR(Domain.SECURITY, HttpStatus.INTERNAL_SERVER_ERROR, 1, "암호화/복호화 중 예상치 못한 에러가 발생했습니다."),
    UNKNOWN_FILTER_ERROR(Domain.SECURITY, HttpStatus.INTERNAL_SERVER_ERROR, 2, "Spring Security Filter 에서 예상치 못한 에러가 발생했습니다."),
    UNAUTHORIZED(Domain.SECURITY, HttpStatus.UNAUTHORIZED, 1, "인증이 필요합니다."),
    FORBIDDEN(Domain.SECURITY, HttpStatus.FORBIDDEN, 1, "접근 권한이 없습니다."),

    // --- MEMBER ---
    MEMBER_NOT_FOUND(Domain.MEMBER, HttpStatus.INTERNAL_SERVER_ERROR, 1, "id가 %d인 회원을 찾을 수 없습니다."),
    MEMBER_DUPLICATED_FIELD(Domain.MEMBER, HttpStatus.CONFLICT, 1, "이미 사용중인 %s 입니다."),
    ALREADY_COMPLETED_REGISTRATION(Domain.MEMBER, HttpStatus.CONFLICT, 2, "이미 가입이 완료된 회원입니다."),

    // --- OVERVIEW ---
    OVERVIEW_LAYOUT_GRID_SIZE_CONFLICT(Domain.OVERVIEW, HttpStatus.CONFLICT, 1, "이미 %d 크기의 그리드가 적용되어 있습니다."),

    // --- WALLET ---
    WALLET_NOT_FOUND(Domain.WALLET, HttpStatus.NOT_FOUND, 1, "활성 보상 지갑을 찾을 수 없습니다."),

    // --- AIR_QUALITY ---
    BAD_FROM_TO(Domain.AIR_QUALITY, HttpStatus.BAD_REQUEST, 1, "from은 to보다 이전이어야 합니다."),
    BAD_AIR_QUALITY_RESOLUTION(Domain.AIR_QUALITY, HttpStatus.BAD_REQUEST, 2, "Air Quality Resolution은 [ %s ] 만 가능합니다.".formatted(AirQualityResolution.supportedCodes())),
    BAD_AIR_QUALITY_HISTORY_LIMIT(Domain.AIR_QUALITY, HttpStatus.BAD_REQUEST, 3, "Air Quality history limit은 %d 이상 %d 이하만 가능합니다.".formatted(GetTimeseriesHistoryRequest.MIN_LIMIT, GetTimeseriesHistoryRequest.MAX_LIMIT)),

    // --- AI ---
    BAD_AI_CHAT_HISTORY_LIMIT(Domain.AI, HttpStatus.BAD_REQUEST, 1, "AI chat history limit은 1 이상 50 이하만 가능합니다.");

    private final Domain domain;
    private final HttpStatus status;
    private final int number;
    private final String message;

    public String getCode() {
        return String.format("%s_%d_%03d", this.domain.name(), this.status.value(), this.number);
    }
}
