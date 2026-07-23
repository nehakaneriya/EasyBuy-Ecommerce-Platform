package com.easy_buy.CART_ORDER_SERVICE.consumer;

import com.easy_buy.CART_ORDER_SERVICE.service.OrderService;
import com.easy_buy.COMMON_SERVICE.events.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final OrderService orderService;


    @KafkaListener(topics = "payment-topic", groupId = "order-group")
    public void consumePaymentEvent(PaymentEvent paymentEvent) {
        log.info("Received PaymentEvent from Kafka: {}", paymentEvent);

        if (paymentEvent.getOrderId() == null) {
            log.error("Received PaymentEvent with null orderId");
            return;
        }

        try {
            log.info("Updating order payment status from Kafka for Order ID: {} with status: {}",
                    paymentEvent.getOrderId(), paymentEvent.getPaymentStatus());
            orderService.updatePaymentStatus(paymentEvent.getOrderId(), paymentEvent.getPaymentStatus());
        } catch (Exception e) {
            log.error("Failed to update payment status for Order ID: {} from Kafka PaymentEvent",
                    paymentEvent.getOrderId(), e);
        }
    }
}