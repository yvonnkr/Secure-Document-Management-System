package com.yvolabs.securedocs.dtorequest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 10/06/2024
 */

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResetPasswordRequest {

    @NotEmpty(message = "User id cannot be empty or null")
    private String userId;

    @NotEmpty(message = "Password cannot be empty or null")
    private String newPassword;

    @NotEmpty(message = "Confirm Password cannot be empty or null")
    private String confirmNewPassword;
}
