package cn.minerealms.signpicture.entry.content;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 内容位置工具
 * 管理缓存文件路径
 */
public class ContentLocation {
    private final @Nonnull File cacheDir;
    private final @Nonnull File metaDir;

    public ContentLocation(final @Nonnull File baseDir) {
        this.cacheDir = new File(baseDir, "cache");
        this.metaDir = new File(baseDir, "meta");

        if (!this.cacheDir.exists()) {
            this.cacheDir.mkdirs();
        }
        if (!this.metaDir.exists()) {
            this.metaDir.mkdirs();
        }
    }

    /**
     * 将字符串转换为MD5 hash
     */
    private String toHash(final @Nonnull String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            // 如果hash失败，使用简单的替换方案
            return input.replaceAll("[^a-zA-Z0-9._-]", "_");
        }
    }

    public @Nonnull File getCacheFile(final @Nonnull String id) {
        // 使用MD5 hash作为文件名，确保全平台兼容
        String hash = toHash(id);
        return new File(this.cacheDir, hash);
    }

    public @Nonnull File getMetaFile(final @Nonnull String id) {
        // 使用MD5 hash作为文件名
        String hash = toHash(id);
        return new File(this.metaDir, hash + ".json");
    }

    public @Nonnull File getCacheDir() {
        return this.cacheDir;
    }

    public @Nonnull File getMetaDir() {
        return this.metaDir;
    }
}
