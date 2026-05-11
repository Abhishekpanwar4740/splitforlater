package com.splitforlater.expenseservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "expenses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String description;
    private Long totalAmount;
    @Column(name="group_id")
    private UUID groupId;
    private UUID payerId;
    private String expenseType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 1. mappedBy makes this Bidirectional (points to the 'expense' field in ExpenseSplit)
    // 2. orphanRemoval = true ensures that removing an item from this list deletes it from the DB
    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default // Prevents Lombok builder from overriding this with null
    private List<ExpenseSplit> splits = new ArrayList<>();
}


