package org.orbitalLogistic.maintenance.clients.feign;

import org.orbitalLogistic.maintenance.config.FeignConfig;
import org.orbitalLogistic.maintenance.infrastructure.adapters.out.external.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", path = "/api/users", configuration = FeignConfig.class)
public interface UserServiceFeignClient {

    @GetMapping("/{id}")
    UserDTO getUserById(@PathVariable Long id);

    @GetMapping("/{id}/exists")
    Boolean userExists(@PathVariable Long id);
}
