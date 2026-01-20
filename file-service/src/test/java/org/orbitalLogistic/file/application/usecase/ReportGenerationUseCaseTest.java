package org.orbitalLogistic.file.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.file.adapters.exceptions.MinioException;
import org.orbitalLogistic.file.adapters.kafka.dto.ReportDataDTO;
import org.orbitalLogistic.file.application.model.FileCategory;
import org.orbitalLogistic.file.application.ports.PdfReportGenerator;
import org.orbitalLogistic.file.application.ports.StorageOperations;

import java.io.InputStream;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportGenerationUseCaseTest {

    @Mock
    private StorageOperations storageOperations;

    @Mock
    private PdfReportGenerator pdfReportGenerator;

    @InjectMocks
    private ReportGenerationUseCase reportGenerationUseCase;

    private ReportDataDTO testReportData;
    private String reportFormat;

    @BeforeEach
    void setUp() {
        reportFormat = "mission-%d-%s.pdf";
        testReportData = new ReportDataDTO(
                "MISS-001",
                "Mars Exploration",
                "EXPLORATION",
                "HIGH",
                123L,
                456L,
                LocalDateTime.of(2024, 6, 1, 10, 0),
                LocalDateTime.of(2024, 12, 1, 10, 0)
        );
    }

    @Test
    void shouldGenerateReportSuccessfully() throws Exception {
        
        byte[] pdfBytes = "PDF content".getBytes();
        when(pdfReportGenerator.generate(testReportData)).thenReturn(pdfBytes);
        doNothing().when(storageOperations).upload(
                any(FileCategory.class),
                anyString(),
                any(InputStream.class),
                anyLong(),
                anyString()
        );

        
        reportGenerationUseCase.generateReport(testReportData, reportFormat);

        
        verify(pdfReportGenerator, times(1)).generate(testReportData);
        verify(storageOperations, times(1)).upload(
                eq(FileCategory.USER),
                eq("mission-456-MISS-001.pdf"),
                any(InputStream.class),
                eq((long) pdfBytes.length),
                eq("application/pdf")
        );
    }

    @Test
    void shouldCreateCorrectReportFilename() throws Exception {
        
        byte[] pdfBytes = "Test PDF".getBytes();
        when(pdfReportGenerator.generate(testReportData)).thenReturn(pdfBytes);
        doNothing().when(storageOperations).upload(
                any(FileCategory.class),
                anyString(),
                any(InputStream.class),
                anyLong(),
                anyString()
        );

        
        reportGenerationUseCase.generateReport(testReportData, reportFormat);

        
        String expectedFilename = String.format(reportFormat, 
                testReportData.spacecraftId(), 
                testReportData.missionCode());
        verify(storageOperations).upload(
                eq(FileCategory.USER),
                eq(expectedFilename),
                any(InputStream.class),
                anyLong(),
                anyString()
        );
    }

    @Test
    void shouldUploadReportWithCorrectContentType() throws Exception {
        
        byte[] pdfBytes = "PDF content".getBytes();
        when(pdfReportGenerator.generate(testReportData)).thenReturn(pdfBytes);
        doNothing().when(storageOperations).upload(
                any(FileCategory.class),
                anyString(),
                any(InputStream.class),
                anyLong(),
                anyString()
        );

        
        reportGenerationUseCase.generateReport(testReportData, reportFormat);

        
        verify(storageOperations).upload(
                any(FileCategory.class),
                anyString(),
                any(InputStream.class),
                anyLong(),
                eq("application/pdf")
        );
    }

    @Test
    void shouldUploadReportToUserCategory() throws Exception {
        
        byte[] pdfBytes = "PDF content".getBytes();
        when(pdfReportGenerator.generate(testReportData)).thenReturn(pdfBytes);
        doNothing().when(storageOperations).upload(
                any(FileCategory.class),
                anyString(),
                any(InputStream.class),
                anyLong(),
                anyString()
        );

        
        reportGenerationUseCase.generateReport(testReportData, reportFormat);

        
        verify(storageOperations).upload(
                eq(FileCategory.USER),
                anyString(),
                any(InputStream.class),
                anyLong(),
                anyString()
        );
    }

    @Test
    void shouldThrowMinioExceptionWhenUploadFails() throws Exception {
        
        byte[] pdfBytes = "PDF content".getBytes();
        when(pdfReportGenerator.generate(testReportData)).thenReturn(pdfBytes);
        doThrow(new RuntimeException("Storage error")).when(storageOperations).upload(
                any(FileCategory.class),
                anyString(),
                any(InputStream.class),
                anyLong(),
                anyString()
        );

        
        assertThrows(MinioException.class, () -> 
                reportGenerationUseCase.generateReport(testReportData, reportFormat));
        
        verify(pdfReportGenerator, times(1)).generate(testReportData);
        verify(storageOperations, times(1)).upload(
                any(FileCategory.class),
                anyString(),
                any(InputStream.class),
                anyLong(),
                anyString()
        );
    }

    @Test
    void shouldHandlePdfGenerationWithDifferentReportData() throws Exception {
        
        ReportDataDTO differentReport = new ReportDataDTO(
                "TEST-999",
                "Test Mission",
                "CARGO",
                "LOW",
                999L,
                888L,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(30)
        );
        
        byte[] pdfBytes = "Different PDF".getBytes();
        when(pdfReportGenerator.generate(differentReport)).thenReturn(pdfBytes);
        doNothing().when(storageOperations).upload(
                any(FileCategory.class),
                anyString(),
                any(InputStream.class),
                anyLong(),
                anyString()
        );

        
        reportGenerationUseCase.generateReport(differentReport, reportFormat);

        
        verify(pdfReportGenerator).generate(differentReport);
        verify(storageOperations).upload(
                eq(FileCategory.USER),
                eq("mission-888-TEST-999.pdf"),
                any(InputStream.class),
                eq((long) pdfBytes.length),
                eq("application/pdf")
        );
    }

    @Test
    void shouldHandleLargePdfFiles() throws Exception {
        
        byte[] largePdfBytes = new byte[10 * 1024 * 1024]; 
        when(pdfReportGenerator.generate(testReportData)).thenReturn(largePdfBytes);
        doNothing().when(storageOperations).upload(
                any(FileCategory.class),
                anyString(),
                any(InputStream.class),
                anyLong(),
                anyString()
        );

        
        reportGenerationUseCase.generateReport(testReportData, reportFormat);

        
        verify(storageOperations).upload(
                any(FileCategory.class),
                anyString(),
                any(InputStream.class),
                eq((long) largePdfBytes.length),
                anyString()
        );
    }

    @Test
    void shouldHandleReportsWithSpecialCharactersInMissionCode() throws Exception {
        
        ReportDataDTO specialReport = new ReportDataDTO(
                "MISS-001-SPECIAL_TEST",
                "Special Mission",
                "EXPLORATION",
                "HIGH",
                1L,
                2L,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(10)
        );
        
        byte[] pdfBytes = "PDF content".getBytes();
        when(pdfReportGenerator.generate(specialReport)).thenReturn(pdfBytes);
        doNothing().when(storageOperations).upload(
                any(FileCategory.class),
                anyString(),
                any(InputStream.class),
                anyLong(),
                anyString()
        );

        
        reportGenerationUseCase.generateReport(specialReport, reportFormat);

        
        verify(storageOperations).upload(
                any(FileCategory.class),
                eq("mission-2-MISS-001-SPECIAL_TEST.pdf"),
                any(InputStream.class),
                anyLong(),
                anyString()
        );
    }

    @Test
    void shouldHandleReportsWithDifferentFormatStrings() throws Exception {
        
        String customFormat = "report_%d_%s.pdf";
        byte[] pdfBytes = "PDF content".getBytes();
        when(pdfReportGenerator.generate(testReportData)).thenReturn(pdfBytes);
        doNothing().when(storageOperations).upload(
                any(FileCategory.class),
                anyString(),
                any(InputStream.class),
                anyLong(),
                anyString()
        );

        
        reportGenerationUseCase.generateReport(testReportData, customFormat);

        
        verify(storageOperations).upload(
                any(FileCategory.class),
                eq("report_456_MISS-001.pdf"),
                any(InputStream.class),
                anyLong(),
                anyString()
        );
    }

    @Test
    void shouldConvertBytesToInputStreamCorrectly() throws Exception {
        
        byte[] pdfBytes = "Test PDF content for validation".getBytes();
        when(pdfReportGenerator.generate(testReportData)).thenReturn(pdfBytes);
        doNothing().when(storageOperations).upload(
                any(FileCategory.class),
                anyString(),
                any(InputStream.class),
                anyLong(),
                anyString()
        );

        
        reportGenerationUseCase.generateReport(testReportData, reportFormat);

        
        verify(storageOperations).upload(
                any(FileCategory.class),
                anyString(),
                any(InputStream.class),
                eq((long) pdfBytes.length),
                anyString()
        );
    }
}

