package com.somc.cloud.gateway.filters;

import com.google.gson.Gson;
import com.smoc.cloud.common.gateway.request.RequestStardardHeaders;
import com.smoc.cloud.common.gateway.utils.ValidatorUtil;
import com.smoc.cloud.common.response.ResponseCode;
import com.smoc.cloud.common.response.ResponseData;
import com.smoc.cloud.common.response.ResponseDataUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;


/**
 * 自定义全局过滤器
 */
@Slf4j
@Component
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();

        /**
         *  拦截所有非POST请求
         */
        if(!request.getMethod().equals(HttpMethod.POST)){
            URI uri = request.getURI();
            log.warn("[非法请求][数据]URI:{}",uri.toString());

            //响应信息
            ServerHttpResponse response = exchange.getResponse();
            ResponseData responseData = ResponseDataUtil.buildError(ResponseCode.REQUEST_LEGAL_ERROR.getCode(), ResponseCode.REQUEST_LEGAL_ERROR.getMessage());
            byte[] bytes = new Gson().toJson(responseData).getBytes(StandardCharsets.UTF_16);
            DataBuffer bodyDataBuffer = response.bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Flux.just(bodyDataBuffer));
        }


        //HttpHeaders 自定义的headers 组织签名信息
        HttpHeaders headers = request.getHeaders();
        RequestStardardHeaders requestStardardHeaders = new RequestStardardHeaders();
        requestStardardHeaders.setSignatureNonce(headers.getFirst("signature-nonce"));
        requestStardardHeaders.setSignatureAccount(headers.getFirst("signature-account"));
        requestStardardHeaders.setSignature(headers.getFirst("signature"));
        //参数规则验证
        if (!ValidatorUtil.validate(requestStardardHeaders)) {

            URI uri = request.getURI();
            log.warn("[非法POST请求][数据]URI:{}",uri.toString());

            //响应信息
            ServerHttpResponse response = exchange.getResponse();
            ResponseData responseData = ResponseDataUtil.buildError(ResponseCode.REQUEST_LEGAL_ERROR.getCode(), ResponseCode.REQUEST_LEGAL_ERROR.getMessage());
            byte[] bytes = new Gson().toJson(responseData).getBytes(StandardCharsets.UTF_16);
            DataBuffer bodyDataBuffer = response.bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Flux.just(bodyDataBuffer));
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -200;
    }

}