package com.yvolabs.securedocs.resource;

import com.yvolabs.securedocs.domain.Response;
import com.yvolabs.securedocs.dto.User;
import com.yvolabs.securedocs.dtorequest.EmailRequest;
import com.yvolabs.securedocs.dtorequest.QrCodeRequest;
import com.yvolabs.securedocs.dtorequest.ResetPasswordRequest;
import com.yvolabs.securedocs.dtorequest.UserRequest;
import com.yvolabs.securedocs.enumeration.TokenType;
import com.yvolabs.securedocs.service.JwtService;
import com.yvolabs.securedocs.service.UserService;
import com.yvolabs.securedocs.utils.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

import static com.yvolabs.securedocs.utils.RequestUtils.getResponse;
import static java.util.Collections.emptyMap;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 03/06/2024
 */

@RestController
@RequiredArgsConstructor
@RequestMapping(path = {"/user"})
public class UserResource {

    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<Response> saveUser(@RequestBody @Valid UserRequest user, HttpServletRequest request) {

        userService.createUser(user.getFirstName(), user.getLastName(), user.getEmail(), user.getPassword());

        return ResponseEntity.created(getUri())
                .body(getResponse(request, emptyMap(), "Account created. Check your email to enable your account", CREATED));
    }

    @GetMapping("/verify/account")
    public ResponseEntity<Response> verifyUser(@RequestParam("key") String key, HttpServletRequest request) {

        userService.verifyAccountKey(key);

        return ResponseEntity.ok()
                .body(getResponse(request, emptyMap(), "Account verified", OK));
    }

    @PatchMapping("/mfa/setup")
    public ResponseEntity<Response> setupMfa(@AuthenticationPrincipal User userPrincipal, HttpServletRequest request) {
        User user = userService.setUpMfa(userPrincipal.getId());
        Response response = RequestUtils.getResponseBuilder()
                .request(request)
                .data(Map.of("user", user))
                .message("MFA set up successfully")
                .status(OK)
                .build();
        return ResponseEntity.ok(response);

    }

    @PatchMapping("/mfa/cancel")
    public ResponseEntity<Response> cancelMfa(@AuthenticationPrincipal User userPrincipal, HttpServletRequest request) {
        User user = userService.cancelMfa(userPrincipal.getId());
        Response response = RequestUtils.getResponseBuilder()
                .request(request)
                .data(Map.of("user", user))
                .message("MFA canceled successfully")
                .status(OK)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify/qrcode")
    public ResponseEntity<Response> verifyQrcode(@RequestBody QrCodeRequest qrCodeRequest, HttpServletRequest request, HttpServletResponse response) {
        // verify qrCode
        User user = userService.verifyQrCode(qrCodeRequest.getUserId(), qrCodeRequest.getQrCode());

        // after verifying the qrCode successfully, then setting the cookies logs in the user
        jwtService.addCookie(response, user, TokenType.ACCESS);
        jwtService.addCookie(response, user, TokenType.REFRESH);

        // json response
        Response res = RequestUtils.getResponseBuilder()
                .request(request)
                .data(Map.of("user", user))
                .message("QR Code verified")
                .status(OK)
                .build();
        return ResponseEntity.ok(res);
    }

    // START - Reset password endpoints flow when NOT LOGGED-IN
    @PostMapping("/resetpassword")
    public ResponseEntity<Response> resetPassword(@RequestBody @Valid EmailRequest emailRequest, HttpServletRequest request) {
        userService.resetPassword(emailRequest.getEmail());
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "We sent you an email to reset your password", OK));
    }

    @GetMapping("/verify/password")
    public ResponseEntity<Response> verifyPassword(@RequestParam("key") String key, HttpServletRequest request) {
        User user = userService.verifyPasswordKey(key);
        return ResponseEntity.ok().body(getResponse(request, Map.of("user", user), "Enter new password", OK));
    }

    @PostMapping("/resetpassword/reset")
    public ResponseEntity<Response> doResetPassword(@RequestBody @Valid ResetPasswordRequest resetPasswordRequest, HttpServletRequest request) {
        userService.updatePassword(resetPasswordRequest.getUserId(), resetPasswordRequest.getNewPassword(), resetPasswordRequest.getConfirmNewPassword());
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Password reset successfully", OK));
    }
    // END - Reset password endpoints flow when NOT LOGGED-IN


    private URI getUri() {
        return URI.create("");
    }
}
