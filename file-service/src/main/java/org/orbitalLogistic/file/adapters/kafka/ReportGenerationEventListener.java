package org.orbitalLogistic.file.adapters.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.file.adapters.kafka.dto.CargoReportDataDTO;
import org.orbitalLogistic.file.adapters.kafka.dto.UserReportDataDTO;
import org.orbitalLogistic.file.application.usecase.ReportGenerationUseCase;
import org.orbitalLogistic.file.adapters.kafka.dto.MissionReportDataDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReportGenerationEventListener {

    private final ObjectMapper mapper;
    private final ReportGenerationUseCase reportGenerationUseCase;

    @Value("${minio.formats.mission-reports}")
    private String missionReportsFormat;

    @Value("${minio.formats.cargo-reports}")
    private String cargoReportsFormat;

    @Value("${minio.formats.user-reports}")
    private String userReportsFormat;

    /**
     * This method is called when rental is ended, cancelled or force ended.
     * @param message - ReportDataDto as HashMap
     */
    @KafkaListener(topics = "mission-reports-data", groupId = "file-service")
    public void generateMissionReport(HashMap<String, Object> message) {
        MissionReportDataDTO reportData = mapper.convertValue(message, MissionReportDataDTO.class);
        log.info("Generate mission report. " + reportData);
        reportGenerationUseCase.generateMissionReport(reportData, missionReportsFormat);
    }

    @KafkaListener(topics = "user-reports-data", groupId = "file-service")
    public void generateUserReport(HashMap<String, Object> message) {
        UserReportDataDTO reportData = mapper.convertValue(message, UserReportDataDTO.class);
        log.info("Generate user report. " + reportData);
        reportGenerationUseCase.generateUserReport(reportData, userReportsFormat);
    }

    @KafkaListener(topics = "cargo-reports-data", groupId = "file-service")
    public void generateCargoReport(HashMap<String, Object> message) {
        CargoReportDataDTO reportData = mapper.convertValue(message, CargoReportDataDTO.class);
        log.info("Generate cargo report. " + reportData);
        reportGenerationUseCase.generateCargoReport(reportData, cargoReportsFormat);
    }
}
