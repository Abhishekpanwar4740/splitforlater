package com.splitforlater.expenseservice.service;

import com.splitforlater.expenseservice.config.RabbitMQConfig;
import com.splitforlater.common.dto.ExpenseRequestDto;
import com.splitforlater.expenseservice.entity.Expense;
import com.splitforlater.expenseservice.entity.ExpenseSplit;
import com.splitforlater.expenseservice.repository.ExpenseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseService {

    private final ExpenseRepository expenseRepo;
    private final RedissonClient redisson;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public Expense createExpense(ExpenseRequestDto dto, UUID payerId) {
        // 1. Acquire Lock for the Group to ensure sequential balance updates
        RLock lock = redisson.getLock("lock:group:" + dto.getGroupId());

        try {
            if (lock.tryLock(5, 15, TimeUnit.SECONDS)) {
                log.info("Processing expense for group: {}", dto.getGroupId());

                // 2. Map DTO to Entity
                List<ExpenseSplit> splits = dto.getSplits().stream()
                        .map(s -> ExpenseSplit.builder().userId(s.getUserId()).amount(s.getAmount()).build())
                        .collect(Collectors.toList());

                Expense expense = Expense.builder()
                        .description(dto.getDescription())
                        .totalAmount(dto.getTotalAmount())
                        .groupId(dto.getGroupId())
                        .payerId(payerId)
                        .splits(splits)
                        .createdAt(LocalDateTime.now())
                        .build();

                // 3. Persist to PostgreSQL
                Expense saved = expenseRepo.save(expense);

                // 4. Publish Event to AWS Amazon MQ for settlement & notifications
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXPENSE_EXCHANGE, RabbitMQConfig.ROUTING_KEY, dto);

                return saved;
            } else {
                throw new RuntimeException("Could not acquire lock, please try again.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Transaction interrupted");
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }
}
