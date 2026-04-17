package com.example.auth_service.controllers.dtos;

import com.example.auth_service.domain.enums.Role;

public record RegisterDTO(String name, String email, String password, Role role) {
}
