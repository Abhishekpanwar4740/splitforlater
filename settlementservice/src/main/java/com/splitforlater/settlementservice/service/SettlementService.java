package com.splitforlater.settlementservice.service;

import com.splitforlater.settlementservice.dto.UserBalance;
import com.splitforlater.settlementservice.entity.SimplifiedDebt;
import com.splitforlater.settlementservice.repository.SimplifiedDebtRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementService {

    private final SimplifiedDebtRepository debtRepo;

    @Transactional
    public void simplifyDebts(UUID groupId, Map<UUID, Long> netBalances) {
        // 1. Clear old simplified debts for this group
        debtRepo.deleteByGroupId(groupId);

        // 2. Separate into Givers and Receivers
        PriorityQueue<UserBalance> givers = new PriorityQueue<>(Comparator.comparingLong(UserBalance::getBalance));
        PriorityQueue<UserBalance> receivers = new PriorityQueue<>((a, b) -> Long.compare(b.getBalance(), a.getBalance()));

        netBalances.forEach((userId, balance) -> {
            if (balance < 0) givers.add(new UserBalance(userId, balance));
            else if (balance > 0) receivers.add(new UserBalance(userId, balance));
        });

        List<SimplifiedDebt> newDebts = new ArrayList<>();

        // 3. Greedy Matching Algorithm
        while (!givers.isEmpty() && !receivers.isEmpty()) {
            UserBalance giver = givers.poll();
            UserBalance receiver = receivers.poll();

            long amount = Math.min(-giver.getBalance(), receiver.getBalance());

            newDebts.add(SimplifiedDebt.builder()
                    .groupId(groupId)
                    .debtorId(giver.getUserId())
                    .creditorId(receiver.getUserId())
                    .amount(amount)
                    .build());

            giver.setBalance(giver.getBalance() + amount);
            receiver.setBalance(receiver.getBalance() - amount);

            if (giver.getBalance() < 0) givers.add(giver);
            if (receiver.getBalance() > 0) receivers.add(receiver);
        }

        debtRepo.saveAll(newDebts);
        log.info("Simplified {} debts for group {}", newDebts.size(), groupId);
    }
}
