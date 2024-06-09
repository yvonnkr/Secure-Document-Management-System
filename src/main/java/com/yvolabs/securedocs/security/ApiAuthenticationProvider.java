package com.yvolabs.securedocs.security;

import com.yvolabs.securedocs.domain.ApiAuthentication;
import com.yvolabs.securedocs.domain.UserPrincipal;
import com.yvolabs.securedocs.exception.ApiException;
import com.yvolabs.securedocs.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;
import java.util.function.Function;

import static com.yvolabs.securedocs.domain.ApiAuthentication.authenticated;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 05/06/2024
 *@apiNote This is a custom {@code AuthenticationProvider},its like equivalent to {@link DaoAuthenticationProvider}
 *@implNote An {@link AuthenticationProvider} implementation that retrieves user details from a {@link UserDetailsService}
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApiAuthenticationProvider implements AuthenticationProvider {
    private final UserService userService;
    private final BCryptPasswordEncoder encoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var apiAuthentication = authenticationFunction.apply(authentication);
        var user = userService.getUserByEmail(apiAuthentication.getEmail());

        if (user != null) {
            var userCredential = userService.getUserCredentialById(user.getId());
            if (!user.isCredentialsNonExpired())
                throw new ApiException("Credentials are expired. Please reset your password");
            var userPrincipal = new UserPrincipal(user, userCredential);

            validAccount.accept(userPrincipal);

            boolean passwordMatches = encoder.matches(apiAuthentication.getPassword(), userCredential.getPassword());
            if (passwordMatches) {
                return authenticated(user, userPrincipal.getAuthorities());
            } else throw new BadCredentialsException("Email and/or password incorrect. Please try again");

        }
        throw new ApiException("Unable to authenticate");

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return ApiAuthentication.class.isAssignableFrom(authentication);
    }

    private final Function<Authentication, ApiAuthentication> authenticationFunction = (authentication) -> (ApiAuthentication) authentication;

    private final Consumer<UserPrincipal> validAccount = (userPrincipal) -> {
        if (!userPrincipal.isAccountNonLocked()) {
            throw new LockedException("Your account is currently locked");
        }
        if (!userPrincipal.isEnabled()) {
            throw new DisabledException("Your account is currently disabled");
        }
        if (!userPrincipal.isCredentialsNonExpired()) {
            throw new CredentialsExpiredException("Your password has expired. Please update your password");
        }
        if (!userPrincipal.isAccountNonExpired()) {
            throw new DisabledException("Your account has expired. Please contact administrator");
        }
    };
}
