package org.orbitalLogistic.mission.clients;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

public record UserDTO(
    Long id,
    String username,
    String email
) {}

