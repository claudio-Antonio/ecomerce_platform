package com.example.inventory_service.dtos.requests;

import com.example.inventory_service.domain.Product;

import java.time.LocalDateTime;
import java.util.List;

public record CategoryRequest(String name, String description) {
}
