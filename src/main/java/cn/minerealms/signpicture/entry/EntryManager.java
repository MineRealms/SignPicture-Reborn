package cn.minerealms.signpicture.entry;

import cn.minerealms.signpicture.entry.content.ContentId;
import com.google.common.collect.Maps;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Entry管理器
 * 单例，管理所有Entry
 */
public class EntryManager {
    public static final EntryManager instance = new EntryManager();
    
    private final Map<EntryId, Entry> entries = Maps.newConcurrentMap();
    
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
        entries.values().forEach(Entry::onTick);
    }
    
    /**
     * 垃圾回收
     */
    public void collectGarbage() {
        // TODO: 实现GC策略
        entries.values().removeIf(entry -> {
            // 移除长时间未使用的Entry
            return false;
        });
    }
    
    public void clear() {
        entries.clear();
    }
}
