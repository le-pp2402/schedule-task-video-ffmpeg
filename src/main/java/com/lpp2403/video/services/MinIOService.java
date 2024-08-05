package com.lpp2403.video.services;

import io.minio.*;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Service
public class MinIOService {
    private final MinioClient minioClient;

    @Value("${BUCKET_NAME}")
    private String bucketName;

    @Autowired
    public MinIOService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public String newFileName(String filename) {
        log.info(filename);
        String[] words = filename.split("[ ]");
        StringBuilder str = new StringBuilder();
        for (var word : words) {
            word = word.trim();
            if (word.isEmpty()) continue;
            str.append(word).append("_");
        }
        str.deleteCharAt(str.length() - 1);
        log.info(str.toString());
        return str.toString();
    }

    public String uploadVideo(InputStream video, String filePath, String ContentType) throws Exception {
        PutObjectArgs putObj = PutObjectArgs
                .builder()
                .contentType(ContentType)
                .stream(video, video.available(), -1)
                .bucket(bucketName)
                .object(filePath)
                .build();
        minioClient.putObject(putObj);
        return filePath;
    }

    public String uploadVideo(InputStream video, String filePath) throws Exception {
        PutObjectArgs putObj = PutObjectArgs
                .builder()
                .contentType("application/vnd.apple.mpegurl")
                .stream(video, video.available(), -1)
                .bucket(bucketName)
                .object(filePath)
                .build();
        minioClient.putObject(putObj);
        return filePath;
    }

    public String uploadDocument(MultipartFile document, String filePath) throws Exception {
        InputStream input = document.getInputStream();
        PutObjectArgs putObjectArgs = PutObjectArgs
                .builder()
                .contentType(document.getContentType())
                .stream(input, input.available(), -1)
                .bucket(bucketName)
                .object(filePath)
                .build();
        minioClient.putObject(putObjectArgs);
        return filePath;
    }

    public void downloadFile(String file, String downloadAt) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        DownloadObjectArgs dlObjectArgs = DownloadObjectArgs
                .builder()
                .bucket(bucketName)
                .object(file)
                .filename(downloadAt)
                .build();
        minioClient.downloadObject(dlObjectArgs);
    }

    public InputStream getFile(String file) throws IOException, NoSuchAlgorithmException, InvalidKeyException, MinioException {
        GetObjectArgs getObjectArgs = GetObjectArgs
                .builder()
                .bucket(bucketName)
                .object(file)
                .build();
        return minioClient.getObject(getObjectArgs);
    }

    public InputStream getImage(String file) throws IOException, NoSuchAlgorithmException, InvalidKeyException, MinioException {
        GetObjectArgs getObjectArgs = GetObjectArgs
                .builder()
                .bucket(bucketName)
                .object(file)
                .build();
        return minioClient.getObject(getObjectArgs);
    }

    public void delete(String path) throws IOException, NoSuchAlgorithmException, InvalidKeyException, MinioException {
        String bucket = path.substring(0, path.indexOf("/"));
        String file = path.substring(path.indexOf("/") + 1);
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                .bucket(bucket)
                .object(file)
                .build();
        minioClient.removeObject(removeObjectArgs);
    }
}
