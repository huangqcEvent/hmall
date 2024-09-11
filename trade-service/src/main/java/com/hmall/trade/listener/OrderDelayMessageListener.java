package com.hmall.trade.listener;

import com.hmall.api.client.PayClient;
import com.hmall.trade.constant.MQConstants;
import com.hmall.trade.domain.po.Order;
import com.hmall.trade.service.impl.OrderServiceImpl;
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
public class OrderDelayMessageListener {
    private final OrderServiceImpl orderService;
    private final PayClient payClient;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstants.DELAY_ORDER_QUEUE_NAME),
            exchange = @Exchange(name = MQConstants.DELAY_EXCHANGE_NAME, type = ExchangeTypes.DIRECT,delayed = "true"),
            key = MQConstants.DELAY_ORDER_KEY
    ))
    public void handleOrderDelayMessage(Long orderId) {
        log.info("收到延迟消息，订单id为{}", orderId);
        Order order = orderService.getById(orderId);
        //如果订单存在并且订单状态为未支付
        if (order != null && order.getStatus() == 1){
            //获取交易单支付状态
            int payOrderStatus = payClient.getPayOrderStatus(orderId);
            if(payOrderStatus == 3){
                // 4.1.已支付，标记订单状态为已支付
                orderService.markOrderPaySuccess(orderId);
            }else{
                // TODO 4.2.未支付，取消订单，回复库存
                orderService.cancelOrder(orderId);
            }
        }
    }
}
