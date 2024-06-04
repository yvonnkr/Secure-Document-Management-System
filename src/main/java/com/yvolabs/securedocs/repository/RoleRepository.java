package com.yvolabs.securedocs.repository;

import com.yvolabs.securedocs.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 03/06/2024
 */

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByNameIgnoreCase(String name);
}
