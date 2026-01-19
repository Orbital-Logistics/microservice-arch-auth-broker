package org.orbitalLogistic.file.infrastructure.adapters.in.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.file.application.ports.in.CreateCargoManifestCommand;
import org.orbitalLogistic.file.application.ports.in.CreateCargoManifestUseCase;
import org.orbitalLogistic.file.application.ports.in.GetCargoManifestsUseCase;
import org.orbitalLogistic.file.application.ports.in.UpdateCargoManifestCommand;
import org.orbitalLogistic.file.application.ports.in.UpdateCargoManifestUseCase;
import org.orbitalLogistic.file.domain.model.CargoManifest;
import org.orbitalLogistic.file.domain.model.enums.ManifestPriority;
import org.orbitalLogistic.file.domain.model.enums.ManifestStatus;
import org.orbitalLogistic.file.infrastructure.adapters.in.rest.dto.CargoManifestRequestDTO;
import org.orbitalLogistic.file.infrastructure.adapters.in.rest.dto.CargoManifestResponseDTO;
import org.orbitalLogistic.file.infrastructure.adapters.in.rest.dto.PageResponseDTO;
import org.orbitalLogistic.file.infrastructure.adapters.in.rest.mapper.CargoManifestRestMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CargoManifestRestControllerTest {

    @Mock
    private CreateCargoManifestUseCase createCargoManifestUseCase;

    @Mock
    private GetCargoManifestsUseCase getCargoManifestsUseCase;

    @Mock
    private UpdateCargoManifestUseCase updateCargoManifestUseCase;

    @Mock
    private CargoManifestRestMapper cargoManifestRestMapper;

    @InjectMocks
    private CargoManifestRestController cargoManifestRestController;

    @Test
    @DisplayName("Should get all manifests successfully")
    void getAllManifests_Success() {
        CargoManifest manifest = CargoManifest.builder()
                .id(1L)
                .spacecraftId(1L)
                .cargoId(1L)
                .storageUnitId(1L)
                .quantity(10)
                .loadedByUserId(1L)
                .manifestStatus(ManifestStatus.PENDING)
                .priority(ManifestPriority.NORMAL)
                .build();

        CargoManifestResponseDTO responseDTO = new CargoManifestResponseDTO(
                1L, 1L, "Spacecraft-1", 1L, "Cargo-1", 1L, "Unit-1",
                10, LocalDateTime.now(), null, 1L, "User-1", null, null,
                org.orbitalLogistic.file.infrastructure.adapters.in.rest.dto.ManifestStatus.PENDING,
                org.orbitalLogistic.file.infrastructure.adapters.in.rest.dto.ManifestPriority.NORMAL
        );

        when(getCargoManifestsUseCase.getAllManifests(0, 20)).thenReturn(List.of(manifest));
        when(getCargoManifestsUseCase.countAllManifests()).thenReturn(1L);
        when(cargoManifestRestMapper.toResponseDTO(manifest)).thenReturn(responseDTO);
        ResponseEntity<PageResponseDTO<CargoManifestResponseDTO>> response = 
                cargoManifestRestController.getAllManifests(0, 20);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().content().size());
        assertEquals("1", response.getHeaders().getFirst("X-Total-Count"));
    }

    @Test
    @DisplayName("Should get manifests by spacecraft successfully")
    void getManifestsBySpacecraft_Success() {
        CargoManifest manifest = CargoManifest.builder()
                .id(1L)
                .spacecraftId(1L)
                .cargoId(1L)
                .storageUnitId(1L)
                .quantity(10)
                .loadedByUserId(1L)
                .manifestStatus(ManifestStatus.PENDING)
                .priority(ManifestPriority.NORMAL)
                .build();

        CargoManifestResponseDTO responseDTO = new CargoManifestResponseDTO(
                1L, 1L, "Spacecraft-1", 1L, "Cargo-1", 1L, "Unit-1",
                10, LocalDateTime.now(), null, 1L, "User-1", null, null,
                org.orbitalLogistic.file.infrastructure.adapters.in.rest.dto.ManifestStatus.PENDING,
                org.orbitalLogistic.file.infrastructure.adapters.in.rest.dto.ManifestPriority.NORMAL
        );

        when(getCargoManifestsUseCase.getManifestsBySpacecraft(1L, 0, 20)).thenReturn(List.of(manifest));
        when(getCargoManifestsUseCase.countManifestsBySpacecraft(1L)).thenReturn(1L);
        when(cargoManifestRestMapper.toResponseDTO(manifest)).thenReturn(responseDTO);
        ResponseEntity<PageResponseDTO<CargoManifestResponseDTO>> response = 
                cargoManifestRestController.getManifestsBySpacecraft(1L, 0, 20);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().content().size());
    }

    @Test
    @DisplayName("Should get manifest by id successfully")
    void getManifestById_Success() {
        CargoManifest manifest = CargoManifest.builder()
                .id(1L)
                .spacecraftId(1L)
                .cargoId(1L)
                .storageUnitId(1L)
                .quantity(10)
                .loadedByUserId(1L)
                .manifestStatus(ManifestStatus.PENDING)
                .priority(ManifestPriority.NORMAL)
                .build();

        CargoManifestResponseDTO responseDTO = new CargoManifestResponseDTO(
                1L, 1L, "Spacecraft-1", 1L, "Cargo-1", 1L, "Unit-1",
                10, LocalDateTime.now(), null, 1L, "User-1", null, null,
                org.orbitalLogistic.file.infrastructure.adapters.in.rest.dto.ManifestStatus.PENDING,
                org.orbitalLogistic.file.infrastructure.adapters.in.rest.dto.ManifestPriority.NORMAL
        );

        when(getCargoManifestsUseCase.getManifestById(1L)).thenReturn(manifest);
        when(cargoManifestRestMapper.toResponseDTO(manifest)).thenReturn(responseDTO);
        ResponseEntity<CargoManifestResponseDTO> response = cargoManifestRestController.getManifestById(1L);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
    }

    @Test
    @DisplayName("Should create manifest successfully")
    void createManifest_Success() {
        CargoManifestRequestDTO requestDTO = new CargoManifestRequestDTO(
                1L, 1L, 1L, 10, LocalDateTime.now(), null, 1L, null,
                org.orbitalLogistic.file.infrastructure.adapters.in.rest.dto.ManifestStatus.PENDING,
                org.orbitalLogistic.file.infrastructure.adapters.in.rest.dto.ManifestPriority.NORMAL
        );

        CreateCargoManifestCommand command = new CreateCargoManifestCommand(
                1L, 1L, 1L, 10, LocalDateTime.now(), null, 1L, null,
                ManifestStatus.PENDING, ManifestPriority.NORMAL
        );

        CargoManifest manifest = CargoManifest.builder()
                .id(1L)
                .spacecraftId(1L)
                .cargoId(1L)
                .storageUnitId(1L)
                .quantity(10)
                .loadedByUserId(1L)
                .manifestStatus(ManifestStatus.PENDING)
                .priority(ManifestPriority.NORMAL)
                .build();

        CargoManifestResponseDTO responseDTO = new CargoManifestResponseDTO(
                1L, 1L, "Spacecraft-1", 1L, "Cargo-1", 1L, "Unit-1",
                10, LocalDateTime.now(), null, 1L, "User-1", null, null,
                org.orbitalLogistic.file.infrastructure.adapters.in.rest.dto.ManifestStatus.PENDING,
                org.orbitalLogistic.file.infrastructure.adapters.in.rest.dto.ManifestPriority.NORMAL
        );

        when(cargoManifestRestMapper.toCreateCommand(requestDTO)).thenReturn(command);
        when(createCargoManifestUseCase.createManifest(command)).thenReturn(manifest);
        when(cargoManifestRestMapper.toResponseDTO(manifest)).thenReturn(responseDTO);
        ResponseEntity<CargoManifestResponseDTO> response = cargoManifestRestController.createManifest(requestDTO);
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
    }

    @Test
    @DisplayName("Should update manifest successfully")
    void updateManifest_Success() {
        CargoManifestRequestDTO requestDTO = new CargoManifestRequestDTO(
                1L, 1L, 1L, 10, LocalDateTime.now(), null, 1L, null,
                org.orbitalLogistic.file.infrastructure.adapters.in.rest.dto.ManifestStatus.LOADED,
                org.orbitalLogistic.file.infrastructure.adapters.in.rest.dto.ManifestPriority.HIGH
        );

        UpdateCargoManifestCommand command = new UpdateCargoManifestCommand(
                1L, 1L, 1L, 1L, 10, LocalDateTime.now(), null, 1L, null,
                ManifestStatus.LOADED, ManifestPriority.HIGH
        );

        CargoManifest manifest = CargoManifest.builder()
                .id(1L)
                .spacecraftId(1L)
                .cargoId(1L)
                .storageUnitId(1L)
                .quantity(10)
                .loadedByUserId(1L)
                .manifestStatus(ManifestStatus.LOADED)
                .priority(ManifestPriority.HIGH)
                .build();

        CargoManifestResponseDTO responseDTO = new CargoManifestResponseDTO(
                1L, 1L, "Spacecraft-1", 1L, "Cargo-1", 1L, "Unit-1",
                10, LocalDateTime.now(), null, 1L, "User-1", null, null,
                org.orbitalLogistic.file.infrastructure.adapters.in.rest.dto.ManifestStatus.LOADED,
                org.orbitalLogistic.file.infrastructure.adapters.in.rest.dto.ManifestPriority.HIGH
        );

        when(cargoManifestRestMapper.toUpdateCommand(1L, requestDTO)).thenReturn(command);
        when(updateCargoManifestUseCase.updateManifest(command)).thenReturn(manifest);
        when(cargoManifestRestMapper.toResponseDTO(manifest)).thenReturn(responseDTO);
        ResponseEntity<CargoManifestResponseDTO> response = cargoManifestRestController.updateManifest(1L, requestDTO);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ManifestStatus.LOADED.name(), response.getBody().manifestStatus().name());
    }

    @Test
    @DisplayName("Should limit page size to 50")
    void getAllManifests_LimitPageSize() {
        when(getCargoManifestsUseCase.getAllManifests(0, 50)).thenReturn(List.of());
        when(getCargoManifestsUseCase.countAllManifests()).thenReturn(0L);
        ResponseEntity<PageResponseDTO<CargoManifestResponseDTO>> response = 
                cargoManifestRestController.getAllManifests(0, 100);
        verify(getCargoManifestsUseCase).getAllManifests(0, 50);
    }
}
