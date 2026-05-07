package cn.minerealms.signpicture.entry;

import javax.annotation.Nonnull;

/**
 * Entry ID
 * 标识一个Entry实例
 */
public class EntryId {
    private final @Nonnull String id;
    
    public EntryId(@Nonnull String id) {
        this.id = id;
    }
    
    public @Nonnull String getId() {
        return this.id;
    }
    
    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof EntryId)) return false;
        return this.id.equals(((EntryId) obj).id);
    }
    
    @Override
    public String toString() {
        return "EntryId[" + id + "]";
    }
    
    public static @Nonnull EntryId from(@Nonnull String id) {
        return new EntryId(id);
    }
}
