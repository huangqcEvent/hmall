package com.hmall.pay.listener;

import com.hmall.pay.domain.po.PayOrder;
import com.hmall.pay.service.IPayOrderService;
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
public class orderStatusListener {
    private final IPayOrderService payOrderService;
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(value = "trade.direct", type = ExchangeTypes.DIRECT),
            key = "pay.status",
            value = @Queue(value = "update.status", durable = "true")
    ))
    public void updateOrderStatus(Long orderId) {
        // TODO 更新订单状态
        PayOrder payOrder = payOrderService.get(orderId);
        log.error("走到这里了");
        if (payOrder.getStatus() != 2) {
            PayOrder payOrder1 = new PayOrder();
            payOrder1.setId(payOrder.getId());
            payOrder1.setStatus(2);
            payOrderService.updateById(payOrder1);
        }
    }
}
