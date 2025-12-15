package org.orbitalLogistic.maintenance;

import org.springframework.boot.SpringApplication;

public class TestMaintenanceServiceApplication {
    public static void main(String[] args) {
        SpringApplication.from(MaintenanceServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }
}
