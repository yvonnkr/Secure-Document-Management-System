package com.yvolabs.securedocs.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yvolabs.securedocs.domain.ApiAuthentication;
import com.yvolabs.securedocs.domain.Response;
import com.yvolabs.securedocs.dto.User;
import com.yvolabs.securedocs.dtorequest.LoginRequest;
import com.yvolabs.securedocs.service.JwtService;
import com.yvolabs.securedocs.service.UserService;
import com.yvolabs.securedocs.utils.RequestUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.util.Map;

import static com.yvolabs.securedocs.constant.Constants.LOGIN_PATH;
import static com.yvolabs.securedocs.enumeration.LoginType.LOGIN_ATTEMPT;
import static com.yvolabs.securedocs.enumeration.TokenType.ACCESS;
import static com.yvolabs.securedocs.enumeration.TokenType.REFRESH;
import static com.yvolabs.securedocs.utils.RequestUtils.handleErrorResponse;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 06/06/2024
 * <p>
 * We will use this class for login endpoint instead of using the controller.
 */

@Slf4j
public class ApiAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final UserService userService;
    private final JwtService jwtService;


    public ApiAuthenticationFilter(AuthenticationManager authenticationManager, UserService userService, JwtService jwtService) {
        super(new AntPathRequestMatcher(LOGIN_PATH, POST.name()), authenticationManager);
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            LoginRequest loginRequest = new ObjectMapper().readValue(request.getInputStream(), LoginRequest.class);

            userService.updateLoginAttempt(loginRequest.getEmail(), LOGIN_ATTEMPT);
            var authentication = ApiAuthentication.unauthenticated(loginRequest.getEmail(), loginRequest.getPassword());
            return getAuthenticationManager().authenticate(authentication);

        } catch (Exception ex) {
            log.error(ex.getMessage());
            handleErrorResponse(request, response, ex);
            return null;

        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException {
        var user = (User) authentication.getPrincipal();
        userService.updateLoginAttempt(user.getEmail(), LOGIN_ATTEMPT);
        var httpResponse = user.isMfa() ? sendQrCode(request, user) : sendResponse(request, response, user);
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(OK.value());
        var out = response.getOutputStream();
        var mapper = new ObjectMapper();
        mapper.writeValue(out, httpResponse);
        out.flush();
    }

    private Response sendResponse(HttpServletRequest request, HttpServletResponse response, User user) {
        jwtService.addCookie(response, user, ACCESS);
        jwtService.addCookie(response, user, REFRESH);
        return RequestUtils.getResponseBuilder()
                .request(request)
                .data(Map.of("user", user))
                .message("Login Success")
                .status(OK)
                .build();
    }

    private Response sendQrCode(HttpServletRequest request, User user) {
        return RequestUtils.getResponseBuilder()
                .request(request)
                .data(Map.of("user", user))
                .message("Please enter QR code")
                .status(OK)
                .build();
    }
}
