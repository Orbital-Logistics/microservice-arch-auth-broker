package org.orbitalLogistic.spacecraft.infrastructure.adapters.out.external;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.spacecraft.clients.ResilientCargoServiceClient;
import org.orbitalLogistic.spacecraft.dto.common.SpacecraftCargoUsageDTO;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CargoValidationAdapter Tests")
class CargoValidationAdapterTest {

    @Mock
    private ResilientCargoServiceClient cargoServiceClient;

    @InjectMocks
    private CargoValidationAdapter cargoValidationAdapter;

    private static final Long SPACECRAFT_ID = 1L;

    @Test
    @DisplayName("Проверка использования корабля в грузах - корабль используется (масса > 0)")
    void isSpacecraftUsedInCargo_WithMassUsage_ReturnsTrue() {
        // Arrange
        SpacecraftCargoUsageDTO usageDTO = new SpacecraftCargoUsageDTO(
                SPACECRAFT_ID,
                new BigDecimal("1000.00"),
                new BigDecimal("0.00")
        );
        when(cargoServiceClient.getSpacecraftCargoUsage(SPACECRAFT_ID)).thenReturn(usageDTO);

        // Act
        boolean result = cargoValidationAdapter.isSpacecraftUsedInCargo(SPACECRAFT_ID);

        // Assert
        assertTrue(result);
        verify(cargoServiceClient).getSpacecraftCargoUsage(SPACECRAFT_ID);
    }

    @Test
    @DisplayName("Проверка использования корабля в грузах - корабль используется (объем > 0)")
    void isSpacecraftUsedInCargo_WithVolumeUsage_ReturnsTrue() {
        // Arrange
        SpacecraftCargoUsageDTO usageDTO = new SpacecraftCargoUsageDTO(
                SPACECRAFT_ID,
                new BigDecimal("0.00"),
                new BigDecimal("500.00")
        );
        when(cargoServiceClient.getSpacecraftCargoUsage(SPACECRAFT_ID)).thenReturn(usageDTO);

        // Act
        boolean result = cargoValidationAdapter.isSpacecraftUsedInCargo(SPACECRAFT_ID);

        // Assert
        assertTrue(result);
        verify(cargoServiceClient).getSpacecraftCargoUsage(SPACECRAFT_ID);
    }

    @Test
    @DisplayName("Проверка использования корабля в грузах - корабль используется (масса и объем > 0)")
    void isSpacecraftUsedInCargo_WithBothUsages_ReturnsTrue() {
        // Arrange
        SpacecraftCargoUsageDTO usageDTO = new SpacecraftCargoUsageDTO(
                SPACECRAFT_ID,
                new BigDecimal("1000.00"),
                new BigDecimal("500.00")
        );
        when(cargoServiceClient.getSpacecraftCargoUsage(SPACECRAFT_ID)).thenReturn(usageDTO);

        // Act
        boolean result = cargoValidationAdapter.isSpacecraftUsedInCargo(SPACECRAFT_ID);

        // Assert
        assertTrue(result);
        verify(cargoServiceClient).getSpacecraftCargoUsage(SPACECRAFT_ID);
    }

    @Test
    @DisplayName("Проверка использования корабля в грузах - корабль не используется")
    void isSpacecraftUsedInCargo_NotUsed_ReturnsFalse() {
        // Arrange
        SpacecraftCargoUsageDTO usageDTO = new SpacecraftCargoUsageDTO(
                SPACECRAFT_ID,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
        when(cargoServiceClient.getSpacecraftCargoUsage(SPACECRAFT_ID)).thenReturn(usageDTO);

        // Act
        boolean result = cargoValidationAdapter.isSpacecraftUsedInCargo(SPACECRAFT_ID);

        // Assert
        assertFalse(result);
        verify(cargoServiceClient).getSpacecraftCargoUsage(SPACECRAFT_ID);
    }

    @Test
    @DisplayName("Проверка использования корабля в грузах - usageDTO null")
    void isSpacecraftUsedInCargo_NullUsageDTO_ReturnsFalse() {
        // Arrange
        when(cargoServiceClient.getSpacecraftCargoUsage(SPACECRAFT_ID)).thenReturn(null);

        // Act
        boolean result = cargoValidationAdapter.isSpacecraftUsedInCargo(SPACECRAFT_ID);

        // Assert
        assertFalse(result);
        verify(cargoServiceClient).getSpacecraftCargoUsage(SPACECRAFT_ID);
    }

    @Test
    @DisplayName("Проверка использования корабля в грузах - исключение при вызове сервиса")
    void isSpacecraftUsedInCargo_ServiceThrowsException_ReturnsFalse() {
        // Arrange
        when(cargoServiceClient.getSpacecraftCargoUsage(SPACECRAFT_ID))
                .thenThrow(new RuntimeException("Cargo service unavailable"));

        // Act
        boolean result = cargoValidationAdapter.isSpacecraftUsedInCargo(SPACECRAFT_ID);

        // Assert
        assertFalse(result);
        verify(cargoServiceClient).getSpacecraftCargoUsage(SPACECRAFT_ID);
    }

    @Test
    @DisplayName("Проверка использования корабля в грузах - отрицательные значения использования")
    void isSpacecraftUsedInCargo_NegativeUsage_ReturnsFalse() {
        // Arrange
        SpacecraftCargoUsageDTO usageDTO = new SpacecraftCargoUsageDTO(
                SPACECRAFT_ID,
                new BigDecimal("-100.00"),
                new BigDecimal("-50.00")
        );
        when(cargoServiceClient.getSpacecraftCargoUsage(SPACECRAFT_ID)).thenReturn(usageDTO);

        // Act
        boolean result = cargoValidationAdapter.isSpacecraftUsedInCargo(SPACECRAFT_ID);

        // Assert
        assertFalse(result);
        verify(cargoServiceClient).getSpacecraftCargoUsage(SPACECRAFT_ID);
    }

    @Test
    @DisplayName("Проверка использования корабля в грузах - очень малое положительное значение массы")
    void isSpacecraftUsedInCargo_VerySmallMassUsage_ReturnsTrue() {
        // Arrange
        SpacecraftCargoUsageDTO usageDTO = new SpacecraftCargoUsageDTO(
                SPACECRAFT_ID,
                new BigDecimal("0.01"),
                BigDecimal.ZERO
        );
        when(cargoServiceClient.getSpacecraftCargoUsage(SPACECRAFT_ID)).thenReturn(usageDTO);

        // Act
        boolean result = cargoValidationAdapter.isSpacecraftUsedInCargo(SPACECRAFT_ID);

        // Assert
        assertTrue(result);
        verify(cargoServiceClient).getSpacecraftCargoUsage(SPACECRAFT_ID);
    }

    @Test
    @DisplayName("Проверка использования корабля в грузах - очень малое положительное значение объема")
    void isSpacecraftUsedInCargo_VerySmallVolumeUsage_ReturnsTrue() {
        // Arrange
        SpacecraftCargoUsageDTO usageDTO = new SpacecraftCargoUsageDTO(
                SPACECRAFT_ID,
                BigDecimal.ZERO,
                new BigDecimal("0.001")
        );
        when(cargoServiceClient.getSpacecraftCargoUsage(SPACECRAFT_ID)).thenReturn(usageDTO);

        // Act
        boolean result = cargoValidationAdapter.isSpacecraftUsedInCargo(SPACECRAFT_ID);

        // Assert
        assertTrue(result);
        verify(cargoServiceClient).getSpacecraftCargoUsage(SPACECRAFT_ID);
    }

    @Test
    @DisplayName("Проверка использования разных кораблей")
    void isSpacecraftUsedInCargo_DifferentSpacecrafts() {
        // Arrange
        Long spacecraftId1 = 1L;
        Long spacecraftId2 = 2L;

        SpacecraftCargoUsageDTO usageDTO1 = new SpacecraftCargoUsageDTO(
                spacecraftId1,
                new BigDecimal("1000.00"),
                BigDecimal.ZERO
        );

        SpacecraftCargoUsageDTO usageDTO2 = new SpacecraftCargoUsageDTO(
                spacecraftId2,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        when(cargoServiceClient.getSpacecraftCargoUsage(spacecraftId1)).thenReturn(usageDTO1);
        when(cargoServiceClient.getSpacecraftCargoUsage(spacecraftId2)).thenReturn(usageDTO2);

        // Act
        boolean result1 = cargoValidationAdapter.isSpacecraftUsedInCargo(spacecraftId1);
        boolean result2 = cargoValidationAdapter.isSpacecraftUsedInCargo(spacecraftId2);

        // Assert
        assertTrue(result1);
        assertFalse(result2);
        verify(cargoServiceClient).getSpacecraftCargoUsage(spacecraftId1);
        verify(cargoServiceClient).getSpacecraftCargoUsage(spacecraftId2);
    }

    @Test
    @DisplayName("Проверка использования корабля в грузах - максимальная загрузка")
    void isSpacecraftUsedInCargo_MaximumLoad_ReturnsTrue() {
        // Arrange
        SpacecraftCargoUsageDTO usageDTO = new SpacecraftCargoUsageDTO(
                SPACECRAFT_ID,
                new BigDecimal("50000.00"),
                new BigDecimal("10000.00")
        );
        when(cargoServiceClient.getSpacecraftCargoUsage(SPACECRAFT_ID)).thenReturn(usageDTO);

        // Act
        boolean result = cargoValidationAdapter.isSpacecraftUsedInCargo(SPACECRAFT_ID);

        // Assert
        assertTrue(result);
        verify(cargoServiceClient).getSpacecraftCargoUsage(SPACECRAFT_ID);
    }

    @Test
    @DisplayName("Проверка использования корабля в грузах - NullPointerException при проверке значений")
    void isSpacecraftUsedInCargo_NullValuesInDTO_ReturnsFalse() {
        // Arrange
        when(cargoServiceClient.getSpacecraftCargoUsage(SPACECRAFT_ID))
                .thenThrow(new NullPointerException("Null value in DTO"));

        // Act
        boolean result = cargoValidationAdapter.isSpacecraftUsedInCargo(SPACECRAFT_ID);

        // Assert
        assertFalse(result);
        verify(cargoServiceClient).getSpacecraftCargoUsage(SPACECRAFT_ID);
    }

    @Test
    @DisplayName("Проверка использования корабля в грузах - timeout исключение")
    void isSpacecraftUsedInCargo_TimeoutException_ReturnsFalse() {
        // Arrange
        when(cargoServiceClient.getSpacecraftCargoUsage(SPACECRAFT_ID))
                .thenThrow(new RuntimeException("Timeout connecting to cargo service"));

        // Act
        boolean result = cargoValidationAdapter.isSpacecraftUsedInCargo(SPACECRAFT_ID);

        // Assert
        assertFalse(result);
        verify(cargoServiceClient).getSpacecraftCargoUsage(SPACECRAFT_ID);
    }
}

