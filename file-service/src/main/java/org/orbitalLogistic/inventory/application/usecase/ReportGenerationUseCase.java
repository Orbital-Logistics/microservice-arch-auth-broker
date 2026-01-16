package org.orbitalLogistic.inventory.application.usecase;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.inventory.application.model.FileCategory;
import org.orbitalLogistic.inventory.application.ports.PdfReportGenerator;
import org.orbitalLogistic.inventory.application.ports.StorageOperations;
import org.orbitalLogistic.inventory.adapters.kafka.dto.ReportDataDTO;
import org.orbitalLogistic.inventory.adapters.exceptions.MinioException;
import org.orbitalLogistic.inventory.application.services.FileFormat;

import java.io.*;

@RequiredArgsConstructor
public class ReportGenerationUseCase {

    private final StorageOperations storageOperations;
    private final PdfReportGenerator pdfReportGenerator;

    /**
     * Generate the report from template pdf file with user's credentials and its rental data.
     * @param report - all required data for pdf.
     */
    public void generateReport(ReportDataDTO report, String reportFormat, String dateFormat) {
        byte[] bytes = pdfReportGenerator.generate(report);
        String reportName = String.format(reportFormat, report.spacecraftId(),
                FileFormat.getStringDateFormat(report.scheduledDeparture(), dateFormat));

        InputStream resultInputStream = new ByteArrayInputStream(bytes);
        try {
            storageOperations.upload(FileCategory.USER, reportName, resultInputStream, bytes.length,
                    "application/pdf");
        } catch (Exception e) {
            throw new MinioException(e.getMessage());
        }
    }

}
