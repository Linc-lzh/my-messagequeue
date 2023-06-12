package com.message.config;

import com.message.model.MyConstants;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Bean
    public Queue directQueue() {
        return new Queue(MyConstants.QUEUE_TX_MSG);
    }
}
