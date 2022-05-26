package com.smoc.cloud.filters.routes;

import com.smoc.cloud.filters.filter.EndGatewayFilter;
import com.smoc.cloud.filters.filter.full_filter.CommonExtendFilterParamsGatewayFilter;
import com.smoc.cloud.filters.filter.full_filter.MessageExtendFilterParamsGatewayFilter;
import com.smoc.cloud.filters.filter.full_filter.NumberExtendFilterParamsGatewayFilter;
import com.smoc.cloud.filters.filter.full_filter.StartFullFilterGatewayFilter;
import com.smoc.cloud.filters.filter.message_filter.MessageGatewayFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

/**
 * 路由
 */
@Slf4j
@Component
public class Routes {

    @Autowired
    private CommonExtendFilterParamsGatewayFilter commonExtendFilterParamsGatewayFilter;

    @Autowired
    private NumberExtendFilterParamsGatewayFilter numberExtendFilterParamsGatewayFilter;

    @Autowired
    private MessageExtendFilterParamsGatewayFilter messageExtendFilterParamsGatewayFilter;

    //只过滤短信内容
    @Autowired
    private MessageGatewayFilter messageGatewayFilter;

    @Autowired
    private StartFullFilterGatewayFilter startFullFilterGatewayFilter;

    @Autowired
    private EndGatewayFilter endGatewayFilter;


    @Bean
    public RouteLocator identityRouteLocator(RouteLocatorBuilder builder) {

        return builder.routes()
                //拦截所有GET请求
                .route(r -> r.method(HttpMethod.GET).filters(f -> f.stripPrefix(1)).uri("lb://smoc"))

                /**
                 * 全量过滤
                 */
                .route(r -> r.method(HttpMethod.POST).and().path("/smoc-filters/full-filter/**").and().readBody(String.class, requestBody -> {
                            return true;
                        }).filters(f -> f.stripPrefix(1).filter(startFullFilterGatewayFilter).filter(commonExtendFilterParamsGatewayFilter).filter(numberExtendFilterParamsGatewayFilter).filter(messageExtendFilterParamsGatewayFilter).filter(endGatewayFilter)).uri("lb://smoc")

                )
                /**
                 * 单独内容过滤
                 */
                .route(r -> r.method(HttpMethod.POST).and().path("/smoc-filters/message-filter/**").and().readBody(String.class, requestBody -> {
                            return true;
                        }).filters(f -> f.stripPrefix(1).filter(messageGatewayFilter).filter(endGatewayFilter)).uri("lb://smoc")

                )
                /**
                 * 单独手机号码过滤
                 */
                .route(r -> r.method(HttpMethod.POST).and().path("/smoc-filters/number-filter/**").and().readBody(String.class, requestBody -> {
                            return true;
                        }).filters(f -> f.stripPrefix(1).filter(endGatewayFilter)).uri("lb://smoc")

                ).build();
    }

}
