package com.splitforlater.expenseservice.service;

import com.splitforlater.common.dto.*;
import com.splitforlater.common.config.RabbitMQConfig;
import com.splitforlater.expenseservice.entity.Expense;
import com.splitforlater.expenseservice.entity.ExpenseSplit;
import com.splitforlater.expenseservice.repository.ExpenseRepository;
import com.splitforlater.expenseservice.repository.GroupRepository;
import jakarta.persistence.EntityNotFoundException;
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
    private final GroupRepository groupRepository;
    /**
     * Used by the Frontend to get a specific expense's details.
     */
    @Transactional
    public Expense getExpenseById(UUID expenseId, UUID requesterId) {
        Expense expense = expenseRepo.findById(expenseId)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found with ID: " + expenseId));

        // SECURITY CHECK: Ensure the user requesting this expense is actually
        // a member of the group. You should not be able to view strangers' expenses!
        // Note: You might need to inject your GroupRepository or GroupClient here to verify.

         boolean isMember = groupRepository.existsByIdAndMembers_Id(expense.getGroupId(), requesterId);
         if (!isMember) {
             throw new SecurityException("You are not authorized to view expenses in this group.");
         }

        return expense;
    }

    /**
     * Used internally by the Settlement Service (via Feign Client)
     * to calculate the global balance sheet.
     */
    @Transactional
    public List<Expense> getExpensesByGroupId(UUID groupId, UUID requesterId) {
        // Because this is primarily an internal system-to-system call
        // (SettlementService calling ExpenseService), we might bypass the user authorization check.
        // If you expose this to the frontend directly, you MUST add the requesterId check here too!
        boolean isMember = groupRepository.existsByIdAndMembers_Id(groupId, requesterId);
        if (!isMember) {
            throw new SecurityException("You are not authorized to view expenses in this group.");
        }

        return expenseRepo.findByGroupId(groupId);
    }
    @Transactional
    public Expense createExpense(ExpenseDto dto, UUID payerId) {
        // 1. Acquire Lock for the Group to ensure sequential balance updates
        RLock lock = redisson.getLock("lock:group:" + dto.getGroup().getGroupId());

        try {
            if (lock.tryLock(5, 15, TimeUnit.SECONDS)) {
                log.info("Processing expense for group: {}", dto.getGroup().getGroupId());

                // 2. Map DTO to Entity
                List<ExpenseSplit> splits = dto.getSplits().stream()
                        .map(s -> ExpenseSplit.builder().userId(s.getUser().getUserId()).amount(s.getAmount()).build())
                        .collect(Collectors.toList());

                Expense expense = Expense.builder()
                        .description(dto.getDescription())
                        .totalAmount(dto.getTotalAmount())
                        .groupId(dto.getGroup().getGroupId())
                        .payerId(payerId)
                        .expenseType(dto.getExpenseType().name())
                        .splits(splits)
                        .createdAt(LocalDateTime.now())
                        .build();

                // 3. Persist to PostgreSQL
                Expense saved = expenseRepo.save(expense);
                ExpenseEvent event = ExpenseEvent.builder()
                        .groupId(dto.getGroup().getGroupId())
                        .expenseId(saved.getId())
                        .action(ExpenseEvent.Action.CREATED)
                        .build();

                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXPENSE_EXCHANGE_NAME,
                        RabbitMQConfig.EXPENSE_ROUTING_KEY,
                        event
                );
                if(ExpenseEnum.SETTLEMENT.equals(dto.getExpenseType())){
                    log.info("Created settlement expense {} and published CREATED event for group {}", saved.getId(), dto.getGroup().getGroupId());
                    List<SplitDto> userSplits=dto.getSplits();
                    SettlementEventDto settlementEventDto= SettlementEventDto.builder()
                            .group(dto.getGroup())
                            .payeeUser(dto.getUser())
                            .creditorUser(userSplits.getFirst().getUser())
                            .amount(dto.getTotalAmount())
                            .build();
                    rabbitTemplate.convertAndSend(
                            RabbitMQConfig.SETTLEMENT_EXCHANGE_NAME,
                            RabbitMQConfig.SETTLEMENT_ROUTING_KEY,
                            settlementEventDto
                    );
                }
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

    @Transactional
    public Expense updateExpense(UUID expenseId, ExpenseDto dto, UUID requesterId) {
        // 1. Fetch existing expense
        Expense existingExpense = expenseRepo.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        // 2. Security Check (Only the payer can edit their expense)
        if (!existingExpense.getPayerId().equals(requesterId)) {
            throw new SecurityException("Not authorized to update this expense");
        }

        // Verify the group hasn't changed (moving expenses between groups is usually not allowed)
        if (!existingExpense.getGroupId().equals(dto.getGroup().getGroupId())) {
            throw new IllegalArgumentException("Cannot move expense to a different group");
        }

        UUID groupId = existingExpense.getGroupId();

        // 3. Acquire Lock for the Group
        RLock lock = redisson.getLock("lock:group:" + groupId);

        try {
            if (lock.tryLock(5, 15, TimeUnit.SECONDS)) {
                log.info("Updating expense {} for group: {}", expenseId, groupId);

                // 4. Update basic scalar fields
                existingExpense.setDescription(dto.getDescription());
                existingExpense.setTotalAmount(dto.getTotalAmount());
                // Optionally update the payer if the frontend allows changing who paid
                // existingExpense.setPayerId(dto.getPayerId());

                // 5. Update the Splits safely
                // Clear the old list. Because of orphanRemoval=true, JPA will delete the old rows.
                existingExpense.getSplits().clear();

                // Map the new splits from the DTO
                List<ExpenseSplit> newSplits = dto.getSplits().stream()
                        .map(s -> {
                            ExpenseSplit split = ExpenseSplit.builder()
                                    .userId(s.getUser().getUserId())
                                    .amount(s.getAmount())
                                    .build();
                            // IMPORTANT: Set the parent reference if your mapping requires it
                            // split.setExpense(existingExpense);
                            return split;
                        })
                        .collect(Collectors.toList());

                // Add the new splits back to the managed collection
                existingExpense.getSplits().addAll(newSplits);

                // 6. Persist to PostgreSQL
                Expense saved = expenseRepo.save(existingExpense);

                // 7. Publish Event to RabbitMQ
                ExpenseEvent event = ExpenseEvent.builder()
                        .groupId(groupId)
                        .expenseId(expenseId)
                        .action(ExpenseEvent.Action.UPDATED)
                        // Optional: include the payload if your consumer needs it without fetching
                        // .expenseDetails(dto)
                        .build();

                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXPENSE_EXCHANGE_NAME,
                        RabbitMQConfig.EXPENSE_ROUTING_KEY,
                        event
                );

                return saved;
            } else {
                throw new RuntimeException("Could not acquire lock for update, please try again.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Transaction interrupted");
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }

    @Transactional
    public void deleteExpense(UUID expenseId, UUID requesterId) {
        Expense expense = expenseRepo.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        // 1. Security Check (Only creator or involved user can delete)
        if (!expense.getPayerId().equals(requesterId)) {
            throw new SecurityException("Not authorized to delete this expense");
        }

        UUID groupId = expense.getGroupId();

        // 2. Lock the group to prevent race conditions during calculation
        RLock lock = redisson.getLock("lock:group:" + groupId);
        try {
            if (lock.tryLock(5, 15, TimeUnit.SECONDS)) {

                // 3. Delete from DB (or Soft Delete: expense.setActive(false); expenseRepo.save(expense);)
                expenseRepo.delete(expense);

                // 4. Publish DELETED event
                ExpenseEvent event = ExpenseEvent.builder()
                        .groupId(groupId)
                        .expenseId(expenseId)
                        .action(ExpenseEvent.Action.DELETED)
                        .build();

                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXPENSE_EXCHANGE_NAME,
                        RabbitMQConfig.EXPENSE_ROUTING_KEY,
                        event
                );

                log.info("Deleted expense {} and published DELETED event for group {}", expenseId, groupId);
            } else {
                throw new RuntimeException("Could not acquire lock for deletion.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Transaction interrupted");
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }
}
