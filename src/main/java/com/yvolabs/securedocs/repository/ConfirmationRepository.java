package com.yvolabs.securedocs.repository;

import com.yvolabs.securedocs.entity.ConfirmationEntity;
import com.yvolabs.securedocs.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 03/06/2024
 */

public interface ConfirmationRepository extends JpaRepository<ConfirmationEntity, Long> {

    Optional<ConfirmationEntity> findByKey(String key);

    Optional<ConfirmationEntity> findByUserEntity(UserEntity userEntity);
}
