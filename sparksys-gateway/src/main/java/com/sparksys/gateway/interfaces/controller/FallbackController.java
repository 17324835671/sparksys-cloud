package com.sparksys.gateway.interfaces.controller;

import com.sparksys.commons.core.api.result.ApiResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * description: 响应超时熔断处理器
 *
 * @author zhouxinlei
 * @date 2020-05-24 12:17:49
 */
@RestController
public class FallbackController {

    @GetMapping("/fallback")
    public ApiResult fallback() {
        return ApiResult.timeOut();
    }
}
