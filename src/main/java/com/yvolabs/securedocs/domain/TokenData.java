package com.yvolabs.securedocs.domain;

import com.yvolabs.securedocs.dto.User;
import io.jsonwebtoken.Claims;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 07/06/2024
 */

@Builder
@Getter
@Setter
public class TokenData {
    private User user;
    private Claims claims;
    private boolean valid;
    private List<GrantedAuthority> authorities;

}
