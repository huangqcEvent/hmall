package com.hmall.api.client;

import com.hmall.api.client.fallback.PayClientFallback;
import com.hmall.api.config.DefaultFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "pay-service",configuration = DefaultFeignConfig.class,fallbackFactory = PayClientFallback.class)
public interface PayClient {
    //获取交易单支付状态
    @GetMapping("/pay-orders/{orderId}")
    int getPayOrderStatus(Long orderId);
}
