package com.yvolabs.securedocs.service;

import com.yvolabs.securedocs.dto.User;
import com.yvolabs.securedocs.entity.CredentialEntity;
import com.yvolabs.securedocs.entity.RoleEntity;
import com.yvolabs.securedocs.enumeration.LoginType;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 03/06/2024
 */

public interface UserService {

    void createUser(String firstName, String lastName, String email, String password);

    RoleEntity getRoleName(String name);

    void verifyAccountKey(String key);

    void updateLoginAttempt(String email, LoginType loginType);

    User getUserByUserId(String userId);

    User getUserByEmail(String email);

    CredentialEntity getUserCredentialById(Long userId);

    User setUpMfa(Long id);

    User cancelMfa(Long id);

    User verifyQrCode(String userId, String qrCode);

    void resetPassword(String email);

    User verifyPasswordKey(String key);

    void updatePassword(String userId, String newPassword, String confirmNewPassword);

    void updatePassword(String userId, String currentPassword, String newPassword, String confirmNewPassword);

    User updateUser(String userId, String firstName, String lastName, String email, String phone, String bio);

    void updateRole(String userId, String role);

    void toggleAccountExpired(String userId);

    void toggleAccountLocked(String userId);

    void toggleAccountEnabled(String userId);

    void toggleCredentialsExpired(String userId);

    String uploadPhoto(String userId, MultipartFile file);
}
