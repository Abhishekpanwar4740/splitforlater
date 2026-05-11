package com.splitforlater.settlementservice.controller;

import com.splitforlater.settlementservice.entity.SimplifiedDebt;
import com.splitforlater.settlementservice.repository.SimplifiedDebtRepository;
import com.splitforlater.settlementservice.service.SettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
@Tag(name = "Settlement Service", description = "Endpoints for managing simplified debts and settlements")
public class SettlementController {
    private final SettlementService settlementService;
    @Operation(summary = "Get simplified debts for a group")
    @GetMapping("/{groupId}")
    public List<SimplifiedDebt> getSimplifiedDebts(@PathVariable UUID groupId,
                                                   @RequestHeader("X-User-Id") UUID requesterId) {
        return settlementService.getSimplifiedDebts(groupId,requesterId);
    }
}
