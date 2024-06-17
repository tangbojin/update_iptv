package com.tbj;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class IPTVStreamExtractor {

    public static void main(String[] args) {
        // 定义URLs和对应的key
        String[] urls = {
                "https://api.gzstv.com/v1/tv/ch03/",
                "https://api.gzstv.com/v1/tv/ch04/",
                "https://api.gzstv.com/v1/tv/ch05/",
                "https://api.gzstv.com/v1/tv/ch06/"
        };
        String[] keys = {"gz3", "gz4", "gz5", "gz6"};
        Map<String, String> streamUrls = new HashMap<>();

        OkHttpClient client = new OkHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        // 请求URLs并获取stream_url
        for (int i = 0; i < urls.length; i++) {
            Request request = new Request.Builder().url(urls[i]).build();
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    JsonNode rootNode = mapper.readTree(response.body().string());
                    String streamUrl = rootNode.path("stream_url").asText();
                    streamUrls.put(keys[i], streamUrl);
                } else {
                    System.out.println("Request failed with HTTP " + response.code());
                }
            } catch (IOException e) {
                System.out.println("Error fetching or parsing data from " + urls[i]);
                e.printStackTrace();
            }
        }

//        String m3uFilePath = "E:\\my_tv.m3u";
//        String m3uFilePath = ClassLoader.getSystemResource("my_tv.m3u").getPath();
        String m3uFilePath = "src/main/resources/my_tv.m3u";
        System.out.println("user.dir:  "+System.getProperty("user.dir"));
        System.out.println(IPTVStreamExtractor.class.getClassLoader().getResource("my_tv.m3u"));
        System.out.println("m3uFilePath:  " + m3uFilePath);

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(m3uFilePath));
             BufferedWriter writer = Files.newBufferedWriter(Paths.get(m3uFilePath + ".tmp"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("贵州-3") || line.contains("贵州-4") || line.contains("贵州-5") || line.contains("贵州-6")) {
                    // 根据当前行的频道标识获取对应的key
                    String key = line.split("贵州-")[1];
                    // 读取下一行，替换为stream_url
                    String nextLine = reader.readLine();
                    writer.write(line + System.lineSeparator());
                    writer.write(streamUrls.get("gz" + key) + System.lineSeparator());
                    System.out.println("line:  " + key + streamUrls.get("gz" + key));
                } else {
                    writer.write(line + System.lineSeparator());
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading or writing m3u file.");
            e.printStackTrace();
        }

        // 替换原始m3u文件
        try {
            Path oldFilePath = Paths.get(m3uFilePath);
            Path newFilePath = Paths.get(m3uFilePath + ".tmp");
            Files.move(newFilePath, oldFilePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("m3u file has been updated successfully.");
            Thread.sleep(3000);
        } catch (IOException e) {
            System.out.println("Error replacing the original m3u file.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.exit(0);
    }
}