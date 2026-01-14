package org.orbitalLogistic.mission.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MissionStatus {
    PLANNING, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED;

    @JsonCreator
    public static MissionStatus from(String value) {
        if (value == null ) return null;
        String s = value.trim().toUpperCase();
        for (MissionStatus t : values()) {
            if (t.name().equals(s)) return t;
        }
        throw new IllegalArgumentException("Unknown MissionStatus value: '" + value + "'");
    }

    @JsonValue
    public String toValue() {return name();}
}


