package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaFileProcessService;
import groovy.util.logging.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author lzw
 * @Date 2025/1/5 16:33
 * @description  MediaFileProcess接口实现
 */

@Slf4j
@Service
public class MediaFileProcessServiceImpl implements MediaFileProcessService {
   @Autowired
   private MediaProcessMapper mediaProcessMapper;

   @Autowired
   private MediaFilesMapper mediaFilesMapper;

   @Autowired
   private MediaProcessHistoryMapper mediaProcessHistoryMapper;

    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        List<MediaProcess> mediaProcesses = mediaProcessMapper.selectListByShardIndex(shardTotal, shardIndex, count);
        return mediaProcesses;
    }

    //实现如下
    public boolean startTask(long id) {
        int result = mediaProcessMapper.startTask(id);
        return result<=0?false:true;
    }

    @Transactional
    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if(mediaProcess == null){
           return;
        }
        //如果任务执行失败
         if(status.equals("3")){
             mediaProcess.setStatus("3");
             mediaProcess.setFailCount(mediaProcess.getFailCount()+1);
             mediaProcess.setErrormsg(errorMsg);
             mediaProcessMapper.updateById(mediaProcess);
             return;

         }
        //如果任务执行成功
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);


        //更新mediaFile表中的url
         mediaFiles.setUrl(url);
         mediaFilesMapper.updateById(mediaFiles);
        //更新MediaProcess表的状态
        mediaProcess.setStatus("2");
        mediaProcess.setCreateDate(LocalDateTime.now());
        mediaProcess.setUrl(url);
        mediaProcessMapper.updateById(mediaProcess);
        //将mediaProcess表插入到mediaProcessHistory中
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
         mediaProcessHistoryMapper.insert(mediaProcessHistory);
        //从MediaProcess删除当前任务
         mediaProcessMapper.deleteById(taskId);


    }

}
