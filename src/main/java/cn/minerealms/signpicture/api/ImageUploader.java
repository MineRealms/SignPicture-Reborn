package cn.minerealms.signpicture.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.CompletableFuture;

/**
 * 图片上传API接口
 */
public interface ImageUploader {

    /**
     * 上传图片文件
     *
     * @param file   图片文件
     * @param apiKey API密钥
     * @return 上传结果（URL）
     */
    @Nonnull
    CompletableFuture<UploadResult> upload(@Nonnull File file, @Nullable String apiKey);

    /**
     * 上传BufferedImage
     *
     * @param image  图片
     * @param apiKey API密钥
     * @return 上传结果（URL）
     */
    @Nonnull
    CompletableFuture<UploadResult> upload(@Nonnull BufferedImage image, @Nullable String apiKey);

    /**
     * 获取上传器名称
     */
    @Nonnull
    String getName();

    /**
     * 上传结果
     */
    class UploadResult {
        private final boolean success;
        private final String url;
        private final String error;

        public UploadResult(boolean success, @Nullable String url, @Nullable String error) {
            this.success = success;
            this.url = url;
            this.error = error;
        }

        public static UploadResult success(@Nonnull String url) {
            return new UploadResult(true, url, null);
        }

        public static UploadResult failure(@Nonnull String error) {
            return new UploadResult(false, null, error);
        }

        public boolean isSuccess() {
            return success;
        }

        @Nullable
        public String getUrl() {
            return url;
        }

        @Nullable
        public String getError() {
            return error;
        }
    }
}
