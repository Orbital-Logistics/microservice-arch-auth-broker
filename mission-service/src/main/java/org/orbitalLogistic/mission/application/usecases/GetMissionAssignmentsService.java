package org.orbitalLogistic.mission.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.mission.application.ports.in.GetMissionAssignmentsUseCase;
import org.orbitalLogistic.mission.application.ports.out.MissionAssignmentRepository;
import org.orbitalLogistic.mission.domain.model.MissionAssignment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetMissionAssignmentsService implements GetMissionAssignmentsUseCase {

    private final MissionAssignmentRepository assignmentRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<MissionAssignment> getById(Long id) {
        log.debug("Finding mission assignment by id: {}", id);
        return assignmentRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MissionAssignment> getByMissionId(Long missionId) {
        log.debug("Finding mission assignments by mission id: {}", missionId);
        return assignmentRepository.findByMissionId(missionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MissionAssignment> getByUserId(Long userId) {
        log.debug("Finding mission assignments by user id: {}", userId);
        return assignmentRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MissionAssignment> searchAssignments(Long missionId, Long userId, int page, int size) {
        log.debug("Searching mission assignments with filters - missionId: {}, userId: {}, page: {}, size: {}", 
                  missionId, userId, page, size);
        int offset = page * size;
        return assignmentRepository.findWithFilters(missionId, userId, size, offset);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAssignments(Long missionId, Long userId) {
        log.debug("Counting mission assignments with filters - missionId: {}, userId: {}", missionId, userId);
        return assignmentRepository.countWithFilters(missionId, userId);
    }
}
