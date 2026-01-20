package org.orbitalLogistic.file.adapters.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.file.adapters.kafka.dto.ReportDataDTO;
import org.orbitalLogistic.file.application.usecase.ReportGenerationUseCase;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportGenerationEventListenerTest {

    @Mock
    private ObjectMapper mapper;

    @Mock
    private ReportGenerationUseCase reportGenerationUseCase;

    @InjectMocks
    private ReportGenerationEventListener listener;

    private String reportFormat;

    @BeforeEach
    void setUp() {
        reportFormat = "mission-%d-%s.pdf";
        ReflectionTestUtils.setField(listener, "reportsFormat", reportFormat);
    }

    @Test
    void shouldProcessValidReportGenerationMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put("missionCode", "MISS-001");
        message.put("missionName", "Mars Exploration");
        message.put("missionType", "EXPLORATION");
        message.put("priority", "HIGH");
        message.put("commandingOfficerId", 123L);
        message.put("spacecraftId", 456L);
        message.put("scheduledDeparture", "2024-06-01T10:00:00");
        message.put("scheduledArrival", "2024-12-01T10:00:00");

        ReportDataDTO reportData = new ReportDataDTO(
                "MISS-001",
                "Mars Exploration",
                "EXPLORATION",
                "HIGH",
                123L,
                456L,
                LocalDateTime.parse("2024-06-01T10:00:00"),
                LocalDateTime.parse("2024-12-01T10:00:00")
        );

        when(mapper.convertValue(message, ReportDataDTO.class)).thenReturn(reportData);
        doNothing().when(reportGenerationUseCase).generateReport(any(ReportDataDTO.class), anyString());

        
        listener.generateReport(message);

        
        verify(mapper, times(1)).convertValue(message, ReportDataDTO.class);
        verify(reportGenerationUseCase, times(1)).generateReport(eq(reportData), eq(reportFormat));
    }

    @Test
    void shouldPassCorrectReportFormatToUseCase() {
        HashMap<String, Object> message = new HashMap<>();
        ReportDataDTO reportData = new ReportDataDTO(
                "TEST-001",
                "Test Mission",
                "TEST",
                "LOW",
                1L,
                2L,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(30)
        );

        when(mapper.convertValue(message, ReportDataDTO.class)).thenReturn(reportData);
        doNothing().when(reportGenerationUseCase).generateReport(any(ReportDataDTO.class), anyString());

        
        listener.generateReport(message);

        
        verify(reportGenerationUseCase).generateReport(any(ReportDataDTO.class), eq("mission-%d-%s.pdf"));
    }

    @Test
    void shouldHandleMessageWithAllRequiredFields() {
        HashMap<String, Object> message = createCompleteMessage();
        ReportDataDTO reportData = createReportData();

        when(mapper.convertValue(message, ReportDataDTO.class)).thenReturn(reportData);
        doNothing().when(reportGenerationUseCase).generateReport(any(ReportDataDTO.class), anyString());

        
        listener.generateReport(message);

        
        verify(mapper).convertValue(message, ReportDataDTO.class);
        verify(reportGenerationUseCase).generateReport(reportData, reportFormat);
    }

    @Test
    void shouldHandleMessageWithDifferentMissionTypes() {
        String[] missionTypes = {"EXPLORATION", "CARGO", "PERSONNEL", "EMERGENCY"};

        for (String missionType : missionTypes) {
            HashMap<String, Object> message = createCompleteMessage();
            message.put("missionType", missionType);

            ReportDataDTO reportData = new ReportDataDTO(
                    "MISS-001",
                    "Test Mission",
                    missionType,
                    "HIGH",
                    1L,
                    2L,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusDays(30)
            );

            when(mapper.convertValue(message, ReportDataDTO.class)).thenReturn(reportData);
            doNothing().when(reportGenerationUseCase).generateReport(any(ReportDataDTO.class), anyString());

            
            listener.generateReport(message);

            
            verify(reportGenerationUseCase).generateReport(reportData, reportFormat);
        }
    }

    @Test
    void shouldHandleMessageWithDifferentPriorities() {
        String[] priorities = {"LOW", "MEDIUM", "HIGH", "CRITICAL"};

        for (String priority : priorities) {
            HashMap<String, Object> message = createCompleteMessage();
            message.put("priority", priority);

            ReportDataDTO reportData = new ReportDataDTO(
                    "MISS-001",
                    "Test Mission",
                    "EXPLORATION",
                    priority,
                    1L,
                    2L,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusDays(30)
            );

            when(mapper.convertValue(message, ReportDataDTO.class)).thenReturn(reportData);
            doNothing().when(reportGenerationUseCase).generateReport(any(ReportDataDTO.class), anyString());

            
            listener.generateReport(message);

            
            verify(reportGenerationUseCase).generateReport(reportData, reportFormat);
        }
    }

    @Test
    void shouldHandleMapperConversionCorrectly() {
        HashMap<String, Object> message = createCompleteMessage();
        ReportDataDTO expectedReportData = createReportData();

        when(mapper.convertValue(message, ReportDataDTO.class)).thenReturn(expectedReportData);
        doNothing().when(reportGenerationUseCase).generateReport(any(ReportDataDTO.class), anyString());

        
        listener.generateReport(message);

        
        verify(mapper, times(1)).convertValue(eq(message), eq(ReportDataDTO.class));
        verify(reportGenerationUseCase, times(1)).generateReport(expectedReportData, reportFormat);
    }

    @Test
    void shouldHandleMessageWithLongMissionCodes() {
        HashMap<String, Object> message = createCompleteMessage();
        message.put("missionCode", "VERY-LONG-MISSION-CODE-123456789");

        ReportDataDTO reportData = new ReportDataDTO(
                "VERY-LONG-MISSION-CODE-123456789",
                "Test Mission",
                "EXPLORATION",
                "HIGH",
                1L,
                2L,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(30)
        );

        when(mapper.convertValue(message, ReportDataDTO.class)).thenReturn(reportData);
        doNothing().when(reportGenerationUseCase).generateReport(any(ReportDataDTO.class), anyString());

        
        listener.generateReport(message);

        
        verify(reportGenerationUseCase).generateReport(reportData, reportFormat);
    }

    @Test
    void shouldHandleMessageWithLargeIds() {
        HashMap<String, Object> message = createCompleteMessage();
        message.put("commandingOfficerId", Long.MAX_VALUE);
        message.put("spacecraftId", Long.MAX_VALUE - 1);

        ReportDataDTO reportData = new ReportDataDTO(
                "MISS-001",
                "Test Mission",
                "EXPLORATION",
                "HIGH",
                Long.MAX_VALUE,
                Long.MAX_VALUE - 1,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(30)
        );

        when(mapper.convertValue(message, ReportDataDTO.class)).thenReturn(reportData);
        doNothing().when(reportGenerationUseCase).generateReport(any(ReportDataDTO.class), anyString());

        
        listener.generateReport(message);

        
        verify(reportGenerationUseCase).generateReport(reportData, reportFormat);
    }

    @Test
    void shouldHandleMessageWithDatesInFuture() {
        HashMap<String, Object> message = createCompleteMessage();
        LocalDateTime futureDate = LocalDateTime.now().plusYears(10);
        
        ReportDataDTO reportData = new ReportDataDTO(
                "MISS-001",
                "Future Mission",
                "EXPLORATION",
                "HIGH",
                1L,
                2L,
                futureDate,
                futureDate.plusMonths(6)
        );

        when(mapper.convertValue(message, ReportDataDTO.class)).thenReturn(reportData);
        doNothing().when(reportGenerationUseCase).generateReport(any(ReportDataDTO.class), anyString());

        
        listener.generateReport(message);

        
        verify(reportGenerationUseCase).generateReport(reportData, reportFormat);
    }

    
    private HashMap<String, Object> createCompleteMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put("missionCode", "MISS-001");
        message.put("missionName", "Test Mission");
        message.put("missionType", "EXPLORATION");
        message.put("priority", "HIGH");
        message.put("commandingOfficerId", 1L);
        message.put("spacecraftId", 2L);
        message.put("scheduledDeparture", "2024-06-01T10:00:00");
        message.put("scheduledArrival", "2024-12-01T10:00:00");
        return message;
    }

    private ReportDataDTO createReportData() {
        return new ReportDataDTO(
                "MISS-001",
                "Test Mission",
                "EXPLORATION",
                "HIGH",
                1L,
                2L,
                LocalDateTime.parse("2024-06-01T10:00:00"),
                LocalDateTime.parse("2024-12-01T10:00:00")
        );
    }
}

