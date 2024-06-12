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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.BiFunction;

import static com.yvolabs.securedocs.constant.Constants.PHOTO_DIRECTORY;
import static com.yvolabs.securedocs.utils.UserUtils.*;
import static com.yvolabs.securedocs.validation.UserValidation.verifyAccountStatus;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
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
        var userEntity = userRepository.findUserByUserId(userId).orElseThrow(() -> new ApiException("User not found with id: " + userId));
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

    @Override
    public void resetPassword(String email) {
        UserEntity userEntity = getUserEntityByEmail(email);
        ConfirmationEntity existingConfirmation = getUserConfirmation(userEntity);
        if (existingConfirmation != null) {
            //send email EVENT using existing confirmation
            publisher.publishEvent(new UserEvent(userEntity, EventType.RESETPASSWORD, Map.of("key", existingConfirmation.getKey())));
        } else {
            // create a new confirmation & send resetPassword email EVENT with new confirmation
            ConfirmationEntity newConfirmation = new ConfirmationEntity(userEntity);
            confirmationRepository.save(newConfirmation);
            publisher.publishEvent(new UserEvent(userEntity, EventType.RESETPASSWORD, Map.of("key", newConfirmation.getKey())));
        }
    }

    @Override
    public User verifyPasswordKey(String key) {
        ConfirmationEntity confirmationEntity = getUserConfirmation(key);
        UserEntity userEntity = getUserEntityByEmail(confirmationEntity.getUserEntity().getEmail());
        verifyAccountStatus(userEntity);
        confirmationRepository.delete(confirmationEntity);
        return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
    }

    @Override
    public void updatePassword(String userId, String newPassword, String confirmNewPassword) {
        if (!confirmNewPassword.equals(newPassword)) {
            throw new ApiException("Passwords don't match. Please try again.");
        }
        UserEntity userEntity = getUserEntityByUserId(userId);
        CredentialEntity credential = getUserCredentialById(userEntity.getId());
        credential.setPassword(encoder.encode(newPassword));
        credentialRepository.save(credential);
    }

    @Override
    public void updatePassword(String userId, String currentPassword, String newPassword, String confirmNewPassword) {
        if (!confirmNewPassword.equals(newPassword)) {
            throw new ApiException("Passwords don't match. Please try again.");
        }
        UserEntity userEntity = getUserEntityByUserId(userId);
        verifyAccountStatus(userEntity);

        CredentialEntity credential = getUserCredentialById(userEntity.getId());
        if (!encoder.matches(currentPassword, credential.getPassword())) {
            throw new ApiException("Existing Password is incorrect. Please try again.");
        }
        credential.setPassword(encoder.encode(newPassword));
        credentialRepository.save(credential);
    }

    @Override
    public User updateUser(String userId, String firstName, String lastName, String email, String phone, String bio) {
        UserEntity userEntity = getUserEntityByUserId(userId);
        userEntity.setFirstName(firstName);
        userEntity.setLastName(lastName);
        userEntity.setEmail(email);
        userEntity.setPhone(phone);
        userEntity.setBio(bio);
        userRepository.save(userEntity);
        return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
    }

    @Override
    public void updateRole(String userId, String role) {
        UserEntity userEntity = getUserEntityByUserId(userId);
        userEntity.setRole(getRoleName(role));
        userRepository.save(userEntity);
    }

    @Override
    public void toggleAccountExpired(String userId) {
        UserEntity userEntity = getUserEntityByUserId(userId);
        userEntity.setAccountNonExpired(!userEntity.isAccountNonExpired());
        userRepository.save(userEntity);

    }

    @Override
    public void toggleAccountLocked(String userId) {
        UserEntity userEntity = getUserEntityByUserId(userId);
        userEntity.setAccountNonLocked(!userEntity.isAccountNonLocked());
        userRepository.save(userEntity);

    }

    @Override
    public void toggleAccountEnabled(String userId) {
        UserEntity userEntity = getUserEntityByUserId(userId);
        userEntity.setEnabled(!userEntity.isEnabled());
        userRepository.save(userEntity);

    }

    @Override
    public void toggleCredentialsExpired(String userId) {
        // Account credentials will be expired if the updatedAt is 90 days after createdAt (@see MappedSuperClass Auditable.class, @see UserUtils::isCredentialsNonExpired)
        //set credential.setUpdatedAt to a past date that is over 90days in the past - in this case we passed the epoch time 01/01/1990
        // this will trigger the logic to expire the credentials
        UserEntity userEntity = getUserEntityByUserId(userId);
        CredentialEntity credential = getUserCredentialById(userEntity.getId());
        credential.setUpdatedAt(LocalDateTime.of(1990, 1, 1, 11, 11));
        credentialRepository.save(credential);
        // * Note: This logic maybe flawed as the Auditable class will override the updated date before saving.
    }

    @Override
    public String uploadPhoto(String userId, MultipartFile file) {
        UserEntity userEntity = getUserEntityByUserId(userId);
        String photoUrl = photoFunction.apply(userId, file);
        userEntity.setImageUrl(photoUrl + "?timestamp=" + System.currentTimeMillis()); // to make the url unique everytime its uploaded, we added timestamp
        userRepository.save(userEntity);
        return photoUrl;
    }

    private final BiFunction<String, MultipartFile, String> photoFunction = (id, file) -> {
        String filename = id + ".png";
        try {
            //STORE FILE: for prod would store in cloud storage =  Eg AWS S3 bucket. This is for local testing only!!!
            Path fileStorageLocation = Paths.get(PHOTO_DIRECTORY).toAbsolutePath().normalize();
            if (!Files.exists(fileStorageLocation)) {
                Files.createDirectories(fileStorageLocation);
            }
            Files.copy(file.getInputStream(), fileStorageLocation.resolve(filename), REPLACE_EXISTING);

            return ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/user/image/" + filename)
                    .toUriString();
        } catch (Exception e) {
            throw new ApiException("Unable to save image " + filename);
        }
    };

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

    private ConfirmationEntity getUserConfirmation(UserEntity userEntity) {
        return confirmationRepository.findByUserEntity(userEntity)
                .orElse(null);
    }

    private UserEntity createNewUser(String firstName, String lastName, String email) {
        RoleEntity role = getRoleName(Authority.USER.name());
        return UserUtils.createUserEntity(firstName, lastName, email, role);
    }


}
