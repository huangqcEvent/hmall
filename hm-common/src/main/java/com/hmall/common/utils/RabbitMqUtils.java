package com.hmall.common.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class RabbitMqUtils {
     private final RabbitTemplate rabbitTemplate;
     public void sendMessage(String exchange, String routingKey, Object message) {
         rabbitTemplate.convertAndSend(exchange, routingKey, message);
     }
     public void sendDelayMessage(String exchange, String routingKey, Object message, int delayTime){
         rabbitTemplate.convertAndSend(exchange,routingKey,message,message1 -> {
             message1.getMessageProperties().setDelay(delayTime);
             return message1;
         });
     }
     public void sendHeaderMessage(String exchange, String routingKey, Object message, String headerKey, Long headerValue) {
         rabbitTemplate.convertAndSend(exchange, routingKey, message, message1 -> {
             message1.getMessageProperties().setHeader(headerKey, headerValue);
             return message1;
         });
     }
     //生产者确认机制
     public void sendMessageWithConfirm(String exchange, String routingKey, Object message) {
         CorrelationData correlationData = new CorrelationData();
         correlationData.getFuture().addCallback(new ListenableFutureCallback<CorrelationData.Confirm>() {
             @Override
             public void onFailure(Throwable ex) {
                 log.error("消息发送失败:{}", ex.getMessage());
             }

             @Override
             public void onSuccess(CorrelationData.Confirm result) {
                 if (result.isAck()) {
                     log.info("消息发送成功");
                 } else {
                     log.error("消息发送失败:{}", result.getReason());
                 }

             }
         });
         rabbitTemplate.convertAndSend(exchange, routingKey, message);
     }
}
