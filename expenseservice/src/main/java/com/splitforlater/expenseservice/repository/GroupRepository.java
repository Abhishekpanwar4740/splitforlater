package com.splitforlater.expenseservice.repository;

import com.splitforlater.expenseservice.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {
    // Standard CRUD operations are inherited
    boolean existsByIdAndMembers_Id(UUID groupId, UUID memberId);
//    @Query("SELECT COUNT(g) > 0 FROM Group g JOIN g.members m WHERE g.id = :groupId AND m.id = :memberId")
//    boolean isUserInGroup(@Param("groupId") UUID groupId, @Param("memberId") UUID memberId);
}
