package com.example.order_service.infra.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

import com.example.order_service.IntegrationTestBase;
import com.example.order_service.domain.Order;
import com.example.order_service.domain.enums.OrderStatus;
import com.example.order_service.infra.kafka.events.StockReservedEvent;
import com.example.order_service.repositories.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

class OrderKafkaFlowIT extends IntegrationTestBase {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    @DisplayName("Should update database entity to CONFIRMED when StockReservedEvent arrives via Kafka topic")
    void shouldConfirmOrderWhenStockReservedEventIsReceived() throws Exception {

        Order pendingOrder = new Order();
        // NÃO definimos o ID manualmente aqui. Deixamos o @GeneratedValue agir.
        pendingOrder.setUserId(UUID.randomUUID());
        pendingOrder.setTotalAmount(199.90);
        pendingOrder.setStatus(OrderStatus.PENDING);
        pendingOrder.setCreatedAt(LocalDateTime.now());
        pendingOrder.setUpdatedAt(LocalDateTime.now());
        pendingOrder.setItems(new ArrayList<>());

        // Salva e força a persistência imediata para gerar o UUID real no banco de dados
        Order savedOrder = orderRepository.saveAndFlush(pendingOrder);
        UUID generatedOrderId = savedOrder.getId(); // Captura o ID gerado pelo banco

        // Prepara o evento simulado com o ID correto que o banco acabou de criar
        StockReservedEvent responseEvent = new StockReservedEvent(
                UUID.randomUUID().toString(),
                generatedOrderId.toString(),
                LocalDateTime.now()
        );

        // Publica no tópico do Kafka do Testcontainers
        kafkaTemplate.send("stock-reserved", generatedOrderId.toString(), responseEvent);

        // O Awaitility aguarda assincronamente até que o StockEventConsumer processe a mensagem
        await().atMost(5, SECONDS).untilAsserted(() -> {
            Order updatedOrder = orderRepository.findById(generatedOrderId).orElse(null);
            assertThat(updatedOrder).isNotNull();
            assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        });
    }
}