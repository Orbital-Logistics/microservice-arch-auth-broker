package org.orbitalLogistic.spacecraft.services;

import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.spacecraft.clients.ResilientCargoServiceClient;
import org.orbitalLogistic.spacecraft.dto.common.SpacecraftCargoUsageDTO;
import org.orbitalLogistic.spacecraft.dto.common.PageResponseDTO;
import org.orbitalLogistic.spacecraft.dto.request.SpacecraftRequestDTO;
import org.orbitalLogistic.spacecraft.dto.response.SpacecraftResponseDTO;
import org.orbitalLogistic.spacecraft.entities.Spacecraft;
import org.orbitalLogistic.spacecraft.entities.enums.SpacecraftStatus;
import org.orbitalLogistic.spacecraft.entities.SpacecraftType;
import org.orbitalLogistic.spacecraft.exceptions.DataNotFoundException;
import org.orbitalLogistic.spacecraft.exceptions.SpacecraftAlreadyExistsException;
import org.orbitalLogistic.spacecraft.exceptions.SpacecraftNotFoundException;
import org.orbitalLogistic.spacecraft.mappers.SpacecraftMapper;
import org.orbitalLogistic.spacecraft.repositories.SpacecraftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@Validated
public class SpacecraftService {

    private final SpacecraftRepository spacecraftRepository;
    private final SpacecraftMapper spacecraftMapper;
    private final JdbcTemplate jdbcTemplate;
    private final ResilientCargoServiceClient cargoServiceClient;

    private SpacecraftTypeService spacecraftTypeService;

    public SpacecraftService(SpacecraftRepository spacecraftRepository,
                            SpacecraftMapper spacecraftMapper,
                            JdbcTemplate jdbcTemplate,
                            ResilientCargoServiceClient cargoServiceClient) {
        this.spacecraftRepository = spacecraftRepository;
        this.spacecraftMapper = spacecraftMapper;
        this.jdbcTemplate = jdbcTemplate;
        this.cargoServiceClient = cargoServiceClient;
    }

    @Autowired
    public void setSpacecraftTypeService(@Lazy SpacecraftTypeService spacecraftTypeService) {
        this.spacecraftTypeService = spacecraftTypeService;
    }

    public Mono<PageResponseDTO<SpacecraftResponseDTO>> getSpacecrafts(String name, String status, int page, int size) {
        return Mono.fromCallable(() -> getSpacecraftsBlocking(name, status, page, size))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public PageResponseDTO<SpacecraftResponseDTO> getSpacecraftsBlocking(String name, String status, int page, int size) {
        int offset = page * size;
        List<Spacecraft> spacecrafts = spacecraftRepository.findWithFilters(name, status, size, offset);
        long total = spacecraftRepository.countWithFilters(name, status);

        List<SpacecraftResponseDTO> spacecraftDTOs = spacecrafts.stream()
                .map(this::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponseDTO<>(spacecraftDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1);
    }

    public Mono<List<SpacecraftResponseDTO>> getSpacecraftsScroll(int page, int size) {
        return Mono.fromCallable(() -> getSpacecraftsScrollBlocking(page, size))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public List<SpacecraftResponseDTO> getSpacecraftsScrollBlocking(int page, int size) {
        int offset = page * size;
        List<Spacecraft> spacecrafts = spacecraftRepository.findWithFilters(null, null, size + 1, offset);

        return spacecrafts.stream()
                .limit(size)
                .map(this::toResponseDTO)
                .toList();
    }

    public Mono<SpacecraftResponseDTO> getSpacecraftById(Long id) {
        return Mono.fromCallable(() -> getSpacecraftByIdBlocking(id))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public SpacecraftResponseDTO getSpacecraftByIdBlocking(Long id) {
        Spacecraft spacecraft = spacecraftRepository.findById(id)
                .orElseThrow(() -> new SpacecraftNotFoundException("Spacecraft not found with id: " + id));
        return toResponseDTO(spacecraft);
    }

    public Mono<SpacecraftResponseDTO> createSpacecraft(SpacecraftRequestDTO request) {
        return Mono.fromCallable(() -> createSpacecraftBlocking(request))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public SpacecraftResponseDTO createSpacecraftBlocking(SpacecraftRequestDTO request) {
        if (spacecraftRepository.existsByRegistryCode(request.registryCode())) {
            throw new SpacecraftAlreadyExistsException("Spacecraft with registry code already exists: " + request.registryCode());
        }

        spacecraftTypeService.getEntityByIdBlocking(request.spacecraftTypeId());

        String sql = "INSERT INTO spacecraft " +
                     "(registry_code, name, spacecraft_type_id, mass_capacity, volume_capacity, status, current_location) " +
                     "VALUES (?, ?, ?, ?, ?, ?::spacecraft_status_enum, ?) " +
                     "RETURNING id";

        Long newId = jdbcTemplate.queryForObject(sql, Long.class,
                request.registryCode(),
                request.name(),
                request.spacecraftTypeId(),
                request.massCapacity(),
                request.volumeCapacity(),
                request.status().name(),
                request.currentLocation()
        );

        Spacecraft saved = spacecraftRepository.findById(newId)
                .orElseThrow(() -> new DataNotFoundException("Failed to create spacecraft"));

        return toResponseDTO(saved);
    }

    public Mono<SpacecraftResponseDTO> updateSpacecraft(Long id, SpacecraftRequestDTO request) {
        return Mono.fromCallable(() -> updateSpacecraftBlocking(id, request))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public SpacecraftResponseDTO updateSpacecraftBlocking(Long id, SpacecraftRequestDTO request) {
        Spacecraft spacecraft = spacecraftRepository.findById(id)
                .orElseThrow(() -> new SpacecraftNotFoundException("Spacecraft not found with id: " + id));

        if (!spacecraft.getRegistryCode().equals(request.registryCode()) &&
            spacecraftRepository.existsByRegistryCode(request.registryCode())) {
            throw new SpacecraftAlreadyExistsException("Spacecraft with registry code already exists: " + request.registryCode());
        }

        spacecraftTypeService.getEntityByIdBlocking(request.spacecraftTypeId());

        String sql = "UPDATE spacecraft SET " +
                     "registry_code = ?, " +
                     "name = ?, " +
                     "spacecraft_type_id = ?, " +
                     "mass_capacity = ?, " +
                     "volume_capacity = ?, " +
                     "status = ?::spacecraft_status_enum, " +
                     "current_location = ? " +
                     "WHERE id = ?";

        jdbcTemplate.update(sql,
                request.registryCode(),
                request.name(),
                request.spacecraftTypeId(),
                request.massCapacity(),
                request.volumeCapacity(),
                request.status().name(),
                request.currentLocation(),
                id
        );

        spacecraft.setRegistryCode(request.registryCode());
        spacecraft.setName(request.name());
        spacecraft.setSpacecraftTypeId(request.spacecraftTypeId());
        spacecraft.setMassCapacity(request.massCapacity());
        spacecraft.setVolumeCapacity(request.volumeCapacity());
        spacecraft.setStatus(request.status() != null ? request.status() : spacecraft.getStatus());
        spacecraft.setCurrentLocation(request.currentLocation());

        return toResponseDTO(spacecraft);
    }

    public void deleteSpacecraft(Long id) {
        Mono.<Void>fromRunnable(() -> deleteSpacecraftBlocking(id))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public void deleteSpacecraftBlocking(Long id) {
        if (!spacecraftRepository.existsById(id)) {
            throw new SpacecraftNotFoundException("Spacecraft not found with id: " + id);
        }
        try {
            spacecraftRepository.deleteById(id);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new org.orbitalLogistic.spacecraft.exceptions.DataNotFoundException(
                "Cannot delete spacecraft with id: " + id + ". It is referenced by other entities (missions, maintenance logs, etc.)."
            );
        }
    }

    public Mono<List<SpacecraftResponseDTO>> getAvailableSpacecrafts() {
        return Mono.fromCallable(this::getAvailableSpacecraftsBlocking)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public List<SpacecraftResponseDTO> getAvailableSpacecraftsBlocking() {
        return spacecraftRepository.findAvailableForMission().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public Mono<SpacecraftResponseDTO> updateSpacecraftStatus(Long id, SpacecraftStatus status) {
        return Mono.fromCallable(() -> updateSpacecraftStatusBlocking(id, status))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public SpacecraftResponseDTO updateSpacecraftStatusBlocking(Long id, SpacecraftStatus status) {
        Spacecraft spacecraft = spacecraftRepository.findById(id)
                .orElseThrow(() -> new SpacecraftNotFoundException("Spacecraft not found with id: " + id));

        String sql = "UPDATE spacecraft SET status = ?::spacecraft_status_enum WHERE id = ?";
        jdbcTemplate.update(sql, status.name(), id);

        spacecraft.setStatus(status);
        return toResponseDTO(spacecraft);
    }

    public Mono<SpacecraftResponseDTO> changeSpacecraftLocation(Long id, String newLocation) {
        return Mono.fromCallable(() -> changeSpacecraftLocationBlocking(id, newLocation))
                .subscribeOn(Schedulers.boundedElastic());
    }


    public SpacecraftResponseDTO changeSpacecraftLocationBlocking(Long id, String newLocation) {
        Spacecraft spacecraft = spacecraftRepository.findById(id)
                .orElseThrow(() -> new SpacecraftNotFoundException("Spacecraft not found with id: " + id));

        String sql = "UPDATE spacecraft SET current_location = ? WHERE id = ?";
        jdbcTemplate.update(sql, newLocation, id);

        spacecraft.setCurrentLocation(newLocation);
        return toResponseDTO(spacecraft);
    }

    public Mono<SpacecraftResponseDTO> putSpacecraftInMaintenance(Long id) {
        return Mono.fromCallable(() -> putSpacecraftInMaintenanceBlocking(id))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public SpacecraftResponseDTO putSpacecraftInMaintenanceBlocking(Long id) {
        return updateSpacecraftStatusBlocking(id, SpacecraftStatus.MAINTENANCE);
    }

    public Mono<SpacecraftResponseDTO> putSpacecraftInTransit(Long id) {
        return Mono.fromCallable(() -> putSpacecraftInTransitBlocking(id))
                .subscribeOn(Schedulers.boundedElastic());
    }


    public SpacecraftResponseDTO putSpacecraftInTransitBlocking(Long id) {
        return updateSpacecraftStatusBlocking(id, SpacecraftStatus.IN_TRANSIT);
    }

    public Mono<SpacecraftResponseDTO> dockSpacecraft(Long id, String location) {
        return Mono.fromCallable(() -> dockSpacecraftBlocking(id, location))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public SpacecraftResponseDTO dockSpacecraftBlocking(Long id, String location) {
        SpacecraftResponseDTO response = changeSpacecraftLocationBlocking(id, location);
        return updateSpacecraftStatusBlocking(id, SpacecraftStatus.DOCKED);
    }

    public Mono<Spacecraft> getEntityById(Long id) {
        return Mono.fromCallable(() -> getEntityByIdBlocking(id))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Spacecraft getEntityByIdBlocking(Long id) {
        return spacecraftRepository.findById(id)
                .orElseThrow(() -> new SpacecraftNotFoundException("Spacecraft not found with id: " + id));
    }

    public Mono<Boolean> spacecraftExists(Long id) {
        return Mono.fromCallable(() -> spacecraftExistsBlocking(id))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public boolean spacecraftExistsBlocking(Long id) {
        return spacecraftRepository.existsById(id);
    }

    private SpacecraftResponseDTO toResponseDTO(Spacecraft spacecraft) {
        SpacecraftType spacecraftType = spacecraftTypeService.getEntityByIdBlocking(spacecraft.getSpacecraftTypeId());

        BigDecimal currentMassUsage = BigDecimal.ZERO;
        BigDecimal currentVolumeUsage = BigDecimal.ZERO;

        try {
            SpacecraftCargoUsageDTO cargoUsage = cargoServiceClient.getSpacecraftCargoUsage(spacecraft.getId());
            if (cargoUsage != null) {
                currentMassUsage = cargoUsage.currentMassUsage();
                currentVolumeUsage = cargoUsage.currentVolumeUsage();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch cargo usage for spacecraft {}: {}. Using zero values.",
                     spacecraft.getId(), e.getMessage());
        }

        return spacecraftMapper.toResponseDTO(
            spacecraft,
            spacecraftType.getTypeName(),
            spacecraftType.getClassification(),
            currentMassUsage,
            currentVolumeUsage
        );
    }
}
