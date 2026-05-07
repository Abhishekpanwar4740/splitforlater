package com.splitforlater.expenseservice.controller;

import com.splitforlater.common.dto.ExpenseRequestDto;
import com.splitforlater.expenseservice.entity.Expense;
import com.splitforlater.expenseservice.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Tag(name = "Expense Service")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Operation(summary = "Add a new expense and trigger async debt simplification")
    @PostMapping
    public ResponseEntity<Expense> createExpense(
            @Valid @RequestBody ExpenseRequestDto dto,
            @RequestHeader("X-User-Id") UUID payerId) {

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(expenseService.createExpense(dto, payerId));
    }
}
