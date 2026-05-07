package cn.minerealms.signpicture.attr;

import cn.minerealms.signpicture.attr.prop.*;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 属性读取器
 * 从文本解析图片属性
 */
public class AttrReaders {

    public static final @Nonnull AttrReaders BLANK = new AttrReaders();

    // 属性模式: key=value 或 key:value
    private static final Pattern ATTR_PATTERN = Pattern.compile("([a-zA-Z]+)[:=]([^,\\s]+)");

    private final Map<String, String> attributes = new HashMap<>();

    private SizeData sizeData = SizeData.DefaultSize;
    private RotationData rotationData = RotationData.DefaultRotation;
    private OffsetData offsetData = OffsetData.DefaultOffset;
    private AnimationData animationData = AnimationData.DefaultAnimation;
    private TextureData textureData = TextureData.DefaultTexture;

    private boolean hasInvalidMeta = false;

    public AttrReaders() {
        // 空构造器
    }

    /**
     * 从文本解析属性
     *
     * @param text 属性文本
     */
    public AttrReaders(@Nonnull String text) {
        parse(text);
    }

    /**
     * 解析属性文本
     */
    private void parse(@Nonnull String text) {
        Matcher matcher = ATTR_PATTERN.matcher(text);

        while (matcher.find()) {
            String key = matcher.group(1).toLowerCase();
            String value = matcher.group(2);
            attributes.put(key, value);
        }

        // 解析各种属性
        parseSizeData();
        parseRotationData();
        parseOffsetData();
        parseAnimationData();
        parseTextureData();
    }

    /**
     * 解析大小属性
     * 格式: w=100, h=200, x2, y3
     */
    private void parseSizeData() {
        try {
            float width = parseFloat("w", 1.0f);
            float height = parseFloat("h", 1.0f);

            // 支持简写: x2 表示宽度x2
            if (attributes.containsKey("x")) {
                width = parseFloat("x", 1.0f);
            }
            // y2 表示高度x2
            if (attributes.containsKey("y")) {
                height = parseFloat("y", 1.0f);
            }

            this.sizeData = new SizeData(width, height);
        } catch (Exception e) {
            this.hasInvalidMeta = true;
        }
    }

    /**
     * 解析旋转属性
     * 格式: rx=90, ry=180, rz=45 或 X90, Y180, Z45
     */
    private void parseRotationData() {
        try {
            float x = parseFloat("rx", 0.0f);
            float y = parseFloat("ry", 0.0f);
            float z = parseFloat("rz", 0.0f);

            // 支持简写: X90 表示X轴旋转90度
            if (attributes.containsKey("x")) {
                String val = attributes.get("x");
                if (val.matches("\\d+")) {
                    x = Float.parseFloat(val);
                }
            }
            if (attributes.containsKey("y")) {
                String val = attributes.get("y");
                if (val.matches("\\d+")) {
                    y = Float.parseFloat(val);
                }
            }
            if (attributes.containsKey("z")) {
                String val = attributes.get("z");
                if (val.matches("\\d+")) {
                    z = Float.parseFloat(val);
                }
            }

            this.rotationData = new RotationData(x, y, z);
        } catch (Exception e) {
            this.hasInvalidMeta = true;
        }
    }

    /**
     * 解析偏移属性
     * 格式: ox=0.5, oy=1.0, oz=0.0
     */
    private void parseOffsetData() {
        try {
            float x = parseFloat("ox", 0.0f);
            float y = parseFloat("oy", 0.0f);
            float z = parseFloat("oz", 0.0f);

            this.offsetData = new OffsetData(x, y, z);
        } catch (Exception e) {
            this.hasInvalidMeta = true;
        }
    }

    /**
     * 解析动画属性
     * 格式: easing=linear, redstone=none
     */
    private void parseAnimationData() {
        try {
            String easing = attributes.getOrDefault("easing", "linear");
            String redstone = attributes.getOrDefault("redstone", "none");

            this.animationData = new AnimationData(easing, redstone);
        } catch (Exception e) {
            this.hasInvalidMeta = true;
        }
    }

    /**
     * 解析纹理属性
     * 格式: u=0.0, v=0.0, opacity=1.0
     */
    private void parseTextureData() {
        try {
            float u = parseFloat("u", 0.0f);
            float v = parseFloat("v", 0.0f);
            float w = parseFloat("w", 1.0f);
            float h = parseFloat("h", 1.0f);
            float opacity = parseFloat("opacity", 1.0f);
            boolean repeat = parseBoolean("repeat", false);
            boolean mipmap = parseBoolean("mipmap", true);
            boolean lighting = parseBoolean("lighting", true);

            this.textureData = new TextureData(u, v, w, h, opacity, repeat, mipmap, lighting);
        } catch (Exception e) {
            this.hasInvalidMeta = true;
        }
    }

    // 辅助方法
    private float parseFloat(String key, float defaultValue) {
        String value = attributes.get(key);
        if (value == null) return defaultValue;
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private int parseInt(String key, int defaultValue) {
        String value = attributes.get(key);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private boolean parseBoolean(String key, boolean defaultValue) {
        String value = attributes.get(key);
        if (value == null) return defaultValue;
        return "true".equalsIgnoreCase(value) || "1".equals(value) || "yes".equalsIgnoreCase(value);
    }

    // Getters
    public SizeData getSizeData() {
        return sizeData;
    }

    public RotationData getRotationData() {
        return rotationData;
    }

    public OffsetData getOffsetData() {
        return offsetData;
    }

    public AnimationData getAnimationData() {
        return animationData;
    }

    public TextureData getTextureData() {
        return textureData;
    }

    public boolean hasInvalidMeta() {
        return hasInvalidMeta;
    }

    @Override
    public String toString() {
        return String.format("AttrReaders[size=%s, rotation=%s, offset=%s, animation=%s, texture=%s, invalid=%s]",
                sizeData, rotationData, offsetData, animationData, textureData, hasInvalidMeta);
    }
}
