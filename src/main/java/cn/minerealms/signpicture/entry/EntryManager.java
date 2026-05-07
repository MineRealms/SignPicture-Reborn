package cn.minerealms.signpicture.entry;

import cn.minerealms.signpicture.Config;
import cn.minerealms.signpicture.Log;
import cn.minerealms.signpicture.entry.content.ContentId;
import com.google.common.collect.Maps;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Entry管理器
 * 单例，管理所有Entry
 */
public class EntryManager {
    public static final EntryManager instance = new EntryManager();

    private final Map<EntryId, Entry> entries = Maps.newConcurrentMap();
    private int tickCount = 0;

    private EntryManager() {
    }

    /**
     * 获取或创建Entry
     */
    public @Nonnull Entry get(@Nonnull EntryId entryId, @Nonnull ContentId contentId) {
        return entries.computeIfAbsent(entryId, key -> new Entry(key, contentId));
    }

    /**
     * Tick所有Entry
     */
    public void tickAll() {
        tickCount++;
        entries.values().forEach(Entry::onTick);

        // 每隔一定时间执行GC
        int gcInterval = Config.COMMON.entryGCtick.get();
        if (gcInterval > 0 && tickCount % gcInterval == 0) {
            collectGarbage();
        }
    }

    /**
     * 垃圾回收
     * 移除长时间未使用的Entry
     */
    public void collectGarbage() {
        // 最大空闲时间：5分钟
        long maxIdleTime = TimeUnit.MINUTES.toMillis(5);

        int beforeSize = entries.size();
        entries.values().removeIf(entry -> {
            if (entry.shouldCollect(maxIdleTime)) {
                Log.debug("Collecting Entry: " + entry.getId());
                return true;
            }
            return false;
        });

        int afterSize = entries.size();
        int collected = beforeSize - afterSize;

        if (collected > 0) {
            Log.info("Entry GC: collected " + collected + " entries, " + afterSize + " remaining");
        }
    }

    /**
     * 清除所有Entry缓存
     * @return 清除的Entry数量
     */
    public int clear() {
        int count = entries.size();
        entries.clear();
        return count;
    }
}
