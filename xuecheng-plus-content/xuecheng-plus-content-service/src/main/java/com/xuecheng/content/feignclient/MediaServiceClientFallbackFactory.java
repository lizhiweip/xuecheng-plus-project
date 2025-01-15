package com.xuecheng.content.feignclient;

import feign.hystrix.FallbackFactory;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @Author lzw
 * @Date 2025/1/13 15:52
 * @description
 */
@Slf4j
@Component
public class MediaServiceClientFallbackFactory implements FallbackFactory<MediaServiceClient> {
    //拿到当时熔断的异常信息
    @Override
    public MediaServiceClient create(Throwable throwable) {
        return new MediaServiceClient() {
            //发生熔断上传服务调用此方法执行降级逻辑
            @Override
            public String upload(MultipartFile filedata, String objectName) throws IOException {
                log.debug("远程调用上传文件的接口发生熔断{}", throwable.toString(),throwable);

                return "";
            }
        };
    }
}
