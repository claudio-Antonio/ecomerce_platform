package com.example.order_service.services;

import com.example.order_service.domain.Payment;
import com.example.order_service.domain.enums.PaymentStatus;
import com.example.order_service.dtos.requests.PaymentRequest;
import com.example.order_service.dtos.responses.PaymentResponse;
import com.example.order_service.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository repository;

    public Payment create(PaymentRequest request) {
        Payment payment = new Payment();
        payment.setPaymentMethod(request.paymentMethod());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setProcessedAt(LocalDateTime.now());
        payment.setAmount(0.0);
        payment.setTransactionId(UUID.randomUUID().toString());

        return repository.save(payment);
    }

    public List<Payment> findAll() {
        return repository.findAll();
    }

    public Payment findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));
    }

    public Payment update(UUID id, PaymentRequest request) {
        Payment p = findById(id);
        p.setPaymentMethod(request.paymentMethod());
        return repository.save(p);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }
}
