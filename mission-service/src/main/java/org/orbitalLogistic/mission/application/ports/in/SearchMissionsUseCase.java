package org.orbitalLogistic.mission.application.ports.in;

import org.orbitalLogistic.mission.domain.model.Mission;

import java.util.List;

public interface SearchMissionsUseCase {
    List<Mission> searchMissions(String missionCode, String status, String missionType, int page, int size);
    long countMissions(String missionCode, String status, String missionType);
}
