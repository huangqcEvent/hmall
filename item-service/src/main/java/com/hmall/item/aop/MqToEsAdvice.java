package com.hmall.item.aop;

import com.hmall.item.domain.dto.ItemDoc;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Aspect
@Component
public class MqToEsAdvice {
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Pointcut("execution(* com.hmall.item.controller.ItemController.saveItem(..))")
    public void ESAdd(){}
    @Pointcut("execution(* com.hmall.item.controller.ItemController.updateItem(..))")
    public void ESUpdate(){}
    @Pointcut("execution(* com.hmall.item.controller.ItemController.deleteItemById(..))")
    public void ESDelete(){}

    @Around("ESAdd()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Object proceed = pjp.proceed();
        Object[] args = pjp.getArgs();
        ItemDoc itemDoc = (ItemDoc) args[0];
        //发送异步请求新增商品
        sendMessage(itemDoc,"add");
        return proceed;
    }
    @Around("ESUpdate()")
    public Object around1(ProceedingJoinPoint pjp) throws Throwable {
        Object proceed = pjp.proceed();
        Object[] args = pjp.getArgs();
        ItemDoc itemDoc = (ItemDoc) args[0];
        //发送异步请求新增商品
        sendMessage(itemDoc,"update");
        return proceed;
    }
    @Around("ESDelete()")
    public Object around2(ProceedingJoinPoint pjp) throws Throwable {
        Object proceed = pjp.proceed();
        Object[] args = pjp.getArgs();
        Long id = (Long) args[0];
        ItemDoc itemDoc = new ItemDoc();
        itemDoc.setId(String.valueOf(id));
        //发送异步请求删除
        sendMessage(itemDoc,"delete");
        return proceed;
    }

    private void sendMessage(ItemDoc itemDoc,String method){
         rabbitTemplate.convertAndSend("es.item.direct","item.change",itemDoc, message->{
             message.getMessageProperties().setHeader("method",method);
             return message;
         });
    }
}
