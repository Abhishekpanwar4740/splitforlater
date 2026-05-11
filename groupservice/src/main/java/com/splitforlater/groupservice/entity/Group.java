package com.splitforlater.groupservice.entity;

import com.splitforlater.common.dto.ExpenseDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "groups")
@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String name;
    private String description;
    @Column(name = "created_by")
    private UUID createdBy;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private List<GroupMember> members;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private List<Expense> expenses;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

