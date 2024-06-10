package com.yvolabs.securedocs.service.impl;

import com.yvolabs.securedocs.cache.CacheStore;
import com.yvolabs.securedocs.domain.RequestContext;
import com.yvolabs.securedocs.dto.User;
import com.yvolabs.securedocs.entity.ConfirmationEntity;
import com.yvolabs.securedocs.entity.CredentialEntity;
import com.yvolabs.securedocs.entity.RoleEntity;
import com.yvolabs.securedocs.entity.UserEntity;
import com.yvolabs.securedocs.enumeration.Authority;
import com.yvolabs.securedocs.enumeration.EventType;
import com.yvolabs.securedocs.enumeration.LoginType;
import com.yvolabs.securedocs.event.UserEvent;
import com.yvolabs.securedocs.exception.ApiException;
import com.yvolabs.securedocs.repository.ConfirmationRepository;
import com.yvolabs.securedocs.repository.CredentialRepository;
import com.yvolabs.securedocs.repository.RoleRepository;
import com.yvolabs.securedocs.repository.UserRepository;
import com.yvolabs.securedocs.service.UserService;
import com.yvolabs.securedocs.utils.UserUtils;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

import static com.yvolabs.securedocs.utils.UserUtils.*;
import static org.apache.commons.lang3.StringUtils.EMPTY;


/**
 * @author Yvonne N
 * @version 1.0
 * @since 03/06/2024
 */

@Service
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CredentialRepository credentialRepository;
    private final ConfirmationRepository confirmationRepository;
    private final BCryptPasswordEncoder encoder;
    private final ApplicationEventPublisher publisher;
    private final CacheStore<String, Integer> userCache;


    @Override
    public void createUser(String firstName, String lastName, String email, String password) {

        UserEntity userEntity = userRepository.save(createNewUser(firstName, lastName, email));

        CredentialEntity credentialEntity = new CredentialEntity(userEntity, encoder.encode(password));
        credentialRepository.save(credentialEntity);

        ConfirmationEntity confirmationEntity = new ConfirmationEntity(userEntity);
        confirmationRepository.save(confirmationEntity);

        // send registration email event
        UserEvent userEvent = new UserEvent(
                userEntity,
                EventType.REGISTRATION,
                Map.of("key", confirmationEntity.getKey())
        );
        publisher.publishEvent(userEvent);
    }

    @Override
    public RoleEntity getRoleName(String name) {
        return roleRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ApiException("Role not found"));
    }

    @Override
    public void verifyAccountKey(String key) {

        ConfirmationEntity confirmationEntity = getUserConfirmation(key);
        UserEntity userEntity = getUserEntityByEmail(confirmationEntity.getUserEntity().getEmail());

        userEntity.setEnabled(true);
        userRepository.save(userEntity);

        confirmationRepository.delete(confirmationEntity);
    }

    /**
     * This method is being called in our {@code ApiAuthentication::attemptAuthentication} not the controller
     */
    @Override
    public void updateLoginAttempt(String email, LoginType loginType) {

        UserEntity userEntity = getUserEntityByEmail(email);
        RequestContext.setUserId(userEntity.getId());

        switch (loginType) {
            case LOGIN_ATTEMPT -> {
                if (userCache.get(userEntity.getEmail()) == null) {
                    userEntity.setLoginAttempts(0);
                    userEntity.setAccountNonLocked(true);
                }
                userEntity.setLoginAttempts(userEntity.getLoginAttempts() + 1);
                userCache.put(userEntity.getEmail(), userEntity.getLoginAttempts());

                if (userCache.get(userEntity.getEmail()) > 5) {
                    userEntity.setAccountNonLocked(false);
                }
            }

            case LOGIN_SUCCESS -> {
                userEntity.setAccountNonLocked(true);
                userEntity.setLoginAttempts(0);
                userEntity.setLastLogin(LocalDateTime.now());
                userCache.evict(userEntity.getEmail());
            }
        }

        userRepository.save(userEntity);

    }

    @Override
    public User getUserByUserId(String userId) {
        var userEntity = userRepository.findUserByUserId(userId).orElseThrow(() -> new ApiException("User not found by id: " + userId));
        return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
    }

    @Override
    public User getUserByEmail(String email) {
        UserEntity userEntity = getUserEntityByEmail(email);
        return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
    }

    @Override
    public CredentialEntity getUserCredentialById(Long userId) {
        return credentialRepository.getCredentialByUserEntityId(userId)
                .orElseThrow(() -> new ApiException("Unable to find user credential by id: " + userId));
    }

    @Override
    public User setUpMfa(Long id) {
        UserEntity userEntity = getUserEntityById(id);
        String codeSecret = qrCodeSecret.get();
        userEntity.setQrCodeImageUri(qrCodeImageUri.apply(userEntity.getEmail(), codeSecret));
        userEntity.setQrCodeSecret(codeSecret);
        userEntity.setMfa(true);
        userRepository.save(userEntity);
        return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
    }

    @Override
    public User cancelMfa(Long id) {
        UserEntity userEntity = getUserEntityById(id);
        userEntity.setMfa(false);
        userEntity.setQrCodeSecret(EMPTY);
        userEntity.setQrCodeImageUri(EMPTY);
        userRepository.save(userEntity);
        return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
    }

    @Override
    public User verifyQrCode(String userId, String qrCode) {
        UserEntity userEntity = getUserEntityByUserId(userId);
        boolean isVerified = verifyCode(qrCode, userEntity.getQrCodeSecret());
        log.info("isQrCodeVerified : {}", isVerified);
        return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));

    }

    /**
     *
     * @param qrCode is the code received by user after scanning the qrCode, usually some digits
     * @param qrCodeSecret is the qrSecret saved during setup
     * @return a boolean, throws if verification failed
     */
    private boolean verifyCode(String qrCode, String qrCodeSecret) {
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        CodeVerifier codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
        if (codeVerifier.isValidCode(qrCodeSecret, qrCode)) {
            return true;
        } else {
            throw new ApiException("Invalid QR Code. Please try again");
        }

    }

    private UserEntity getUserEntityByUserId(String userId) {
        return userRepository.findUserByUserId(userId).orElseThrow(() -> new ApiException("User not found by userId: " + userId));
    }

    private UserEntity getUserEntityById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ApiException("User not found by id: " + id));
    }

    private UserEntity getUserEntityByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException("User not found by email: " + email));
    }

    private ConfirmationEntity getUserConfirmation(String key) {
        return confirmationRepository.findByKey(key)
                .orElseThrow(() -> new ApiException("Confirmation key not found"));
    }

    private UserEntity createNewUser(String firstName, String lastName, String email) {
        RoleEntity role = getRoleName(Authority.USER.name());
        return UserUtils.createUserEntity(firstName, lastName, email, role);
    }


}
