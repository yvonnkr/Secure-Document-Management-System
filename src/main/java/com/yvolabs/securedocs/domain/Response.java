package com.yvolabs.securedocs.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 03/06/2024
 */

@JsonInclude(NON_DEFAULT)
@Builder
public record Response(
        String time,
        int code,
        String path,
        HttpStatus status,
        String message,
        String exception,
        Map<?, ?> data
) {
}
