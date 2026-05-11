package com.splitforlater.notificationservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.messagemanager.sns.SnsMessageManager;
import software.amazon.awssdk.regions.Region;


@Configuration
public class SnsMessageManagerConfig {

    @Bean
    public SnsMessageManager snsMessageManager(
            @Value("${AWS_REGION:us-east-1}") String region) {

        // Use the builder methods actually present in the interface
        return SnsMessageManager.builder()
                .region(Region.of(region))
                .build();
    }
}

