package com.yvolabs.securedocs.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 01/06/2024
 *
 * @see Auditable Every entity created will inherit from this class, which has predefined properties and actions
 */

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class UserEntity extends Auditable {

    @Column(updatable = false, unique = true, nullable = false)
    private String userId;

    private String firstName;
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    private Integer loginAttempts;
    private LocalDateTime lastLogin;
    private String phone;
    private String bio;
    private String imageUrl;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean enabled;
    private boolean mfa;

    @JsonIgnore
    private String qrCodeSecret;

    @Column(columnDefinition = "text")
    private String qrCodeImageUri;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"), //ref UserEntity
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id") // ref RoleEntity
    )
    private RoleEntity roles;
}
