package org.orbitalLogistic.mission.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AssignmentRole {
    COMMANDER, PILOT, ENGINEER, SCIENTIST, CARGO_OFFICER;

    @JsonCreator
    public static AssignmentRole from(String value) {
        if (value == null ) return null;
        String s = value.trim().toUpperCase();
        for (AssignmentRole t : values()) {
            if (t.name().equals(s)) return t;
        }
        throw new IllegalArgumentException("Unknown AssignmentRole value: '" + value + "'");
    }

    @JsonValue
    public String toValue() {return name();}
}

