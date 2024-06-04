package com.yvolabs.securedocs.resource;

import com.yvolabs.securedocs.domain.Response;
import com.yvolabs.securedocs.dtorequest.UserRequest;
import com.yvolabs.securedocs.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

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

    private URI getUri() {
        return URI.create("");
    }
}
