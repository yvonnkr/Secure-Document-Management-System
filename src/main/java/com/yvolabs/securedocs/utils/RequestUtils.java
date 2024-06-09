package com.yvolabs.securedocs.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yvolabs.securedocs.domain.Response;
import com.yvolabs.securedocs.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 03/06/2024
 */
public class RequestUtils {

    private static final BiConsumer<HttpServletResponse, Response> writeResponse = (httpResponse, response) -> {
        try {
            var outputStream = httpResponse.getOutputStream();
            new ObjectMapper().writeValue(outputStream, response);
            outputStream.flush();

        } catch (Exception exception) {
            throw new ApiException(exception.getMessage());
        }
    };

    private static final BiFunction<Exception, HttpStatus, String> errorReason = (exception, httpStatus) -> {
        if (httpStatus.isSameCodeAs(FORBIDDEN)) return "You do not have permission";
        if (httpStatus.isSameCodeAs(UNAUTHORIZED)) return "You are not logged in";

        if (exception instanceof DisabledException || exception instanceof LockedException || exception instanceof BadCredentialsException || exception instanceof CredentialsExpiredException || exception instanceof ApiException) {
            return exception.getMessage();
        }

        if (httpStatus.is5xxServerError()) {
            return "An internal server error occurred";
        } else {
            return "An error occurred, please try again";
        }

    };

    @Builder(builderMethodName = "getResponseBuilder")
    public static Response getResponse(HttpServletRequest request, Map<?, ?> data, String message, HttpStatus status) {
        return Response.builder()
                .time(LocalDateTime.now().toString())
                .code(status.value())
                .path(request.getRequestURI())
                .status(HttpStatus.valueOf(status.value()))
                .message(message)
                .exception(EMPTY)
                .data(data)
                .build();
    }


    public static void handleErrorResponse(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        if (exception instanceof AccessDeniedException) {
            Response apiResponse = getErrorResponse(request, response, exception, FORBIDDEN);
            writeResponse.accept(response, apiResponse);
        }
    }

    private static Response getErrorResponse(HttpServletRequest request, HttpServletResponse response, Exception exception, HttpStatus status) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(status.value());
        return Response.builder()
                .time(LocalDateTime.now().toString())
                .code(status.value())
                .path(request.getRequestURI())
                .status(HttpStatus.valueOf(status.value()))
                .message(errorReason.apply(exception, status))
                .exception(getRootCauseMessage(exception))
                .data(emptyMap())
                .build();
    }


}
