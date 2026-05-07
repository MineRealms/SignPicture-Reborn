package cn.minerealms.signpicture.entry.content;

import cn.minerealms.signpicture.Config;
import cn.minerealms.signpicture.util.ThreadUtils;
import com.google.common.collect.Maps;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * 内容管理器
 * 单例，管理所有Content的生命周期
 */
public class ContentManager {
    public static final ContentManager instance = new ContentManager();
    
    private final Map<ContentId, Content> contents = Maps.newConcurrentMap();
    private final ExecutorService loadExecutor;
    private ContentLocation location;
    
    private ContentManager() {
        this.loadExecutor = ThreadUtils.newFixedCachedThreadPool(
            Config.COMMON.contentLoadThreads.get(),
            "SignPicture-ContentLoad-%d"
        );
    }
    
    public void init(@Nonnull File baseDir) {
        this.location = new ContentLocation(baseDir);
    }
    
    /**
     * 获取或创建Content
     */
    public @Nonnull Content get(@Nonnull ContentId id) {
        return contents.computeIfAbsent(id, key -> {
            Content content = new Content(key, location);
            content.onInit();
            
            // 异步加载
            loadExecutor.submit(() -> {
                try {
                    content.onAsyncProcess();
                    content.onDivisionProcess();
                } catch (Exception e) {
                    content.getState().setErrorMessage(e);
                }
            });
            
            return content;
        });
    }
    
    /**
     * 垃圾回收
     */
    public void collectGarbage() {
        contents.values().removeIf(content -> {
            // TODO: 实现更智能的GC策略
            return false;
        });
    }
    
    public void shutdown() {
        loadExecutor.shutdown();
        contents.values().forEach(Content::onCollect);
        contents.clear();
    }
}
