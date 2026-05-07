package com.splitforlater.notificationservice.service;

import io.awspring.cloud.sns.core.SnsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SnsTemplate snsTemplate;
    private final SesClient sesClient;
    private final RedissonClient redisson;
    @Value("${io.awspring.cloud.ses.source}")
    private String fromEmail;

    public void sendSms(String phoneNumber, String message) {
        snsTemplate.sendNotification("ExpenseAlerts", message, "Splitwise Update");
        log.info("SMS sent to {}", phoneNumber);
    }

    public void sendEmail(String toEmail, String subject, String body) {
        SendEmailRequest request = SendEmailRequest.builder()
                .destination(d -> d.toAddresses(toEmail))
                .message(m -> m.subject(s -> s.data(subject))
                        .body(b -> b.text(t -> t.data(body))))
                .source(fromEmail)
                .build();

        sesClient.sendEmail(request);
        log.info("Email sent to {}", toEmail);
    }

    public void sendThrottledEmail(String userId, String email, String body) {
        // 1. Create a RateLimiter: Allow only 1 email per 10 seconds per user
        RRateLimiter limiter = redisson.getRateLimiter("customer_limit:" + userId);
        limiter.trySetRate(RateType.OVERALL, 1, 10, RateIntervalUnit.SECONDS);

        if (limiter.tryAcquire()) {
            // Logic to send SES Email
            sendEmail(email, "New Update", body);
        } else {
            log.warn("Notification throttled for user: {}", userId);
        }
    }
}
