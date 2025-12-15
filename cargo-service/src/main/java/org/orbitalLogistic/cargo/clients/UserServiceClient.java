package org.orbitalLogistic.cargo.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", path = "/api/users", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {

    @GetMapping("/{id}/username")
    String getUserById(@PathVariable Long id);
}

