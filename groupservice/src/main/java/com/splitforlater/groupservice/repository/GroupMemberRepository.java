package com.splitforlater.groupservice.repository;

import com.splitforlater.groupservice.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    boolean existsByGroupIdAndUserId(UUID groupId, UUID userId);

    List<GroupMember> findByUserId(UUID userId);

    Optional<GroupMember> findByGroupIdAndUserId(UUID groupId, UUID userId);

    // Used for settling balances: fetches all members of a specific group
    List<GroupMember> findAllByGroupId(UUID groupId);
}