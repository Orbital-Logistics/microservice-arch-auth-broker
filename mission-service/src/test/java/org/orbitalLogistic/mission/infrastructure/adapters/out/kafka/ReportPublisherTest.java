package org.orbitalLogistic.mission.infrastructure.adapters.out.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.mission.application.ports.in.CreateMissionCommand;
import org.orbitalLogistic.mission.domain.model.enums.MissionPriority;
import org.orbitalLogistic.mission.domain.model.enums.MissionType;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportPublisher Unit Tests")
class ReportPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Captor
    private ArgumentCaptor<String> topicCaptor;

    @Captor
    private ArgumentCaptor<Object> messageCaptor;

    private ReportPublisher reportPublisher;

    @BeforeEach
    void setUp() {
        reportPublisher = new ReportPublisher(kafkaTemplate);
    }

    @Test
    @DisplayName("send должен отправить команду в топик mission-reports-data")
    void send_ValidCommand_SendsToKafka() {
        CreateMissionCommand command = new CreateMissionCommand(
                "TEST-001",
                "Test Mission",
                MissionType.CARGO_TRANSPORT,
                MissionPriority.HIGH,
                1L,
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(10)
        );

        reportPublisher.send(command);

        verify(kafkaTemplate, times(1)).send(topicCaptor.capture(), messageCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo("mission-reports-data");
        assertThat(messageCaptor.getValue()).isEqualTo(command);
    }

    @Test
    @DisplayName("send должен отправить команду с различными типами миссий")
    void send_DifferentMissionTypes_SendsToKafka() {
        MissionType[] missionTypes = MissionType.values();

        for (MissionType type : missionTypes) {
            CreateMissionCommand command = new CreateMissionCommand(
                    "TEST-" + type.name(),
                    "Test Mission " + type.name(),
                    type,
                    MissionPriority.MEDIUM,
                    1L,
                    1L,
                    LocalDateTime.now().plusDays(1),
                    LocalDateTime.now().plusDays(10)
            );

            reportPublisher.send(command);
        }

        verify(kafkaTemplate, times(missionTypes.length)).send(eq("mission-reports-data"), any(CreateMissionCommand.class));
    }

    @Test
    @DisplayName("send должен отправить команду с различными приоритетами")
    void send_DifferentPriorities_SendsToKafka() {
        MissionPriority[] priorities = MissionPriority.values();

        for (MissionPriority priority : priorities) {
            CreateMissionCommand command = new CreateMissionCommand(
                    "TEST-" + priority.name(),
                    "Test Mission " + priority.name(),
                    MissionType.CARGO_TRANSPORT,
                    priority,
                    1L,
                    1L,
                    LocalDateTime.now().plusDays(1),
                    LocalDateTime.now().plusDays(10)
            );

            reportPublisher.send(command);
        }

        verify(kafkaTemplate, times(priorities.length)).send(eq("mission-reports-data"), any(CreateMissionCommand.class));
    }

    @Test
    @DisplayName("send должен отправить команду с минимальными данными")
    void send_MinimalCommand_SendsToKafka() {
        CreateMissionCommand command = new CreateMissionCommand(
                "MIN-001",
                "Minimal Mission",
                MissionType.SCIENCE_EXPEDITION,
                MissionPriority.LOW,
                1L,
                1L,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1)
        );

        reportPublisher.send(command);

        verify(kafkaTemplate, times(1)).send("mission-reports-data", command);
    }

    @Test
    @DisplayName("send должен корректно обработать команду с граничными датами")
    void send_CommandWithEdgeDates_SendsToKafka() {
        LocalDateTime farFuture = LocalDateTime.now().plusYears(10);
        
        CreateMissionCommand command = new CreateMissionCommand(
                "LONG-001",
                "Long Term Mission",
                MissionType.PERSONNEL_TRANSPORT,
                MissionPriority.HIGH,
                1L,
                1L,
                LocalDateTime.now().plusDays(1),
                farFuture
        );

        reportPublisher.send(command);

        verify(kafkaTemplate, times(1)).send(topicCaptor.capture(), messageCaptor.capture());
        
        CreateMissionCommand sentCommand = (CreateMissionCommand) messageCaptor.getValue();
        assertThat(sentCommand.scheduledArrival()).isEqualTo(farFuture);
    }

    @Test
    @DisplayName("send должен отправлять каждую команду независимо")
    void send_MultipleCalls_SendsEachIndependently() {
        CreateMissionCommand command1 = new CreateMissionCommand(
                "TEST-001",
                "First Mission",
                MissionType.CARGO_TRANSPORT,
                MissionPriority.HIGH,
                1L,
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(10)
        );

        CreateMissionCommand command2 = new CreateMissionCommand(
                "TEST-002",
                "Second Mission",
                MissionType.PERSONNEL_TRANSPORT,
                MissionPriority.MEDIUM,
                2L,
                2L,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(12)
        );

        reportPublisher.send(command1);
        reportPublisher.send(command2);

        verify(kafkaTemplate, times(2)).send(eq("mission-reports-data"), any(CreateMissionCommand.class));
    }

    @Test
    @DisplayName("send не должен выбрасывать исключение при отправке")
    void send_ValidCommand_DoesNotThrowException() {
        CreateMissionCommand command = new CreateMissionCommand(
                "TEST-001",
                "Test Mission",
                MissionType.CARGO_TRANSPORT,
                MissionPriority.HIGH,
                1L,
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(10)
        );

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> reportPublisher.send(command));
    }
}
