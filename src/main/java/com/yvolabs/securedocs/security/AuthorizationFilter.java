package com.yvolabs.securedocs.security;

import com.yvolabs.securedocs.domain.RequestContext;
import com.yvolabs.securedocs.domain.Token;
import com.yvolabs.securedocs.domain.TokenData;
import com.yvolabs.securedocs.dto.User;
import com.yvolabs.securedocs.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.Arrays;
import java.util.List;

import static com.yvolabs.securedocs.constant.Constants.PUBLIC_ROUTES;
import static com.yvolabs.securedocs.domain.ApiAuthentication.authenticated;
import static com.yvolabs.securedocs.enumeration.TokenType.ACCESS;
import static com.yvolabs.securedocs.enumeration.TokenType.REFRESH;
import static com.yvolabs.securedocs.utils.RequestUtils.handleErrorResponse;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 08/06/2024
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthorizationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        try {

            var accessToken = jwtService.extractToken(request, ACCESS.getValue());
            if (accessToken.isPresent() && jwtService.getTokenData(accessToken.get(), TokenData::isValid)) {
                SecurityContextHolder.getContext().setAuthentication(getAuthentication(accessToken.get(), request));
                RequestContext.setUserId(jwtService.getTokenData(accessToken.get(), TokenData::getUser).getId());
            } else {
                var refreshToken = jwtService.extractToken(request, REFRESH.getValue());
                if (refreshToken.isPresent() && jwtService.getTokenData(refreshToken.get(), TokenData::isValid)) {
                    var user = jwtService.getTokenData(refreshToken.get(), TokenData::getUser);
                    SecurityContextHolder.getContext().setAuthentication(getAuthentication(jwtService.createToken(user, Token::getAccess), request));
                    jwtService.addCookie(response, user, ACCESS);
                    RequestContext.setUserId(user.getId());
                } else {
                    SecurityContextHolder.clearContext();
                }
            }

            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            log.error(exception.getMessage());
            handleErrorResponse(request, response, exception);

        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        boolean shouldNotFilter = request.getMethod().equalsIgnoreCase(HttpMethod.OPTIONS.name()) || Arrays.asList(PUBLIC_ROUTES).contains(request.getRequestURI());
        if (shouldNotFilter) {
            RequestContext.setUserId(0L); //system
        }
        return shouldNotFilter;
    }

    private Authentication getAuthentication(String token, HttpServletRequest request) {
        User user = jwtService.getTokenData(token, TokenData::getUser);
        List<GrantedAuthority> authorities = jwtService.getTokenData(token, TokenData::getAuthorities);
        var authentication = authenticated(user, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authentication;

    }
}
