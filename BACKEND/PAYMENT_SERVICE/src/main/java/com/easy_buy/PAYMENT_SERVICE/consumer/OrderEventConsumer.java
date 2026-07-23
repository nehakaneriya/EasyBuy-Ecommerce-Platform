package com.easy_buy.PAYMENT_SERVICE.consumer;

import com.easy_buy.COMMON_SERVICE.events.OrderEvent;
import com.easy_buy.COMMON_SERVICE.events.PaymentEvent;
import com.easy_buy.PAYMENT_SERVICE.dto.request.PaymentRequest;
import com.easy_buy.PAYMENT_SERVICE.dto.response.PaymentResponse;
import com.easy_buy.PAYMENT_SERVICE.entity.Payment;
import com.easy_buy.PAYMENT_SERVICE.entity.PaymentMethod;
import com.easy_buy.PAYMENT_SERVICE.entity.PaymentStatus;
import com.easy_buy.PAYMENT_SERVICE.producer.PaymentEventPublisher;
import com.easy_buy.PAYMENT_SERVICE.repository.PaymentRepository;
import com.easy_buy.PAYMENT_SERVICE.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher paymentEventPublisher;
    private final PaymentService paymentService;

    private final String ORDER_TOPIC = "order-topic";

    @KafkaListener(topics = ORDER_TOPIC, groupId = "payment-group")
    public void consumeOrderCreatedEvent(OrderEvent orderEvent) {
        log.info("Received OrderEvent from Kafka: {}", orderEvent);

        if (orderEvent.getOrderId() == null) {
            log.error("Received OrderEvent with null orderId");
            return;
        }

        try {
            PaymentRequest paymentRequest = new PaymentRequest(
                    orderEvent.getOrderId(),
                    orderEvent.getUserId(),
                    orderEvent.getTotalAmount() != null ? orderEvent.getTotalAmount() : BigDecimal.ZERO,
                    PaymentMethod.ONLINE_PAYMENT,
                    UUID.randomUUID().toString(),
                    orderEvent.getMessage() != null ? orderEvent.getMessage() : "Kafka Order Event"
            );

            log.info("Processing payment via Kafka consumer for Order ID: {}", orderEvent.getOrderId());
            PaymentResponse paymentResponse = paymentService.initiatePayment(paymentRequest);

            // Publish success acknowledgment event
            PaymentEvent paymentEvent = new PaymentEvent();
            paymentEvent.setOrderId(paymentResponse.getOrderId());
            paymentEvent.setPaymentId(paymentResponse.getPaymentId());
            paymentEvent.setPaymentStatus(paymentResponse.getPaymentStatus().name());
            paymentEvent.setMessage("Payment processed successfully via Kafka consumer");
            paymentEventPublisher.publishPaymentEvent(paymentEvent);

        } catch (Exception e) {
            log.error("Error processing payment via Kafka consumer for Order ID: {}", orderEvent.getOrderId(), e);

            // Publish failure acknowledgment event
            PaymentEvent paymentEvent = new PaymentEvent();
            paymentEvent.setOrderId(orderEvent.getOrderId());
            paymentEvent.setPaymentId(null);
            paymentEvent.setPaymentStatus(PaymentStatus.FAILED.name());
            paymentEvent.setMessage(e.getMessage());
            paymentEventPublisher.publishPaymentEvent(paymentEvent);
        }
    }
}