package org.orbitalLogistic.spacecraft.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.spacecraft.dto.request.SpacecraftTypeRequestDTO;
import org.orbitalLogistic.spacecraft.dto.response.SpacecraftTypeResponseDTO;
import org.orbitalLogistic.spacecraft.entities.SpacecraftType;
import org.orbitalLogistic.spacecraft.entities.enums.SpacecraftClassification;
import org.orbitalLogistic.spacecraft.exceptions.SpacecraftTypeNotFoundException;
import org.orbitalLogistic.spacecraft.mappers.SpacecraftTypeMapper;
import org.orbitalLogistic.spacecraft.repositories.SpacecraftTypeRepository;
import org.orbitalLogistic.spacecraft.services.SpacecraftTypeService;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpacecraftTypeServiceTest {

    @Mock
    private SpacecraftTypeRepository spacecraftTypeRepository;

    @Mock
    private SpacecraftTypeMapper spacecraftTypeMapper;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private SpacecraftTypeService spacecraftTypeService;

    private SpacecraftType testSpacecraftType;
    private SpacecraftTypeRequestDTO spacecraftTypeRequest;
    private SpacecraftTypeResponseDTO spacecraftTypeResponse;

    @BeforeEach
    void setUp() {
        testSpacecraftType = SpacecraftType.builder()
                .id(1L)
                .typeName("Cargo Hauler")
                .classification(SpacecraftClassification.CARGO_HAULER)
                .maxCrewCapacity(10)
                .build();

        spacecraftTypeRequest = new SpacecraftTypeRequestDTO(
                "Personnel Transport",
                SpacecraftClassification.PERSONNEL_TRANSPORT,
                50
        );

        spacecraftTypeResponse = new SpacecraftTypeResponseDTO(
                testSpacecraftType.getId(),
                testSpacecraftType.getTypeName(),
                testSpacecraftType.getClassification(),
                testSpacecraftType.getMaxCrewCapacity()
        );
    }

    @Test
    @DisplayName("Получение всех типов кораблей - успешно")
    void getAllSpacecraftTypes_Success() {
        List<SpacecraftType> types = List.of(testSpacecraftType);
        when(spacecraftTypeRepository.findAll()).thenReturn(types);
        when(spacecraftTypeMapper.toResponseDTO(any(SpacecraftType.class)))
                .thenReturn(spacecraftTypeResponse);

        List<SpacecraftTypeResponseDTO> result = spacecraftTypeService.getAllSpacecraftTypesBlocking();

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(spacecraftTypeRepository).findAll();
        verify(spacecraftTypeMapper, times(1)).toResponseDTO(any(SpacecraftType.class));
    }

    @Test
    @DisplayName("Получение всех типов кораблей - пустой список")
    void getAllSpacecraftTypes_EmptyList() {
        when(spacecraftTypeRepository.findAll()).thenReturn(List.of());

        List<SpacecraftTypeResponseDTO> result = spacecraftTypeService.getAllSpacecraftTypesBlocking();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(spacecraftTypeRepository).findAll();
    }

    @Test
    @DisplayName("Получение типа корабля по ID - успешно")
    void getSpacecraftTypeById_Success() {
        when(spacecraftTypeRepository.findById(1L)).thenReturn(Optional.of(testSpacecraftType));
        when(spacecraftTypeMapper.toResponseDTO(testSpacecraftType)).thenReturn(spacecraftTypeResponse);

        SpacecraftTypeResponseDTO result = spacecraftTypeService.getSpacecraftTypeByIdBlocking(1L);

        assertNotNull(result);
        assertEquals("Cargo Hauler", result.typeName());
        assertEquals(SpacecraftClassification.CARGO_HAULER, result.classification());

        verify(spacecraftTypeRepository).findById(1L);
        verify(spacecraftTypeMapper).toResponseDTO(testSpacecraftType);
    }

    @Test
    @DisplayName("Получение типа корабля по ID - не найден")
    void getSpacecraftTypeById_NotFound() {
        when(spacecraftTypeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(SpacecraftTypeNotFoundException.class,
                () -> spacecraftTypeService.getSpacecraftTypeByIdBlocking(999L));

        verify(spacecraftTypeRepository).findById(999L);
        verify(spacecraftTypeMapper, never()).toResponseDTO(any());
    }

    @Test
    @DisplayName("Создание типа корабля - успешно")
    void createSpacecraftType_Success() {
        SpacecraftType newType = SpacecraftType.builder()
                .id(2L)
                .typeName("Personnel Transport")
                .classification(SpacecraftClassification.PERSONNEL_TRANSPORT)
                .maxCrewCapacity(50)
                .build();

        SpacecraftTypeResponseDTO newResponse = new SpacecraftTypeResponseDTO(
                2L, "Personnel Transport", SpacecraftClassification.PERSONNEL_TRANSPORT, 50
        );

        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(), any(), any()))
                .thenReturn(2L);
        when(spacecraftTypeRepository.findById(2L)).thenReturn(Optional.of(newType));
        when(spacecraftTypeMapper.toResponseDTO(newType)).thenReturn(newResponse);

        SpacecraftTypeResponseDTO result = spacecraftTypeService.createSpacecraftTypeBlocking(spacecraftTypeRequest);

        assertNotNull(result);
        assertEquals("Personnel Transport", result.typeName());
        assertEquals(SpacecraftClassification.PERSONNEL_TRANSPORT, result.classification());

        verify(jdbcTemplate).queryForObject(anyString(), eq(Long.class), any(), any(), any());
        verify(spacecraftTypeRepository).findById(2L);
        verify(spacecraftTypeMapper).toResponseDTO(newType);
    }

    @Test
    @DisplayName("Создание типа корабля - ошибка при сохранении")
    void createSpacecraftType_SaveFailed() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(), any(), any()))
                .thenReturn(2L);
        when(spacecraftTypeRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(SpacecraftTypeNotFoundException.class,
                () -> spacecraftTypeService.createSpacecraftTypeBlocking(spacecraftTypeRequest));

        verify(jdbcTemplate).queryForObject(anyString(), eq(Long.class), any(), any(), any());
        verify(spacecraftTypeRepository).findById(2L);
        verify(spacecraftTypeMapper, never()).toResponseDTO(any());
    }

    @Test
    @DisplayName("Получение сущности типа корабля по ID - успешно")
    void getEntityById_Success() {
        when(spacecraftTypeRepository.findById(1L)).thenReturn(Optional.of(testSpacecraftType));

        SpacecraftType result = spacecraftTypeService.getEntityByIdBlocking(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Cargo Hauler", result.getTypeName());

        verify(spacecraftTypeRepository).findById(1L);
    }

    @Test
    @DisplayName("Получение сущности типа корабля по ID - не найден")
    void getEntityById_NotFound() {
        when(spacecraftTypeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(SpacecraftTypeNotFoundException.class,
                () -> spacecraftTypeService.getEntityByIdBlocking(999L));

        verify(spacecraftTypeRepository).findById(999L);
    }
}
