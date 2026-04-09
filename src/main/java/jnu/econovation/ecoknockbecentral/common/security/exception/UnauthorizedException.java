package jnu.econovation.ecoknockbecentral.common.security.exception;

import javax.naming.AuthenticationException;
import jnu.econovation.ecoknockbecentral.common.exception.constants.ErrorCode;

public class UnauthorizedException extends AuthenticationException {

    public UnauthorizedException() {
        super(ErrorCode.UNAUTHORIZED.getMessage());
    }

}
