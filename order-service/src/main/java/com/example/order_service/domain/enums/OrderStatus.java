package com.example.order_service.domain.enums;

public enum OrderStatus {
    PENDING("pending"),
    CONFIRMED("confirmed"),
    SHIPPED("shipped"),
    DELIVERED("delivered"),
    CANCELLED("canceled");

    String value;

    OrderStatus(String value) {
        this.value = value;
    }

    public String getValue() {return  value;}
}
