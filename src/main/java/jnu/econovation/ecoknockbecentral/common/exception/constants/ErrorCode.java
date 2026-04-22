package jnu.econovation.ecoknockbecentral.common.exception.constants;

import jnu.econovation.ecoknockbecentral.airquality.dto.request.AirQualityResolution;
import jnu.econovation.ecoknockbecentral.airquality.dto.request.GetTimeseriesHistoryRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // --- COMMON ---
    INVALID_INPUT_VALUE(Domain.COMMON, HttpStatus.BAD_REQUEST, 1, "유효하지 않은 입력 값입니다."),
    BAD_DATA_SYNTAX(Domain.COMMON, HttpStatus.BAD_REQUEST, 2, "%s"),
    BAD_DATA_MEANING(Domain.COMMON, HttpStatus.UNPROCESSABLE_CONTENT, 1, "%s"),

    INTERNAL_SERVER_ERROR(Domain.COMMON, HttpStatus.INTERNAL_SERVER_ERROR, 1, "서버 내부 오류입니다."),

    // --- AUTH ---
    INVALID_REDIRECT_URI(Domain.AUTH, HttpStatus.BAD_REQUEST, 1, "유효하지 않은 Redirect URI 입니다. -> %s"),
    INVALID_AUTH_CODE(Domain.AUTH, HttpStatus.BAD_REQUEST, 2, "유효하지 않은 인가 코드입니다."),
    OAUTH2_SERVER_ERROR(Domain.AUTH, HttpStatus.INTERNAL_SERVER_ERROR, 1, "OAUTH2 인증 서버에 오류가 발생했습니다."),
    OAUTH2_UNKNOWN_ERROR(Domain.AUTH, HttpStatus.INTERNAL_SERVER_ERROR, 2, "OAUTH2 인증 관련 알 수 없는 오류가 발생했습니다."),

    // --- SECURITY ---
    ENCRYPTION_ERROR(Domain.SECURITY, HttpStatus.INTERNAL_SERVER_ERROR, 1, "암호화/복호화 중 예상치 못한 에러가 발생했습니다."),
    UNKNOWN_FILTER_ERROR(Domain.SECURITY, HttpStatus.INTERNAL_SERVER_ERROR, 2, "Spring Security Filter 에서 예상치 못한 에러가 발생했습니다."),
    UNAUTHORIZED(Domain.SECURITY, HttpStatus.UNAUTHORIZED, 1, "인증이 필요합니다."),

    // --- MEMBER ---
    MEMBER_NOT_FOUND(Domain.MEMBER, HttpStatus.INTERNAL_SERVER_ERROR, 1, "id가 %d인 회원을 찾을 수 없습니다."),
    MEMBER_DUPLICATED_FIELD(Domain.MEMBER, HttpStatus.CONFLICT, 1, "이미 사용중인 %s 입니다."),
    ALREADY_COMPLETED_REGISTRATION(Domain.MEMBER, HttpStatus.CONFLICT, 2, "이미 가입이 완료된 회원입니다."),


    // --- AIR_QUALITY ---
    BAD_FROM_TO(Domain.AIR_QUALITY, HttpStatus.BAD_REQUEST, 1, "from은 to보다 이전이어야 합니다."),
    BAD_AIR_QUALITY_RESOLUTION(Domain.AIR_QUALITY, HttpStatus.BAD_REQUEST, 2, "Air Quality Resolution은 [ %s ] 만 가능합니다.".formatted(AirQualityResolution.supportedCodes())),
    BAD_AIR_QUALITY_HISTORY_LIMIT(Domain.AIR_QUALITY, HttpStatus.BAD_REQUEST, 3, "Air Quality history limit은 %d 이상 %d 이하만 가능합니다.".formatted(GetTimeseriesHistoryRequest.MIN_LIMIT, GetTimeseriesHistoryRequest.MAX_LIMIT));

    private final Domain domain;
    private final HttpStatus status;
    private final int number;
    private final String message;

    public String getCode() {
        return String.format("%s_%d_%03d", this.domain.name(), this.status.value(), this.number);
    }
}
