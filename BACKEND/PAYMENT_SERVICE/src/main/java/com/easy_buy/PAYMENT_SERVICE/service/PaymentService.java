package com.easy_buy.PAYMENT_SERVICE.service;

import com.easy_buy.PAYMENT_SERVICE.dto.request.PaymentRequest;
import com.easy_buy.PAYMENT_SERVICE.dto.response.PaymentResponse;
import com.easy_buy.PAYMENT_SERVICE.entity.PaymentStatus;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    // Initiate a new payment for an order
    PaymentResponse initiatePayment(PaymentRequest request);

    // Get payment details by paymentId
    PaymentResponse getPaymentById(UUID paymentId);

    // Get payment details by orderId
    PaymentResponse getPaymentByOrderId(UUID orderId);

    // Get payment by unique paymentNumber
    PaymentResponse getPaymentByNumber(String paymentNumber);

    // Get all payments of a specific user (newest first)
    List<PaymentResponse> getPaymentsByUserId(UUID userId);

    // Get all payments by status (e.g., all PENDING)
    List<PaymentResponse> getPaymentsByStatus(PaymentStatus status);

    // Update payment status (e.g., mark as COMPLETED or FAILED)
    PaymentResponse updatePaymentStatus(UUID paymentId, PaymentStatus newStatus);

    // Process refund for a payment
    PaymentResponse refundPayment(UUID paymentId);
}
