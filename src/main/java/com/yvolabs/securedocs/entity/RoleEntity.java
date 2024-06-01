package com.yvolabs.securedocs.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yvolabs.securedocs.enumeration.Authority;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 01/06/2024
 */

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class RoleEntity extends Auditable {
    private String name;
    private Authority authorities;
}
