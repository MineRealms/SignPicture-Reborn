package cn.minerealms.signpicture.attr.prop;

import cn.minerealms.signpicture.attr.IPropComposable;

import javax.annotation.Nonnull;

/**
 * 动画数据
 * 缓动函数和红石触发
 */
public class AnimationData implements IPropComposable {
    public static final @Nonnull AnimationData DefaultAnimation = new AnimationData("linear", "none");
    
    private final String easing;
    private final String redstone;
    
    public AnimationData(final String easing, final String redstone) {
        this.easing = easing;
        this.redstone = redstone;
    }
    
    public String getEasing() {
        return this.easing;
    }
    
    public String getRedstone() {
        return this.redstone;
    }
    
    @Override
    public @Nonnull String compose() {
        StringBuilder sb = new StringBuilder();
        if (!"linear".equals(easing)) {
            sb.append("t").append(easing);
        }
        if (!"none".equals(redstone)) {
            sb.append("k").append(redstone);
        }
        return sb.toString();
    }
    
    public static @Nonnull AnimationData create(final String easing, final String redstone) {
        return new AnimationData(easing, redstone);
    }
}
