package com.hmall.api.client.fallback;

import com.hmall.api.client.TradeClient;
import org.springframework.cloud.openfeign.FallbackFactory;

public class TradeClientFallback implements FallbackFactory<TradeClient> {
    @Override
    public TradeClient create(Throwable cause) {
        return new TradeClient() {
            @Override
            public void markOrderPaySuccess(Long orderId) {
                throw new RuntimeException(cause);
            }
        };
    }
}
