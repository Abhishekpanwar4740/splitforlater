package com.splitforlater.expenseservice.controller;

import com.splitforlater.common.dto.ExpenseDto;
import com.splitforlater.expenseservice.entity.Expense;
import com.splitforlater.expenseservice.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Tag(name = "Expense Service", description = "Endpoints for managing group expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Operation(summary = "Add a new expense and trigger async debt simplification")
    @PostMapping
    public ResponseEntity<Expense> createExpense(
            @Valid @RequestBody ExpenseDto dto,
            @RequestHeader("X-User-Id") UUID payerId) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(expenseService.createExpense(dto, payerId));
    }

    @Operation(summary = "Get a specific expense by its ID")
    @GetMapping("/{expenseId}")
    public ResponseEntity<Expense> getExpense(
            @PathVariable UUID expenseId,
            @RequestHeader("X-User-Id") UUID requesterId) {

        // In your service layer, ensure you verify that 'requesterId' is part of the
        // group this expense belongs to before returning the data!
        try {
            return ResponseEntity.ok(expenseService.getExpenseById(expenseId, requesterId));
        }catch (SecurityException e){
            // If the requester is not authorized to view this expense, return 403 Forbidden
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }catch (EntityNotFoundException e){
            // If the expense doesn't exist, return 404 Not Found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(summary = "Get all expenses for a specific group (Used by Settlement Service)")
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<Expense>> getExpensesByGroup(
            @PathVariable UUID groupId,
            @RequestHeader("X-User-Id") UUID requesterId) {

        // Note: I omitted X-User-Id here because this is often called internally
        // by your SettlementService Feign client which might not pass a user context.
        try {
            return ResponseEntity.ok(expenseService.getExpensesByGroupId(groupId, requesterId));
        }catch (SecurityException e){
            // If you want to enforce security even for internal calls, you can catch the exception and return 403 Forbidden.
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @Operation(summary = "Update an expense and trigger async debt recalculation")
    @PutMapping("/{expenseId}")
    public ResponseEntity<Expense> updateExpense(
            @PathVariable UUID expenseId,
            @Valid @RequestBody ExpenseDto dto,
            @RequestHeader("X-User-Id") UUID requesterId) {

        // Using ACCEPTED (202) or OK (200) is fine here. ACCEPTED implies the
        // background Settlement recalculation is still processing.
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(expenseService.updateExpense(expenseId, dto, requesterId));
    }

    @Operation(summary = "Delete an expense and trigger async debt recalculation")
    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable UUID expenseId,
            @RequestHeader("X-User-Id") UUID requesterId) {

        expenseService.deleteExpense(expenseId, requesterId);

        // 204 No Content is the standard HTTP response for a successful deletion
        return ResponseEntity.noContent().build();
    }
}