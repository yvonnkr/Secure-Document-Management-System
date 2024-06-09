package com.yvolabs.securedocs.service;

import com.yvolabs.securedocs.cache.CacheStore;
import com.yvolabs.securedocs.entity.ConfirmationEntity;
import com.yvolabs.securedocs.entity.CredentialEntity;
import com.yvolabs.securedocs.entity.RoleEntity;
import com.yvolabs.securedocs.entity.UserEntity;
import com.yvolabs.securedocs.enumeration.Authority;
import com.yvolabs.securedocs.enumeration.EventType;
import com.yvolabs.securedocs.event.UserEvent;
import com.yvolabs.securedocs.repository.ConfirmationRepository;
import com.yvolabs.securedocs.repository.CredentialRepository;
import com.yvolabs.securedocs.repository.RoleRepository;
import com.yvolabs.securedocs.repository.UserRepository;
import com.yvolabs.securedocs.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 07/06/2024
 */

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private CredentialRepository credentialRepository;
    @Mock
    private ConfirmationRepository confirmationRepository;
    @Mock
    private BCryptPasswordEncoder encoder;
    @Mock
    private ApplicationEventPublisher publisher;
    @Mock
    private CacheStore<String, Integer> userCache;

    @InjectMocks
    private UserServiceImpl userServiceImpl;

    @Test
    @DisplayName("Test find user by ID")
    public void getUserByUserIdTest() {
        // Arrange - Given
        var userEntity = getUserEntity();
        var credentialEntity = getCredentialEntity();
        when(userRepository.findUserByUserId("1")).thenReturn(Optional.of(userEntity));
        when(credentialRepository.getCredentialByUserEntityId(1L)).thenReturn(Optional.of(credentialEntity));

        var userByUserId = userServiceImpl.getUserByUserId("1");

        assertThat(userByUserId.getFirstName()).isEqualTo(userEntity.getFirstName());
        assertThat(userByUserId.getUserId()).isEqualTo("1");
    }

    @Test
    public void createUserTest() {
        when(userRepository.save(any(UserEntity.class))).thenReturn(getUserEntity());
        when(encoder.encode(any(String.class))).thenReturn("123Encoded");
        when(roleRepository.findByNameIgnoreCase(any(String.class))).thenReturn(Optional.of(getRoleEntity()));
        when(credentialRepository.save(any(CredentialEntity.class))).thenReturn(getCredentialEntity());
        when(confirmationRepository.save(any(ConfirmationEntity.class))).thenReturn(getConfirmationEntity());
        doNothing().when(publisher).publishEvent(any(UserEvent.class));

        userServiceImpl.createUser("john", "doe", "john@test.com", "123456");

        verify(userRepository).save(any(UserEntity.class));
        verify(encoder).encode(any(String.class));
        verify(roleRepository).findByNameIgnoreCase(any(String.class));
        verify(credentialRepository).save(any(CredentialEntity.class));
        verify(confirmationRepository).save(any(ConfirmationEntity.class));
        verify(publisher).publishEvent(any(UserEvent.class));

    }

    @Test
    public void verifyAccountKeyTest() {
        when(userRepository.findByEmailIgnoreCase(any(String.class))).thenReturn(Optional.of(getUserEntity()));
        when(userRepository.save(any(UserEntity.class))).thenReturn(getUserEntity());
        when(confirmationRepository.findByKey(anyString())).thenReturn(Optional.of(getConfirmationEntity()));
        doNothing().when(confirmationRepository).delete(any(ConfirmationEntity.class));
        userServiceImpl.verifyAccountKey("john");

        verify(userRepository).findByEmailIgnoreCase(any(String.class));
        verify(confirmationRepository).delete(any(ConfirmationEntity.class));
        verify(userRepository).save(any(UserEntity.class));
        verify(confirmationRepository).delete(any(ConfirmationEntity.class));
    }

    private UserEvent getUserEvent() {
        return UserEvent.builder()
                .user(getUserEntity())
                .type(EventType.REGISTRATION)
                .data(Map.of("key", "someConfirmationKey"))
                .build();
    }

    private ConfirmationEntity getConfirmationEntity() {
        return ConfirmationEntity.builder()
                .key("someKey")
                .userEntity(getUserEntity())
                .build();
    }

    private UserEntity getUserEntity() {

        var userEntity = UserEntity.builder()
                .userId("1")
                .firstName("john")
                .lastName("doe")
                .email("john@test.com")
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
                .role(getRoleEntity())
                .build();
        userEntity.setId(1L);
        userEntity.setCreatedAt(LocalDateTime.of(1990, 11, 1, 1, 11, 11));
        userEntity.setUpdatedAt(LocalDateTime.of(1990, 11, 1, 1, 11, 11));
        userEntity.setLastLogin(LocalDateTime.of(1990, 11, 1, 1, 11, 11));

        return userEntity;

    }

    private RoleEntity getRoleEntity() {
        return RoleEntity.builder()
                .name(Authority.USER.name())
                .authorities(Authority.USER)
                .build();
    }

    private CredentialEntity getCredentialEntity() {
        var credentialEntity = CredentialEntity.builder()
                .password("123encoded")
                .userEntity(getUserEntity())
                .build();
        credentialEntity.setUpdatedAt(LocalDateTime.of(1990, 11, 1, 1, 11, 11));
        return credentialEntity;
    }


}