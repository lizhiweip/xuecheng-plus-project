package com.xuecheng.content.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Author lzw
 * @Date 2025/1/14 9:17
 * @description
 */

@FeignClient(value = "search", fallbackFactory = SearchServiceClientFallbackFactory.class )
public interface SearchServiceClient {
    @PostMapping("/search/index/course")
    public Boolean add(@RequestBody CourseIndex courseIndex);
}
