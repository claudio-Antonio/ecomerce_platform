package com.example.order_service.domain.enums;

public enum PaymentStatus {
    PENDING("pending"),
    AUTHORIZED("authorized"),
    PAID("paid"),
    FAILED("failed"),
    REFUNDED("refunded");

    String value;

    PaymentStatus(String value) {
        this.value = value;
    }

    public String getValue() {return value;}
}
