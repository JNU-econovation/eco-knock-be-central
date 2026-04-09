package jnu.econovation.ecoknockbecentral.common.exception.client;

import jnu.econovation.ecoknockbecentral.common.exception.constants.ErrorCode;
import lombok.Getter;

@Getter
public class BadDataMeaningException extends ClientException {

    private final String message;

    public BadDataMeaningException(String message) {
        super(ErrorCode.BAD_DATA_MEANING);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return String.format(getErrorCode().getMessage(), message);
    }
}