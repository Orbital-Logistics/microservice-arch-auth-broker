package org.orbitalLogistic.mission.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.mission.clients.SpacecraftDTO;
import org.orbitalLogistic.mission.clients.SpacecraftServiceClient;
import org.orbitalLogistic.mission.clients.UserDTO;
import org.orbitalLogistic.mission.clients.UserServiceClient;
import org.orbitalLogistic.mission.clients.resilient.ResilientSpacecraftService;
import org.orbitalLogistic.mission.clients.resilient.ResilientUserService;
import org.orbitalLogistic.mission.dto.common.PageResponseDTO;
import org.orbitalLogistic.mission.dto.request.MissionRequestDTO;
import org.orbitalLogistic.mission.dto.response.MissionResponseDTO;
import org.orbitalLogistic.mission.entities.Mission;
import org.orbitalLogistic.mission.entities.enums.MissionStatus;
import org.orbitalLogistic.mission.exceptions.InvalidOperationException;
import org.orbitalLogistic.mission.exceptions.MissionAlreadyExistsException;
import org.orbitalLogistic.mission.exceptions.MissionNotFoundException;
import org.orbitalLogistic.mission.exceptions.UserServiceException;
import org.orbitalLogistic.mission.exceptions.UserServiceNotFound;
import org.orbitalLogistic.mission.mappers.MissionMapper;
import org.orbitalLogistic.mission.repositories.MissionAssignmentRepository;
import org.orbitalLogistic.mission.repositories.MissionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository missionRepository;
    private final MissionAssignmentRepository missionAssignmentRepository;
    private final MissionMapper missionMapper;
    private final ResilientUserService userServiceClient;
    private final ResilientSpacecraftService spacecraftServiceClient;

    public PageResponseDTO<MissionResponseDTO> getAllMissions(int page, int size) {
        int offset = page * size;
        List<Mission> missions = missionRepository.findWithFilters(null, null, null, size, offset);
        long total = missionRepository.countWithFilters(null, null, null);

        List<MissionResponseDTO> missionDTOs = missions.stream()
                .map(this::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponseDTO<>(missionDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1);
    }

    public PageResponseDTO<MissionResponseDTO> searchMissions(String missionCode, String status, String missionType, int page, int size) {
        int offset = page * size;
        List<Mission> missions = missionRepository.findWithFilters(missionCode, status, missionType, size, offset);
        long total = missionRepository.countWithFilters(missionCode, status, missionType);

        List<MissionResponseDTO> missionDTOs = missions.stream()
                .map(this::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponseDTO<>(missionDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1);
    }

    public MissionResponseDTO getMissionById(Long id) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new MissionNotFoundException("Mission not found with id: " + id));
        return toResponseDTO(mission);
    }

    public MissionResponseDTO createMission(MissionRequestDTO request) {
        if (missionRepository.existsByMissionCode(request.missionCode())) {
            throw new MissionAlreadyExistsException("Mission with code already exists: " + request.missionCode());
        }
        // Проверяем результат!
        Boolean userExists = userServiceClient.userExists(request.commandingOfficerId());
        spacecraftServiceClient.getSpacecraftById(request.spacecraftId());
        
        if (userExists == null || !userExists) {
            throw new InvalidOperationException("Commanding officer not found with id: " + request.commandingOfficerId());
        }

        Mission mission = missionMapper.toEntity(request);
        Mission saved = missionRepository.save(mission);
        return toResponseDTO(saved);
    }

    public MissionResponseDTO updateMission(Long id, MissionRequestDTO request) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new MissionNotFoundException("Mission not found with id: " + id));

        if (!mission.getMissionCode().equals(request.missionCode()) &&
                missionRepository.existsByMissionCode(request.missionCode())) {
            throw new MissionAlreadyExistsException("Mission with code already exists: " + request.missionCode());
        }

        // Валидация commanding officer
        // try {
        Boolean userExists = userServiceClient.userExists(request.commandingOfficerId());
        if (userExists == null || !userExists) {
            throw new InvalidOperationException("Commanding officer not found with id: " + request.commandingOfficerId());
        }
        // } catch (Exception e) {
        //     log.error("Failed to validate commanding officer: {}", e.getMessage());
        //     throw new InvalidOperationException("Unable to validate commanding officer.");
        // }

        // Валидация spacecraft
        // try {
        Boolean spacecraftExists = spacecraftServiceClient.spacecraftExists(request.spacecraftId());
        if (spacecraftExists == null || !spacecraftExists) {
            throw new InvalidOperationException("Spacecraft not found with id: " + request.spacecraftId());
        }
        // } catch (Exception e) {
        //     log.error("Failed to validate spacecraft: {}", e.getMessage());
        //     throw new InvalidOperationException("Unable to validate spacecraft.");
        // }

        mission.setMissionCode(request.missionCode());
        mission.setMissionName(request.missionName());
        mission.setMissionType(request.missionType());
        mission.setPriority(request.priority());
        mission.setCommandingOfficerId(request.commandingOfficerId());
        mission.setSpacecraftId(request.spacecraftId());
        mission.setScheduledDeparture(request.scheduledDeparture());
        mission.setScheduledArrival(request.scheduledArrival());

        Mission updated = missionRepository.save(mission);
        return toResponseDTO(updated);
    }

    public MissionResponseDTO updateMissionStatus(Long id, MissionStatus status) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new MissionNotFoundException("Mission not found with id: " + id));

        mission.setStatus(status);
        Mission updated = missionRepository.save(mission);
        return toResponseDTO(updated);
    }

    public void deleteMission(Long id) {
        if (!missionRepository.existsById(id)) {
            throw new MissionNotFoundException("Mission not found with id: " + id);
        }
        missionRepository.deleteById(id);
    }

    public List<MissionResponseDTO> getMissionsByCommander(Long commanderId) {
        List<Mission> missions = missionRepository.findByCommandingOfficerId(commanderId);
        return missions.stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<MissionResponseDTO> getMissionsBySpacecraft(Long spacecraftId) {
        List<Mission> missions = missionRepository.findBySpacecraftId(spacecraftId);
        return missions.stream()
                .map(this::toResponseDTO)
                .toList();
    }

    private MissionResponseDTO toResponseDTO(Mission mission) {
        String commanderName = "Unknown";
        String spacecraftName = "Unknown";

        // try {
        UserDTO user = userServiceClient.getUserById(mission.getCommandingOfficerId());
        if (user != null) {
            commanderName = user.username();
        }
        // } catch (Exception e) {
        //     log.warn("Failed to fetch commander name for mission {}: {}", mission.getId(), e.getMessage());
        // }

        // try {
        SpacecraftDTO spacecraft = spacecraftServiceClient.getSpacecraftById(mission.getSpacecraftId());
        if (spacecraft != null) {
            spacecraftName = spacecraft.name();
        }
        // } catch (Exception e) {
        //     log.warn("Failed to fetch spacecraft name for mission {}: {}", mission.getId(), e.getMessage());
        // }

        Integer crewCount = missionAssignmentRepository.countByMissionId(mission.getId());

        return missionMapper.toResponseDTO(mission, commanderName, spacecraftName, crewCount);
    }
}

