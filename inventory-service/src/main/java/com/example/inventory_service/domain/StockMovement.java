package com.example.inventory_service.domain;

import com.example.inventory_service.domain.enums.MovementType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "tb_stock_movement")
public class StockMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private MovementType type;
    @Column(nullable = false)
    private Integer quantity;
    @Column(nullable = false)
    private String reason;
    @Column(nullable = true)
    private UUID orderId;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;
}
