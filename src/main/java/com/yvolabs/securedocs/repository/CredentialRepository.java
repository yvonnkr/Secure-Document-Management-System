package com.yvolabs.securedocs.repository;

import com.yvolabs.securedocs.entity.CredentialEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 03/06/2024
 */
public interface CredentialRepository extends JpaRepository<CredentialEntity, Long> {

    Optional<CredentialEntity> getCredentialByUserEntityId(Long userId);

}
