package com.hmall.trade.listener;

import com.hmall.trade.domain.po.Order;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PayStatusListener {
    private final IOrderService orderService;
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(value = "pay.direct", type = ExchangeTypes.DIRECT),
            key = "pay.success",
            value = @Queue(value = "trade.pay.success.queue", durable = "true")
    ))
    // TODO 存在消息重复处理问题
    public void handlePaySuccess(Long payOrderId) {
        //查询订单
        //基于业务做判断
        //解决幂等操作
        Order order = orderService.getById(payOrderId);
        if (order == null || order.getStatus() != 1){
            //TODO 订单不存在，或者状态不是待支付，直接返回
            return;
        }
        //判断状态是否是否是已支付
        //标记订单状态为已支付
        orderService.markOrderPaySuccess(payOrderId);
        log.info("支付成功，订单id:{}", payOrderId);
    }

}
