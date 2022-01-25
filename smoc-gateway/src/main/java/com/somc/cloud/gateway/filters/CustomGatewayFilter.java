package com.somc.cloud.gateway.filters;

import com.google.gson.Gson;
import com.smoc.cloud.common.gateway.request.RequestSignModel;
import com.smoc.cloud.common.gateway.request.RequestStardardHeaders;
import com.smoc.cloud.common.gateway.utils.ValidatorUtil;
import com.somc.cloud.gateway.configuration.GatewayConfigurationProperties;
import com.smoc.cloud.common.gateway.utils.HMACUtil;
import com.smoc.cloud.common.response.ResponseCode;
import com.smoc.cloud.common.response.ResponseData;
import com.smoc.cloud.common.response.ResponseDataUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 自定义 GatewayFilter
 */
@Slf4j
@Component
public class CustomGatewayFilter {

    @Autowired
    private GatewayConfigurationProperties gatewayConfigurationProperties;

    /**
     * 验签过滤器
     *
     * @return
     */
    @Bean
    public GatewayFilter verifySignatureGatewayFilter() {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

                //获取body内容
                String requestBody = "";
                if (HttpMethod.POST.equals(exchange.getRequest().getMethod())) {
                    requestBody = exchange.getAttribute("cachedRequestBodyObject");
                }

                //HttpHeaders 自定义的headers 组织签名信息
                HttpHeaders headers = exchange.getRequest().getHeaders();

                String signatureNonce = headers.getFirst("signature-nonce");
                String signatureAccount = headers.getFirst("signature-account");
                String signature = headers.getFirst("signature");

                RequestStardardHeaders requestHeaderData = new RequestStardardHeaders();
                requestHeaderData.setSignatureNonce(signatureNonce);
                requestHeaderData.setSignatureAccount(signatureAccount);
                requestHeaderData.setSignature(signature);
                log.info("[接口请求][账户:{}]header数据:{}", signatureAccount, new Gson().toJson(requestHeaderData));

                //处理signatureNonce 重防攻击

                //判断 signatureAccount 是否存在，如果存在，则取用户的md5_HMAC,密钥
                String md5HmacKey = "";

                //requestBody 为空
                if (StringUtils.isEmpty(requestBody)) {
                    return errorHandle(exchange, ResponseCode.PARAM_ERROR.getCode(), ResponseCode.PARAM_ERROR.getMessage());
                }

                //校验数据请求的数据结构
                RequestSignModel model;
                try {
                    model = new Gson().fromJson(requestBody, RequestSignModel.class);
                } catch (Exception e) {
                    return errorHandle(exchange, ResponseCode.PARAM_FORMAT_ERROR.getCode(), ResponseCode.PARAM_FORMAT_ERROR.getMessage());
                }

                log.info("[接口请求][账户:{}]body数据:{}", signatureAccount, new Gson().toJson(model));
                //身份证规则验证  验证身证号 及姓名
                if(!ValidatorUtil.validate(model)){
                    String errorMessage = ValidatorUtil.validateMessage(model);
                    return errorHandle(exchange, ResponseCode.PARAM_FORMAT_ERROR.getCode(), errorMessage);
                }

                //签名数据
                StringBuffer signData = new StringBuffer();
                signData.append(signatureNonce.trim());
                signData.append(signatureAccount.trim());
                signData.append(model.getOrderNo().trim());
                signData.append(model.getName().trim());
                signData.append(model.getCardNo().trim());

                //校验签名数据
                boolean verifySign = HMACUtil.verifySign(signData.toString(), requestHeaderData.getSignature(), gatewayConfigurationProperties.getMd5HmacKey(), gatewayConfigurationProperties.getSignStyle());
                //签名正确
                if (verifySign) {
                    return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                        //被执行后调用 post
                    }));
                }
                //响应信息
                return errorHandle(exchange, ResponseCode.SIGN_ERROR.getCode(), ResponseCode.SIGN_ERROR.getMessage());
            }
        };
    }

    public Mono<Void> errorHandle(ServerWebExchange exchange, String errorCode, String errorMessage) {
        //响应信息
        ServerHttpResponse response = exchange.getResponse();
        ResponseData responseData = ResponseDataUtil.buildError(errorCode, errorMessage);
        log.error("[响应数据]数据:{}", new Gson().toJson(responseData));
        byte[] bytes = new Gson().toJson(responseData).getBytes(StandardCharsets.UTF_16);
        DataBuffer bodyDataBuffer = response.bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Flux.just(bodyDataBuffer));
    }


}
