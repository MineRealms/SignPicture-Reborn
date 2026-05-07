package cn.minerealms.signpicture.util;

import cn.minerealms.signpicture.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;

/**
 * HTTP下载器
 */
public class Downloader {
    private final HttpClient client;
    private final ExecutorService executor;
    
    public Downloader(int threads, int timeout) {
        RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(timeout)
            .setSocketTimeout(timeout)
            .setConnectionRequestTimeout(timeout)
            .build();
        
        this.client = HttpClientBuilder.create()
            .setDefaultRequestConfig(config)
            .setUserAgent("SignPicture-Rebornified/1.0")
            .build();
        
        this.executor = ThreadUtils.newFixedCachedThreadPool(threads, "SignPicture-Download-%d");
    }
    
    /**
     * 下载URL内容
     */
    public void download(@Nonnull String url, @Nonnull DownloadCallback callback) {
        executor.submit(() -> {
            try {
                HttpGet request = new HttpGet(url);
                HttpResponse response = client.execute(request);
                HttpEntity entity = response.getEntity();
                
                if (entity != null) {
                    try (InputStream stream = entity.getContent()) {
                        callback.onSuccess(stream, entity.getContentLength());
                    }
                } else {
                    callback.onError(new IOException("Empty response"));
                }
            } catch (Exception e) {
                Log.error("Download failed: " + url, e);
                callback.onError(e);
            }
        });
    }
    
    public void shutdown() {
        executor.shutdown();
    }
    
    public interface DownloadCallback {
        void onSuccess(InputStream stream, long contentLength) throws IOException;
        void onError(Exception e);
    }
}
