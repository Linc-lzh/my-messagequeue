package com.message.controller;

import com.message.mapper.OrderMapper;
import com.message.model.Order;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/v1")
public class TestController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private OrderMapper orderMapper;

    @RequestMapping("/selectAllOrder")
    @ResponseBody
    public String selectAllOrder(){
        List<Order> orders = orderMapper.selectAll();
        System.out.println(orders);
        return "hello";
    }
}
