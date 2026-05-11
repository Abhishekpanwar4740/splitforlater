package com.splitforlater.settlementservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "group_members", indexes = {
        @Index(name = "idx_user_groups", columnList = "userId")
})
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class GroupMember {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name="group_id")
    private UUID groupId;
    private UUID userId;
    private Long pendingBalance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
