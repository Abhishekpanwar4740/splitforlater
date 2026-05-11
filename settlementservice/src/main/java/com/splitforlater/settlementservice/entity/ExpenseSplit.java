package com.splitforlater.settlementservice.entity;

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
    private UUID userId;
    private Long amount; // Using Long for monetary values (e.g., storing cents) is excellent practice!
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // The ManyToOne side handles the actual foreign key in the database
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    @ToString.Exclude // Crucial: Prevents infinite loop when Lombok generates toString()
    @EqualsAndHashCode.Exclude // Crucial: Prevents StackOverflow in Set/Map collections
    private Expense expense;
}