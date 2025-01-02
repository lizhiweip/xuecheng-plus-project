package com.xuecheng.media;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;


import java.io.*;
import java.util.*;

/**
 * @Author lzw
 * @Date 2024/12/31 15:28
 * @description 测试大文件上传
 */
public class BigFileTest {

    //分块测试
    @Test
    public void testChunk() throws IOException {
       //源文件
        File sourceFile = new File("C:\\Users\\20900\\Videos\\k.mp4");
        //分块文件存储路径
        String chunkFilePath = "E:\\springboot\\学成在线\\day04 项目实战\\资料\\upload\\chunk\\";
        //分块文件大小
        int chunkSize = 1024 * 1024 * 5;
        //分块文件的个数
        int chunkNum = (int) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        //使用流从源文件读数据，向分块文件写数据
        RandomAccessFile raf_r = new RandomAccessFile(sourceFile, "r");
        //缓冲区
        byte[] bytes = new byte[1024];
        for (int i = 0; i < chunkNum; i++) {
            File chunkFile = new File(chunkFilePath + i);
            //分块文件写入流
            RandomAccessFile raf_rw = new RandomAccessFile(chunkFile, "rw");
            int len = -1;

            while((len = raf_r.read(bytes)) != -1){
                raf_rw.write(bytes, 0, len);
                if(chunkFile.length() >= chunkSize){
                    break;
                }
            }
         raf_rw.close();

        }
        raf_r.close();


    }


    //合并
    @Test
    public void testMerge() throws IOException {
        //块文件目录
        File chunkFilePath = new File("E:\\springboot\\学成在线\\day04 项目实战\\资料\\upload\\chunk/");
        //源文件
        File sourceFile = new File("C:\\Users\\20900\\Videos\\k.mp4");
        //合并后的文件
        File mergeFile = new File("E:\\springboot\\学成在线\\day04 项目实战\\资料\\upload\\merge\\k2.mp4");
        //取出所有的分块文件
        File [] files= chunkFilePath.listFiles();
        //把数组转成list
        List<File> fileList = Arrays.asList(files);
        //对分开文件排序
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName());
            }
        });
        //向合并文件写的流
        RandomAccessFile raf_rw = new RandomAccessFile(mergeFile, "rw");
        //缓冲区
        byte[] bytes = new byte[1024];


        //遍历分块文件，向右合并的文件写
        for (File file : fileList) {
            RandomAccessFile raf_r = new RandomAccessFile(file, "r");
            int len = -1;
            while((len = raf_r.read(bytes)) != -1){
                 raf_rw.write(bytes, 0, len);
            }
            raf_r.close();
        }
        raf_rw.close();
        //对合并的文件进行校验
        FileInputStream fileInputStream = new FileInputStream(mergeFile);
        FileInputStream fileOutputStream = new FileInputStream(sourceFile);
        String i = DigestUtils.md5Hex(fileInputStream);
        String o = DigestUtils.md5Hex(fileOutputStream);
        if(!i.equals(o)){
            System.out.println("文件合并成功");
        }

    }


}
