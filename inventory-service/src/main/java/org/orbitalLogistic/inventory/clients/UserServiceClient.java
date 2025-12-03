package org.orbitalLogistic.inventory.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", path = "/api/users", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {

    @GetMapping("/{id}")
    UserDTO getUserById(@PathVariable Long id);

    @GetMapping("/{id}/exists")
    Boolean userExists(@PathVariable Long id);
}

