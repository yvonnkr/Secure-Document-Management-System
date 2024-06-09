package com.yvolabs.securedocs.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import static com.yvolabs.securedocs.utils.RequestUtils.handleErrorResponse;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 08/06/2024
 */

@Component
public class ApiAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception) {
        handleErrorResponse(request, response, exception);
    }
}
