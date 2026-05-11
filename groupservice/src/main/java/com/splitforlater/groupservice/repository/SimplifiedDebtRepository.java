package com.splitforlater.groupservice.repository;

import com.splitforlater.groupservice.entity.SimplifiedDebt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SimplifiedDebtRepository extends JpaRepository<SimplifiedDebt, Long> {

    List<SimplifiedDebt> findByGroupId(UUID groupId);
    @Query(value = "SELECT SUM(sd.amount) FROM SimplifiedDebt sd WHERE sd.groupId = :groupId AND (sd.debtorId = :memberId OR sd.creditorId = :memberId)",nativeQuery = true)
    Long getTotalDebtByGroupIdAndDebtorId(UUID groupId, UUID memberId);
    @Query(value = "SELECT * FROM SimplifiedDebt sd WHERE sd.groupId = :groupId AND ad.amount>0",nativeQuery = true)
    List<SimplifiedDebt> findAllByGroupIdAndAmountNotCleared(UUID groupId);
}
