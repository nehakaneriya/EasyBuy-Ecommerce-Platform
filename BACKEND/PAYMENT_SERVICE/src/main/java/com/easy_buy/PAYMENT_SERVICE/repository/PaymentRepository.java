package com.easy_buy.PAYMENT_SERVICE.repository;

import com.easy_buy.PAYMENT_SERVICE.entity.Payment;
import com.easy_buy.PAYMENT_SERVICE.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    // Find payment by orderId (one order has one payment)
    Optional<Payment> findByOrderId(UUID orderId);

    // Find all payments of a specific user, newest first
    List<Payment> findByUserIdOrderByCreatedAtDesc(UUID userId);

    // Find payment by unique paymentNumber
    Optional<Payment> findByPaymentNumber(String paymentNumber);

    // Find all payments by status (e.g. all PENDING payments)
    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);

    // Check if a payment already exists for a given orderId
    boolean existsByOrderId(UUID orderId);
}
