package org.orbitalLogistic.inventory.entities.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TransactionType {
    LOAD, UNLOAD, TRANSFER, ADJUSTMENT, CONSUMPTION;

    @JsonCreator
    public static TransactionType from(String value) {
        if (value == null ) return null;
        String s = value.trim().toUpperCase();
        for (TransactionType t : values()) {
            if (t.name().equals(s)) return t;
        }
        throw new IllegalArgumentException("Unknown TransactionType value: '" + value + "'");
    }

    @JsonValue
    public String toValue() {return name();}
}

