package org.orbitalLogistic.inventory.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cargo-service", fallback = CargoServiceClientFallback.class)
public interface CargoServiceClient {

    @GetMapping("/api/cargos/{id}")
    CargoDTO getCargoById(@PathVariable Long id);

    @GetMapping("/api/cargos/{id}/exists")
    Boolean cargoExists(@PathVariable Long id);

    @GetMapping("/api/storage-units/{id}")
    StorageUnitDTO getStorageUnitById(@PathVariable Long id);

    @GetMapping("/api/storage-units/{id}/exists")
    Boolean storageUnitExists(@PathVariable Long id);
}

