package org.orbitalLogistic.cargo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CargoServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CargoServiceApplication.class, args);
    }
}











