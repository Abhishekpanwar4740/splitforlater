package com.splitforlater.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "expense_splits")
@Data
@Builder @NoArgsConstructor @AllArgsConstructor
public class ExpenseSplit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    @ToString.Exclude // Crucial: Prevents infinite loop when Lombok generates toString()
    @EqualsAndHashCode.Exclude // Crucial: Prevents StackOverflow in Set/Map collections
    private Expense expense;
    private UUID userId;
    private Long amount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}