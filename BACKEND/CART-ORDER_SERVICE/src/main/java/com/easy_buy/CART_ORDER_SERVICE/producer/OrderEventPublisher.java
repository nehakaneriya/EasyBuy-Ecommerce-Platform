package com.easy_buy.CART_ORDER_SERVICE.producer;


import com.easy_buy.COMMON_SERVICE.events.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventPublisher {

    private final Logger logger = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final String ORDER_TOPIC = "order-topic";

    public OrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderEvent(OrderEvent orderEvent) {
        try {
            this.kafkaTemplate.send(ORDER_TOPIC, orderEvent);
            logger.info("Order event published Successfully : ", orderEvent);
        } catch (Exception e) {
            logger.error("Error while publishing order event: {}", e.getMessage());
        }
    }
}