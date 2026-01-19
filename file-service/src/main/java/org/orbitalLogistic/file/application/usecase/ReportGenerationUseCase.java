package org.orbitalLogistic.file.application.usecase;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.file.application.model.FileCategory;
import org.orbitalLogistic.file.application.ports.PdfReportGenerator;
import org.orbitalLogistic.file.application.ports.StorageOperations;
import org.orbitalLogistic.file.adapters.kafka.dto.ReportDataDTO;
import org.orbitalLogistic.file.adapters.exceptions.MinioException;

import java.io.*;

@RequiredArgsConstructor
public class ReportGenerationUseCase {

    private final StorageOperations storageOperations;
    private final PdfReportGenerator pdfReportGenerator;

    /**
     * Generate the report from template pdf file with user's credentials and its rental data.
     * @param report - all required data for pdf.
     */
    public void generateReport(ReportDataDTO report, String reportFormat) {
        byte[] bytes = pdfReportGenerator.generate(report);
        String reportName = String.format(reportFormat, report.spacecraftId(), report.missionCode());

        InputStream resultInputStream = new ByteArrayInputStream(bytes);
        try {
            storageOperations.upload(FileCategory.USER, reportName, resultInputStream, bytes.length,
                    "application/pdf");
        } catch (Exception e) {
            throw new MinioException(e.getMessage());
        }
    }

}
