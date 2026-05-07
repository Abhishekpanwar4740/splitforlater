package com.splitforlater.settlementservice.controller;

import com.splitforlater.settlementservice.entity.SimplifiedDebt;
import com.splitforlater.settlementservice.repository.SimplifiedDebtRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
public class SettlementController {
    private final SimplifiedDebtRepository repo;

    @GetMapping("/{groupId}")
    public List<SimplifiedDebt> getSimplified(@PathVariable UUID groupId) {
        return repo.findByGroupId(groupId);
    }
}
