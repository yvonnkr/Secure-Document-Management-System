package com.yvolabs.securedocs.service.impl;

import com.yvolabs.securedocs.entity.ConfirmationEntity;
import com.yvolabs.securedocs.entity.CredentialEntity;
import com.yvolabs.securedocs.entity.RoleEntity;
import com.yvolabs.securedocs.entity.UserEntity;
import com.yvolabs.securedocs.enumeration.Authority;
import com.yvolabs.securedocs.enumeration.EventType;
import com.yvolabs.securedocs.event.UserEvent;
import com.yvolabs.securedocs.exception.ApiException;
import com.yvolabs.securedocs.repository.ConfirmationRepository;
import com.yvolabs.securedocs.repository.CredentialRepository;
import com.yvolabs.securedocs.repository.RoleRepository;
import com.yvolabs.securedocs.repository.UserRepository;
import com.yvolabs.securedocs.service.UserService;
import com.yvolabs.securedocs.utils.UserUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Map;


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
    //    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher publisher;


    @Override
    public void createUser(String firstName, String lastName, String email, String password) {

        UserEntity userEntity = userRepository.save(createNewUser(firstName, lastName, email));

        CredentialEntity credentialEntity = new CredentialEntity(userEntity, password); //todo encode password
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

        assert userEntity != null;
        userEntity.setEnabled(true);
        userRepository.save(userEntity);

        confirmationRepository.delete(confirmationEntity);
    }

    private UserEntity getUserEntityByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException("User not found"));
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
