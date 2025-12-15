package org.orbitalLogistic.mission.services;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.mission.clients.UserDTO;
import org.orbitalLogistic.mission.clients.UserServiceClient;
import org.orbitalLogistic.mission.clients.resilient.ResilientUserService;
import org.orbitalLogistic.mission.dto.common.PageResponseDTO;
import org.orbitalLogistic.mission.dto.request.MissionAssignmentRequestDTO;
import org.orbitalLogistic.mission.dto.response.MissionAssignmentResponseDTO;
import org.orbitalLogistic.mission.entities.Mission;
import org.orbitalLogistic.mission.entities.MissionAssignment;
import org.orbitalLogistic.mission.exceptions.InvalidOperationException;
import org.orbitalLogistic.mission.exceptions.MissionAssignmentNotFoundException;
import org.orbitalLogistic.mission.exceptions.MissionNotFoundException;
import org.orbitalLogistic.mission.mappers.MissionAssignmentMapper;
import org.orbitalLogistic.mission.repositories.MissionAssignmentRepository;
import org.orbitalLogistic.mission.repositories.MissionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MissionAssignmentService {

    private final MissionAssignmentRepository missionAssignmentRepository;
    private final MissionRepository missionRepository;
    private final MissionAssignmentMapper missionAssignmentMapper;
    private final ResilientUserService userServiceClient;

    public PageResponseDTO<MissionAssignmentResponseDTO> getAllAssignments(int page, int size) {
        int offset = page * size;
        List<MissionAssignment> assignments = missionAssignmentRepository.findWithFilters(null, null, size, offset);
        long total = missionAssignmentRepository.countWithFilters(null, null);

        List<MissionAssignmentResponseDTO> assignmentDTOs = assignments.stream()
                .map(this::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);

        return new PageResponseDTO<>(assignmentDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1);
    }

    public MissionAssignmentResponseDTO getAssignmentById(Long id) {
        MissionAssignment assignment = missionAssignmentRepository.findById(id)
                .orElseThrow(() -> new MissionAssignmentNotFoundException("Mission assignment not found with id: " + id));
        return toResponseDTO(assignment);
    }

    public List<MissionAssignmentResponseDTO> getAssignmentsByMission(Long missionId) {
        List<MissionAssignment> assignments = missionAssignmentRepository.findByMissionId(missionId);
        return assignments.stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<MissionAssignmentResponseDTO> getAssignmentsByUser(Long userId) {
        List<MissionAssignment> assignments = missionAssignmentRepository.findByUserId(userId);
        return assignments.stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public MissionAssignmentResponseDTO createAssignment(MissionAssignmentRequestDTO request) {
        Mission mission = missionRepository.findById(request.missionId())
                .orElseThrow(() -> new MissionNotFoundException("Mission not found with id: " + request.missionId()));

        try {
            Boolean userExists = userServiceClient.userExists(request.userId());
            if (userExists == null || !userExists) {
                throw new InvalidOperationException("User not found with id: " + request.userId());
            }
        } catch (Exception e) {
            log.error("Failed to validate user: {}", e.getMessage());
            throw new InvalidOperationException("Unable to validate user. User service may be unavailable.");
        }

        List<MissionAssignment> existing = missionAssignmentRepository.findByMissionIdAndUserId(
                request.missionId(), request.userId());
        if (!existing.isEmpty()) {
            throw new InvalidOperationException("User already assigned to this mission");
        }

        MissionAssignment assignment = missionAssignmentMapper.toEntity(request);
        MissionAssignment saved = missionAssignmentRepository.save(assignment);
        return toResponseDTO(saved);
    }

    public void deleteAssignment(Long id) {
        if (!missionAssignmentRepository.existsById(id)) {
            throw new MissionAssignmentNotFoundException("Mission assignment not found with id: " + id);
        }
        missionAssignmentRepository.deleteById(id);
    }

    private MissionAssignmentResponseDTO toResponseDTO(MissionAssignment assignment) {
        String missionName = "Unknown";
        String userName = "Unknown";

        try {
            Mission mission = missionRepository.findById(assignment.getMissionId()).orElse(null);
            if (mission != null) {
                missionName = mission.getMissionName();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch mission name: {}", e.getMessage());
        }

        try {
            UserDTO user = userServiceClient.getUserById(assignment.getUserId());
            if (user != null) {
                userName = user.username();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch user name: {}", e.getMessage());
        }

        return missionAssignmentMapper.toResponseDTO(assignment, missionName, userName);
    }
}

