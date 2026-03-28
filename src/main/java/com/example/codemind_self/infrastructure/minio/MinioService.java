package com.example.codemind_self.infrastructure.minio;


import com.example.codemind_self.common.exception.BusinessException;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;


import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public String upload(MultipartFile file){
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename != null && originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase() : "";

        String objectName = UUID.randomUUID().toString().replace("-","") + suffix;
        try{
            minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(minioProperties.getBucketName())
                        .object(objectName)
                        .stream(file.getInputStream(),file.getSize(),-1)
                        .contentType(file.getContentType())
                        .build()
        );
        }catch (Exception e){
            log.error("上传失败",e);
            throw new BusinessException("文件上传失败");

        }
        return objectName;
    }

    public String getPresignedUrl(String objectName){
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectName)


                            .method(Method.GET)
                            .expiry(7 * 24 * 3600)
                            .build()
            );
        }catch (Exception e){
            log.error("Minio 获取预签名失败");
            throw new BusinessException("获取文件URL失败");
        }

    }

    public String downloadAsString(String objectName){
        try {
            GetObjectResponse response = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectName)
                            .build()
            );
            return new String(response.readAllBytes());
        }catch (Exception e){
            log.error("Minio 下载文件失败");
            throw new BusinessException("文件下载失败");
        }
    }
}
