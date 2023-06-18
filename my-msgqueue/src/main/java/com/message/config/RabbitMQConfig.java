package com.message.config;

import com.message.model.MyConstants;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Bean(MyConstants.QUEUE_TX_MSG)
    public Queue txMsgQueue() {
        return QueueBuilder.durable(MyConstants.QUEUE_TX_MSG).build();
    }

    @Bean(MyConstants.EXCHANGE_NAME)
    public Exchange txExchange(){
        return ExchangeBuilder.topicExchange(MyConstants.EXCHANGE_NAME).durable(true).build();
    }

    @Bean
    public Binding confirmExchange(@Qualifier(MyConstants.EXCHANGE_NAME) Exchange exchange,
                                   @Qualifier(MyConstants.QUEUE_TX_MSG) Queue queue){
        return BindingBuilder.bind(queue).to(exchange).with("topic").noargs();
    }
}
