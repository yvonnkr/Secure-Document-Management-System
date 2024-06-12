package com.yvolabs.securedocs.resource;

import com.yvolabs.securedocs.domain.Response;
import com.yvolabs.securedocs.dto.User;
import com.yvolabs.securedocs.dtorequest.*;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static com.yvolabs.securedocs.constant.Constants.PHOTO_DIRECTORY;
import static com.yvolabs.securedocs.utils.RequestUtils.getResponse;
import static java.util.Collections.emptyMap;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

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

    @GetMapping("/profile")
    public ResponseEntity<Response> getUserProfile(@AuthenticationPrincipal User userPrincipal, HttpServletRequest request) {
        User user = userService.getUserByUserId(userPrincipal.getUserId());
        return ResponseEntity.ok().body(getResponse(request, Map.of("user", user), "Profile retrieved", OK));
    }

    @PatchMapping("/profile/update")
    public ResponseEntity<Response> updateUserProfile(@AuthenticationPrincipal User userPrincipal, @RequestBody UserRequest userRequest, HttpServletRequest request) {
        User user = userService.updateUser(userPrincipal.getUserId(), userRequest.getFirstName(), userRequest.getLastName(), userRequest.getEmail(), userRequest.getPhone(), userRequest.getBio());
        return ResponseEntity.ok().body(getResponse(request, Map.of("user", user), "User updated successfully", OK));
    }

    @PatchMapping("/update-role")
    public ResponseEntity<Response> updateRole(@AuthenticationPrincipal User userPrincipal, @RequestBody RoleRequest roleRequest, HttpServletRequest request) {
        userService.updateRole(userPrincipal.getUserId(), roleRequest.getRole());
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Role updated successfully", OK));
    }

    @PatchMapping("/toggle-account-expired")
    public ResponseEntity<Response> toggleAccountExpired(@AuthenticationPrincipal User userPrincipal, HttpServletRequest request) {
        userService.toggleAccountExpired(userPrincipal.getUserId());
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Toggle Account Expired Success", OK));
    }

    @PatchMapping("/toggle-account-locked")
    public ResponseEntity<Response> toggleAccountLocked(@AuthenticationPrincipal User userPrincipal, HttpServletRequest request) {
        userService.toggleAccountLocked(userPrincipal.getUserId());
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Toggle Account Locked Success", OK));
    }

    @PatchMapping("/toggle-account-enabled")
    public ResponseEntity<Response> toggleAccountEnabled(@AuthenticationPrincipal User userPrincipal, HttpServletRequest request) {
        userService.toggleAccountEnabled(userPrincipal.getUserId());
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Toggle Account Enabled Success", OK));
    }

    @PatchMapping("/toggle-credentials-expired")
    public ResponseEntity<Response> toggleCredentialsExpired(@AuthenticationPrincipal User userPrincipal, HttpServletRequest request) {
        userService.toggleCredentialsExpired(userPrincipal.getUserId());
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Toggle Credential Expired Success", OK));
    }

    // START - Reset password endpoint when LOGGED-IN
    @PatchMapping("/update-password")
    public ResponseEntity<Response> updatePassword(@AuthenticationPrincipal User userPrincipal, @RequestBody UpdatePasswordRequest passwordRequest, HttpServletRequest request) {
        userService.updatePassword(userPrincipal.getUserId(), passwordRequest.getCurrentPassword(), passwordRequest.getNewPassword(), passwordRequest.getConfirmNewPassword());
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Password updated successfully", OK));
    }

    @PatchMapping("/upload-photo")
    public ResponseEntity<Response> uploadPhoto(@AuthenticationPrincipal User userPrincipal, @RequestParam("file") MultipartFile file, HttpServletRequest request) {
        String imageUrl = userService.uploadPhoto(userPrincipal.getUserId(), file);
        return ResponseEntity.ok().body(getResponse(request, Map.of("file", imageUrl), "Photo uploaded successfully", OK));
    }

    @GetMapping(value = "/image/{filename}", produces = {IMAGE_PNG_VALUE, IMAGE_JPEG_VALUE})
    public byte[] getPhoto(@PathVariable("filename") String filename) throws IOException {
        return Files.readAllBytes(Paths.get(PHOTO_DIRECTORY + filename));
    }


    private URI getUri() {
        return URI.create("");
    }
}
