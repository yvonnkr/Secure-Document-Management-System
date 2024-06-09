package com.yvolabs.securedocs.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import static com.yvolabs.securedocs.utils.RequestUtils.handleErrorResponse;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 08/06/2024
 */

@Component
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) {
        handleErrorResponse(request, response, exception);
    }
}
