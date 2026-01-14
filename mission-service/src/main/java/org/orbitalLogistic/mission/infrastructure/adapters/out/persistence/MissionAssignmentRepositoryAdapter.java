package org.orbitalLogistic.mission.infrastructure.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.mission.application.ports.out.MissionAssignmentRepository;
import org.orbitalLogistic.mission.domain.model.MissionAssignment;
import org.orbitalLogistic.mission.infrastructure.adapters.out.persistence.entity.MissionAssignmentJpaEntity;
import org.orbitalLogistic.mission.infrastructure.adapters.out.persistence.mapper.MissionAssignmentPersistenceMapper;
import org.orbitalLogistic.mission.infrastructure.adapters.out.persistence.repository.MissionAssignmentJdbcRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
public class MissionAssignmentRepositoryAdapter implements MissionAssignmentRepository {

    private final MissionAssignmentJdbcRepository jdbcRepository;
    private final MissionAssignmentPersistenceMapper mapper;

    @Override
    public MissionAssignment save(MissionAssignment missionAssignment) {
        MissionAssignmentJpaEntity entity = mapper.toEntity(missionAssignment);
        MissionAssignmentJpaEntity saved = jdbcRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<MissionAssignment> findById(Long id) {
        return jdbcRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<MissionAssignment> findAll() {
        return StreamSupport.stream(jdbcRepository.findAll().spliterator(), false)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<MissionAssignment> findByMissionId(Long missionId) {
        return jdbcRepository.findByMissionId(missionId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<MissionAssignment> findByUserId(Long userId) {
        return jdbcRepository.findByUserId(userId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<MissionAssignment> findByMissionIdAndUserId(Long missionId, Long userId) {
        return jdbcRepository.findByMissionIdAndUserId(missionId, userId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<MissionAssignment> findWithFilters(Long missionId, Long userId, int limit, int offset) {
        return jdbcRepository.findWithFilters(missionId, userId, limit, offset).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countWithFilters(Long missionId, Long userId) {
        return jdbcRepository.countWithFilters(missionId, userId);
    }

    @Override
    public int countByMissionId(Long missionId) {
        return jdbcRepository.countByMissionId(missionId);
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
