package com.yvolabs.securedocs.utils;

import com.yvolabs.securedocs.dto.User;
import com.yvolabs.securedocs.entity.CredentialEntity;
import com.yvolabs.securedocs.entity.RoleEntity;
import com.yvolabs.securedocs.entity.UserEntity;
import org.springframework.beans.BeanUtils;

import java.util.UUID;

import static com.yvolabs.securedocs.constant.Constants.NINETY_DAYS;
import static java.time.LocalDateTime.now;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 03/06/2024
 */

public class UserUtils {

    public static UserEntity createUserEntity(String firstName, String lastName, String email, RoleEntity role) {

        return UserEntity.builder()
                .userId(UUID.randomUUID().toString())
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .loginAttempts(0)
                .lastLogin(now())
                .accountNonExpired(true)
                .accountNonLocked(true)
                .enabled(false)
                .mfa(false)
                .qrCodeSecret(EMPTY)
                .phone(EMPTY)
                .bio(EMPTY)
                .imageUrl("https://cdn-icons-png.flaticon.com/512/149/149071.png")
                .role(role)
                .build();
    }

    public static User fromUserEntity(UserEntity userEntity, RoleEntity role, CredentialEntity credentialEntity) {
        User user = new User();
        BeanUtils.copyProperties(userEntity, user);
        user.setLastLogin(userEntity.getLastLogin().toString());
        user.setCredentialsNonExpired(isCredentialsNonExpired(credentialEntity));
        user.setCreatedAt(userEntity.getCreatedAt().toString());
        user.setUpdatedAt(userEntity.getUpdatedAt().toString());
        user.setRole(role.getName());
        user.setAuthorities(role.getAuthorities().getValue());
        return user;
    }

    public static boolean isCredentialsNonExpired(CredentialEntity credentialEntity) {
        return credentialEntity.getUpdatedAt().plusDays(NINETY_DAYS).isAfter(now());
    }
}
