package com.yvolabs.securedocs.event;

import com.yvolabs.securedocs.entity.UserEntity;
import com.yvolabs.securedocs.enumeration.EventType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 02/06/2024
 */

@Getter
@Setter
@AllArgsConstructor
public class UserEvent {

    private UserEntity user;
    private EventType type;
    private Map<?, ?> data;
}
