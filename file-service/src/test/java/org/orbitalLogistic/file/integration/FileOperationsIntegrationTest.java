package org.orbitalLogistic.file.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.file.adapters.security.UserPrincipal;
import org.orbitalLogistic.file.application.usecase.StorageOperationsUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Tag("integration-tests")
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
        "minio.formats.reports=mission-%d-%s.pdf"
})
class FileOperationsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StorageOperationsUseCase storageOperationsUseCase;

    @Container
    static GenericContainer<?> minioContainer = new GenericContainer<>("minio/minio:latest")
            .withExposedPorts(9000)
            .withEnv("MINIO_ROOT_USER", "minioadmin")
            .withEnv("MINIO_ROOT_PASSWORD", "minioadmin")
            .withCommand("server", "/data")
            .withReuse(false);

    private UserPrincipal adminPrincipal;
    private UserPrincipal supportPrincipal;
    private UserPrincipal userPrincipal;
    private UsernamePasswordAuthenticationToken adminAuth;
    private UsernamePasswordAuthenticationToken supportAuth;
    private UsernamePasswordAuthenticationToken userAuth;

    @DynamicPropertySource
    static void registerMinioProperties(DynamicPropertyRegistry registry) {
        String minioUrl = "http://127.0.0.1:" + minioContainer.getMappedPort(9000);
        registry.add("minio.url", () -> minioUrl);
        registry.add("minio.access-key", () -> "minioadmin");
        registry.add("minio.secret-key", () -> "minioadmin");
    }


    @BeforeEach
    void setUp() throws Exception {

        String minioUrl = "http://127.0.0.1:" + minioContainer.getMappedPort(9000);
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioUrl)
                .credentials("minioadmin", "minioadmin")
                .build();

        
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket("test-user-files").build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket("test-user-files").build());
        }

        
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket("test-default").build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket("test-default").build());
        }

        adminPrincipal = new UserPrincipal(1L, "admin", List.of("ADMIN"));
        adminAuth = new UsernamePasswordAuthenticationToken(
                adminPrincipal, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        supportPrincipal = new UserPrincipal(2L, "support", List.of("SUPPORT"));
        supportAuth = new UsernamePasswordAuthenticationToken(
                supportPrincipal, null, List.of(new SimpleGrantedAuthority("ROLE_SUPPORT")));

        userPrincipal = new UserPrincipal(3L, "user", List.of("USER"));
        userAuth = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void adminFileUploadAndDownloadLifecycle_Integration() throws Exception {
        
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-integration.pdf",
                "application/pdf",
                "Integration test PDF content".getBytes()
        );

        
        mockMvc.perform(multipart("/api/files/upload-file")
                        .file(file)
                        .param("path", "integration-test")
                        .param("category", "USER")
                        .with(authentication(adminAuth))
                        .with(csrf()))
                .andExpect(status().isOk());

        
        mockMvc.perform(get("/api/files/get-files-list")
                        .param("path", "integration-test")
                        .param("category", "USER")
                        .with(authentication(supportAuth)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());

        
        mockMvc.perform(get("/api/files/download-file")
                        .param("path", "integration-test/test-integration.pdf")
                        .param("category", "USER")
                        .with(authentication(supportAuth)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));

        
        mockMvc.perform(delete("/api/files/remove-file")
                        .param("path", "integration-test/test-integration.pdf")
                        .param("category", "USER")
                        .with(authentication(adminAuth))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void userCanOnlyAccessOwnReports_Integration() throws Exception {
        
        mockMvc.perform(get("/api/files/get-reports-list")
                        .with(authentication(userAuth)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());

        
        mockMvc.perform(get("/api/files/get-files-list")
                        .param("path", "")
                        .param("category", "USER")
                        .with(authentication(userAuth)))
                .andExpect(status().isForbidden());
    }

    @Test
    void supportCanReadButNotWrite_Integration() throws Exception {
        
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "support-test.pdf",
                "application/pdf",
                "Support test content".getBytes()
        );

        
        mockMvc.perform(multipart("/api/files/upload-file")
                        .file(file)
                        .param("path", "test")
                        .param("category", "USER")
                        .with(authentication(supportAuth))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        
        mockMvc.perform(delete("/api/files/remove-file")
                        .param("path", "test/file.pdf")
                        .param("category", "USER")
                        .with(authentication(supportAuth))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        
        mockMvc.perform(get("/api/files/get-files-list")
                        .param("path", "")
                        .param("category", "USER")
                        .with(authentication(supportAuth)))
                .andExpect(status().isOk());
    }

    @Test
    void fileUploadValidation_Integration() throws Exception {
        
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        
        mockMvc.perform(multipart("/api/files/upload-file")
                        .file(emptyFile)
                        .param("path", "test")
                        .param("category", "USER")
                        .with(authentication(adminAuth))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        
        byte[] largeContent = new byte[11 * 1024 * 1024];
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large.pdf",
                "application/pdf",
                largeContent
        );

        
        mockMvc.perform(multipart("/api/files/upload-file")
                        .file(largeFile)
                        .param("path", "test")
                        .param("category", "USER")
                        .with(authentication(adminAuth))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void defaultCategoryIsAppliedWhenNotProvided_Integration() throws Exception {
        
        mockMvc.perform(get("/api/files/get-files-list")
                        .param("path", "")
                        .with(authentication(adminAuth)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void unauthorizedAccessIsDenied_Integration() throws Exception {
        
        mockMvc.perform(get("/api/files/get-reports-list"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/files/get-files-list")
                        .param("path", ""))
                .andExpect(status().isForbidden());
    }
}

