package org.orbitalLogistic.mission.infrastructure.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.mission.application.ports.out.MissionRepository;
import org.orbitalLogistic.mission.domain.model.Mission;
import org.orbitalLogistic.mission.domain.model.enums.MissionStatus;
import org.orbitalLogistic.mission.infrastructure.adapters.out.persistence.entity.MissionJpaEntity;
import org.orbitalLogistic.mission.infrastructure.adapters.out.persistence.mapper.MissionPersistenceMapper;
import org.orbitalLogistic.mission.infrastructure.adapters.out.persistence.repository.MissionJdbcRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
public class MissionRepositoryAdapter implements MissionRepository {

    private final MissionJdbcRepository jdbcRepository;
    private final MissionPersistenceMapper mapper;

    @Override
    public Mission save(Mission mission) {
        MissionJpaEntity entity = mapper.toEntity(mission);
        MissionJpaEntity saved = jdbcRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Mission> findById(Long id) {
        return jdbcRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Mission> findByMissionCode(String missionCode) {
        return jdbcRepository.findByMissionCode(missionCode)
                .map(mapper::toDomain);
    }

    @Override
    public List<Mission> findAll() {
        return StreamSupport.stream(jdbcRepository.findAll().spliterator(), false)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Mission> findByStatus(MissionStatus status) {
        return jdbcRepository.findByStatus(status).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Mission> findByCommandingOfficerId(Long commandingOfficerId) {
        return jdbcRepository.findByCommandingOfficerId(commandingOfficerId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Mission> findBySpacecraftId(Long spacecraftId) {
        return jdbcRepository.findBySpacecraftId(spacecraftId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Mission> findWithFilters(String missionCode, String status, String missionType, int limit, int offset) {
        return jdbcRepository.findWithFilters(missionCode, status, missionType, limit, offset).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countWithFilters(String missionCode, String status, String missionType) {
        return jdbcRepository.countWithFilters(missionCode, status, missionType);
    }

    @Override
    public boolean existsById(Long id) {
        return jdbcRepository.existsById(id);
    }

    @Override
    public boolean existsByMissionCode(String missionCode) {
        return jdbcRepository.existsByMissionCode(missionCode);
    }

    @Override
    public void deleteById(Long id) {
        jdbcRepository.deleteById(id);
    }

    @Override
    public void deleteAll() {
        jdbcRepository.deleteAll();
    }
}
