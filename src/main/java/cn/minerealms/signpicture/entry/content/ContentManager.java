package cn.minerealms.signpicture.entry.content;

import cn.minerealms.signpicture.Config;
import cn.minerealms.signpicture.Log;
import cn.minerealms.signpicture.util.ThreadUtils;
import com.google.common.collect.Maps;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 内容管理器
 * 单例，管理所有Content的生命周期
 */
public class ContentManager {
    public static final ContentManager instance = new ContentManager();

    private final Map<ContentId, Content> contents = Maps.newConcurrentMap();
    private final ExecutorService loadExecutor;
    private ContentLocation location;
    private int tickCount = 0;

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
     * Tick所有Content
     */
    public void tickAll() {
        tickCount++;

        // 每隔一定时间执行GC
        int gcInterval = Config.COMMON.contentGCtick.get();
        if (gcInterval > 0 && tickCount % gcInterval == 0) {
            collectGarbage();
        }
    }

    /**
     * 垃圾回收
     * 移除未被Entry引用的Content
     */
    public void collectGarbage() {
        // 最大内存使用：从配置读取（字节）
        long maxMemory = Config.COMMON.contentMaxByte.get();

        // 计算当前内存使用（简化版：假设每个Content平均1MB）
        long estimatedMemory = contents.size() * 1024 * 1024L;

        if (estimatedMemory > maxMemory) {
            int beforeSize = contents.size();

            // 移除最老的Content直到内存使用低于限制
            int toRemove = (int) ((estimatedMemory - maxMemory) / (1024 * 1024));

            contents.values().stream()
                .limit(toRemove)
                .forEach(content -> {
                    content.onCollect();
                    contents.remove(content.getId());
                    Log.debug("Collecting Content: " + content.getId());
                });

            int afterSize = contents.size();
            int collected = beforeSize - afterSize;

            if (collected > 0) {
                Log.info("Content GC: collected " + collected + " contents, " + afterSize + " remaining");
            }
        }
    }

    /**
     * 清除所有Content缓存
     * @return 清除的Content数量
     */
    public int clear() {
        int count = contents.size();
        contents.values().forEach(Content::onCollect);
        contents.clear();
        return count;
    }

    public void shutdown() {
        loadExecutor.shutdown();
        clear();
    }
}
