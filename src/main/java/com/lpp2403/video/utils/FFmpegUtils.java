package com.lpp2403.video.utils;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FFmpegUtils {

    public static final Path temDir = Paths.get(System.getProperty("java.io.tmpdir"));
    private static final Logger LOGGER = LoggerFactory.getLogger(FFmpegUtils.class);

    public static String transcodeToM3U8(String video) throws IOException, InterruptedException {

        Path resultPath = Paths.get(temDir.toString(), "result");
        Files.createDirectories(resultPath);

        // ffmpeg -i VIDEO_video -c:v libx264 -c:a copy -hls_time 30 -hls_playlist_type vod -hls_segment_filename %06d.ts index.m3u8
        var commands = new ArrayList<String>();
        commands.add("ffmpeg");
        commands.add("-i");
        commands.add(temDir.resolve(video).toString());
        commands.add("-c:v");
        commands.add("libx264");
        commands.add("-c:a");
        commands.add("copy");
        commands.add("-hls_time");
        commands.add(Constant.TS_SECONDS);
        commands.add("-hls_playlist_type");
        commands.add("vod");
        commands.add("-hls_segment_filename");
        commands.add("%05d.ts");
        commands.add("index.m3u8");

        Process process = new ProcessBuilder()
                .command(commands)
                .directory(resultPath.toFile())
                .start();

         new Thread(() -> {
           try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
             LOGGER.info(line);
            }
           } catch (IOException ignored) {
           }
          }).start();

          new Thread(() -> {
           try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
             LOGGER.error(line);
            }
           } catch (IOException ignored) {
           }
          }).start();

        if (process.waitFor() != 0) {
            throw new RuntimeException("Có lỗi trong quá trình chuck video");
        }

        Files.delete(temDir.resolve(video));
        return Paths.get(temDir.toString(), "/result").toString();
    }

    public static Map<String, InputStream> ChunkVideoFile(String videoFileName, String prefixPath) throws IOException, InterruptedException {
        String path = FFmpegUtils.transcodeToM3U8(videoFileName);

        BufferedReader br = new BufferedReader(new FileReader(path + "/index.m3u8"));
        var lines = new ArrayList<String>();
        while (true) {
            String line = br.readLine();
            if (line == null) break;
            lines.add(line);
        }
        br.close();

        Map<String, String> encryptTsFile = new HashMap<>();
        for (int i = 0; i < lines.size(); ++i) {
            if (lines.get(i).endsWith(".ts")) {
                String etfs = UUID.randomUUID().toString().replaceAll("-", "");
                System.out.println(etfs);
                encryptTsFile.put(lines.get(i), etfs + ".ts");
                lines.set(i, prefixPath + etfs + ".ts");
            }
        }

        FileWriter fr = new FileWriter(path + "/index.m3u8");
        for (String line : lines) {
            fr.write(line + "\n");
        }
        fr.close();

        var map = new HashMap<String, InputStream>();
        var files = Paths.get(path).toFile().listFiles();
        if (files != null) {
            for (var file : files) {
                String key = (encryptTsFile.containsKey(file.getName()) ? encryptTsFile.get(file.getName()) : file.getName());
                map.put(key, (InputStream) FileUtils.openInputStream(file));
                Files.delete(Paths.get(path, file.getName()));
            }
        }

        return map;
    }
}
