package com.message.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/v1")
public class TestController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RequestMapping("/test")
    @ResponseBody
    public String hello(){
        System.out.println("======hello world=======");
        return "hello";
    }
}
