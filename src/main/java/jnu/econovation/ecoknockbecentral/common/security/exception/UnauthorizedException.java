package jnu.econovation.ecoknockbecentral.common.security.exception;

import jnu.econovation.ecoknockbecentral.common.exception.constants.ErrorCode;
import org.springframework.security.core.AuthenticationException;

public class UnauthorizedException extends AuthenticationException {

    public UnauthorizedException() {
        super(ErrorCode.UNAUTHORIZED.getMessage());
    }

}
