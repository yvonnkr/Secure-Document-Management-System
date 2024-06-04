package com.yvolabs.securedocs.utils;

import com.yvolabs.securedocs.domain.Response;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 03/06/2024
 */
public class RequestUtils {

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
}
