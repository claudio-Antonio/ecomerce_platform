package com.example.inventory_service.domain.enums;

public enum MovementType {
    IN("in"),
    OUT("out"),
    RESERVED("reserved"),
    RELEASED("released");

    String value;

    MovementType(String value) {
        this.value = value;
    }

    String getValue() {
        return value;
    }
}
