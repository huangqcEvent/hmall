package com.hmall.api.client.fallback;

import com.hmall.api.client.PayClient;
import org.springframework.cloud.openfeign.FallbackFactory;

public class PayClientFallback implements FallbackFactory<PayClient> {
    @Override
    public PayClient create(Throwable cause) {
        return new PayClient(){
            @Override
            public int getPayOrderStatus(Long orderId) {
                return 99;
            }
        };
    }
}
