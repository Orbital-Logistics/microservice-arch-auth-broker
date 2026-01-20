package org.orbitalLogistic.file.adapters.web.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.file.adapters.security.JwtAuthenticationFilter;
import org.orbitalLogistic.file.adapters.security.JwtService;
import org.orbitalLogistic.file.adapters.security.SecurityConfig;
import org.orbitalLogistic.file.adapters.security.UserPrincipal;
import org.orbitalLogistic.file.application.model.FileCategory;
import org.orbitalLogistic.file.application.ports.dto.FileDto;
import org.orbitalLogistic.file.application.usecase.DownloadUserFilesUseCase;
import org.orbitalLogistic.file.application.usecase.GetReportsUserUseCase;
import org.orbitalLogistic.file.application.usecase.StorageOperationsUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FileController.class)
@Import({SecurityConfig.class, FileControllerTest.TestSecurityConfig.class})
@TestPropertySource(properties = {
        "minio.formats.reports=mission-%d-%s.pdf",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
        "spring.cloud.config.enabled=false"
})
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DownloadUserFilesUseCase downloadUserFilesUseCase;

    @MockitoBean
    private GetReportsUserUseCase getReportsUserUseCase;

    @MockitoBean
    private StorageOperationsUseCase storageOperationsUseCase;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void shouldGetReportsListForAuthenticatedUser() throws Exception {
        
        UserPrincipal userPrincipal = new UserPrincipal(1L, "user1", List.of("USER"));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        
        List<String> reports = Arrays.asList(
                "mission-1-ABC123.pdf",
                "mission-2-XYZ789.pdf"
        );
        when(getReportsUserUseCase.execute(anyString(), anyLong())).thenReturn(reports);

        
        mockMvc.perform(get("/api/files/get-reports-list")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value("mission-1-ABC123.pdf"))
                .andExpect(jsonPath("$[1]").value("mission-2-XYZ789.pdf"));

        verify(getReportsUserUseCase, times(1)).execute(anyString(), anyLong());
    }

    @Test
    void shouldReturn401WhenGettingReportsListWithoutAuth() throws Exception {
        
        mockMvc.perform(get("/api/files/get-reports-list"))
                .andExpect(status().isForbidden());

        verify(getReportsUserUseCase, never()).execute(anyString(), anyLong());
    }

    @Test
    void shouldDownloadReportForAuthenticatedUser() throws Exception {
        
        String missionCode = "ABC123";
        byte[] pdfContent = "PDF content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(pdfContent);
        FileDto fileDto = new FileDto("mission-1-ABC123.pdf", inputStream);
        
        UserPrincipal userPrincipal = new UserPrincipal(1L, "user1", List.of("USER"));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(downloadUserFilesUseCase.execute(any(), any(), any()))
                .thenReturn(fileDto);

        
        mockMvc.perform(post("/api/files/download-report")
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"" + missionCode + "\""))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-disposition",
                        "attachment; filename=\"mission-1-ABC123.pdf\""))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));

        verify(downloadUserFilesUseCase, times(1))
                .execute(eq(1L), eq("mission-%d-%s.pdf"), eq("\"ABC123\""));
    }

    

    @Test
    void shouldGetFilesListForSupportRole() throws Exception {
        
        UserPrincipal userPrincipal = new UserPrincipal(2L, "support1", List.of("SUPPORT"));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, List.of(new SimpleGrantedAuthority("ROLE_SUPPORT")));
        
        List<String> files = Arrays.asList("file1.pdf", "file2.pdf");
        when(storageOperationsUseCase.getListDir(any(FileCategory.class), anyString()))
                .thenReturn(files);

        
        mockMvc.perform(get("/api/files/get-files-list")
                        .with(authentication(auth))
                        .param("path", "reports/")
                        .param("category", "USER"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(storageOperationsUseCase, times(1))
                .getListDir(eq(FileCategory.USER), eq("reports/"));
    }

    @Test
    void shouldGetFilesListForAdminRole() throws Exception {
        
        UserPrincipal userPrincipal = new UserPrincipal(3L, "admin1", List.of("ADMIN"));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        
        List<String> files = Arrays.asList("file1.pdf");
        when(storageOperationsUseCase.getListDir(any(FileCategory.class), anyString()))
                .thenReturn(files);

        
        mockMvc.perform(get("/api/files/get-files-list")
                        .with(authentication(auth))
                        .param("path", ""))
                .andExpect(status().isOk());

        verify(storageOperationsUseCase, times(1))
                .getListDir(eq(FileCategory.DEFAULT), eq(""));
    }

    @Test
    void shouldReturn403WhenUserTriesToAccessSupportEndpoints() throws Exception {
        
        UserPrincipal userPrincipal = new UserPrincipal(1L, "user1", List.of("USER"));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        
        
        mockMvc.perform(get("/api/files/get-files-list")
                        .with(authentication(auth))
                        .param("path", ""))
                .andExpect(status().isForbidden());

        verify(storageOperationsUseCase, never())
                .getListDir(any(FileCategory.class), anyString());
    }

    @Test
    void shouldDownloadFileForSupportRole() throws Exception {
        
        UserPrincipal userPrincipal = new UserPrincipal(2L, "support1", List.of("SUPPORT"));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, List.of(new SimpleGrantedAuthority("ROLE_SUPPORT")));
        
        byte[] content = "File content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(content);
        FileDto fileDto = new FileDto("test.pdf", inputStream);

        when(storageOperationsUseCase.download(any(FileCategory.class), anyString()))
                .thenReturn(fileDto);

        
        mockMvc.perform(get("/api/files/download-file")
                        .with(authentication(auth))
                        .param("path", "reports/test.pdf")
                        .param("category", "USER"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"test.pdf\""))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));

        verify(storageOperationsUseCase, times(1))
                .download(eq(FileCategory.USER), eq("reports/test.pdf"));
    }

    

    @Test
    void shouldUploadFileForAdminRole() throws Exception {
        
        UserPrincipal userPrincipal = new UserPrincipal(3L, "admin1", List.of("ADMIN"));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        doNothing().when(storageOperationsUseCase)
                .upload(any(FileCategory.class), anyString(), any(InputStream.class),
                        anyLong(), anyString());

        
        mockMvc.perform(multipart("/api/files/upload-file")
                        .file(file)
                        .param("path", "reports")
                        .param("category", "USER")
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(storageOperationsUseCase, times(1))
                .upload(eq(FileCategory.USER), eq("reports/test.pdf"),
                        any(InputStream.class), eq(11L), eq("application/pdf"));
    }

    @Test
    void shouldRejectEmptyFileUpload() throws Exception {
        
        UserPrincipal userPrincipal = new UserPrincipal(3L, "admin1", List.of("ADMIN"));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                new byte[0]
        );

        
        mockMvc.perform(multipart("/api/files/upload-file")
                        .file(emptyFile)
                        .param("path", "reports")
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(storageOperationsUseCase, never())
                .upload(any(), anyString(), any(), anyLong(), anyString());
    }

    @Test
    void shouldRejectFileLargerThan10MB() throws Exception {
        
        UserPrincipal userPrincipal = new UserPrincipal(3L, "admin1", List.of("ADMIN"));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        
        byte[] largeContent = new byte[11 * 1024 * 1024]; 
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large.pdf",
                "application/pdf",
                largeContent
        );

        
        mockMvc.perform(multipart("/api/files/upload-file")
                        .file(largeFile)
                        .param("path", "reports")
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(storageOperationsUseCase, never())
                .upload(any(), anyString(), any(), anyLong(), anyString());
    }

    @Test
    void shouldReturn403WhenSupportTriesToUploadFile() throws Exception {
        
        UserPrincipal userPrincipal = new UserPrincipal(2L, "support1", List.of("SUPPORT"));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, List.of(new SimpleGrantedAuthority("ROLE_SUPPORT")));
        
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        
        mockMvc.perform(multipart("/api/files/upload-file")
                        .file(file)
                        .param("path", "reports")
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(storageOperationsUseCase, never())
                .upload(any(), anyString(), any(), anyLong(), anyString());
    }

    @Test
    void shouldRemoveFileForAdminRole() throws Exception {
        
        UserPrincipal userPrincipal = new UserPrincipal(3L, "admin1", List.of("ADMIN"));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        
        doNothing().when(storageOperationsUseCase)
                .remove(any(FileCategory.class), anyString());

        
        mockMvc.perform(delete("/api/files/remove-file")
                        .param("path", "reports/test.pdf")
                        .param("category", "USER")
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(storageOperationsUseCase, times(1))
                .remove(eq(FileCategory.USER), eq("reports/test.pdf"));
    }

    @Test
    void shouldReturn403WhenSupportTriesToRemoveFile() throws Exception {
        
        UserPrincipal userPrincipal = new UserPrincipal(2L, "support1", List.of("SUPPORT"));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, List.of(new SimpleGrantedAuthority("ROLE_SUPPORT")));
        
        
        mockMvc.perform(delete("/api/files/remove-file")
                        .param("path", "reports/test.pdf")
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(storageOperationsUseCase, never())
                .remove(any(FileCategory.class), anyString());
    }

    @Test
    void shouldUseDefaultCategoryWhenNotProvided() throws Exception {
        
        UserPrincipal userPrincipal = new UserPrincipal(3L, "admin1", List.of("ADMIN"));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        
        List<String> files = Arrays.asList("file1.txt");
        when(storageOperationsUseCase.getListDir(any(FileCategory.class), anyString()))
                .thenReturn(files);

        
        mockMvc.perform(get("/api/files/get-files-list")
                        .with(authentication(auth))
                        .param("path", "test/"))
                .andExpect(status().isOk());

        verify(storageOperationsUseCase, times(1))
                .getListDir(eq(FileCategory.DEFAULT), eq("test/"));
    }

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService) {
            return new JwtAuthenticationFilter(jwtService);
        }
    }
}

