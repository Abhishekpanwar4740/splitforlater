package com.splitforlater.settlementservice.repository;

import com.splitforlater.settlementservice.entity.SimplifiedDebt;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SimplifiedDebtRepository extends JpaRepository<SimplifiedDebt, Long> {

    List<SimplifiedDebt> findByGroupId(UUID groupId);

    @Modifying
    @Transactional
    @Query("DELETE FROM SimplifiedDebt s WHERE s.groupId = :groupId")
    void deleteByGroupId(@Param("groupId") UUID groupId);
}
