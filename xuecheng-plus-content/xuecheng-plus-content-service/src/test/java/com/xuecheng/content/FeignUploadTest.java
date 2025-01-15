package com.xuecheng.content;

import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @Author lzw
 * @Date 2025/1/13 14:43
 * @description 远程调用媒资服务
 */
@SpringBootTest
public class FeignUploadTest {

    @Autowired
    MediaServiceClient mediaServiceClient;

    @Test
    public void test() throws IOException {

        //将file转成MultipartFile
        File file = new File("E:\\springboot\\xc-ui-pc-static-portal\\index.html");
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        //远程调用
        String upload = mediaServiceClient.upload(multipartFile, "course/index.html");
        if (upload == null) {
            System.out.println("走了降级逻辑");
        }
    }
}
