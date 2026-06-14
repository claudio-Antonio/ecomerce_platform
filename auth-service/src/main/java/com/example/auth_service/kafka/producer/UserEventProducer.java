package com.example.auth_service.kafka.producer;

import com.example.auth_service.domain.User;
import com.example.auth_service.kafka.events.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserEventProducer {
    private final KafkaTemplate<String, Object>  kafkaTemplate;

    @Async
    public void publishUserRegistered(User user) {
        var event = new UserRegisteredEvent(UUID.randomUUID().toString(), user.getId().toString(), user.getEmail(),
                user.getName(), user.getRole().toString(), LocalDateTime.now());
        kafkaTemplate.send("user-registered", user.getId().toString(),event);
    }
}
