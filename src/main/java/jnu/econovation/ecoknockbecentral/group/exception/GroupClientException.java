package jnu.econovation.ecoknockbecentral.group.exception;

import jnu.econovation.ecoknockbecentral.common.exception.client.ClientException;
import jnu.econovation.ecoknockbecentral.common.exception.constants.ErrorCode;

public class GroupClientException extends ClientException {

    public GroupClientException(ErrorCode errorCode) {
        super(errorCode);
    }
}
