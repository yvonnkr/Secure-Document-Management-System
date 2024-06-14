package com.yvolabs.securedocs.exception;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.yvolabs.securedocs.domain.Response;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.stream.Collectors;

import static com.yvolabs.securedocs.utils.RequestUtils.handleErrorResponse;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.springframework.http.HttpStatus.*;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 14/06/2024
 */

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class HandleException extends ResponseEntityExceptionHandler implements ErrorController {
    private final HttpServletRequest request;

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body, @Nonnull HttpHeaders headers, @Nonnull HttpStatusCode statusCode, @Nonnull WebRequest webRequest) {
        log.error(String.format("handleExceptionInternal: %s", exception.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(exception.getMessage(), getRootCauseMessage(exception), request, statusCode), statusCode);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception, @Nonnull HttpHeaders headers, @Nonnull HttpStatusCode statusCode, @Nonnull WebRequest webRequest) {
        log.error(String.format("handleMethodArgumentNotValid: %s", exception.getMessage()));
        var fieldErrors = exception.getBindingResult().getFieldErrors();
        var fieldsMessage = fieldErrors.stream().map(FieldError::getDefaultMessage).collect(Collectors.joining(", "));
        return new ResponseEntity<>(handleErrorResponse(fieldsMessage, getRootCauseMessage(exception), request, statusCode), statusCode);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Response> handleApiException(ApiException exception) {
        log.error(String.format("handleApiException: %s", exception.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(exception.getMessage(), getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Response> handleBadCredentialsException(BadCredentialsException exception) {
        log.error(String.format("handleBadCredentialsException: %s", exception.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(exception.getMessage(), getRootCauseMessage(exception), request, UNAUTHORIZED), UNAUTHORIZED);
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<Response> handleSQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException exception) {
        log.error(String.format("handleSQLIntegrityConstraintViolationException: %s", exception.getMessage()));
        String message = exception.getMessage().contains("Duplicate entry") ? "Information already exists" : exception.getMessage();
        return new ResponseEntity<>(handleErrorResponse(message, getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(UnrecognizedPropertyException.class)
    public ResponseEntity<Response> handleUnrecognizedPropertyException(UnrecognizedPropertyException exception) {
        log.error(String.format("handleUnrecognizedPropertyException: %s", exception.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(exception.getMessage(), getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Response> handleAccessDeniedException(AccessDeniedException exception) {
        log.error(String.format("handleAccessDeniedException: %s", exception.getMessage()));
        return new ResponseEntity<>(handleErrorResponse("Access denied. You don't have access", getRootCauseMessage(exception), request, FORBIDDEN), FORBIDDEN);
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<Response> handleEmptyResultDataAccessException(EmptyResultDataAccessException exception) {
        log.error(String.format("handleEmptyResultDataAccessException: %s", exception.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(exception.getMessage(), getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(CredentialsExpiredException.class)
    public ResponseEntity<Response> handleCredentialsExpiredException(CredentialsExpiredException exception) {
        log.error(String.format("handleCredentialsExpiredException: %s", exception.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(exception.getMessage(), getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Response> handleDisabledException(DisabledException exception) {
        log.error(String.format("handleDisabledException: %s", exception.getMessage()));
        return new ResponseEntity<>(handleErrorResponse("User account is currently disabled", getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Response> handleLockedException(LockedException exception) {
        log.error(String.format("handleLockedException: %s", exception.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(exception.getMessage(), getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Response> handleDuplicateKeyException(DuplicateKeyException exception) {
        log.error(String.format("handleDuplicateKeyException: %s", exception.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(processErrorMessage(exception), getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Response> handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        log.error(String.format("handleDuplicateKeyException: %s", exception.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(processErrorMessage(exception), getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Response> handleDataAccessException(DataAccessException exception) {
        log.error(String.format("handleDataAccessException: %s", exception.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(processErrorMessage(exception), getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<Response> handleTransactionSystemException(TransactionSystemException exception) {
        log.error(String.format("handleDataIntegrityViolationException: %s", exception.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(processErrorMessage(exception), getRootCauseMessage(exception), request, INTERNAL_SERVER_ERROR), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response> handleALlOtherException(Exception exception) {
        log.error(String.format("handleALlOtherException: %s", exception.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(processErrorMessage(exception), getRootCauseMessage(exception), request, INTERNAL_SERVER_ERROR), INTERNAL_SERVER_ERROR);
    }

    private String processErrorMessage(Exception exception) {
        if (exception instanceof ApiException) {
            return exception.getMessage();
        }
        //if(exception instanceof TransactionSystemException) { return getRootCauseMessage(exception).split(":")[1]; }
        if (exception.getMessage() != null) {
            if (exception.getMessage().contains("duplicate") && exception.getMessage().contains("AccountVerifications")) {
                return "You already verified your account.";
            }
            if (exception.getMessage().contains("duplicate") && exception.getMessage().contains("ResetPasswordVerifications")) {
                return "We already sent you an email to reset your password.";
            }
            if (exception.getMessage().contains("duplicate") && exception.getMessage().contains("Key (email)")) {
                return "Email already exists. Use a different email and try again.";
            }
            if (exception.getMessage().contains("duplicate")) {
                return "Duplicate entry. Please try again.";
            }
        }
        return "An error occurred. Please try again.";
    }
}
