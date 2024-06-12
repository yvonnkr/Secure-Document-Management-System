package com.yvolabs.securedocs.dtorequest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 11/06/2024
 */

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdatePasswordRequest {

    @NotEmpty(message = "Current Password cannot be empty or null")
    private String currentPassword;

    @NotEmpty(message = "New Password cannot be empty or null")
    private String newPassword;

    @NotEmpty(message = "Confirm New Password cannot be empty or null")
    private String confirmNewPassword;
}
