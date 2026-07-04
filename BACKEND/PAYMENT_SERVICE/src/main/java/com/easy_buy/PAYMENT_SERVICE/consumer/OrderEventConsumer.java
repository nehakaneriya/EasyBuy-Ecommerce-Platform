package com.easy_buy.PAYMENT_SERVICE.consumer;

import com.easy_buy.COMMON_SERVICE.events.OrderEvent;
import com.easy_buy.COMMON_SERVICE.events.PaymentEvent;
import com.easy_buy.PAYMENT_SERVICE.entity.Payment;
import com.easy_buy.PAYMENT_SERVICE.entity.PaymentMethod;
import com.easy_buy.PAYMENT_SERVICE.entity.PaymentStatus;
import com.easy_buy.PAYMENT_SERVICE.producer.PaymentEventPublisher;
import com.easy_buy.PAYMENT_SERVICE.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher paymentEventPublisher;

    @KafkaListener(topics = "order-topic", groupId = "payment-service-group")
    @Transactional
    public void consumeOrderEvent(OrderEvent event) {

        log.info("OrderEvent received: orderId={}, userId={}, status={}, totalAmount={}",
                event.getOrderId(), event.getUserId(), event.getStatus(), event.getTotalAmount());

        // Duplicate check — agar payment already exist karti hai to skip karo
        if (paymentRepository.existsByOrderId(event.getOrderId())) {
            log.warn("Payment already exists for orderId={} — skipping", event.getOrderId());
            return;
        }

        // Sirf PENDING orders ka payment record banana hai
        if (!"PENDING".equals(event.getStatus())) {
            log.info("OrderEvent status is '{}' — skipping payment creation for orderId={}",
                    event.getStatus(), event.getOrderId());
            return;
        }

        // Naya PENDING payment record banao
        // paymentMethod abhi null hai — user baad mein checkout pe provide karega
        Payment payment = Payment.builder()
                .orderId(event.getOrderId())
                .userId(event.getUserId())
                .paymentNumber(UUID.randomUUID().toString())
                .amount(event.getTotalAmount())
                .paymentStatus(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.OFFLINE_PAYMENT) // default — update hoga jab user payment kare
                .remarks("Auto-created from OrderEvent")
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment record created from OrderEvent: paymentId={}, orderId={}, amount={}",
                savedPayment.getPaymentId(), savedPayment.getOrderId(), savedPayment.getAmount());

        // OFFLINE_PAYMENT ke liye turant COMPLETED publish karo
        if (savedPayment.getPaymentMethod() == PaymentMethod.OFFLINE_PAYMENT) {
            savedPayment.setPaymentStatus(PaymentStatus.COMPLETED);
            savedPayment.setPaidAt(Instant.now());
            paymentRepository.save(savedPayment);

            PaymentEvent paymentEvent = new PaymentEvent();
            paymentEvent.setOrderId(savedPayment.getOrderId());
            paymentEvent.setPaymentId(savedPayment.getPaymentId());
            paymentEvent.setPaymentStatus(PaymentStatus.COMPLETED.name());
            paymentEvent.setMessage("OFFLINE payment auto-completed");
            paymentEventPublisher.publishPaymentEvent(paymentEvent);
        }
    }
}
