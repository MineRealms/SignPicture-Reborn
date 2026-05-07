package cn.minerealms.signpicture.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * URL缩短服务接口
 */
public interface UrlShortener {

    /**
     * 缩短URL
     *
     * @param url    原始URL
     * @param apiKey API密钥
     * @return 缩短后的URL
     */
    @Nonnull
    CompletableFuture<ShortenResult> shorten(@Nonnull String url, @Nullable String apiKey);

    /**
     * 获取服务名称
     */
    @Nonnull
    String getName();

    /**
     * 缩短结果
     */
    class ShortenResult {
        private final boolean success;
        private final String shortUrl;
        private final String error;

        public ShortenResult(boolean success, @Nullable String shortUrl, @Nullable String error) {
            this.success = success;
            this.shortUrl = shortUrl;
            this.error = error;
        }

        public static ShortenResult success(@Nonnull String shortUrl) {
            return new ShortenResult(true, shortUrl, null);
        }

        public static ShortenResult failure(@Nonnull String error) {
            return new ShortenResult(false, null, error);
        }

        public boolean isSuccess() {
            return success;
        }

        @Nullable
        public String getShortUrl() {
            return shortUrl;
        }

        @Nullable
        public String getError() {
            return error;
        }
    }
}
