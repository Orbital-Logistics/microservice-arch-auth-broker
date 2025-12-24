package org.orbitalLogistic.spacecraft.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.spacecraft.clients.ResilientCargoServiceClient;
import org.orbitalLogistic.spacecraft.exceptions.SpacecraftAlreadyExistsException;
import org.orbitalLogistic.spacecraft.exceptions.SpacecraftNotFoundException;
import org.orbitalLogistic.spacecraft.mappers.SpacecraftMapper;
import org.orbitalLogistic.spacecraft.repositories.SpacecraftRepository;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpacecraftServiceExceptionTest {

    @Mock
    private SpacecraftRepository spacecraftRepository;

    @Mock
    private SpacecraftMapper spacecraftMapper;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ResilientCargoServiceClient cargoServiceClient;

    @Mock
    private SpacecraftTypeService spacecraftTypeService;

    @InjectMocks
    private SpacecraftService spacecraftService;

    @Test
    void getSpacecraftById_NotFound() {
        when(spacecraftRepository.findById(1L)).thenReturn(java.util.Optional.empty());
        assertThrows(SpacecraftNotFoundException.class, () -> spacecraftService.getSpacecraftByIdBlocking(1L));
    }

    @Test
    void createSpacecraft_AlreadyExists() {
        var req = mock(org.orbitalLogistic.spacecraft.dto.request.SpacecraftRequestDTO.class);
        when(req.registryCode()).thenReturn("RC-1");
        when(spacecraftRepository.existsByRegistryCode("RC-1")).thenReturn(true);

        assertThrows(SpacecraftAlreadyExistsException.class, () -> spacecraftService.createSpacecraftBlocking(req));
    }

    @Test
    void deleteSpacecraft_NotFound() {
        when(spacecraftRepository.existsById(999L)).thenReturn(false);
        assertThrows(SpacecraftNotFoundException.class, () -> spacecraftService.deleteSpacecraftBlocking(999L));
    }
}
