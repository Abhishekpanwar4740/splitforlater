package com.splitforlater.notificationservice.config;

import com.splitforlater.common.config.RabbitMQConfig;
import com.splitforlater.common.dto.SettlementEventDto;
import com.splitforlater.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.SETTLEMENT_QUEUE_NAME)
    public void handleSettlementEvent(SettlementEventDto event) {
        String creditorMessage = String.format("%s settled amount of %s with you in group %s.",
                event.getPayeeUser().getName(), event.getAmount(), event.getGroup().getGroupId());
        String payeeMessage = String.format("You settled amount of %s with %s in group %s.",
                event.getAmount(), event.getCreditorUser().getName(), event.getGroup().getGroupId());
        // In a real app, you'd fetch user contact info from User Service
        notificationService.sendEmail(event.getCreditorUser().getEmail(), "Settlement Update", creditorMessage,event.getGroup().getGroupId());
        notificationService.sendEmail(event.getPayeeUser().getEmail(), "Settlement Update", payeeMessage,event.getGroup().getGroupId());
    }
}
