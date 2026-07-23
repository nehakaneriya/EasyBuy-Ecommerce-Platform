package com.easy_buy.INVENTORY_SERVICE.consumer;

import com.easy_buy.COMMON_SERVICE.events.PaymentEvent;
import com.easy_buy.INVENTORY_SERVICE.client.OrderFeignClient;
import com.easy_buy.INVENTORY_SERVICE.dtos.request.ReleaseStockRequest;
import com.easy_buy.INVENTORY_SERVICE.dtos.response.OrderResponse;
import com.easy_buy.INVENTORY_SERVICE.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final OrderFeignClient orderClient;
    private final InventoryService inventoryService;

    @KafkaListener(topics = "payment-topic", groupId = "inventory-group")
    public void consumePaymentEvent(PaymentEvent paymentEvent) {
        log.info("Received PaymentEvent in inventory-service from Kafka: {}", paymentEvent);

        if (paymentEvent.getOrderId() == null) {
            log.error("Received PaymentEvent with null orderId in inventory-service");
            return;
        }

        if ("FAILED".equalsIgnoreCase(paymentEvent.getPaymentStatus())) {
            log.warn("Payment failed for Order ID: {}. Releasing inventory stock...", paymentEvent.getOrderId());
            try {
                // Fetch order details to know the items and quantities to release
                OrderResponse orderResponse = orderClient.getOrderById(paymentEvent.getOrderId());
                if (orderResponse == null || orderResponse.getOrderItems() == null) {
                    log.error("Failed to fetch order details or items for Order ID: {} during compensation", paymentEvent.getOrderId());
                    return;
                }

                // Release stock for each item in the order
                orderResponse.getOrderItems().forEach(item -> {
                    try {
                        log.info("Releasing stock for Product ID: {}, Quantity: {}", item.getProductId(), item.getQuantity());
                        inventoryService.releaseStockByProductId(item.getProductId(), new ReleaseStockRequest(item.getQuantity()));
                    } catch (Exception ex) {
                        log.error("Failed to release stock for Product ID: {} during payment failed compensation", item.getProductId(), ex);
                    }
                });
                log.info("Inventory successfully released for Order ID: {} after payment failure", paymentEvent.getOrderId());
            } catch (Exception e) {
                log.error("Error retrieving order details or releasing inventory for Order ID: {}", paymentEvent.getOrderId(), e);
            }
        }
    }
}
