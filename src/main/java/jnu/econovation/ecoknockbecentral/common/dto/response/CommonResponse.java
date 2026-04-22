package jnu.econovation.ecoknockbecentral.common.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import jnu.econovation.ecoknockbecentral.common.exception.constants.ErrorCode;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse<T> {

    private final boolean isSuccess;
    private final String message;
    private final String errorCode;
    private final T result;

    private static final String SUCCESS_MESSAGE = "success";

    public CommonResponse(boolean isSuccess, String message, String errorCode, T result) {
        this.isSuccess = isSuccess;
        this.message = message;
        this.errorCode = errorCode;
        this.result = result;
    }

    public static CommonResponse<Void> ofSuccess() {
        return new CommonResponse<>(true, SUCCESS_MESSAGE, null, null);
    }

    public static <T> CommonResponse<T> ofSuccess(T result) {
        return new CommonResponse<>(true, SUCCESS_MESSAGE, null, result);
    }

    public static CommonResponse<Void> ofFailure(ErrorCode errorCode) {
        return new CommonResponse<>(
                false,
                errorCode.getMessage(),
                errorCode.getCode(),
                null
        );
    }

    public static CommonResponse<Void> ofFailure(ErrorCode errorCode, String customMessage) {
        return new CommonResponse<>(
                false,
                customMessage,
                errorCode.getCode(),
                null
        );
    }
}