package org.orbitalLogistic.file.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.file.adapters.exceptions.MinioException;
import org.orbitalLogistic.file.adapters.kafka.dto.MissionReportDataDTO;
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

    private MissionReportDataDTO testReportData;
    private String reportFormat;

    @BeforeEach
    void setUp() {
        reportFormat = "mission-%d-%s.pdf";
        testReportData = new MissionReportDataDTO(
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
    void shouldGenerateMissionReportSuccessfully() throws Exception {
        
        byte[] pdfBytes = "PDF content".getBytes();
        when(pdfReportGenerator.generate(testReportData)).thenReturn(pdfBytes);
        doNothing().when(storageOperations).upload(
                any(FileCategory.class),
                anyString(),
                any(InputStream.class),
                anyLong(),
                anyString()
        );

        
        reportGenerationUseCase.generateMissionReport(testReportData, reportFormat);

        
        verify(pdfReportGenerator, times(1)).generate(testReportData);
        verify(storageOperations, times(1)).upload(
                eq(FileCategory.USER),
                eq("mission-1-mission-MISS-001.pdf"),
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

        
        reportGenerationUseCase.generateMissionReport(testReportData, reportFormat);

        
        verify(storageOperations).upload(
                eq(FileCategory.USER),
                eq("mission-1-mission-MISS-001.pdf"),
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

        
        reportGenerationUseCase.generateMissionReport(testReportData, reportFormat);

        
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

        
        reportGenerationUseCase.generateMissionReport(testReportData, reportFormat);

        
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
                reportGenerationUseCase.generateMissionReport(testReportData, reportFormat));
        
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
        
        MissionReportDataDTO differentReport = new MissionReportDataDTO(
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

        
        reportGenerationUseCase.generateMissionReport(differentReport, reportFormat);

        
        verify(pdfReportGenerator).generate(differentReport);
        verify(storageOperations).upload(
                eq(FileCategory.USER),
                eq("mission-1-mission-TEST-999.pdf"),
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

        
        reportGenerationUseCase.generateMissionReport(testReportData, reportFormat);

        
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
        
        MissionReportDataDTO specialReport = new MissionReportDataDTO(
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

        
        reportGenerationUseCase.generateMissionReport(specialReport, reportFormat);

        
        verify(storageOperations).upload(
                any(FileCategory.class),
                eq("mission-1-mission-MISS-001-SPECIAL_TEST.pdf"),
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

        
        reportGenerationUseCase.generateMissionReport(testReportData, customFormat);

        
        verify(storageOperations).upload(
                any(FileCategory.class),
                eq("report_1_mission-MISS-001.pdf"),
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

        
        reportGenerationUseCase.generateMissionReport(testReportData, reportFormat);

        
        verify(storageOperations).upload(
                any(FileCategory.class),
                anyString(),
                any(InputStream.class),
                eq((long) pdfBytes.length),
                anyString()
        );
    }
}

