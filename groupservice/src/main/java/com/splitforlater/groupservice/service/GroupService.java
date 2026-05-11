package com.splitforlater.groupservice.service;

import com.splitforlater.common.exceptionhandling.GroupNotFoundException;
import com.splitforlater.common.exceptionhandling.UserNotFoundException;
import com.splitforlater.groupservice.dto.GroupRequestDto;
import com.splitforlater.groupservice.entity.Group;
import com.splitforlater.groupservice.entity.GroupMember;
import com.splitforlater.groupservice.entity.User;
import com.splitforlater.groupservice.repository.GroupMemberRepository;
import com.splitforlater.groupservice.repository.GroupRepository;
import com.splitforlater.groupservice.repository.SimplifiedDebtRepository;
import com.splitforlater.groupservice.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
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
    private final UserRepository userRepository;
    private final SimplifiedDebtRepository simplifiedDebtRepository;

    @Transactional
    public Group createGroup(GroupRequestDto dto, UUID creatorId) {
        Group group = Group.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .createdBy(creatorId)
                .createdAt(LocalDateTime.now())
                .build();

        Group savedGroup = groupRepo.save(group);
        addMemberToGroup(savedGroup.getId(), creatorId, creatorId); // Add creator as first member
        return savedGroup;
    }

    public void addMemberToGroup(UUID groupId, UUID userId,UUID requesterId) {
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
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
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

    @Transactional
    public Group getGroupById(UUID groupId, UUID requesterId) {
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found"));

        // Security Check: Only members should be able to view the group
        if (!isUserInGroup(group, requesterId)) {
            throw new SecurityException("You do not have permission to view this group.");
        }

        return group;
    }

    @Transactional
    public Group updateGroup(UUID groupId, GroupRequestDto dto, UUID requesterId) {
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found"));

        // Security Check: Only members (or specifically group admins) can update
        if (!isUserInGroup(group, requesterId)) {
            throw new SecurityException("You do not have permission to update this group.");
        }

        group.setName(dto.getName());
        // update other fields like description, group picture, etc.

        return groupRepo.save(group);
    }

    @Transactional
    public void deleteGroup(UUID groupId, UUID requesterId) {
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));

        // Security Check: Usually only the creator/admin can delete the group
        if (!group.getCreatedBy().equals(requesterId)) {
            throw new SecurityException("Only the group creator can delete this group.");
        }

        // SPLITWISE BUSINESS LOGIC:
        // You cannot delete a group if there are unsettled expenses!
         boolean hasUnsettledExpenses = !simplifiedDebtRepository.findAllByGroupIdAndAmountNotCleared(groupId).isEmpty();
         if (hasUnsettledExpenses) {
             throw new IllegalStateException("Cannot delete group with unsettled balances. Please settle up first.");
         }

        // It is often safer to do a "Soft Delete" by changing a status flag
        // group.setActive(false);
        //groupRepo.save(group);
         groupRepo.delete(group);
    }

    @Transactional
    public void removeMemberFromGroup(UUID groupId, UUID memberId, UUID requesterId) {

        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));

        User userToRemove = userRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Security Check: A user can remove themselves, OR the group creator can remove them.
        if (!requesterId.equals(memberId) && !group.getCreatedBy().equals(requesterId)) {
            throw new SecurityException("You do not have permission to remove this user.");
        }

        if (!isUserInGroup(group, memberId)) {
            throw new IllegalArgumentException("User is not a member of this group.");
        }

        // SPLITWISE BUSINESS LOGIC:
        // A user CANNOT leave a group if they owe money or are owed money within that group.
        Long userBalanceInGroup = simplifiedDebtRepository.getTotalDebtByGroupIdAndDebtorId(groupId, memberId);
        if (userBalanceInGroup.compareTo(0L) != 0) {
            throw new IllegalStateException("User cannot be removed: All balances must be settled first.");
        }
        group.getMembers().remove(userToRemove);
        groupRepo.save(group);
        log.info("User {} removed from group {}", memberId, groupId);
    }

    // --- Helper Methods ---

    private boolean isUserInGroup(Group group, UUID userId) {
        return group.getMembers().stream()
                .anyMatch(member -> member.getId().equals(userId));
    }
}
