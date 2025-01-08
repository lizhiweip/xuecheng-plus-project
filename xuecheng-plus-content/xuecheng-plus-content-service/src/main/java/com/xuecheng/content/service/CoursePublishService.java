package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import org.springframework.stereotype.Service;

/**
 * @Author lzw
 * @Date 2025/1/8 18:53
 * @description  课程发布相关接口
 */

public interface CoursePublishService {

    /**
     * @description 获取课程预览信息
     * @param courseId 课程id
     * @return com.xuecheng.content.model.dto.CoursePreviewDto
     * @author Mr.M
     * @date 2022/9/16 15:36
     */
    public CoursePreviewDto getCoursePreviewInfo(Long courseId);

}
