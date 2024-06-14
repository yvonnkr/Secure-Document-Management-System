package com.yvolabs.securedocs.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yvolabs.securedocs.domain.RequestContext;
import com.yvolabs.securedocs.exception.ApiException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.AlternativeJdkIdGenerator;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 01/06/2024
 *
 * @apiNote Every entity created will inherit from this class, which has predefined properties and actions
 */

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"createdAt", "updatedAt"}, allowGetters = true)
public class Auditable {

    @Id
    @SequenceGenerator(name = "primary_key_seq", sequenceName = "primary_key_seq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "primary_key_seq")
    @Column(name = "id", updatable = false)
    private Long id;

    private String referenceId = new AlternativeJdkIdGenerator().generateId().toString();

    @NotNull
    private Long createdBy;

    @NotNull
    private Long updatedBy;

    @NotNull
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void beforePersist() {
        //Long userId = 0L; // set as system user_id 0 temporally for testing
        Long userId = RequestContext.getUserId();
        if (userId == null) {
            throw new ApiException("Cannot persist entity without user ID in Request Context for this thread");
        }

        setCreatedAt(now());
        setCreatedBy(userId);
        setUpdatedBy(userId);
        setUpdatedAt(now());
    }

    @PreUpdate
    public void beforeUpdate() {
        //Long userId = 0L; // set as system user_id 0 temporally for testing
        Long userId = RequestContext.getUserId();
        if (userId == null) {
            throw new ApiException("Cannot update entity without user ID in Request Context for this thread");
        }

        setUpdatedAt(now());
        setUpdatedBy(userId);
    }

}
