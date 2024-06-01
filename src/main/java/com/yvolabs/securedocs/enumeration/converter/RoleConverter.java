package com.yvolabs.securedocs.enumeration.converter;

import com.yvolabs.securedocs.enumeration.Authority;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 01/06/2024
 *
 * @apiNote This class converts the Authority Enum From String to Enum and Vice-Versa
 */

@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<Authority, String> {

    @Override
    public String convertToDatabaseColumn(Authority authority) {
        if (authority == null) {
            return null;
        }
        return authority.getValue();
    }

    @Override
    public Authority convertToEntityAttribute(String code) {
        if (code == null) {
            return null;
        }

        return Stream.of(Authority.values())
                .filter((authority -> authority.getValue().equals(code)))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);

    }
}
