package org.orbitalLogistic.file;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean(initMethod = "start", destroyMethod = "stop")
    GenericContainer<?> minioContainer() {
        return new GenericContainer<>(DockerImageName.parse("minio/minio:latest"))
                .withExposedPorts(9000)
                .withEnv("MINIO_ROOT_USER", "minioadmin")
                .withEnv("MINIO_ROOT_PASSWORD", "minioadmin")
                .withCommand("server", "/data")
                .withReuse(false);
    }
}

