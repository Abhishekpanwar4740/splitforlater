package com.splitforlater.notificationservice.controller;

import com.splitforlater.notificationservice.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationLog, UUID> {
}
