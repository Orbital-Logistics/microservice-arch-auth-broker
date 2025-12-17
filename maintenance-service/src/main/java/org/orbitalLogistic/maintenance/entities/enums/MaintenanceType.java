package org.orbitalLogistic.maintenance.entities.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MaintenanceType {
    ROUTINE, REPAIR, UPGRADE, INSPECTION;

    @JsonCreator
    public static MaintenanceType from(String value) {
        if (value == null ) return null;
        String s = value.trim().toUpperCase();
        for (MaintenanceType t : values()) {
            if (t.name().equals(s)) return t;
        }
        throw new IllegalArgumentException("Unknown MaintenanceType value: '" + value + "'");
    }

    @JsonValue
    public String toValue() {return name();}
}
