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
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bitly URL缩短器实现
 */
public class BitlyShortener implements UrlShortener {

    private static final String BITLY_API_URL = "https://api-ssl.bitly.com/v4/shorten";
    private static final Pattern LINK_PATTERN = Pattern.compile("\"link\":\"([^\"]+)\"");

    @Override
    @Nonnull
    public CompletableFuture<UrlShortener.ShortenResult> shorten(@Nonnull String url, @Nullable String apiKey) {
        return CompletableFuture.supplyAsync(() -> {
            if (apiKey == null || apiKey.isEmpty()) {
                return UrlShortener.ShortenResult.failure("Bitly API key not configured");
            }

            try {
                Log.debug("Shortening URL with Bitly: " + url);

                HttpClient client = HttpClientBuilder.create().build();
                HttpPost post = new HttpPost(BITLY_API_URL);

                // 添加请求头
                post.setHeader("Authorization", "Bearer " + apiKey);
                post.setHeader("Content-Type", "application/json");

                // 构建JSON请求体
                String jsonBody = String.format("{\"long_url\":\"%s\"}", escapeJson(url));
                post.setEntity(new StringEntity(jsonBody, "UTF-8"));

                // 发送请求
                HttpResponse response = client.execute(post);
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode == 200 || statusCode == 201) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    String shortUrl = extractShortUrl(responseBody);

                    if (shortUrl != null) {
                        Log.info("Bitly shorten successful: " + shortUrl);
                        return UrlShortener.ShortenResult.success(shortUrl);
                    } else {
                        Log.error("Failed to parse Bitly response");
                        return UrlShortener.ShortenResult.failure("Failed to parse response");
                    }
                } else {
                    String errorMsg = "Bitly API returned status " + statusCode;
                    try {
                        String responseBody = EntityUtils.toString(response.getEntity());
                        Log.error(errorMsg + ": " + responseBody);
                    } catch (Exception e) {
                        Log.error(errorMsg);
                    }
                    return UrlShortener.ShortenResult.failure(errorMsg);
                }
            } catch (IOException e) {
                Log.error("Bitly shorten failed", e);
                return UrlShortener.ShortenResult.failure("Shorten failed: " + e.getMessage());
            }
        });
    }

    /**
     * 从Bitly API响应中提取短链接
     */
    private String extractShortUrl(String jsonResponse) {
        Matcher matcher = LINK_PATTERN.matcher(jsonResponse);
        if (matcher.find()) {
            String url = matcher.group(1);
            // 解码转义的斜杠
            return url.replace("\\/", "/");
        }
        return null;
    }

    /**
     * 转义JSON字符串
     */
    private String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    @Override
    @Nonnull
    public String getName() {
        return "Bitly";
    }
}
