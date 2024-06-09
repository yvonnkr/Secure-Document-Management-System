package com.yvolabs.securedocs.domain;

import com.yvolabs.securedocs.dto.User;
import com.yvolabs.securedocs.exception.ApiException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Collection;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 06/06/2024
 */

@SuppressWarnings("LombokGetterMayBeUsed")
public class ApiAuthentication extends AbstractAuthenticationToken {

    private static final String PASSWORD_PROTECTED = "[PASSWORD PROTECTED]";
    private static final String EMAIL_PROTECTED = "[EMAIL PROTECTED]";

    private User user;
    private final String email;
    private final String password;
    private final boolean authenticated;

    private ApiAuthentication(String email, String password) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.email = email;
        this.password = password;
        this.authenticated = false;
    }

    private ApiAuthentication(User user, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.user = user;
        this.email = EMAIL_PROTECTED;
        this.password = PASSWORD_PROTECTED;
        this.authenticated = true;
    }

    public static AbstractAuthenticationToken unauthenticated(String email, String password) {
        return new ApiAuthentication(email, password);
    }

    public static AbstractAuthenticationToken authenticated(User user, Collection<? extends GrantedAuthority> authorities) {
        return new ApiAuthentication(user, authorities);
    }

    @Override
    public Object getCredentials() {
        return PASSWORD_PROTECTED;
    }

    @Override
    public Object getPrincipal() {
        return this.user;
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        throw new ApiException("you cannot set authentication");
    }

    @Override
    public boolean isAuthenticated() {
        return this.authenticated;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPassword() {
        return this.password;
    }


}
