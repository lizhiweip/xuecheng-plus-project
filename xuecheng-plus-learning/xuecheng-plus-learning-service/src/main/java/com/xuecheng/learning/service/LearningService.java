package com.xuecheng.learning.service;

import com.xuecheng.base.model.RestResponse;

/**
 * @Author lzw
 * @Date 2025/1/31 14:48
 * @description 在线学习相关接口
 */
public interface LearningService {
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId);
}
