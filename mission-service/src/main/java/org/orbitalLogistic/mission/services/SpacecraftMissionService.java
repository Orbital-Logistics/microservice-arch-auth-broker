package org.orbitalLogistic.mission.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.mission.clients.SpacecraftDTO;
import org.orbitalLogistic.mission.clients.resilient.ResilientSpacecraftService;
import org.orbitalLogistic.mission.dto.request.SpacecraftMissionRequestDTO;
import org.orbitalLogistic.mission.dto.response.SpacecraftMissionResponseDTO;
import org.orbitalLogistic.mission.entities.Mission;
import org.orbitalLogistic.mission.entities.SpacecraftMission;
import org.orbitalLogistic.mission.exceptions.MissionNotFoundException;
import org.orbitalLogistic.mission.exceptions.MissionSpacecraftExistsException;
import org.orbitalLogistic.mission.mappers.SpacecraftMissionMapper;
import org.orbitalLogistic.mission.repositories.MissionRepository;
import org.orbitalLogistic.mission.repositories.SpacecraftMissionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpacecraftMissionService {

    private final SpacecraftMissionRepository spacecraftMissionRepository;
    private final MissionRepository missionRepository;
    private final SpacecraftMissionMapper spacecraftMissionMapper;
    private final ResilientSpacecraftService spacecraftServiceClient;

    public List<SpacecraftMissionResponseDTO> getAllSpacecraftMissions() {
        List<SpacecraftMission> spacecraftMissions = (List<SpacecraftMission>) spacecraftMissionRepository.findAll();
        return spacecraftMissions.stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<SpacecraftMissionResponseDTO> getBySpacecraft(Long spacecraftId) {
        List<SpacecraftMission> spacecraftMissions = spacecraftMissionRepository.findBySpacecraftId(spacecraftId);
        return spacecraftMissions.stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<SpacecraftMissionResponseDTO> getByMission(Long missionId) {
        List<SpacecraftMission> spacecraftMissions = spacecraftMissionRepository.findByMissionId(missionId);
        return spacecraftMissions.stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public SpacecraftMissionResponseDTO createSpacecraftMission(SpacecraftMissionRequestDTO request) {
        if (!missionRepository.existsById(request.missionId())) {
            throw new MissionNotFoundException("Mission not found with id: " + request.missionId());
        }

        if (spacecraftMissionRepository.existsBySpacecraftIdAndMissionId(request.spacecraftId(), request.missionId())) {
            throw new MissionSpacecraftExistsException("Such combination of mission id and spacecraft id is already exists!");
        }

        SpacecraftMission spacecraftMission = spacecraftMissionMapper.toEntity(request);
        SpacecraftMission saved = spacecraftMissionRepository.save(spacecraftMission);
        return toResponseDTO(saved);
    }

    private SpacecraftMissionResponseDTO toResponseDTO(SpacecraftMission spacecraftMission) {
        String spacecraftName = "Unknown";
        String missionName = "Unknown";

        try {
            SpacecraftDTO spacecraft = spacecraftServiceClient.getSpacecraftById(spacecraftMission.getSpacecraftId());
            if (spacecraft != null) {
                spacecraftName = spacecraft.name();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch spacecraft name: {}", e.getMessage());
        }

        try {
            Mission mission = missionRepository.findById(spacecraftMission.getMissionId()).orElse(null);
            if (mission != null) {
                missionName = mission.getMissionName();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch mission name: {}", e.getMessage());
        }

        return spacecraftMissionMapper.toResponseDTO(spacecraftMission, spacecraftName, missionName);
    }
}

