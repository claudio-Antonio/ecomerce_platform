package com.example.inventory_service.infra.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic stockReservedTopic() {
        return TopicBuilder.name("stock-reserved")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic stockFailedTopic() {
        return TopicBuilder.name("stock-failed")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
