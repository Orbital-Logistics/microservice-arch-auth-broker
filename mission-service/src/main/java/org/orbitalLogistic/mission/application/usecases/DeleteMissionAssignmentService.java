package org.orbitalLogistic.mission.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.mission.application.ports.in.DeleteMissionAssignmentUseCase;
import org.orbitalLogistic.mission.application.ports.out.MissionAssignmentRepository;
import org.orbitalLogistic.mission.domain.exception.InvalidOperationException;
import org.orbitalLogistic.mission.domain.exception.MissionAssignmentNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteMissionAssignmentService implements DeleteMissionAssignmentUseCase {

    private final MissionAssignmentRepository assignmentRepository;

    @Override
    @Transactional
    public void deleteAssignment(Long id) {
        log.debug("Deleting mission assignment with id: {}", id);
        
        if (!assignmentRepository.existsById(id)) {
            throw new MissionAssignmentNotFoundException("Mission assignment not found with id: " + id);
        }
        
        try {
            assignmentRepository.deleteById(id);
            log.info("Mission assignment deleted with id: {}", id);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new InvalidOperationException(
                "Cannot delete mission assignment with id: " + id + ". It is referenced by other entities."
            );
        }
    }
}
