package jnu.econovation.ecoknockbecentral.common.exception.client;

import jnu.econovation.ecoknockbecentral.common.exception.BusinessException;
import jnu.econovation.ecoknockbecentral.common.exception.constants.ErrorCode;

public abstract class ClientException extends BusinessException {

    public ClientException(ErrorCode errorCode) {
        super(errorCode);
    }
}