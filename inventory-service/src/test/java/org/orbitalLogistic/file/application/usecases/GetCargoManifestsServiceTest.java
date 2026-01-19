package org.orbitalLogistic.file.application.usecases;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.file.application.ports.out.CargoManifestRepository;
import org.orbitalLogistic.file.domain.model.CargoManifest;
import org.orbitalLogistic.file.domain.model.enums.ManifestPriority;
import org.orbitalLogistic.file.domain.model.enums.ManifestStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCargoManifestsServiceTest {

    @Mock
    private CargoManifestRepository cargoManifestRepository;

    @InjectMocks
    private GetCargoManifestsService getCargoManifestsService;

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

        when(cargoManifestRepository.findAll(20, 0)).thenReturn(List.of(manifest));
        List<CargoManifest> result = getCargoManifestsService.getAllManifests(0, 20);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().getId());
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

        when(cargoManifestRepository.findBySpacecraftId(1L, 20, 0)).thenReturn(List.of(manifest));
        List<CargoManifest> result = getCargoManifestsService.getManifestsBySpacecraft(1L, 0, 20);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().getSpacecraftId());
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

        when(cargoManifestRepository.findById(1L)).thenReturn(Optional.of(manifest));
        CargoManifest result = getCargoManifestsService.getManifestById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("Should throw exception when manifest not found")
    void getManifestById_NotFound() {
        when(cargoManifestRepository.findById(999L)).thenReturn(Optional.empty());
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> getCargoManifestsService.getManifestById(999L)
        );

        assertEquals("Cargo manifest not found with id: 999", exception.getMessage());
    }

    @Test
    @DisplayName("Should count all manifests")
    void countAllManifests_Success() {
        when(cargoManifestRepository.countAll()).thenReturn(5L);
        long result = getCargoManifestsService.countAllManifests();
        assertEquals(5L, result);
    }

    @Test
    @DisplayName("Should count manifests by spacecraft")
    void countManifestsBySpacecraft_Success() {
        when(cargoManifestRepository.countBySpacecraftId(1L)).thenReturn(3L);
        long result = getCargoManifestsService.countManifestsBySpacecraft(1L);
        assertEquals(3L, result);
    }
}
