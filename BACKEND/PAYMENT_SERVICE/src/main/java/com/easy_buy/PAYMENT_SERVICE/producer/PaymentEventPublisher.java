package com.easy_buy.PAYMENT_SERVICE.producer;

import com.easy_buy.COMMON_SERVICE.events.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String PAYMENT_TOPIC = "payment-topic";

    public void publishPaymentEvent(PaymentEvent paymentEvent) {
        try {
            log.info("Publishing PaymentEvent to Kafka: {}", paymentEvent);
            this.kafkaTemplate.send(PAYMENT_TOPIC, paymentEvent);
            log.info("PaymentEvent published: orderId={}, paymentStatus={}, paymentId={}",
                    paymentEvent.getOrderId(), paymentEvent.getPaymentStatus(), paymentEvent.getPaymentId());
        } catch (Exception e) {
            log.error("Error publishing PaymentEvent for orderId={}: {}",
                    paymentEvent.getOrderId(), e.getMessage());
        }
    }
}
