package com.example.auth_service.kafka.events;

import jakarta.validation.Valid;

import java.time.LocalDateTime;

public record UserRegisteredEvent(@Valid String eventId, @Valid String userId, @Valid String email, @Valid String name, @Valid String role, @Valid
                                  LocalDateTime occurredAt) {
}
