package com.yvolabs.securedocs.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 07/06/2024
 */

@Builder
@Getter
@Setter
public class Token {
    private String access;
    private String refresh;
}
