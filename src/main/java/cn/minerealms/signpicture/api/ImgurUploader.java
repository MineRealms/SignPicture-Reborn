package cn.minerealms.signpicture.api;

import cn.minerealms.signpicture.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Imgur上传器实现
 */
public class ImgurUploader implements ImageUploader {

    private static final String IMGUR_API_URL = "https://api.imgur.com/3/image";
    private static final Pattern LINK_PATTERN = Pattern.compile("\"link\":\"([^\"]+)\"");

    @Override
    @Nonnull
    public CompletableFuture<ImageUploader.UploadResult> upload(@Nonnull File file, @Nullable String apiKey) {
        return CompletableFuture.supplyAsync(() -> {
            if (apiKey == null || apiKey.isEmpty()) {
                return ImageUploader.UploadResult.failure("Imgur API key not configured");
            }

            try {
                Log.debug("Uploading file to Imgur: " + file.getName());

                // 读取文件并转换为Base64
                byte[] fileBytes = readFileBytes(file);
                String base64Image = Base64.getEncoder().encodeToString(fileBytes);

                return uploadBase64(base64Image, apiKey);
            } catch (IOException e) {
                Log.error("Imgur upload failed", e);
                return ImageUploader.UploadResult.failure("Upload failed: " + e.getMessage());
            }
        });
    }

    @Override
    @Nonnull
    public CompletableFuture<ImageUploader.UploadResult> upload(@Nonnull BufferedImage image, @Nullable String apiKey) {
        return CompletableFuture.supplyAsync(() -> {
            if (apiKey == null || apiKey.isEmpty()) {
                return ImageUploader.UploadResult.failure("Imgur API key not configured");
            }

            try {
                Log.debug("Uploading BufferedImage to Imgur");

                // 将BufferedImage转换为Base64
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "png", baos);
                byte[] imageBytes = baos.toByteArray();
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);

                return uploadBase64(base64Image, apiKey);
            } catch (IOException e) {
                Log.error("Imgur upload failed", e);
                return ImageUploader.UploadResult.failure("Upload failed: " + e.getMessage());
            }
        });
    }

    /**
     * 上传Base64编码的图片
     */
    private ImageUploader.UploadResult uploadBase64(String base64Image, String apiKey) {
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(IMGUR_API_URL);

            // 添加请求头
            post.setHeader("Authorization", "Client-ID " + apiKey);
            post.setHeader("Content-Type", "application/x-www-form-urlencoded");

            // 构建请求体（使用form-urlencoded格式）
            String requestBody = "image=" + java.net.URLEncoder.encode(base64Image, "UTF-8") + "&type=base64";
            post.setEntity(new StringEntity(requestBody, "UTF-8"));

            // 发送请求
            HttpResponse response = client.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 200) {
                String responseBody = EntityUtils.toString(response.getEntity());
                String imageUrl = extractImageUrl(responseBody);

                if (imageUrl != null) {
                    Log.info("Imgur upload successful: " + imageUrl);
                    return ImageUploader.UploadResult.success(imageUrl);
                } else {
                    Log.error("Failed to parse Imgur response");
                    return ImageUploader.UploadResult.failure("Failed to parse response");
                }
            } else {
                String errorMsg = "Imgur API returned status " + statusCode;
                try {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    Log.error(errorMsg + ": " + responseBody);
                } catch (Exception e) {
                    Log.error(errorMsg);
                }
                return ImageUploader.UploadResult.failure(errorMsg);
            }
        } catch (IOException e) {
            Log.error("Imgur upload failed", e);
            return ImageUploader.UploadResult.failure("Upload failed: " + e.getMessage());
        }
    }

    /**
     * 读取文件字节
     */
    private byte[] readFileBytes(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        }
    }

    /**
     * 从Imgur API响应中提取图片URL
     */
    private String extractImageUrl(String jsonResponse) {
        Matcher matcher = LINK_PATTERN.matcher(jsonResponse);
        if (matcher.find()) {
            String url = matcher.group(1);
            // 解码转义的斜杠
            return url.replace("\\/", "/");
        }
        return null;
    }

    @Override
    @Nonnull
    public String getName() {
        return "Imgur";
    }
}
