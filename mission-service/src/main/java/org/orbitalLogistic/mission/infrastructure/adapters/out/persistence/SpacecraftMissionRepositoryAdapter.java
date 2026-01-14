package org.orbitalLogistic.mission.infrastructure.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.mission.application.ports.out.SpacecraftMissionRepository;
import org.orbitalLogistic.mission.domain.model.SpacecraftMission;
import org.orbitalLogistic.mission.infrastructure.adapters.out.persistence.entity.SpacecraftMissionJpaEntity;
import org.orbitalLogistic.mission.infrastructure.adapters.out.persistence.mapper.SpacecraftMissionPersistenceMapper;
import org.orbitalLogistic.mission.infrastructure.adapters.out.persistence.repository.SpacecraftMissionJdbcRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
public class SpacecraftMissionRepositoryAdapter implements SpacecraftMissionRepository {

    private final SpacecraftMissionJdbcRepository jdbcRepository;
    private final SpacecraftMissionPersistenceMapper mapper;

    @Override
    public SpacecraftMission save(SpacecraftMission spacecraftMission) {
        SpacecraftMissionJpaEntity entity = mapper.toEntity(spacecraftMission);
        SpacecraftMissionJpaEntity saved = jdbcRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<SpacecraftMission> findById(Long id) {
        return jdbcRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<SpacecraftMission> findAll() {
        return StreamSupport.stream(jdbcRepository.findAll().spliterator(), false)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<SpacecraftMission> findBySpacecraftId(Long spacecraftId) {
        return jdbcRepository.findBySpacecraftId(spacecraftId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<SpacecraftMission> findByMissionId(Long missionId) {
        return jdbcRepository.findByMissionId(missionId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsBySpacecraftIdAndMissionId(Long spacecraftId, Long missionId) {
        return jdbcRepository.existsBySpacecraftIdAndMissionId(spacecraftId, missionId);
    }

    @Override
    public boolean existsById(Long id) {
        return jdbcRepository.existsById(id);
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
