package org.orbitalLogistic.file.adapters.reports;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.file.adapters.kafka.dto.MissionReportDataDTO;
import org.orbitalLogistic.file.application.model.FileCategory;
import org.orbitalLogistic.file.application.ports.StorageOperations;
import org.orbitalLogistic.file.application.ports.dto.FileDto;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PdfReportGeneratorImplTest {

    @Mock
    private StorageOperations storageOperations;

    @InjectMocks
    private PdfReportGeneratorImpl pdfReportGenerator;

    private MissionReportDataDTO testReportData;

    @BeforeEach
    void setUp() {
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
    void shouldGeneratePdfReportSuccessfully() {
        
        byte[] templatePdfContent = createMockPdfTemplate();
        InputStream templateStream = new ByteArrayInputStream(templatePdfContent);
        FileDto templateFile = new FileDto("Mission report.pdf", templateStream);

        when(storageOperations.download(eq(FileCategory.USER), eq("reports/Mission report.pdf")))
                .thenReturn(templateFile);

        
        byte[] result = pdfReportGenerator.generate(testReportData);

        
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(storageOperations, times(1))
                .download(eq(FileCategory.USER), eq("reports/Mission report.pdf"));
    }

    @Test
    void shouldDownloadTemplateFromCorrectLocation() {
        
        byte[] templatePdfContent = createMockPdfTemplate();
        InputStream templateStream = new ByteArrayInputStream(templatePdfContent);
        FileDto templateFile = new FileDto("Mission report.pdf", templateStream);

        when(storageOperations.download(any(FileCategory.class), anyString()))
                .thenReturn(templateFile);

        
        pdfReportGenerator.generate(testReportData);

        
        verify(storageOperations).download(
                eq(FileCategory.USER),
                eq("reports/Mission report.pdf")
        );
    }

    @Test
    void shouldThrowRuntimeExceptionWhenTemplateDownloadFails() {
        
        when(storageOperations.download(any(FileCategory.class), anyString()))
                .thenThrow(new RuntimeException("Template not found"));

        
        assertThrows(RuntimeException.class, () -> 
                pdfReportGenerator.generate(testReportData));
        
        verify(storageOperations, times(1))
                .download(eq(FileCategory.USER), eq("reports/Mission report.pdf"));
    }

    @Test
    void shouldHandleReportDataWithDifferentMissionTypes() {
        
        MissionReportDataDTO cargoMission = new MissionReportDataDTO(
                "CARGO-001",
                "Cargo Transport",
                "CARGO",
                "MEDIUM",
                1L,
                2L,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(5)
        );

        byte[] templatePdfContent = createMockPdfTemplate();
        InputStream templateStream = new ByteArrayInputStream(templatePdfContent);
        FileDto templateFile = new FileDto("Mission report.pdf", templateStream);

        when(storageOperations.download(any(FileCategory.class), anyString()))
                .thenReturn(templateFile);

        
        byte[] result = pdfReportGenerator.generate(cargoMission);

        
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void shouldHandleReportDataWithDifferentPriorities() {
        
        MissionReportDataDTO lowPriorityMission = new MissionReportDataDTO(
                "TEST-001",
                "Test Mission",
                "EXPLORATION",
                "LOW",
                999L,
                888L,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(30)
        );

        byte[] templatePdfContent = createMockPdfTemplate();
        InputStream templateStream = new ByteArrayInputStream(templatePdfContent);
        FileDto templateFile = new FileDto("Mission report.pdf", templateStream);

        when(storageOperations.download(any(FileCategory.class), anyString()))
                .thenReturn(templateFile);

        
        byte[] result = pdfReportGenerator.generate(lowPriorityMission);

        
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void shouldHandleReportWithLongMissionNames() {
        
        MissionReportDataDTO longNameMission = new MissionReportDataDTO(
                "LONG-001",
                "Very Long Mission Name That Contains Multiple Words And Descriptions For Testing Purposes",
                "EXPLORATION",
                "HIGH",
                1L,
                2L,
                LocalDateTime.now(),
                LocalDateTime.now().plusMonths(6)
        );

        byte[] templatePdfContent = createMockPdfTemplate();
        InputStream templateStream = new ByteArrayInputStream(templatePdfContent);
        FileDto templateFile = new FileDto("Mission report.pdf", templateStream);

        when(storageOperations.download(any(FileCategory.class), anyString()))
                .thenReturn(templateFile);

        
        byte[] result = pdfReportGenerator.generate(longNameMission);

        
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void shouldHandleReportWithSpecialCharacters() {
        
        MissionReportDataDTO specialCharMission = new MissionReportDataDTO(
                "MISS-001_SPECIAL",
                "Special Mission",
                "EXPLORATION",
                "HIGH",
                1L,
                2L,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(10)
        );

        byte[] templatePdfContent = createMockPdfTemplate();
        InputStream templateStream = new ByteArrayInputStream(templatePdfContent);
        FileDto templateFile = new FileDto("Mission report.pdf", templateStream);

        when(storageOperations.download(any(FileCategory.class), anyString()))
                .thenReturn(templateFile);

        
        byte[] result = pdfReportGenerator.generate(specialCharMission);

        
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void shouldHandleReportWithLargeIds() {
        
        MissionReportDataDTO largeIdMission = new MissionReportDataDTO(
                "LARGE-001",
                "Large ID Mission",
                "EXPLORATION",
                "HIGH",
                Long.MAX_VALUE,
                Long.MAX_VALUE - 1,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(15)
        );

        byte[] templatePdfContent = createMockPdfTemplate();
        InputStream templateStream = new ByteArrayInputStream(templatePdfContent);
        FileDto templateFile = new FileDto("Mission report.pdf", templateStream);

        when(storageOperations.download(any(FileCategory.class), anyString()))
                .thenReturn(templateFile);

        
        byte[] result = pdfReportGenerator.generate(largeIdMission);

        
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void shouldHandleReportWithFutureDates() {
        
        LocalDateTime futureDate = LocalDateTime.now().plusYears(10);
        MissionReportDataDTO futureMission = new MissionReportDataDTO(
                "FUTURE-001",
                "Future Mission",
                "EXPLORATION",
                "HIGH",
                1L,
                2L,
                futureDate,
                futureDate.plusMonths(6)
        );

        byte[] templatePdfContent = createMockPdfTemplate();
        InputStream templateStream = new ByteArrayInputStream(templatePdfContent);
        FileDto templateFile = new FileDto("Mission report.pdf", templateStream);

        when(storageOperations.download(any(FileCategory.class), anyString()))
                .thenReturn(templateFile);

        
        byte[] result = pdfReportGenerator.generate(futureMission);

        
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void shouldCallStorageOperationsOnlyOnce() {
        
        byte[] templatePdfContent = createMockPdfTemplate();
        InputStream templateStream = new ByteArrayInputStream(templatePdfContent);
        FileDto templateFile = new FileDto("Mission report.pdf", templateStream);

        when(storageOperations.download(any(FileCategory.class), anyString()))
                .thenReturn(templateFile);

        
        pdfReportGenerator.generate(testReportData);

        
        verify(storageOperations, times(1))
                .download(any(FileCategory.class), anyString());
    }

    
    private byte[] createMockPdfTemplate() {
        
        
        return ("%PDF-1.4\n" +
                "1 0 obj\n" +
                "<< /Type /Catalog /Pages 2 0 R >>\n" +
                "endobj\n" +
                "2 0 obj\n" +
                "<< /Type /Pages /Kids [3 0 R] /Count 1 >>\n" +
                "endobj\n" +
                "3 0 obj\n" +
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] >>\n" +
                "endobj\n" +
                "xref\n" +
                "0 4\n" +
                "0000000000 65535 f\n" +
                "0000000009 00000 n\n" +
                "0000000058 00000 n\n" +
                "0000000115 00000 n\n" +
                "trailer\n" +
                "<< /Size 4 /Root 1 0 R >>\n" +
                "startxref\n" +
                "190\n" +
                "%%EOF").getBytes();
    }
}

