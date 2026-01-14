package org.orbitalLogistic.mission.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MissionType {
    CARGO_TRANSPORT, PERSONNEL_TRANSPORT, SCIENCE_EXPEDITION;

    @JsonCreator
    public static MissionType from(String value) {
        if (value == null ) return null;
        String s = value.trim().toUpperCase();
        for (MissionType t : values()) {
            if (t.name().equals(s)) return t;
        }
        throw new IllegalArgumentException("Unknown MissionType value: '" + value + "'");
    }

    @JsonValue
    public String toValue() {return name();}
}

