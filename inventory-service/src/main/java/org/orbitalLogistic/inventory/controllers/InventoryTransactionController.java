package org.orbitalLogistic.inventory.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.inventory.dto.common.PageResponseDTO;
import org.orbitalLogistic.inventory.dto.request.InventoryTransactionRequestDTO;
import org.orbitalLogistic.inventory.dto.response.InventoryTransactionResponseDTO;
import org.orbitalLogistic.inventory.services.InventoryTransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory-transactions")
@RequiredArgsConstructor
public class InventoryTransactionController {

    private final InventoryTransactionService inventoryTransactionService;

    @GetMapping
    public ResponseEntity<PageResponseDTO<InventoryTransactionResponseDTO>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > 50) size = 50;

        PageResponseDTO<InventoryTransactionResponseDTO> response =
                inventoryTransactionService.getAllTransactions(page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(response.totalElements()))
                .body(response);
    }

    @GetMapping("/cargo/{cargoId}")
    public ResponseEntity<PageResponseDTO<InventoryTransactionResponseDTO>> getTransactionsByCargo(
            @PathVariable Long cargoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > 50) size = 50;

        PageResponseDTO<InventoryTransactionResponseDTO> response =
                inventoryTransactionService.getTransactionsByCargo(cargoId, page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(response.totalElements()))
                .body(response);
    }

    @PostMapping
    public ResponseEntity<InventoryTransactionResponseDTO> createTransaction(
            @Valid @RequestBody InventoryTransactionRequestDTO request) {

        InventoryTransactionResponseDTO response = inventoryTransactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

