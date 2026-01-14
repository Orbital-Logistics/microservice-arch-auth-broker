package org.orbitalLogistic.mission.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.mission.application.ports.in.CreateMissionAssignmentUseCase;
import org.orbitalLogistic.mission.application.ports.out.MissionAssignmentRepository;
import org.orbitalLogistic.mission.application.ports.out.MissionRepository;
import org.orbitalLogistic.mission.application.ports.out.UserServicePort;
import org.orbitalLogistic.mission.domain.exception.InvalidOperationException;
import org.orbitalLogistic.mission.domain.exception.MissionNotFoundException;
import org.orbitalLogistic.mission.domain.exception.UserServiceNotFound;
import org.orbitalLogistic.mission.domain.model.MissionAssignment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateMissionAssignmentService implements CreateMissionAssignmentUseCase {

    private final MissionAssignmentRepository assignmentRepository;
    private final MissionRepository missionRepository;
    private final UserServicePort userServicePort;

    @Override
    @Transactional
    public MissionAssignment createAssignment(MissionAssignment assignment) {
        log.debug("Creating mission assignment for mission id: {} and user id: {}", 
                  assignment.getMissionId(), assignment.getUserId());
        
        if (!missionRepository.existsById(assignment.getMissionId())) {
            throw new MissionNotFoundException("Mission not found with id: " + assignment.getMissionId());
        }

        if (!userServicePort.userExists(assignment.getUserId())) {
            throw new UserServiceNotFound("User not found with id: " + assignment.getUserId());
        }

        List<MissionAssignment> existing = assignmentRepository.findByMissionIdAndUserId(
                assignment.getMissionId(), assignment.getUserId());
        if (!existing.isEmpty()) {
            throw new InvalidOperationException("User already assigned to this mission");
        }

        MissionAssignment saved = assignmentRepository.save(assignment);
        log.info("Mission assignment created with id: {}", saved.getId());
        return saved;
    }
}
