package com.javaDJ.order_service.controller;

import com.javaDJ.order_service.dto.OrderResponseDTO;
import com.javaDJ.order_service.entity.Order;
import com.javaDJ.order_service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    //Validation
    //logging
    //Exception handling
    @PostMapping
    public String placeNewOrder(@RequestBody Order order) {
        return orderService.placeAnOrder(order);
    }

    @GetMapping("/{orderId}")
    public OrderResponseDTO getOrder(@PathVariable String orderId) {
        return orderService.getOrder(orderId);
    }
}