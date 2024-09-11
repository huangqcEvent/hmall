package com.hmall.common.config;

import lombok.AllArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AllArgsConstructor
@Configuration
public class ErrorMessageConfig {
    private final RabbitTemplate rabbitTemplate;


    private final String queueName="fum";
    @Bean
    public DirectExchange errorMessageExchange(){
        return new DirectExchange("error.exchange");
    }

    @Bean
    public Queue errorMessageQueue(){
        return new Queue(queueName+".error.queue");
    }

    @Bean
    public Binding bindingErrorMessageQueue(Queue errorMessageQueue, DirectExchange errorMessageExchange){
        return BindingBuilder.bind(errorMessageQueue).to(errorMessageExchange).with(queueName);
    }

    //失败处理策略
    @Bean
    public MessageRecoverer messageRecoverer(){
        return new RepublishMessageRecoverer(rabbitTemplate, "error.exchange", queueName);
    }
}

