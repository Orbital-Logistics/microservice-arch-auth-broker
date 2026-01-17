package org.orbitalLogistic.file.adapters.web.controllers;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.file.application.usecase.DownloadUserFilesUseCase;
import org.orbitalLogistic.file.application.usecase.StorageOperationsUseCase;
import org.orbitalLogistic.file.adapters.security.UserPrincipal;
import org.orbitalLogistic.file.application.model.FileCategory;
import org.orbitalLogistic.file.application.usecase.GetReportsUserUseCase;
import org.orbitalLogistic.file.application.ports.dto.FileDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final DownloadUserFilesUseCase downloadUserFilesUseCase;
    private final GetReportsUserUseCase getReportsUserUseCase;
    private final StorageOperationsUseCase storageOperationsUseCase;

    @Value("${minio.formats.reports}")
    private String reportsFormat;

    // ==================== USER OPERATIONS ====================

    @Operation(
            summary = "[USER] Get user's list of reports",
            tags = {"User File Operations"}
    )
    @GetMapping("/get-reports-list")
    public ResponseEntity<List<String>> getReportsList(@AuthenticationPrincipal UserPrincipal principal) {
        List<String> files = getReportsUserUseCase.execute(reportsFormat, principal.getUserId());
        return ResponseEntity.ok(files);
    }

    @Operation(
            summary = "[USER] Download report for the specified date. Write missionCode into swagger without \"\"",
            tags = {"User File Operations"}
    )
    @PostMapping("/download-report")
    public ResponseEntity<InputStreamResource> downloadReport(
            @RequestBody @NotBlank String missionCode,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        FileDto fileDto = downloadUserFilesUseCase.execute(principal.getUserId(), reportsFormat, missionCode);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-disposition", "attachment; filename=\"" + fileDto.filename() + "\"")
                .body(new InputStreamResource(fileDto.inputStream()));
    }

    // ==================== SUPPORT OPERATIONS ====================

    @Operation(
            summary = "[SUPPORT] Get file list of any specified path",
            tags = {"Support File Operations"}
    )
    @GetMapping("/get-files-list")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<List<String>> getFilesList(@RequestParam(defaultValue = "") String path,
                                                     @RequestParam(required = false) FileCategory category) {
        category = category == null ? FileCategory.DEFAULT : category;
        List<String> files = storageOperationsUseCase.getListDir(category, path);
        return ResponseEntity.ok(files);
    }

    @Operation(
            summary = "[SUPPORT] Download file of any specified path",
            tags = {"Support File Operations"}
    )
    @GetMapping("/download-file")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam @NotBlank String path,
                                                        @RequestParam(required = false) FileCategory category) {
        category = category == null ? FileCategory.DEFAULT : category;
        FileDto fileDto = storageOperationsUseCase.download(category, path);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileDto.filename() + "\"")
                .body(new InputStreamResource(fileDto.inputStream()));
    }

    // ==================== ADMIN ONLY OPERATIONS ====================

    @Operation(
            summary = "[ADMIN] Upload file to storage",
            tags = {"Admin File Operations"}
    )
    @PostMapping(value = "/upload-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InputStreamResource> uploadFile(@RequestParam @NotNull String path,
                                                          @RequestParam(required = false) FileCategory category,
                                                          @RequestPart("file") MultipartFile file) throws IOException {
        // shouldn't be empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // max 10 MB (in the configs)
        long maxSize = 10 * 1024 * 1024; // 10 MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }

        // validate filename
        if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            throw new IllegalArgumentException("File name is missing");
        }

        category = category == null ? FileCategory.DEFAULT : category;
        storageOperationsUseCase.upload(category,path + "/" + file.getOriginalFilename(), file.getInputStream(),
                file.getSize(), file.getContentType());
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "[ADMIN] Remove any file",
            tags = {"Admin File Operations"}
    )
    @DeleteMapping("/remove-file")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeFile(@RequestParam @NotBlank String path,
                                           @RequestParam(required = false) FileCategory category) {
        category = category == null ? FileCategory.DEFAULT : category;
        storageOperationsUseCase.remove(category, path);
        return ResponseEntity.ok().build();
    }
}
