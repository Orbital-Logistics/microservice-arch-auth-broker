package org.orbitalLogistic.inventory.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "spacecraft-service", path = "/api/spacecrafts", fallback = SpacecraftServiceClientFallback.class)
public interface SpacecraftServiceClient {

    @GetMapping("/{id}")
    SpacecraftDTO getSpacecraftById(@PathVariable Long id);

    @GetMapping("/{id}/exists")
    Boolean spacecraftExists(@PathVariable Long id);
}

