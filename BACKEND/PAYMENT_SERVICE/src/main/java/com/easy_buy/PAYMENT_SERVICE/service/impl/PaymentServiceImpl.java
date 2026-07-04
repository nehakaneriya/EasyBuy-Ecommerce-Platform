package com.easy_buy.PAYMENT_SERVICE.service.impl;

import com.easy_buy.COMMON_SERVICE.events.PaymentEvent;
import com.easy_buy.PAYMENT_SERVICE.dto.request.PaymentRequest;
import com.easy_buy.PAYMENT_SERVICE.dto.response.PaymentResponse;
import com.easy_buy.PAYMENT_SERVICE.entity.Payment;
import com.easy_buy.PAYMENT_SERVICE.entity.PaymentMethod;
import com.easy_buy.PAYMENT_SERVICE.entity.PaymentStatus;
import com.easy_buy.PAYMENT_SERVICE.exception.ResourceNotFoundException;
import com.easy_buy.PAYMENT_SERVICE.producer.PaymentEventPublisher;
import com.easy_buy.PAYMENT_SERVICE.repository.PaymentRepository;
import com.easy_buy.PAYMENT_SERVICE.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher paymentEventPublisher;

    @Override
    public PaymentResponse initiatePayment(PaymentRequest request) {

        // Duplicate payment check — one order = one payment
        if (paymentRepository.existsByOrderId(request.getOrderId())) {
            throw new IllegalStateException("Payment already exists for orderId: " + request.getOrderId());
        }

        // ONLINE_PAYMENT requires a transactionId from gateway
        if (request.getPaymentMethod() == PaymentMethod.ONLINE_PAYMENT
                && (request.getTransactionId() == null || request.getTransactionId().isBlank())) {
            throw new IllegalArgumentException("transactionId is required for ONLINE_PAYMENT");
        }

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .paymentNumber(UUID.randomUUID().toString())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .transactionId(request.getTransactionId())
                .remarks(request.getRemarks())
                .build();

        // OFFLINE_PAYMENT is considered completed immediately on initiation
        if (request.getPaymentMethod() == PaymentMethod.OFFLINE_PAYMENT) {
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
            payment.setPaidAt(Instant.now());
        }

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment initiated: paymentId={}, orderId={}, method={}, status={}",
                savedPayment.getPaymentId(), savedPayment.getOrderId(),
                savedPayment.getPaymentMethod(), savedPayment.getPaymentStatus());

        return toResponse(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        return toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(UUID orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for orderId: " + orderId));
        return toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByNumber(String paymentNumber) {
        Payment payment = paymentRepository.findByPaymentNumber(paymentNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with paymentNumber: " + paymentNumber));
        return toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByUserId(UUID userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByPaymentStatus(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public PaymentResponse updatePaymentStatus(UUID paymentId, PaymentStatus newStatus) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        // Cannot change status of an already REFUNDED payment
        if (payment.getPaymentStatus() == PaymentStatus.REFUNDED) {
            throw new IllegalStateException("Cannot update status of a REFUNDED payment");
        }

        // Mark paidAt when completing
        if (newStatus == PaymentStatus.COMPLETED && payment.getPaidAt() == null) {
            payment.setPaidAt(Instant.now());
        }

        payment.setPaymentStatus(newStatus);
        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment status updated: paymentId={}, newStatus={}", paymentId, newStatus);

        // Kafka pe publish karo — CART-ORDER SERVICE ko batao
        publishPaymentEvent(savedPayment, newStatus.name(), "Payment status updated to " + newStatus.name());

        return toResponse(savedPayment);
    }

    @Override
    public PaymentResponse refundPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        // Only COMPLETED payments can be refunded
        if (payment.getPaymentStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Only COMPLETED payments can be refunded. Current status: "
                    + payment.getPaymentStatus());
        }

        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        payment.setRefundedAt(Instant.now());
        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment refunded: paymentId={}, orderId={}", paymentId, payment.getOrderId());

        // Kafka pe publish karo — CART-ORDER SERVICE ko batao
        publishPaymentEvent(savedPayment, PaymentStatus.REFUNDED.name(), "Payment refunded");

        return toResponse(savedPayment);
    }

    // Helper — convert Payment entity to PaymentResponse DTO
    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .paymentNumber(payment.getPaymentNumber())
                .amount(payment.getAmount())
                .paymentStatus(payment.getPaymentStatus())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .remarks(payment.getRemarks())
                .paidAt(payment.getPaidAt())
                .refundedAt(payment.getRefundedAt())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    // Helper — PaymentEvent banao aur Kafka pe publish karo
    private void publishPaymentEvent(Payment payment, String status, String message) {
        PaymentEvent event = new PaymentEvent();
        event.setOrderId(payment.getOrderId());
        event.setPaymentId(payment.getPaymentId());
        event.setPaymentStatus(status);
        event.setMessage(message);
        paymentEventPublisher.publishPaymentEvent(event);
    }
}
