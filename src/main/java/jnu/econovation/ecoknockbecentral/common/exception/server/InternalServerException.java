package jnu.econovation.ecoknockbecentral.common.exception.server;

import jnu.econovation.ecoknockbecentral.common.exception.BusinessException;
import jnu.econovation.ecoknockbecentral.common.exception.constants.ErrorCode;

public class InternalServerException extends BusinessException {
    public InternalServerException(Throwable cause) {
        super(ErrorCode.INTERNAL_SERVER_ERROR, cause);
    }
}
