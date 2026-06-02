package com.example.order_service.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.order_service.domain.Payment;
import com.example.order_service.domain.enums.PaymentStatus;
import com.example.order_service.dtos.requests.PaymentRequest;
import com.example.order_service.repositories.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private PaymentRepository repository;

    private UUID paymentId;
    private Payment payment;
    private PaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        paymentId = UUID.randomUUID();

        payment = new Payment();
        payment.setId(paymentId);
        payment.setPaymentMethod("CREDIT_CARD");
        payment.setStatus(PaymentStatus.PENDING);
        payment.setAmount(0.0);
        payment.setTransactionId("PENDING");
        payment.setProcessedAt(LocalDateTime.now());

        paymentRequest = new PaymentRequest("CREDIT_CARD");
    }

    @Test
    @DisplayName("Should initialize payment attributes correctly and save successfully")
    void create_shouldCreatePaymentSuccessfully() {
        when(repository.save(any(Payment.class))).thenReturn(payment);

        Payment result = paymentService.create(paymentRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(paymentId);
        assertThat(result.getPaymentMethod()).isEqualTo("CREDIT_CARD");
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(result.getAmount()).isEqualTo(0.0);
        assertThat(result.getTransactionId()).isEqualTo("PENDING");
        verify(repository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should return list containing payments when records exist")
    void findAll_shouldReturnListOfPayments() {
        when(repository.findAll()).thenReturn(List.of(payment));

        List<Payment> result = paymentService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(paymentId);
        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no payments are registered")
    void findAll_shouldReturnEmptyListWhenNoRecords() {
        when(repository.findAll()).thenReturn(List.of());

        List<Payment> result = paymentService.findAll();

        assertThat(result).isEmpty();
        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return payment when found by id")
    void findById_shouldReturnPaymentWhenFound() {
        when(repository.findById(paymentId)).thenReturn(Optional.of(payment));

        Payment result = paymentService.findById(paymentId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(paymentId);
        verify(repository, times(1)).findById(paymentId);
    }

    @Test
    @DisplayName("Should throw RuntimeException when payment not found by id")
    void findById_shouldThrowRuntimeExceptionWhenNotFound() {
        UUID randomId = UUID.randomUUID();
        when(repository.findById(randomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.findById(randomId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Pagamento não encontrado");
        verify(repository, times(1)).findById(randomId);
    }

    @Test
    @DisplayName("Should modify payment method and save changes when payment exists")
    void update_shouldUpdateAndReturnPayment() {
        PaymentRequest updateRequest = new PaymentRequest("PIX");

        Payment updatedPayment = new Payment();
        updatedPayment.setId(paymentId);
        updatedPayment.setPaymentMethod("PIX");

        when(repository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(repository.save(any(Payment.class))).thenReturn(updatedPayment);

        Payment result = paymentService.update(paymentId, updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getPaymentMethod()).isEqualTo("PIX");
        verify(repository, times(1)).findById(paymentId);
        verify(repository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw exception and never save when updating non-existent payment")
    void update_shouldThrowExceptionAndNeverSaveWhenPaymentNotFound() {
        UUID randomId = UUID.randomUUID();
        when(repository.findById(randomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.update(randomId, paymentRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Pagamento não encontrado");

        verify(repository, times(1)).findById(randomId);
        verify(repository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should call repository deleteById when invoking delete method")
    void delete_shouldCallRepositoryDelete() {
        doNothing().when(repository).deleteById(paymentId);

        paymentService.delete(paymentId);

        verify(repository, times(1)).deleteById(paymentId);
    }
}