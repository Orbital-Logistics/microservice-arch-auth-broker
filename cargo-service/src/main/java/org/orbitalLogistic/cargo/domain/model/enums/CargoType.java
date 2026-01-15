package org.orbitalLogistic.cargo.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CargoType {
    FOOD, EQUIPMENT, SCIENTIFIC, CONSTRUCTION_MATERIALS;

    @JsonCreator
    public static CargoType from(String value) {
        if (value == null) return null;
        String s = value.trim().toUpperCase();
        for (CargoType t : values()) {
            if (t.name().equals(s)) return t;
        }
        throw new IllegalArgumentException("Unknown CargoType value: '" + value + "'");
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
