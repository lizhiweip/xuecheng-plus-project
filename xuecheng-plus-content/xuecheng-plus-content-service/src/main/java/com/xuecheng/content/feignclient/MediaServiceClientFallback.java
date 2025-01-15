package com.xuecheng.content.feignclient;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @Author lzw
 * @Date 2025/1/13 15:49
 * @description
 */
public class MediaServiceClientFallback implements MediaServiceClient {
    @Override
    public String upload(MultipartFile filedata, String objectName) throws IOException {
        return "";
    }
}
