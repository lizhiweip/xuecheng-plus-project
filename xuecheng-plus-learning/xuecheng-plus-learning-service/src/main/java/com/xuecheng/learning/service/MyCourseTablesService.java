package com.xuecheng.learning.service;

import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;

/**
 * @Author lzw
 * @Date 2025/1/17 15:38
 * @description选课相关接口
 */
public interface MyCourseTablesService {

    public XcChooseCourseDto addChooseCourse(String userId, Long courseId);
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId);


}
