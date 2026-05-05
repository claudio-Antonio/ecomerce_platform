package com.example.order_service.services;

import com.example.order_service.clients.InventoryClient;
import com.example.order_service.domain.Order;
import com.example.order_service.domain.OrderItem;
import com.example.order_service.domain.Payment;
import com.example.order_service.domain.enums.OrderStatus;
import com.example.order_service.dtos.requests.OrderRequest;
import com.example.order_service.dtos.requests.PaymentRequest;
import com.example.order_service.infra.kafka.producer.OrderEventProducer;
import com.example.order_service.infra.redis.ProductPriceCacheService;
import com.example.order_service.repositories.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final OrderEventProducer  orderEventProducer;
    private final ProductPriceCacheService productPriceCacheService;

    @Transactional
    public Order create(OrderRequest request) {

        // 1. busca preços e monta os itens
        List<OrderItem> items = request.items().stream()
                .map(itemReq -> {
                    Double price = productPriceCacheService.getPrice(itemReq.productId());
                    return OrderItem.builder()
                            .productId(itemReq.productId())
                            .quantity(itemReq.quantity())
                            .priceAtPurchase(price)
                            .build();
                }).toList();

        // 2. calcula o total
        double total = items.stream()
                .mapToDouble(i -> i.getPriceAtPurchase() * i.getQuantity())
                .sum();

        // 3. cria o pagamento com o total real
        PaymentRequest payRequest = new PaymentRequest(request.PaymentMethod());
        Payment payment = paymentService.create(payRequest);
        payment.setAmount(total);

        // 4. cria o pedido
        Order order = new Order();
        order.setUserId(request.userId());
        order.setTotalAmount(total);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setPayment(payment);

        // 5. vincula os itens ao pedido
        items.forEach(i -> i.setOrder(order));
        order.setItems(new ArrayList<>(items));

        Order saved = orderRepository.save(order);

        // 6. publica no Kafka
        orderEventProducer.publishOrderCreated(saved);

        return saved;
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public Order findById(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
    }

    @Transactional
    public Order update(UUID id, OrderRequest request) {
        Order existing = findById(id);

        // Atualiza campos permitidos
        existing.setUserId(request.userId());
        existing.setUpdatedAt(LocalDateTime.now());

        return orderRepository.save(existing);
    }

    public void delete(UUID id) {
        // Importante: Em sistemas reais, você talvez precise deletar
        // o pagamento vinculado antes de deletar o pedido.
        orderRepository.deleteById(id);
    }
}