package com.message.controller;

import com.message.mapper.MessageInfoMapper;
import com.message.mapper.OrderMapper;
import com.message.model.MessageInfo;
import com.message.model.Order;
import com.message.service.OrderService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Controller
@RequestMapping("/v1")
public class TestController {

    @Autowired
    private OrderService orderService;



    @RequestMapping("/insertOrder")
    @ResponseBody
    public String insertOrder(@RequestParam(value="content") String content) throws Exception {
        orderService.insertOrder(content);
        return "Done";
    }
}
