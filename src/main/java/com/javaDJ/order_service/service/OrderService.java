package com.javaDJ.order_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaDJ.order_service.dto.OrderResponseDTO;
import com.javaDJ.order_service.dto.PaymentDTO;
import com.javaDJ.order_service.dto.UserDTO;
import com.javaDJ.order_service.entity.Order;
import com.javaDJ.order_service.repository.OrderRepository;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.UUID;
@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${order.producer.topic.name}")
    private String topicName;

    @Autowired
    @Lazy
    private RestTemplate restTemplate;

    public String placeAnOrder(Order order) {
        //save a copy in order-service DB

        order.setPurchaseDate(new Date());
        order.setOrderId(UUID.randomUUID().toString().split("-")[0]);
        orderRepository.save(order);
        //send it to payment service using kafka
        try {
            kafkaTemplate.send(topicName, new ObjectMapper().writeValueAsString(order));
        } catch (JsonProcessingException e) {
            e.printStackTrace();//log statement log.error
        }
        return "Your order with (" + order.getOrderId() + ") has been placed ! we will notify once it will confirm";
    }

    public OrderResponseDTO getOrder(String orderId){
        //own DB -> ORDER

        Order order = orderRepository.findByOrderId(orderId);
        //PAYMENT-> REST call payment-service
        PaymentDTO paymentDTO = restTemplate.getForObject("http://localhost:8082/payments/" + orderId, PaymentDTO.class);
        //user-info-> rest call user-service
        UserDTO userDTO = restTemplate.getForObject("http://localhost:8080/users/" + order.getUserId(), UserDTO.class);
        return OrderResponseDTO.builder()
                .order(order)
                .paymentResponse(paymentDTO)
                .userInfo(userDTO)
                .build();
    }
}
