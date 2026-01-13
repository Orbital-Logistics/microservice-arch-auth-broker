package org.orbitalLogistic.maintenance.clients.feign;

import org.orbitalLogistic.maintenance.config.FeignConfig;
import org.orbitalLogistic.maintenance.infrastructure.adapters.out.external.dto.SpacecraftDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "spacecraft-service", path = "/api/spacecrafts", configuration = FeignConfig.class)
public interface SpacecraftServiceFeignClient {

    @GetMapping("/{id}")
    SpacecraftDTO getSpacecraftById(@PathVariable Long id);

    @GetMapping("/{id}/exists")
    Boolean spacecraftExists(@PathVariable Long id);
}
