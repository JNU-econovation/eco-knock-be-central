package jnu.econovation.ecoknockbecentral.common.exception.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse;
import jnu.econovation.ecoknockbecentral.common.exception.client.ClientException;
import jnu.econovation.ecoknockbecentral.common.exception.constants.ErrorCode;
import jnu.econovation.ecoknockbecentral.common.exception.server.InternalServerException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "서버에서 예상치 못한 오류가 발생했습니다.";

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        String parameterName = ex.getParameterName();
        String message = "필요로 하는 파라미터 -> " + parameterName + " 이(가) 없습니다.";
        log.warn("Missing parameter: {}", parameterName);
        return handleExceptionInternal(ErrorCode.INVALID_INPUT_VALUE, message);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        StringBuilder errorMessage = new StringBuilder();

        String firstError = ex.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(ObjectError::getDefaultMessage)
                .orElse("잘못된 요청입니다.");

        errorMessage.append(firstError);

        // for 디버깅
        if (log.isDebugEnabled()) {
            ex.getBindingResult().getAllErrors().forEach(error -> {
                if (error instanceof FieldError fieldError) {
                    log.debug("Validation error - Field: {}, Value: {}, Message: {}",
                            fieldError.getField(), fieldError.getRejectedValue(),
                            error.getDefaultMessage());
                } else {
                    log.debug("Validation error - Message: {}", error.getDefaultMessage());
                }
            });
        }

        return handleExceptionInternal(ErrorCode.INVALID_INPUT_VALUE, errorMessage.toString());

    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        ClientException clientException = findCause(ex, ClientException.class);
        if (clientException != null) {
            return ResponseEntity.status(clientException.getErrorCode().getStatus())
                    .body(CommonResponse.ofFailure(
                            clientException.getErrorCode(),
                            clientException.getMessage()
                    ));
        }

        String message = "잘못된 JSON 형식입니다.";
        Throwable cause = ex.getCause();

        if (cause instanceof JsonProcessingException jsonEx) {
            log.warn("JSON parsing error: {}", jsonEx.getMessage());

            switch (jsonEx) {
                case InvalidFormatException invalidFormatEx -> {
                    String fieldName = invalidFormatEx.getPath().isEmpty() ? "unknown" :
                            invalidFormatEx.getPath().getFirst().getFieldName();
                    message = String.format("필드 '%s'의 값이 올바르지 않습니다: %s",
                            fieldName, invalidFormatEx.getValue());
                }
                case MismatchedInputException mismatchedEx -> {
                    String fieldName = mismatchedEx.getPath().isEmpty() ? "unknown" :
                            mismatchedEx.getPath().getFirst().getFieldName();
                    message = String.format("필드 '%s'의 타입이 맞지 않습니다.", fieldName);
                }
                case JsonMappingException mappingEx -> {
                    String fieldName = mappingEx.getPath().isEmpty() ? "unknown" :
                            mappingEx.getPath().getFirst().getFieldName();

                    String causeMessage =
                            mappingEx.getCause() != null ? mappingEx.getCause().getMessage() : "";

                    message = String.format("필드 '%s'에 문제가 있습니다: %s", fieldName, causeMessage);
                }
                default -> {
                }
            }
        } else {
            log.warn("HTTP message not readable: {}", ex.getMessage());
            message = "요청 본문을 읽을 수 없습니다. JSON 형식을 확인해주세요.";
        }

        return handleExceptionInternal(ErrorCode.INVALID_INPUT_VALUE, message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        ClientException clientException = findCause(ex, ClientException.class);
        if (clientException != null) {
            return ResponseEntity.status(clientException.getErrorCode().getStatus())
                    .body(CommonResponse.ofFailure(
                            clientException.getErrorCode(),
                            clientException.getMessage()
                    ));
        }

        String message = String.format("파라미터 '%s'의 값 '%s'이(가) 올바르지 않습니다.",
                ex.getName(), ex.getValue());
        log.warn("Type mismatch for parameter: {} with value: {}", ex.getName(), ex.getValue());

        return handleExceptionInternal(ErrorCode.INVALID_INPUT_VALUE, message);
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<Object> handleJsonProcessingException(
            JsonProcessingException ex) {
        ClientException clientException = findCause(ex, ClientException.class);
        if (clientException != null) {
            return ResponseEntity.status(clientException.getErrorCode().getStatus())
                    .body(CommonResponse.ofFailure(
                            clientException.getErrorCode(),
                            clientException.getMessage()
                    ));
        }

        log.warn("JSON processing error: {}", ex.getMessage());
        String message = "JSON 처리 중 오류가 발생했습니다: " + ex.getOriginalMessage();
        return handleExceptionInternal(ErrorCode.INVALID_INPUT_VALUE, message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("IllegalArgumentException 발생 : {}", e.getMessage());
        return handleExceptionInternal(ErrorCode.INVALID_INPUT_VALUE, e.getMessage());
    }


    @ExceptionHandler(ClientException.class)
    public ResponseEntity<CommonResponse<Void>> handleClientException(ClientException e) {
        log.warn("클라이언트 예외 발생: Code={}, Message={}", e.getErrorCode().getCode(), e.getMessage());
        return handleClientExceptionInternal(e);
    }


    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<Object> handleServerException(InternalServerException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.error("서버에 의한 오류 발생: Code={}, Message={}", errorCode.getCode(), e.getMessage(), e);

        if (e.getErrorCode().getStatus() != HttpStatus.INTERNAL_SERVER_ERROR) {
            return handleExceptionInternal(errorCode, e.getMessage());
        }

        return handleExceptionInternal(errorCode, INTERNAL_SERVER_ERROR_MESSAGE);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllUncaughtException(Exception e) {
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        log.error("서버에 의한 오류 발생: ", e);
        return handleExceptionInternal(errorCode, INTERNAL_SERVER_ERROR_MESSAGE);
    }


    private ResponseEntity<CommonResponse<Void>> handleClientExceptionInternal(ClientException e) {
        return ResponseEntity.status(e.getErrorCode().getStatus())
                .body(CommonResponse.ofFailure(e.getErrorCode(), e.getMessage()));
    }

    private ResponseEntity<Object> handleExceptionInternal(ErrorCode errorCode, String message) {
        return ResponseEntity.status(errorCode.getStatus())
                .body(CommonResponse.ofFailure(errorCode, message));
    }

    private <T extends Throwable> T findCause(Throwable throwable, Class<T> type) {
        Throwable current = throwable;

        while (current != null) {
            if (type.isInstance(current)) {
                return type.cast(current);
            }

            current = current.getCause();
        }

        return null;
    }
}
