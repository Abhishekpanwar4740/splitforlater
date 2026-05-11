package com.splitforlater.notificationservice.repository;

import com.splitforlater.notificationservice.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationLog, UUID> {
    List<NotificationLog> findByGroupId(UUID groupId);
}
