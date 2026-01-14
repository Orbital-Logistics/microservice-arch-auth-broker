package org.orbitalLogistic.mission.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MissionPriority {
    LOW, MEDIUM, HIGH, CRITICAL;

    @JsonCreator
    public static MissionPriority from(String value) {
        if (value == null ) return null;
        String s = value.trim().toUpperCase();
        for (MissionPriority t : values()) {
            if (t.name().equals(s)) return t;
        }
        throw new IllegalArgumentException("Unknown MissionPriority value: '" + value + "'");
    }

    @JsonValue
    public String toValue() {return name();}
}

