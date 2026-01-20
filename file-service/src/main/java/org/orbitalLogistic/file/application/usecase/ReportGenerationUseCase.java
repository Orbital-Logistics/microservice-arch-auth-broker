package org.orbitalLogistic.file.application.usecase;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.file.adapters.kafka.dto.CargoReportDataDTO;
import org.orbitalLogistic.file.adapters.kafka.dto.UserReportDataDTO;
import org.orbitalLogistic.file.adapters.security.UserPrincipal;
import org.orbitalLogistic.file.application.model.FileCategory;
import org.orbitalLogistic.file.application.ports.PdfReportGenerator;
import org.orbitalLogistic.file.application.ports.StorageOperations;
import org.orbitalLogistic.file.adapters.kafka.dto.MissionReportDataDTO;
import org.orbitalLogistic.file.adapters.exceptions.MinioException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.*;
import java.util.Objects;

@RequiredArgsConstructor
public class ReportGenerationUseCase {

    private final StorageOperations storageOperations;
    private final PdfReportGenerator pdfReportGenerator;

    /**
     * Generate the report from template pdf file with user's credentials and its rental data.
     * @param report - all required data for pdf.
     */
    public void generateMissionReport(MissionReportDataDTO report, String reportFormat) {
        byte[] bytes = pdfReportGenerator.generate(report);
        String reportName = String.format(reportFormat, 1, "mission-" + report.missionCode());

        InputStream resultInputStream = new ByteArrayInputStream(bytes);
        try {
            storageOperations.upload(FileCategory.USER, reportName, resultInputStream, bytes.length,
                    "application/pdf");
        } catch (Exception e) {
            throw new MinioException(e.getMessage());
        }
    }

    public void generateUserReport(UserReportDataDTO report, String reportFormat) {
        byte[] bytes = pdfReportGenerator.generate(report);
        String reportName = String.format(reportFormat, 1, "user-" + report.username());

        InputStream resultInputStream = new ByteArrayInputStream(bytes);
        try {
            storageOperations.upload(FileCategory.USER, reportName, resultInputStream, bytes.length,
                    "application/pdf");
        } catch (Exception e) {
            throw new MinioException(e.getMessage());
        }
    }

    public void generateCargoReport(CargoReportDataDTO report, String reportFormat) {
        byte[] bytes = pdfReportGenerator.generate(report);
        String reportName = String.format(reportFormat, 1, "cargo-" + report.cargoId());

        InputStream resultInputStream = new ByteArrayInputStream(bytes);
        try {
            storageOperations.upload(FileCategory.USER, reportName, resultInputStream, bytes.length,
                    "application/pdf");
        } catch (Exception e) {
            throw new MinioException(e.getMessage());
        }
    }

    public Long getCurrentUserId() {
        Object principal = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();

        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getUserId();
        } else {
            return null;
        }
    }
}
