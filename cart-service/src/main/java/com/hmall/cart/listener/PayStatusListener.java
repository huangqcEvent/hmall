package com.hmall.cart.listener;

import com.hmall.cart.service.ICartService;
import com.hmall.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class PayStatusListener {
    private final ICartService cartService;
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(value = "trade.topic", type = ExchangeTypes.DIRECT),
            key = "order.create",
            value = @Queue(value = "cart.clear.queue", durable = "true")
    ))
    public void deleteCartItemByIds(Set<Long> itemIds) {
        cartService.removeByItemIds(itemIds);
    }
}
