package com.splitforlater.expenseservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "expenses")
@Data
@Builder @NoArgsConstructor @AllArgsConstructor
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String description;
    private Long totalAmount;
    private UUID groupId;
    private UUID payerId;
    private LocalDateTime createdAt;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id")
    private List<ExpenseSplit> splits;
}


