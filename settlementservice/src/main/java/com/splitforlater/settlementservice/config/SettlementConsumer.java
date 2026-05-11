package com.splitforlater.settlementservice.config;

import com.splitforlater.common.config.RabbitMQConfig;
import com.splitforlater.common.dto.ExpenseEvent;
import com.splitforlater.common.dto.SplitDto;
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

    @RabbitListener(queues = RabbitMQConfig.EXPENSE_QUEUE_NAME)
    public void onExpenseEvent(ExpenseEvent event) {
        log.info("Received {} event for group: {}", event.getAction(), event.getGroupId());

        // 1. Fetch full ACTIVE history from Expense Service via Feign
        // If an expense was deleted, the Expense Service will NOT return it in this list.
        List<ExpenseResponseDto> history = expenseClient.getExpensesByGroup(event.getGroupId());

        // 2. Aggregate all expenses into a single Net Balance map
        // The math naturally reflects the deletion because the deleted item is gone from the history.
        Map<UUID, Long> aggregateBalances = calculateNetBalances(history);

        // 3. Run the "Simplify Debt" Algorithm
        // This will wipe the old simplified debts and write the new correct ones.
        settlementService.simplifyDebts(event.getGroupId(), aggregateBalances);
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
                UUID userId = split.getUser().getUserId();
                netBalances.put(userId, netBalances.getOrDefault(userId, 0L) - split.getAmount());
            }
        }
        return netBalances;
    }
}