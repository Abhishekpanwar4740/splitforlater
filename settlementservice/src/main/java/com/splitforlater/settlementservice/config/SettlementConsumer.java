package com.splitforlater.settlementservice.config;

import com.splitforlater.settlementservice.client.ExpenseClient;
import com.splitforlater.settlementservice.dto.ExpenseResponseDto;
import com.splitforlater.settlementservice.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementConsumer {

    private final SettlementService settlementService;
    private final ExpenseClient expenseClient;

    @RabbitListener(queues = "settlement.queue")
    public void onExpenseCreated(ExpenseRequestDto dto) {
        log.info("Received expense event for group: {}", dto.getGroupId());

        // 1. Fetch full history from Expense Service via Feign
        List<ExpenseResponseDto> history = expenseClient.getExpensesByGroup(dto.getGroupId());

        // 2. USE THE METHOD HERE: Aggregate all expenses into a single Net Balance map
        // We pass the entire history to our calculation engine
        Map<UUID, Long> aggregateBalances = calculateNetBalances(history);

        // 3. Run the "Simplify Debt" Algorithm with the calculated balances
        settlementService.simplifyDebts(dto.getGroupId(), aggregateBalances);
    }

    /**
     * THE LOGIC:
     * Converts a list of expenses into a final "Net State" for every user.
     */
    private Map<UUID, Long> calculateNetBalances(List<ExpenseResponseDto> history) {
        Map<UUID, Long> netBalances = new HashMap<>();

        for (ExpenseResponseDto expense : history) {
            UUID payerId = expense.getPayerId();

            // Credit the Payer (They get (+) the total amount)
            netBalances.put(payerId, netBalances.getOrDefault(payerId, 0L) + expense.getTotalAmount());

            // Debit the Participants (They get (-) their specific share)
            for (SplitDto split : expense.getSplits()) {
                UUID userId = split.getUserId();
                netBalances.put(userId, netBalances.getOrDefault(userId, 0L) - split.getAmount());
            }
        }
        return netBalances;
    }
}