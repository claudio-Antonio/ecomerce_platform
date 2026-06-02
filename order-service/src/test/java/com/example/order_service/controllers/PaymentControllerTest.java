package com.example.order_service.controllers;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.example.order_service.domain.Payment;
import com.example.order_service.domain.enums.PaymentStatus;
import com.example.order_service.dtos.requests.PaymentRequest;
import com.example.order_service.dtos.responses.PaymentResponse;
import com.example.order_service.services.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @InjectMocks
    private PaymentController paymentController;

    @Mock
    private PaymentService paymentService;

    private UUID paymentId;
    private Payment payment;
    private PaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        paymentId = UUID.randomUUID();

        payment = new Payment();
        payment.setId(paymentId);
        payment.setPaymentMethod("CREDIT_CARD");
        payment.setTransactionId("TX-12345");
        payment.setStatus(PaymentStatus.PENDING);
        payment.setAmount(150.0);
        payment.setProcessedAt(LocalDateTime.now());

        paymentRequest = new PaymentRequest("CREDIT_CARD");
    }

    @Test
    @DisplayName("Should return 211 Created and mapped response when payload is valid")
    void create_shouldReturn211CreatedWithResponse() {
        when(paymentService.create(any(PaymentRequest.class))).thenReturn(payment);

        ResponseEntity<PaymentResponse> response = paymentController.create(paymentRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(paymentId);
        assertThat(response.getBody().paymentMethod()).isEqualTo("CREDIT_CARD");
        assertThat(response.getBody().status()).isEqualTo("PENDING");
        verify(paymentService, times(1)).create(any(PaymentRequest.class));
    }

    @Test
    @DisplayName("Should return 200 OK with list of mapped payments when records exist")
    void findAll_shouldReturn200WithListOfPayments() {
        when(paymentService.findAll()).thenReturn(List.of(payment));

        ResponseEntity<List<PaymentResponse>> response = paymentController.findAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).id()).isEqualTo(paymentId);
        verify(paymentService, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return 200 OK with empty list when no records exist")
    void findAll_shouldReturn200WithEmptyList() {
        when(paymentService.findAll()).thenReturn(List.of());

        ResponseEntity<List<PaymentResponse>> response = paymentController.findAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    @DisplayName("Should return 200 OK and mapped payment response when found by id")
    void findById_shouldReturn200WithPaymentWhenFound() {
        when(paymentService.findById(paymentId)).thenReturn(payment);

        ResponseEntity<PaymentResponse> response = paymentController.findById(paymentId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(paymentId);
        verify(paymentService, times(1)).findById(paymentId);
    }

    @Test
    @DisplayName("Should bubble up RuntimeException when resource does not exist")
    void findById_shouldThrowExceptionWhenNotFound() {
        UUID randomId = UUID.randomUUID();
        when(paymentService.findById(randomId)).thenThrow(new RuntimeException("Pagamento não encontrado"));

        assertThatThrownBy(() -> paymentController.findById(randomId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Pagamento não encontrado");
    }

    @Test
    @DisplayName("Should return 200 OK and mapped updated payload when modification succeeds")
    void update_shouldReturn200WithUpdatedPayment() {
        when(paymentService.update(eq(paymentId), any(PaymentRequest.class))).thenReturn(payment);

        ResponseEntity<PaymentResponse> response = paymentController.update(paymentId, paymentRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(paymentId);
        verify(paymentService, times(1)).update(eq(paymentId), any(PaymentRequest.class));
    }

    @Test
    @DisplayName("Should return 204 No Content when delete action is completed successfully")
    void delete_shouldReturn204NoContent() {
        doNothing().when(paymentService).delete(paymentId);

        ResponseEntity<Void> response = paymentController.delete(paymentId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(paymentService, times(1)).delete(paymentId);
    }
}