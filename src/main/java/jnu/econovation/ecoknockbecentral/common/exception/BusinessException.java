package jnu.econovation.ecoknockbecentral.common.exception;

import jnu.econovation.ecoknockbecentral.common.exception.constants.ErrorCode;
import lombok.Getter;
import org.springframework.core.NestedRuntimeException;

@Getter
public abstract class BusinessException extends NestedRuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}