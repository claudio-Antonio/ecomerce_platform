package com.example.auth_service.domain.enums;

public enum Role {
    CUSTOMER("customer"),
    SELLER("seller"),
    ADMIN("admin"),
    MODERATOR("moderator");

    private String role;

    Role(String role) {
        this.role = role;
    }

    public String getRole() {return this.role;}
}
