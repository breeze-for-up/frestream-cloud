package com.frestream.file.minio;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.frestream.common.exception.BizException;
import com.frestream.file.service.FileService;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * @description:
 * 
 * @author: TJ
 * @date:  2022-09-01
 **/
@Slf4j(topic = "common-file-service")
@Service("minioFileService")
public class MinioFileServiceImpl implements FileService {

    @Autowired
    private MinioClient minioClient;
    @Autowired
    private MinioConfig minioConfig;



    @Override
    public String upload(MultipartFile multipartFile) {
        return upload(null, multipartFile);
    }

    /**
     * 文件上传
     *
     * @param bucketName 桶名称
     * @param multipartFile 文件
     */
    @Override
    public String upload(String bucketName, MultipartFile multipartFile) {
        if (StrUtil.isBlank(bucketName)) {
            bucketName = minioConfig.getDefaultBucketName();
        }

        createBucketIfIfAbsent(bucketName);

        // 存储文件名取UUID
        String fileName = multipartFile.getOriginalFilename();
        String objectName = IdUtil.fastSimpleUUID() + "." + FileNameUtil.getSuffix(fileName);

        try {
            InputStream inputStream = new ByteArrayInputStream(multipartFile.getBytes());
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, multipartFile.getSize(), -1)
                            .contentType(multipartFile.getContentType())
                            .build());
        } catch (Exception e) {
            log.error("文件上传异常:{}, {}, \n {}", bucketName, fileName, e);
            throw new BizException("minio文件上传异常, 请稍后重试");
        }

        String fileUrl;
        String customDomain = minioConfig.getCustomDomain();
        if (StrUtil.isBlank(customDomain)) {

            fileUrl = getFileUrl(bucketName, objectName);
            fileUrl = fileUrl.substring(0, fileUrl.indexOf("?"));
        } else {
            // 自定义文件路径域名, Nginx 配置代理转发MinIO
            fileUrl = customDomain + '/' + bucketName + "/" + fileName;
        }

        return fileUrl;
    }

    @Override
    public void deleteFile(String fileName) {
        deleteFile(null, fileName);
    }


    /**
     * 删除单个文件
     *
     * @param bucketName 存储桶名称
     * @param fileName 存储桶里的文件名称
     */
    @Override
    public void deleteFile(String bucketName, String fileName) {
        if (StrUtil.isBlank(bucketName)) {
            bucketName = minioConfig.getDefaultBucketName();
        }
        boolean exists = bucketExists(bucketName);
        if (!exists) {
            throw new BizException("桶不存在: " + bucketName);
        }
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build());
        } catch (Exception e) {
            log.error("删除文件异常, bucketName: {}, fileName: {}; \n {}", bucketName, fileName, e);
            throw new BizException("删除文件异常, 文件名:{}", fileName);
        }
    }

    @Override
    public void deleteFile(List<String> fileNames) {
        deleteFile(null, fileNames);
    }

    /**
     * 删除指定桶的多个文件对象
     *
     * @param bucketName  存储桶名称
     * @param fileNames 删除文件集合
     */
    @Override
    public void deleteFile(String bucketName, List<String> fileNames) {
        if (StrUtil.isBlank(bucketName)) {
            bucketName = minioConfig.getDefaultBucketName();
        }
        boolean flag = bucketExists(bucketName);
        if (flag) {
            List<DeleteObject> objects = new LinkedList<>();
            for (String fileName : fileNames) {
                objects.add(new DeleteObject(fileName));
            }
            minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                            .bucket(bucketName)
                            .objects(objects).build());
        }
    }



    // ==========================================  private

    /**
     * 文件访问路径
     *
     * @param bucketName 存储桶名称
     * @param fileName 存储桶里的文件名称
     * @return 访问路径
     */
    private String getFileUrl(String bucketName, String fileName) {
        boolean exist = bucketExists(bucketName);
        if (!exist) {
            log.warn("获取文件访问路径, 桶不存在, bucketName: {}, fileName:{}", bucketName, fileName);
            return "";
        }
        String url = "";
        try {
            url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(fileName)
                            .build());
        } catch (Exception e) {
            log.error("文件路径获取异常, bucketName: {}, fileName: {}; \n {}", bucketName, fileName, e);
        }
        return url;
    }

    /**
     * 检查存储桶是否存在
     *
     * @param bucketName 存储桶名称
     * @return boolean
     */
    private boolean bucketExists(String bucketName) {
        boolean found = false;
        try {
            found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            log.error("{} does not exist: {}", bucketName, e);
        }
        return found;
    }

    /**
     * 若桶不存在, 创建桶
     *
     * @param bucketName 存储桶名称
     */
    private void createBucketIfIfAbsent(String bucketName) {
        boolean exist = bucketExists(bucketName);
        if (!exist) {
            try {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build());

                // 设置存储桶访问权限
                SetBucketPolicyArgs setBucketPolicyArgs = SetBucketPolicyArgs.builder()
                        .bucket(bucketName)
                        .config(publicBucketPolicy(bucketName))
                        .build();
                minioClient.setBucketPolicy(setBucketPolicyArgs);
            } catch (Exception e) {
                log.error("createBucket exception: {}, \n {}", bucketName, e);
            }
        }
    }

    /**
     * public桶策略
     * minio新建存储桶的访问策略是private, 拒绝访问 Access Denied
     *
     * @param bucketName 桶名称
     */
    private static String publicBucketPolicy(String bucketName) {
        /*
         * AWS的S3存储桶策略
         * Principal: 生效用户对象
         * Resource:  指定存储桶
         * Action: 操作行为
         */
        return "{\"Version\":\"2012-10-17\"," +
                "\"Statement\":[{\"Effect\":\"Allow\"," +
                "\"Principal\":{\"AWS\":[\"*\"]}," +
                "\"Action\":[\"s3:ListBucketMultipartUploads\",\"s3:GetBucketLocation\",\"s3:ListBucket\"]," +
                "\"Resource\":[\"arn:aws:s3:::" + bucketName + "\"]}," +
                "{\"Effect\":\"Allow\"," +
                "\"Principal\":{\"AWS\":[\"*\"]}," +
                "\"Action\":[\"s3:ListMultipartUploadParts\",\"s3:PutObject\",\"s3:AbortMultipartUpload\",\"s3:DeleteObject\",\"s3:GetObject\"]," +
                "\"Resource\":[\"arn:aws:s3:::" + bucketName + "/*\"]}]}";
    }
}
