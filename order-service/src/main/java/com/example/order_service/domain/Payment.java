package com.example.order_service.domain;

import com.example.order_service.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "tb_payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    private UUID id;
    @Column(nullable = false)
    private String paymentMethod;
    @Column(nullable = false)
    private String transactionId;
    @Column(nullable = false)
    private PaymentStatus status;
    @Column(nullable = false)
    private Double amount;
    @Column(nullable = false)
    private LocalDateTime processedAt;
    @OneToOne(mappedBy = "payment")
    private Order order;
}
