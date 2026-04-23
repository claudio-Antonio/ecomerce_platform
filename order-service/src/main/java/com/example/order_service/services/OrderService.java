package com.example.order_service.services;

import com.example.order_service.domain.Order;
import com.example.order_service.domain.Payment;
import com.example.order_service.domain.enums.OrderStatus;
import com.example.order_service.dtos.requests.OrderRequest;
import com.example.order_service.dtos.requests.PaymentRequest;
import com.example.order_service.dtos.responses.OrderResponse;
import com.example.order_service.repositories.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;

    @Transactional
    public Order create(OrderRequest request) {
        // 1. Cria o Pedido base
        Order order = new Order();
        order.setUserId(request.userId());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(0.0); // O cálculo pode ser atualizado após adicionar os items

        Order savedOrder = orderRepository.save(order);

        // 2. Cria o Pagamento vinculado ao pedido
        PaymentRequest payRequest = new PaymentRequest(request.PaymentMethod());
        Payment payment = paymentService.create(payRequest);

        // 3. Vincula o pagamento ao pedido e vice-versa
        payment.setOrder(savedOrder);
        savedOrder.setPayment(payment);

        return orderRepository.save(savedOrder);
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