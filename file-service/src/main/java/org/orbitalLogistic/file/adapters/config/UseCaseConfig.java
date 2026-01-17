package org.orbitalLogistic.file.adapters.config;

import org.orbitalLogistic.file.adapters.minio.MinioOperations;
import org.orbitalLogistic.file.adapters.reports.PdfReportGeneratorImpl;
import org.orbitalLogistic.file.application.usecase.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public DownloadUserFilesUseCase downloadUserFilesUseCase(MinioOperations storageOperations) {
        return new DownloadUserFilesUseCase(storageOperations);
    }

    @Bean
    public GetReportsUserUseCase getListUserUseCase(MinioOperations storageOperations) {
        return new GetReportsUserUseCase(storageOperations);
    }

    @Bean
    public ReportGenerationUseCase reportGenerationUseCase(MinioOperations storageOperations,
                                                           PdfReportGeneratorImpl reportGenerator) {
        return new ReportGenerationUseCase(storageOperations, reportGenerator);
    }

    @Bean
    public StorageOperationsUseCase storageOperationsUseCase(MinioOperations storageOperations) {
        return new StorageOperationsUseCase(storageOperations);
    }
}
