package com.splitforlater.groupservice.repository;

import com.splitforlater.groupservice.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {
    // Standard CRUD operations are inherited
}
