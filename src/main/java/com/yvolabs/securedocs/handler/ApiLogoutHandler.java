package com.yvolabs.securedocs.handler;

import com.yvolabs.securedocs.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

import static com.yvolabs.securedocs.enumeration.TokenType.ACCESS;
import static com.yvolabs.securedocs.enumeration.TokenType.REFRESH;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 12/06/2024
 */
@Service
@RequiredArgsConstructor
public class ApiLogoutHandler implements LogoutHandler {
    private final JwtService jwtService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(request, response, authentication);
        jwtService.removeCookie(request, response, ACCESS.getValue());
        jwtService.removeCookie(request, response, REFRESH.getValue());

    }
}
