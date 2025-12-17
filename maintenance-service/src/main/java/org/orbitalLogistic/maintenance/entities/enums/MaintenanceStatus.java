package org.orbitalLogistic.maintenance.entities.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MaintenanceStatus {
    SCHEDULED, IN_PROGRESS, COMPLETED;

    @JsonCreator
    public static MaintenanceStatus from(String value) {
        if (value == null ) return null;
        String s = value.trim().toUpperCase();
        for (MaintenanceStatus t : values()) {
            if (t.name().equals(s)) return t;
        }
        throw new IllegalArgumentException("Unknown MaintenanceStatus value: '" + value + "'");
    }

    @JsonValue
    public String toValue() {return name();}
}
