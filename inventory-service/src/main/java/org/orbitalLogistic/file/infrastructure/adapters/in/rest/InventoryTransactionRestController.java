package org.orbitalLogistic.file.infrastructure.adapters.in.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.file.application.ports.in.CreateInventoryTransactionCommand;
import org.orbitalLogistic.file.application.ports.in.CreateInventoryTransactionUseCase;
import org.orbitalLogistic.file.application.ports.in.GetInventoryTransactionsUseCase;
import org.orbitalLogistic.file.domain.model.InventoryTransaction;
import org.orbitalLogistic.file.infrastructure.adapters.in.rest.dto.InventoryTransactionRequestDTO;
import org.orbitalLogistic.file.infrastructure.adapters.in.rest.dto.PageResponseDTO;
import org.orbitalLogistic.file.infrastructure.adapters.in.rest.dto.InventoryTransactionResponseDTO;
import org.orbitalLogistic.file.infrastructure.adapters.in.rest.mapper.InventoryTransactionRestMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory-transactions")
@RequiredArgsConstructor
public class InventoryTransactionRestController {

    private final CreateInventoryTransactionUseCase createInventoryTransactionUseCase;
    private final GetInventoryTransactionsUseCase getInventoryTransactionsUseCase;
    private final InventoryTransactionRestMapper inventoryTransactionRestMapper;

    @GetMapping
    public ResponseEntity<PageResponseDTO<InventoryTransactionResponseDTO>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > 50) size = 50;

        List<InventoryTransaction> transactions = getInventoryTransactionsUseCase.getAllTransactions(page, size);
        long total = getInventoryTransactionsUseCase.countAllTransactions();

        List<InventoryTransactionResponseDTO> transactionDTOs = transactions.stream()
                .map(inventoryTransactionRestMapper::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);
        PageResponseDTO<InventoryTransactionResponseDTO> response = new PageResponseDTO<>(
                transactionDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1
        );

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(total))
                .body(response);
    }

    @GetMapping("/cargo/{cargoId}")
    public ResponseEntity<PageResponseDTO<InventoryTransactionResponseDTO>> getTransactionsByCargo(
            @PathVariable Long cargoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > 50) size = 50;

        List<InventoryTransaction> transactions = getInventoryTransactionsUseCase.getTransactionsByCargo(cargoId, page, size);
        long total = getInventoryTransactionsUseCase.countTransactionsByCargo(cargoId);

        List<InventoryTransactionResponseDTO> transactionDTOs = transactions.stream()
                .map(inventoryTransactionRestMapper::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);
        PageResponseDTO<InventoryTransactionResponseDTO> response = new PageResponseDTO<>(
                transactionDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1
        );

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(total))
                .body(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER')")
    public ResponseEntity<InventoryTransactionResponseDTO> createTransaction(
            @Valid @RequestBody InventoryTransactionRequestDTO request) {

        CreateInventoryTransactionCommand command = inventoryTransactionRestMapper.toCommand(request);
        InventoryTransaction transaction = createInventoryTransactionUseCase.createTransaction(command);
        InventoryTransactionResponseDTO response = inventoryTransactionRestMapper.toResponseDTO(transaction);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
