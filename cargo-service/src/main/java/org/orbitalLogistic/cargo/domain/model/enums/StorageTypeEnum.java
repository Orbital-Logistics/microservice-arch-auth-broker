package org.orbitalLogistic.cargo.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum StorageTypeEnum {
    AMBIENT, REFRIGERATED, PRESSURIZED, HAZMAT;

    @JsonCreator
    public static StorageTypeEnum from(String value) {
        if (value == null) return null;
        String s = value.trim().toUpperCase();
        for (StorageTypeEnum t : values()) {
            if (t.name().equals(s)) return t;
        }
        throw new IllegalArgumentException("Unknown StorageTypeEnum value: '" + value + "'");
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
