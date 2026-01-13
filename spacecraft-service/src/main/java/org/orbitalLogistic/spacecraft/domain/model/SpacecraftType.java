package org.orbitalLogistic.spacecraft.domain.model;

import lombok.Builder;
import lombok.Value;
import org.orbitalLogistic.spacecraft.domain.model.enums.SpacecraftClassification;

@Value
@Builder(toBuilder = true)
public class SpacecraftType {
    Long id;
    String typeName;
    SpacecraftClassification classification;
    Integer maxCrewCapacity;

    public void validate() {
        if (typeName == null || typeName.isBlank()) {
            throw new IllegalArgumentException("Type name is required");
        }
        if (typeName.length() > 50) {
            throw new IllegalArgumentException("Type name must not exceed 50 characters");
        }
        if (classification == null) {
            throw new IllegalArgumentException("Classification is required");
        }
        if (maxCrewCapacity != null && maxCrewCapacity <= 0) {
            throw new IllegalArgumentException("Max crew capacity must be positive");
        }
    }
}

