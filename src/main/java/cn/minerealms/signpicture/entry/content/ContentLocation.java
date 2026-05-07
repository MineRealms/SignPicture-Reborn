package cn.minerealms.signpicture.entry.content;

import javax.annotation.Nonnull;
import java.io.File;

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
    
    public @Nonnull File getCacheFile(final @Nonnull String id) {
        return new File(this.cacheDir, id);
    }
    
    public @Nonnull File getMetaFile(final @Nonnull String id) {
        return new File(this.metaDir, id + ".json");
    }
    
    public @Nonnull File getCacheDir() {
        return this.cacheDir;
    }
    
    public @Nonnull File getMetaDir() {
        return this.metaDir;
    }
}
