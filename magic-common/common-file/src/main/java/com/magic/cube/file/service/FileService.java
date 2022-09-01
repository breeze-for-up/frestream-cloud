package com.magic.cube.file.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @description: 文件服务
 * 
 * @author: TJ
 * @date:  2022-04-28
 **/
public interface FileService {

    /**
     * 上传文件
     * @param multipartFile 文件对象
     * @return 文件访问路径
     */
    String upload(MultipartFile multipartFile);

    /**
     * 上传文件
     * @param bucketName 桶名称
     * @param multipartFile 文件对象
     * @return 文件访问路径
     */
    String upload(String bucketName, MultipartFile multipartFile);

    /**
     * 删除单个文件, 取默认桶名称
     * @param fileName 文件名
     */
    void deleteFile(String fileName);

    /**
     * 删除单个文件
     * @param bucketName 桶
     * @param fileName 文件名
     */
    void deleteFile(String bucketName, String fileName);

    /**
     * 删除多个文件
     * @param fileNames 文件名集合
     */
    void deleteFile(List<String> fileNames);

    /**
     * 删除多个文件
     * @param bucketName 桶
     * @param fileNames 文件名集合
     */
    void deleteFile(String bucketName, List<String> fileNames);
}
