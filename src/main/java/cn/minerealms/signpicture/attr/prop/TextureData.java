package cn.minerealms.signpicture.attr.prop;

import cn.minerealms.signpicture.attr.IPropComposable;
import cn.minerealms.signpicture.attr.IPropInterpolatable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 纹理数据
 * UV坐标、透明度、混合模式等
 */
public class TextureData implements IPropInterpolatable<TextureData>, IPropComposable {
    public static final @Nonnull TextureData DefaultTexture = new TextureData();
    
    private final float u;
    private final float v;
    private final float w;
    private final float h;
    private final float opacity;
    private final boolean repeat;
    private final boolean mipmap;
    private final boolean lighting;
    
    public TextureData() {
        this(0, 0, 1, 1, 1.0f, false, true, true);
    }
    
    public TextureData(float u, float v, float w, float h, float opacity, 
                      boolean repeat, boolean mipmap, boolean lighting) {
        this.u = u;
        this.v = v;
        this.w = w;
        this.h = h;
        this.opacity = opacity;
        this.repeat = repeat;
        this.mipmap = mipmap;
        this.lighting = lighting;
    }
    
    public float getU() { return u; }
    public float getV() { return v; }
    public float getW() { return w; }
    public float getH() { return h; }
    public float getOpacity() { return opacity; }
    public boolean isRepeat() { return repeat; }
    public boolean isMipmap() { return mipmap; }
    public boolean isLighting() { return lighting; }
    
    @Override
    public @Nonnull TextureData per() {
        return this;
    }
    
    @Override
    public @Nonnull TextureData per(final float per, final @Nullable TextureData before) {
        if (before != null) {
            float nu = getU() * per + before.getU() * (1f - per);
            float nv = getV() * per + before.getV() * (1f - per);
            float nw = getW() * per + before.getW() * (1f - per);
            float nh = getH() * per + before.getH() * (1f - per);
            float no = getOpacity() * per + before.getOpacity() * (1f - per);
            return new TextureData(nu, nv, nw, nh, no, repeat, mipmap, lighting);
        }
        return this;
    }
    
    @Override
    public @Nonnull String compose() {
        StringBuilder sb = new StringBuilder();
        if (u != 0) sb.append("u").append(u);
        if (v != 0) sb.append("v").append(v);
        if (w != 1) sb.append("w").append(w);
        if (h != 1) sb.append("h").append(h);
        if (opacity != 1) sb.append("o").append(opacity);
        if (repeat) sb.append("r1");
        if (!mipmap) sb.append("m0");
        if (!lighting) sb.append("l0");
        return sb.toString();
    }
}
