package com.splitforlater.notificationservice.config;

import com.splitforlater.notificationservice.dto.SettlementEventDto;
import com.splitforlater.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = "notification.queue")
    public void handleSettlementEvent(SettlementEventDto event) {
        String message = String.format("Balance Updated! You owe %s: $%d",
                event.getCreditorName(), event.getAmount());

        // In a real app, you'd fetch user contact info from User Service
        notificationService.sendEmail(event.getUserEmail(), "Settlement Update", message);
    }
}
