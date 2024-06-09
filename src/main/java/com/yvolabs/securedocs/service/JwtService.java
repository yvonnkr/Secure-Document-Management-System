package com.yvolabs.securedocs.service;

import com.yvolabs.securedocs.domain.Token;
import com.yvolabs.securedocs.domain.TokenData;
import com.yvolabs.securedocs.dto.User;
import com.yvolabs.securedocs.enumeration.TokenType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;
import java.util.function.Function;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 07/06/2024
 */
public interface JwtService {
    String createToken(User user, Function<Token, String> tokenFunction);

    Optional<String> extractToken(HttpServletRequest request, String tokenType);

    void addCookie(HttpServletResponse response, User user, TokenType type);

    <T> T getTokenData(String token, Function<TokenData, T> tokenFunction);

    void removeCookie(HttpServletRequest request, HttpServletResponse response, String cookieName);
}
