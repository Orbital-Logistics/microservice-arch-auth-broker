package org.orbitalLogistic.inventory.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.inventory.clients.*;
import org.orbitalLogistic.inventory.clients.resilient.ResilientCargoServiceClient;
import org.orbitalLogistic.inventory.clients.resilient.ResilientSpacecraftService;
import org.orbitalLogistic.inventory.clients.resilient.ResilientUserService;
import org.orbitalLogistic.inventory.dto.common.PageResponseDTO;
import org.orbitalLogistic.inventory.dto.request.CargoManifestRequestDTO;
import org.orbitalLogistic.inventory.dto.response.CargoManifestResponseDTO;
import org.orbitalLogistic.inventory.entities.CargoManifest;
import org.orbitalLogistic.inventory.entities.enums.ManifestPriority;
import org.orbitalLogistic.inventory.entities.enums.ManifestStatus;
import org.orbitalLogistic.inventory.exceptions.CargoManifestNotFoundException;
import org.orbitalLogistic.inventory.exceptions.InvalidOperationException;
import org.orbitalLogistic.inventory.mappers.CargoManifestMapper;
import org.orbitalLogistic.inventory.repositories.CargoManifestRepository;
import org.orbitalLogistic.inventory.services.CargoManifestService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CargoManifestServiceTest {

    @Mock
    private CargoManifestRepository cargoManifestRepository;

    @Mock
    private CargoManifestMapper cargoManifestMapper;

    @Mock
    private ResilientUserService userServiceClient;

    @Mock
    private ResilientCargoServiceClient cargoServiceClient;

    @Mock
    private ResilientSpacecraftService spacecraftServiceClient;

    @InjectMocks
    private CargoManifestService cargoManifestService;

    private CargoManifest testManifest;
    private CargoManifestRequestDTO manifestRequest;
    private CargoManifestResponseDTO manifestResponse;
    private UserDTO testUser;
    private CargoDTO testCargo;
    private SpacecraftDTO testSpacecraft;
    private StorageUnitDTO testStorageUnit;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        testManifest = CargoManifest.builder()
                .id(1L)
                .spacecraftId(1L)
                .cargoId(1L)
                .storageUnitId(1L)
                .quantity(100)
                .loadedAt(now)
                .loadedByUserId(1L)
                .manifestStatus(ManifestStatus.PENDING)
                .priority(ManifestPriority.NORMAL)
                .build();

        manifestRequest = new CargoManifestRequestDTO(
                1L, 1L, 1L, 100,
                now, null, 1L, null,
                ManifestStatus.PENDING,
                ManifestPriority.NORMAL
        );

        testUser = new UserDTO(1L, "John Doe", "john@example.com");
        testCargo = new CargoDTO(1L, "Test Cargo");
        testSpacecraft = new SpacecraftDTO(1L, "SC-001", "Star Carrier");
        testStorageUnit = new StorageUnitDTO(1L, "UNIT-001", "Section A");

        manifestResponse = new CargoManifestResponseDTO(
                1L, 1L, "Star Carrier",
                1L, "Test Cargo",
                1L, "UNIT-001",
                100, now, null,
                1L, "John Doe",
                null, null,
                ManifestStatus.PENDING,
                ManifestPriority.NORMAL
        );
    }

    @Test
    @DisplayName("Получение всех манифестов - успешно")
    void getAllManifests_Success() {
        List<CargoManifest> manifests = List.of(testManifest);
        when(cargoManifestRepository.findAllPaginated(10, 0)).thenReturn(manifests);
        when(cargoManifestRepository.countAll()).thenReturn(1L);
        when(spacecraftServiceClient.getSpacecraftById(anyLong())).thenReturn(testSpacecraft);
        when(cargoServiceClient.getCargoById(anyLong())).thenReturn(testCargo);
        when(cargoServiceClient.getStorageUnitById(anyLong())).thenReturn(testStorageUnit);
        when(userServiceClient.getUserById(anyLong())).thenReturn(testUser);
        when(cargoManifestMapper.toResponseDTO(any(CargoManifest.class), anyString(), anyString(), anyString(), anyString(), isNull()))
                .thenReturn(manifestResponse);

        PageResponseDTO<CargoManifestResponseDTO> result = cargoManifestService.getAllManifests(0, 10);

        assertNotNull(result);
        assertEquals(1, result.totalElements());
        assertEquals(1, result.content().size());

        verify(cargoManifestRepository).findAllPaginated(10, 0);
        verify(cargoManifestRepository).countAll();
    }

    @Test
    @DisplayName("Получение манифестов по кораблю - успешно")
    void getManifestsBySpacecraft_Success() {
        List<CargoManifest> manifests = List.of(testManifest);
        when(cargoManifestRepository.findBySpacecraftIdPaginated(1L, 10, 0)).thenReturn(manifests);
        when(cargoManifestRepository.countBySpacecraftId(1L)).thenReturn(1L);
        when(spacecraftServiceClient.getSpacecraftById(anyLong())).thenReturn(testSpacecraft);
        when(cargoServiceClient.getCargoById(anyLong())).thenReturn(testCargo);
        when(cargoServiceClient.getStorageUnitById(anyLong())).thenReturn(testStorageUnit);
        when(userServiceClient.getUserById(anyLong())).thenReturn(testUser);
        when(cargoManifestMapper.toResponseDTO(any(CargoManifest.class), anyString(), anyString(), anyString(), anyString(), isNull()))
                .thenReturn(manifestResponse);

        PageResponseDTO<CargoManifestResponseDTO> result = cargoManifestService.getManifestsBySpacecraft(1L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.totalElements());

        verify(cargoManifestRepository).findBySpacecraftIdPaginated(1L, 10, 0);
    }

    @Test
    @DisplayName("Получение манифеста по ID - успешно")
    void getManifestById_Success() {
        when(cargoManifestRepository.findById(1L)).thenReturn(Optional.of(testManifest));
        when(spacecraftServiceClient.getSpacecraftById(anyLong())).thenReturn(testSpacecraft);
        when(cargoServiceClient.getCargoById(anyLong())).thenReturn(testCargo);
        when(cargoServiceClient.getStorageUnitById(anyLong())).thenReturn(testStorageUnit);
        when(userServiceClient.getUserById(anyLong())).thenReturn(testUser);
        when(cargoManifestMapper.toResponseDTO(any(CargoManifest.class), anyString(), anyString(), anyString(), anyString(), isNull()))
                .thenReturn(manifestResponse);

        CargoManifestResponseDTO result = cargoManifestService.getManifestById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());

        verify(cargoManifestRepository).findById(1L);
    }

    @Test
    @DisplayName("Получение манифеста по ID - не найден")
    void getManifestById_NotFound() {
        when(cargoManifestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CargoManifestNotFoundException.class,
                () -> cargoManifestService.getManifestById(999L));

        verify(cargoManifestRepository).findById(999L);
    }

    @Test
    @DisplayName("Создание манифеста - успешно")
    void createManifest_Success() {
        when(spacecraftServiceClient.spacecraftExists(1L)).thenReturn(true);
        when(cargoServiceClient.cargoExists(1L)).thenReturn(true);
        when(cargoServiceClient.storageUnitExists(1L)).thenReturn(true);
        when(userServiceClient.userExists(1L)).thenReturn(true);
        when(cargoManifestMapper.toEntity(any(CargoManifestRequestDTO.class))).thenReturn(testManifest);
        when(cargoManifestRepository.save(any(CargoManifest.class))).thenReturn(testManifest);
        when(spacecraftServiceClient.getSpacecraftById(anyLong())).thenReturn(testSpacecraft);
        when(cargoServiceClient.getCargoById(anyLong())).thenReturn(testCargo);
        when(cargoServiceClient.getStorageUnitById(anyLong())).thenReturn(testStorageUnit);
        when(userServiceClient.getUserById(anyLong())).thenReturn(testUser);
        when(cargoManifestMapper.toResponseDTO(any(CargoManifest.class), anyString(), anyString(), anyString(), anyString(), isNull()))
                .thenReturn(manifestResponse);

        CargoManifestResponseDTO result = cargoManifestService.createManifest(manifestRequest);

        assertNotNull(result);

        verify(spacecraftServiceClient).spacecraftExists(1L);
        verify(cargoServiceClient).cargoExists(1L);
        verify(cargoServiceClient).storageUnitExists(1L);
        verify(userServiceClient).userExists(1L);
        verify(cargoManifestRepository).save(any(CargoManifest.class));
    }

    @Test
    @DisplayName("Создание манифеста - корабль не найден")
    void createManifest_SpacecraftNotFound() {
        when(spacecraftServiceClient.spacecraftExists(999L)).thenReturn(false);

        CargoManifestRequestDTO request = new CargoManifestRequestDTO(
                999L, 1L, 1L, 100,
                LocalDateTime.now(), null, 1L, null,
                ManifestStatus.PENDING, ManifestPriority.NORMAL
        );

        assertThrows(InvalidOperationException.class,
                () -> cargoManifestService.createManifest(request));

        verify(spacecraftServiceClient).spacecraftExists(999L);
        verify(cargoManifestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Создание манифеста - груз не найден")
    void createManifest_CargoNotFound() {
        when(spacecraftServiceClient.spacecraftExists(1L)).thenReturn(true);
        when(cargoServiceClient.cargoExists(999L)).thenReturn(false);

        CargoManifestRequestDTO request = new CargoManifestRequestDTO(
                1L, 999L, 1L, 100,
                LocalDateTime.now(), null, 1L, null,
                ManifestStatus.PENDING, ManifestPriority.NORMAL
        );

        assertThrows(InvalidOperationException.class,
                () -> cargoManifestService.createManifest(request));

        verify(cargoServiceClient).cargoExists(999L);
        verify(cargoManifestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Создание манифеста - склад не найден")
    void createManifest_StorageUnitNotFound() {
        when(spacecraftServiceClient.spacecraftExists(1L)).thenReturn(true);
        when(cargoServiceClient.cargoExists(1L)).thenReturn(true);
        when(cargoServiceClient.storageUnitExists(999L)).thenReturn(false);

        CargoManifestRequestDTO request = new CargoManifestRequestDTO(
                1L, 1L, 999L, 100,
                LocalDateTime.now(), null, 1L, null,
                ManifestStatus.PENDING, ManifestPriority.NORMAL
        );

        assertThrows(InvalidOperationException.class,
                () -> cargoManifestService.createManifest(request));

        verify(cargoServiceClient).storageUnitExists(999L);
        verify(cargoManifestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Создание манифеста - пользователь не найден")
    void createManifest_UserNotFound() {
        when(spacecraftServiceClient.spacecraftExists(1L)).thenReturn(true);
        when(cargoServiceClient.cargoExists(1L)).thenReturn(true);
        when(cargoServiceClient.storageUnitExists(1L)).thenReturn(true);
        when(userServiceClient.userExists(999L)).thenReturn(false);

        CargoManifestRequestDTO request = new CargoManifestRequestDTO(
                1L, 1L, 1L, 100,
                LocalDateTime.now(), null, 999L, null,
                ManifestStatus.PENDING, ManifestPriority.NORMAL
        );

        assertThrows(InvalidOperationException.class,
                () -> cargoManifestService.createManifest(request));

        verify(userServiceClient).userExists(999L);
        verify(cargoManifestRepository, never()).save(any());
    }

}

