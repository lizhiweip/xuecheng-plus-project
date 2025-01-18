package com.xuecheng.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.mapper.XcChooseCourseMapper;
import com.xuecheng.learning.mapper.XcCourseTablesMapper;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.MyCourseTablesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author lzw
 * @Date 2025/1/17 15:40
 * @description 选课相关接口实现
 */
@Slf4j
@Service
public class MyCourseTablesServiceImpl implements MyCourseTablesService {

    @Autowired
    XcChooseCourseMapper chooseCourseMapper;

    @Autowired
    XcCourseTablesMapper xcCourseTablesMapper;

    @Autowired
    ContentServiceClient contentServiceClient;

    @Transactional
    @Override
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId) {

        //远程调用内容管理查询课程的收费规则
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if(coursepublish == null){
            XueChengPlusException.cast("课程不存在");
        }
        //收费规则
        String charge = coursepublish.getCharge();
        XcChooseCourse chooseCourse = null;
        if("201000".equals(charge)){//免费课程
            //如果课程免费，会向选课记录表，
            chooseCourse = addFreeCoruse(userId, coursepublish);
            //我的课程表写数据
            XcCourseTables xcCourseTables = addCourseTabls(chooseCourse);

        }else{//如果收费课程，会向选课记录表写数据
            XcChooseCourse xcChooseCourse = addChargeCoruse(userId, coursepublish);
        }
        //判断学生的学习资格
        XcCourseTablesDto xcCourseTablesDto = getLearningStatus(userId, courseId);

       //构造返回值
        XcChooseCourseDto xcChooseCourseDto = new XcChooseCourseDto();
        BeanUtils.copyProperties(chooseCourse, xcChooseCourseDto);
        xcChooseCourseDto.setLearnStatus(xcCourseTablesDto.getLearnStatus());

        return xcChooseCourseDto;
    }

    @Override
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId) {
        XcCourseTablesDto courseTablesDto = new XcCourseTablesDto();
        //查询我的课程表，如果查不到说明没有选课
        XcCourseTables xcCourseTables = getXcCourseTables(userId, courseId);
        if(xcCourseTables == null){
            courseTablesDto.setLearnStatus("702002");
            return courseTablesDto;
        }
        //如果查到了，判断是否过期，如果过期不能继续学习，没有
        boolean before = xcCourseTables.getValidtimeEnd().isBefore(LocalDateTime.now());
        if(before){
            BeanUtils.copyProperties(xcCourseTables, courseTablesDto);
            courseTablesDto.setLearnStatus("702003");
            return courseTablesDto;
        }else{
            BeanUtils.copyProperties(xcCourseTables, courseTablesDto);
            courseTablesDto.setLearnStatus("702001");
            return courseTablesDto;
        }






    }

    //添加免费课程,免费课程加入选课记录表、我的课程表
    public XcChooseCourse addFreeCoruse(String userId, CoursePublish coursepublish) {
        Long courseId = coursepublish.getId();
        //判断，如果存在免费的选课记录且选课状态为成功，直接返回了
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<XcChooseCourse>()
                .eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, courseId)
                .eq(XcChooseCourse::getOrderType, "700001")//免费课程
                .eq(XcChooseCourse::getStatus, "701001");//选课成功

        List<XcChooseCourse> xcChooseCourses = chooseCourseMapper.selectList(queryWrapper);
        if(xcChooseCourses.size() > 0){
            return xcChooseCourses.get(0);
        }
        //向选课记录表写数据
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(courseId);
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setOrderType("700001");//免费课程
        xcChooseCourse.setCoursePrice(coursepublish.getPrice());
        xcChooseCourse.setValidDays(365);
        xcChooseCourse.setStatus("701001");//选课成功
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());//有效期开始时间
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusHours(365));
        int insert = chooseCourseMapper.insert(xcChooseCourse);
        if(insert <= 0){
            XueChengPlusException.cast("添加选课记录失败");
        }
        return xcChooseCourse;
    }


    //添加到我的课程表
    public XcCourseTables addCourseTabls(XcChooseCourse xcChooseCourse){
        //选课成功才能向我的课程表添加
        String status = xcChooseCourse.getStatus();
        if(!"701001".equals(status)){
            XueChengPlusException.cast("选课没有成功无法添加到课程表");
        }
        XcCourseTables xcCourseTables = getXcCourseTables(xcChooseCourse.getUserId(), xcChooseCourse.getCourseId());
        if(xcCourseTables != null){
            return xcCourseTables;
        }
        xcCourseTables = new XcCourseTables();
        BeanUtils.copyProperties(xcChooseCourse, xcCourseTables);
        xcCourseTables.setChooseCourseId(xcChooseCourse.getCourseId());//记录选课表的主键
        xcCourseTables.setCourseType(xcChooseCourse.getOrderType());


        xcCourseTables.setUpdateDate(LocalDateTime.now());
        int insert = xcCourseTablesMapper.insert(xcCourseTables);
        if(insert <= 0){
            XueChengPlusException.cast("添加我的课程表失败");
        }
        return xcCourseTables;
    }


    //添加收费课程
    public XcChooseCourse addChargeCoruse(String userId,CoursePublish coursepublish){

        Long courseId = coursepublish.getId();
        //判断，如果存在收费的选课记录且选课状态为待支付，直接返回了
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<XcChooseCourse>()
                .eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, courseId)
                .eq(XcChooseCourse::getOrderType, "700002")//收费课程
                .eq(XcChooseCourse::getStatus, "701002");//待支付

        List<XcChooseCourse> xcChooseCourses = chooseCourseMapper.selectList(queryWrapper);
        if(xcChooseCourses.size() > 0){
            return xcChooseCourses.get(0);
        }
        //向选课记录表写数据
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(courseId);
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setOrderType("700002");//收费课程
        xcChooseCourse.setCoursePrice(coursepublish.getPrice());
        xcChooseCourse.setValidDays(365);
        xcChooseCourse.setStatus("701002");//待支付
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());//有效期开始时间
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusHours(365));
        int insert = chooseCourseMapper.insert(xcChooseCourse);
        if(insert <= 0){
            XueChengPlusException.cast("添加选课记录失败");
        }
        return xcChooseCourse;
    }
    public XcCourseTables getXcCourseTables(String userId,Long courseId){
        XcCourseTables xcCourseTables = xcCourseTablesMapper.selectOne(new LambdaQueryWrapper<XcCourseTables>().eq(XcCourseTables::getUserId, userId).eq(XcCourseTables::getCourseId, courseId));
        return xcCourseTables;

    }

}
