package com.lpp2403.video.services;

import com.lpp2403.video.repositories.ResourceRepository;
import com.lpp2403.video.utils.Constant;
import com.lpp2403.video.utils.FFmpegUtils;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@Service
@Transactional
public class VideoService {
    private static final Logger LOGGER = LoggerFactory.getLogger(VideoService.class);
    ResourceRepository resourceRepository;
    MinIOService minIOService;

    @Autowired
    public VideoService(MinIOService minIOService, ResourceRepository resourceRepository) {
        this.minIOService = minIOService;
        this.resourceRepository = resourceRepository;
    }

    @Scheduled(fixedRate = 12 * 60 * 60 * 1000L)
    public void run() {
        var videos = resourceRepository.findByIsReadyFalse();
        if (videos.isEmpty()) return;
        for (var video: videos) {
            try {

                cleanTmpFolder("");
                cleanTmpFolder(Constant.RESULT_FOLDER);

                var resource = resourceRepository.findById(video.getId()).orElseThrow(EntityNotFoundException::new);

                String path = video.getVideo();                           // 2309483/video/video
                String id = path.substring(0, path.indexOf('/'));


                String filename = Constant.PREFIX_VIDEO_FILE + id;
                minIOService.downloadFile(path, FFmpegUtils.temDir.resolve(filename).toString());

                StringBuilder sb = new StringBuilder();
                sb.append(Constant.MINIO_VIDEO_DIR);
                sb.append(id);
                sb.append("/video/");

                var result = FFmpegUtils.ChunkVideoFile(filename, sb.toString());


                result.forEach((key, value) -> {
                    try {
                        var MinIODir = id + "/video/" + key;
                        var fileDir = minIOService.uploadVideo(value, MinIODir);
                        if (key.equals("index.m3u8")) {
                            resource.setIsReady(true);
                            resource.setVideo(fileDir);
                        }
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage());
                    }
                });
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
        }
    }

    public void cleanTmpFolder(String folder) {
        var files = Paths.get(FFmpegUtils.temDir.toString(), "/", folder).toFile().listFiles();
        LOGGER.info("Start cleaning folder: " + FFmpegUtils.temDir.toString() + "/" + folder);
        if (files != null) {
            for (var file: files) {
                if (file.getName().contains(Constant.PREFIX_VIDEO_FILE) || file.getName().endsWith(".ts") || file.getName().endsWith(".m3u8")
                ) {
                    try {
                        Files.delete(Paths.get(FFmpegUtils.temDir.toString(), file.getName()));
                    } catch (IOException e) {
                        LOGGER.error("Can't delete file :" + file.getName());
                        LOGGER.error("Cause: " + e.getMessage());
                    }
                }
            }
        }
    }
}
