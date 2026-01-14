package org.orbitalLogistic.mission.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.mission.application.ports.in.DeleteMissionUseCase;
import org.orbitalLogistic.mission.application.ports.out.MissionRepository;
import org.orbitalLogistic.mission.domain.exception.InvalidOperationException;
import org.orbitalLogistic.mission.domain.exception.MissionNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteMissionService implements DeleteMissionUseCase {

    private final MissionRepository missionRepository;

    @Override
    @Transactional
    public void deleteMission(Long id) {
        log.debug("Deleting mission with id: {}", id);
        
        if (!missionRepository.existsById(id)) {
            throw new MissionNotFoundException("Mission not found with id: " + id);
        }
        
        try {
            missionRepository.deleteById(id);
            log.info("Mission deleted with id: {}", id);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new InvalidOperationException(
                "Cannot delete mission with id: " + id + ". It is referenced by other entities (assignments, etc.)."
            );
        }
    }
}
