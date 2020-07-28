package com.sparksys.gateway.infrastructure.filter;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.sparksys.core.support.ResponseResultStatus;
import com.sparksys.core.base.api.result.ApiResult;
import com.sparksys.core.constant.CoreConstant;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.sparksys.gateway.infrastructure.properties.IgnoreUrlsProperties;
import com.sparksys.gateway.infrastructure.resource.StaticResource;
import com.sparksys.gateway.infrastructure.utils.IgnoreUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * description: 权限过滤器
 *
 * @author zhouxinlei
 * @date 2020-05-24 12:16:11
 */
@Component
@Slf4j
@RefreshScope
public class TokenFilter implements GlobalFilter, Ordered {

    @Autowired
    private IgnoreUrlsProperties ignoreUrlsProperties;

    @Override
    public int getOrder() {
        return -1000;
    }

    protected String getHeader(String headerName, ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        String token = StrUtil.EMPTY;
        if (headers.isEmpty()) {
            return token;
        }

        List<String> headerList = headers.get(headerName);
        if (headerList == null || headerList.isEmpty()) {
            return token;
        }
        token = headerList.get(0);

        if (StringUtils.isNotBlank(token)) {
            return token;
        }
        MultiValueMap<String, String> queryParams = request.getQueryParams();
        if (queryParams.isEmpty()) {
            return token;
        }
        return queryParams.getFirst(headerName);
    }

    @SneakyThrows
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 校验是否需要拦截地址
        if (IgnoreUtils.isIgnore(StaticResource.EXCLUDE_PATTERNS, request.getPath().toString())
                || IgnoreUtils.isIgnore(ignoreUrlsProperties.getUrls(), request.getPath().toString())) {
            log.debug("access filter not execute");
            return chain.filter(exchange);
        }
        String token = getHeader(CoreConstant.JwtTokenConstant.JWT_TOKEN_HEADER, request);
        if (StringUtils.isBlank(token)) {
            return errorResponse(response);
        }
        return chain.filter(exchange.mutate().request(request.mutate().build()).build());
    }

    protected Mono<Void> errorResponse(ServerHttpResponse response) {
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        //指定编码，否则在浏览器中会中文乱码
        response.getHeaders().add("Content-Type", "text/plain;charset=UTF-8");
        response.setStatusCode(HttpStatus.OK);
        byte[] bytes = JSON.toJSONString(ApiResult.apiResult(ResponseResultStatus.UN_AUTHORIZED)).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Flux.just(buffer));
    }
}
