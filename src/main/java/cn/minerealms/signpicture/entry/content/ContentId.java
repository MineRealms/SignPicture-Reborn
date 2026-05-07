package cn.minerealms.signpicture.entry.content;

import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 内容ID
 * 标识一个图片资源（URL或ResourceLocation）
 */
public class ContentId {
    public static final @Nonnull ResourceLocation hideTexture =
        ResourceLocation.tryParse("signpicture:textures/state/hide.png");
    public static final @Nonnull ContentId hideContent = ContentId.fromResource(hideTexture);
    
    private final @Nonnull String id;
    
    protected ContentId(@Nonnull String uri) {
        // 直接存储原始URL，不做转换
        this.id = uri;
    }

    public @Nonnull String getID() {
        return this.id;
    }

    public @Nonnull String getURI() {
        // 如果已经是以http或https开头，直接返回
        if (StringUtils.startsWith(this.id, "http://") || StringUtils.startsWith(this.id, "https://")) {
            return this.id;
        }
        // 如果是资源Location (!开头)
        if (StringUtils.startsWith(this.id, "!")) {
            return this.id;
        }
        // 其他情况加上http://前缀
        return "http://" + this.id;
    }
    
    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
    
    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof ContentId)) return false;
        final ContentId other = (ContentId) obj;
        return this.id.equals(other.id);
    }
    
    @Override
    public @Nonnull String toString() {
        return String.format("ContentId [id=%s]", this.id);
    }
    
    public boolean isResource() {
        return this.id.startsWith("!");
    }
    
    public @Nonnull ResourceLocation getResource() {
        String resId = StringUtils.substring(this.id, 1);
        return ResourceLocation.tryParse(resId);
    }
    
    public @Nonnull Content content() {
        return ContentManager.instance.get(this);
    }
    
    public static @Nonnull ContentId from(final @Nonnull String uri) {
        return new ContentId(uri);
    }
    
    public static @Nonnull ContentId fromResource(final @Nonnull ResourceLocation location) {
        return new ContentId("!" + location.toString());
    }
}
