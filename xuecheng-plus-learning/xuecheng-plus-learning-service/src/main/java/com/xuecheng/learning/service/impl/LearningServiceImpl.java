package com.xuecheng.learning.service.impl;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.feignclient.MediaServiceClient;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.service.LearningService;
import com.xuecheng.learning.service.MyCourseTablesService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.ws.Action;

/**
 * @Author lzw
 * @Date 2025/1/31 14:54
 * @description 在线学习接口
 */
@Slf4j
@Service
public class LearningServiceImpl implements LearningService {

    @Autowired
    MyCourseTablesService myCourseTablesService;
    @Autowired
    ContentServiceClient contentServiceClient;
    @Autowired
    MediaServiceClient mediaServiceClient;


    @Override
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId) {
        //查询课程信息
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if(coursepublish == null){
            return RestResponse.validfail("课程不存在");
        }


        //用户已登录
        if(StringUtils.isNotEmpty(userId)){
            //获取学习资格
            XcCourseTablesDto learningStatus = myCourseTablesService.getLearningStatus(userId, courseId);
            String learnStatus = learningStatus.getLearnStatus();
            if("702002".equals(learnStatus)){
                return RestResponse.validfail("无法学习，因为没有选课或选课后没有支付");
            }else if("702003".equals(learnStatus)){
                return RestResponse.validfail("过期需要申请续费或重新支付");
            }else{
                //有资格学习，要返回视频的播放地址
                //远程调用媒资获取视频播放地址
                RestResponse<String> playUrlByMediaId = mediaServiceClient.getPlayUrlByMediaId(mediaId);
                return playUrlByMediaId;
            }

        }
        //远程调用内容管理服务根据课程计划的id去查询课程计划信息，如果is_preview的值为1表示支持试学

        //如果用户没有登录
        //取出课程的收费规则
        String charge = coursepublish.getCharge();
        if("201000".equals(charge)){
            //有资格学习，要返回视频的播放地址
            //远程调用媒资获取视频播放地址
            RestResponse<String> playUrlByMediaId = mediaServiceClient.getPlayUrlByMediaId(mediaId);
            return playUrlByMediaId;
        }

        return RestResponse.validfail("该课程没有选课");
    }
}
