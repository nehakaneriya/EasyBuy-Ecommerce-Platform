package com.easy_buy.CART_ORDER_SERVICE.consumer;

import com.easy_buy.CART_ORDER_SERVICE.entity.Order;
import com.easy_buy.CART_ORDER_SERVICE.entity.PaymentStatus;
import com.easy_buy.CART_ORDER_SERVICE.repository.OrderRepository;
import com.easy_buy.COMMON_SERVICE.events.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final OrderRepository orderRepository;

    @KafkaListener(topics = "payment-topic", groupId = "cart-order-service-group")
    @Transactional
    public void consumePaymentEvent(PaymentEvent event) {

        log.info("PaymentEvent received: orderId={}, paymentStatus={}, paymentId={}",
                event.getOrderId(), event.getPaymentStatus(), event.getPaymentId());

        // Find the order by orderId from payment event
        Optional<Order> optionalOrder = orderRepository.findById(event.getOrderId());

        if (optionalOrder.isEmpty()) {
            log.warn("Order not found for orderId={} — skipping payment status update", event.getOrderId());
            return;
        }

        Order order = optionalOrder.get();

        // Parse payment status from event
        PaymentStatus newPaymentStatus;
        try {
            newPaymentStatus = PaymentStatus.valueOf(event.getPaymentStatus());
        } catch (IllegalArgumentException e) {
            log.error("Unknown paymentStatus received: '{}' for orderId={} — skipping",
                    event.getPaymentStatus(), event.getOrderId());
            return;
        }

        // Update order's paymentStatus
        order.setPaymentStatus(newPaymentStatus);
        orderRepository.save(order);

        log.info("Order paymentStatus updated: orderId={}, newPaymentStatus={}",
                event.getOrderId(), newPaymentStatus);
    }
}
