package com.splitforlater.groupservice.service;

import com.splitforlater.groupservice.dto.GroupRequestDto;
import com.splitforlater.groupservice.entity.Group;
import com.splitforlater.groupservice.entity.GroupMember;
import com.splitforlater.groupservice.repository.GroupMemberRepository;
import com.splitforlater.groupservice.repository.GroupRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {
    private final GroupRepository groupRepo;
    private final GroupMemberRepository memberRepo;
    private final RedissonClient redisson;

    @Transactional
    public Group createGroup(GroupRequestDto dto, UUID creatorId) {
        Group group = Group.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .createdBy(creatorId)
                .createdAt(LocalDateTime.now())
                .build();

        Group savedGroup = groupRepo.save(group);
        addMemberToGroup(savedGroup.getId(), creatorId); // Add creator as first member
        return savedGroup;
    }

    public void addMemberToGroup(UUID groupId, UUID userId) {
        RLock lock = redisson.getLock("lock:group-members:" + groupId);
        try {
            // Wait up to 5s for lock, lease for 10s
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                if (memberRepo.existsByGroupIdAndUserId(groupId, userId)) {
                    throw new RuntimeException("User already in group");
                }

                GroupMember member = GroupMember.builder()
                        .groupId(groupId)
                        .userId(userId)
                        .pendingBalance(0L)
                        .joinedAt(LocalDateTime.now())
                        .build();
                memberRepo.save(member);
                log.info("User {} added to group {}", userId, groupId);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }
}
