package org.orbitalLogistic.spacecraft.clients;

import org.orbitalLogistic.spacecraft.dto.common.SpacecraftCargoUsageDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cargo-service")
public interface CargoServiceClient {

    @GetMapping("/api/spacecrafts/{spacecraftId}/cargo-usage")
    SpacecraftCargoUsageDTO getSpacecraftCargoUsage(@PathVariable Long spacecraftId);
}
