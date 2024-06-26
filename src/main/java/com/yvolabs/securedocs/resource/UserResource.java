package com.yvolabs.securedocs.resource;

import com.yvolabs.securedocs.domain.Response;
import com.yvolabs.securedocs.dto.User;
import com.yvolabs.securedocs.dtorequest.*;
import com.yvolabs.securedocs.enumeration.TokenType;
import com.yvolabs.securedocs.handler.ApiLogoutHandler;
import com.yvolabs.securedocs.service.JwtService;
import com.yvolabs.securedocs.service.UserService;
import com.yvolabs.securedocs.utils.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.yvolabs.securedocs.constant.Constants.FILE_STORAGE_DIRECTORY;
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
    private final ApiLogoutHandler apiLogoutHandler;

    @PostMapping("/register")
    public ResponseEntity<Response> saveUser(@RequestBody @Valid UserRequest user, HttpServletRequest request) {

        userService.createUser(user.getFirstName(), user.getLastName(), user.getEmail(), user.getPassword());

        return ResponseEntity.created(getUri())
                .body(getResponse(request, emptyMap(), "Account created. Check your email to enable your account", CREATED));
    }

    @GetMapping("/verify/account")
    public ResponseEntity<Response> verifyUser(@RequestParam("key") String key, HttpServletRequest request) throws InterruptedException {
        TimeUnit.SECONDS.sleep(3);// simulate verification delay
        userService.verifyAccountKey(key);
        return ResponseEntity.ok(getResponse(request, emptyMap(), "Account verified", OK));
    }

    @PatchMapping("/mfa/setup")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN','SUPER_ADMIN')")
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
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN','SUPER_ADMIN')")
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

    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> getUsers(@AuthenticationPrincipal User userPrincipal, HttpServletRequest request) {
        List<User> users = userService.getUsers();
        return ResponseEntity.ok(getResponse(request, Map.of("users", users), "Users retrieved", OK));

    }

    @GetMapping("/profile")
    @PreAuthorize("hasAnyAuthority('user:read') or hasAnyRole('USER','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> getUserProfile(@AuthenticationPrincipal User userPrincipal, HttpServletRequest request) {
        User user = userService.getUserByUserId(userPrincipal.getUserId());
        return ResponseEntity.ok().body(getResponse(request, Map.of("user", user), "Profile retrieved", OK));
    }

    @PatchMapping("/profile/update")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> updateUserProfile(@AuthenticationPrincipal User userPrincipal, @RequestBody UserRequest userRequest, HttpServletRequest request) {
        User user = userService.updateUser(userPrincipal.getUserId(), userRequest.getFirstName(), userRequest.getLastName(), userRequest.getEmail(), userRequest.getPhone(), userRequest.getBio());
        return ResponseEntity.ok().body(getResponse(request, Map.of("user", user), "User updated successfully", OK));
    }

    @PatchMapping("/update-role")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> updateRole(@AuthenticationPrincipal User userPrincipal, @RequestBody RoleRequest roleRequest, HttpServletRequest request) {
        userService.updateRole(userPrincipal.getUserId(), roleRequest.getRole());
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Role updated successfully", OK));
    }

    @PatchMapping("/toggle-account-expired")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> toggleAccountExpired(@AuthenticationPrincipal User userPrincipal, HttpServletRequest request) {
        userService.toggleAccountExpired(userPrincipal.getUserId());
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Toggle Account Expired Success", OK));
    }

    @PatchMapping("/toggle-account-locked")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> toggleAccountLocked(@AuthenticationPrincipal User userPrincipal, HttpServletRequest request) {
        userService.toggleAccountLocked(userPrincipal.getUserId());
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Toggle Account Locked Success", OK));
    }

    @PatchMapping("/toggle-account-enabled")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> toggleAccountEnabled(@AuthenticationPrincipal User userPrincipal, HttpServletRequest request) {
        userService.toggleAccountEnabled(userPrincipal.getUserId());
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Toggle Account Enabled Success", OK));
    }

    @PatchMapping("/toggle-credentials-expired")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> toggleCredentialsExpired(@AuthenticationPrincipal User userPrincipal, HttpServletRequest request) {
        userService.toggleCredentialsExpired(userPrincipal.getUserId());
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Toggle Credential Expired Success", OK));
    }

    // START - Reset password endpoint when LOGGED-IN
    @PatchMapping("/update-password")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> updatePassword(@AuthenticationPrincipal User userPrincipal, @RequestBody UpdatePasswordRequest passwordRequest, HttpServletRequest request) {
        userService.updatePassword(userPrincipal.getUserId(), passwordRequest.getCurrentPassword(), passwordRequest.getNewPassword(), passwordRequest.getConfirmNewPassword());
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Password updated successfully", OK));
    }

    @PatchMapping("/upload-photo")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> uploadPhoto(@AuthenticationPrincipal User userPrincipal, @RequestParam("file") MultipartFile file, HttpServletRequest request) {
        String imageUrl = userService.uploadPhoto(userPrincipal.getUserId(), file);
        return ResponseEntity.ok().body(getResponse(request, Map.of("file", imageUrl), "Photo uploaded successfully", OK));
    }

    @GetMapping(value = "/image/{filename}", produces = {IMAGE_PNG_VALUE, IMAGE_JPEG_VALUE})
    public byte[] getPhoto(@PathVariable("filename") String filename) throws IOException {
        return Files.readAllBytes(Paths.get(FILE_STORAGE_DIRECTORY + filename));
    }

    @PostMapping("/logout")
    public ResponseEntity<Response> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        apiLogoutHandler.logout(request, response, authentication);
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "You've logged out successfully", OK));
    }


    private URI getUri() {
        return URI.create("");
    }
}
