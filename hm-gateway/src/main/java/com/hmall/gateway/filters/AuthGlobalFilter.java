package com.hmall.gateway.filters;

import cn.hutool.core.collection.CollectionUtil;
import com.hmall.common.exception.UnauthorizedException;
import com.hmall.gateway.config.AuthProperties;
import com.hmall.gateway.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter,Ordered {
    private final AuthProperties authProperties;
    private final JwtTool jwtTool;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //首先获取请求头
        ServerHttpRequest request = exchange.getRequest();
        //判断是否需要拦截
        if (isExcludePath(request.getPath().toString())){
            return chain.filter(exchange);
        }
        //需要，获取token
        String token = null;
        List<String> headers = request.getHeaders().get("authorization");
        if (CollectionUtil.isNotEmpty(headers) && headers != null){
            token = headers.get(0);
        }
        //解析校验token
        Long userId;
        try {
            userId = jwtTool.parseToken(token);
        }catch (UnauthorizedException e){
            //拦截，设置状态码
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        //传递用户消息到请求头
        String userInfo = userId.toString();
        ServerWebExchange wse = exchange.mutate().request(builder -> builder.header("user-info", userInfo)).build();
        System.out.println(userId);
        //放行
        return chain.filter(wse);
    }
    //路径校验
    private boolean isExcludePath(String PathString) {
        List<String> excludePaths = authProperties.getExcludePaths();
        for (String excludePath : excludePaths) {
            if (antPathMatcher.match(excludePath, PathString)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
