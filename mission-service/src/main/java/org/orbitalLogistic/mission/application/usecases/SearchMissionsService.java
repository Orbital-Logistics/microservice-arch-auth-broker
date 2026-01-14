package org.orbitalLogistic.mission.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.mission.application.ports.in.SearchMissionsUseCase;
import org.orbitalLogistic.mission.application.ports.out.MissionRepository;
import org.orbitalLogistic.mission.domain.model.Mission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchMissionsService implements SearchMissionsUseCase {

    private final MissionRepository missionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Mission> searchMissions(String missionCode, String status, String missionType, int page, int size) {
        log.debug("Searching missions with filters - code: {}, status: {}, type: {}, page: {}, size: {}", 
                  missionCode, status, missionType, page, size);
        int offset = page * size;
        return missionRepository.findWithFilters(missionCode, status, missionType, size, offset);
    }

    @Override
    @Transactional(readOnly = true)
    public long countMissions(String missionCode, String status, String missionType) {
        log.debug("Counting missions with filters - code: {}, status: {}, type: {}", 
                  missionCode, status, missionType);
        return missionRepository.countWithFilters(missionCode, status, missionType);
    }
}
