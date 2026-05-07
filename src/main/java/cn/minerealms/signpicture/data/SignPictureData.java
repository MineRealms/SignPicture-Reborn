package cn.minerealms.signpicture.data;

import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * SignPicture数据模型
 * 存储单个SignPicture的所有信息
 */
public class SignPictureData {

    // 唯一标识符（6位短UUID）
    private String uuid;

    // 图片URL（只存URL，不存图片！客户端自己下载）
    private String url;

    // 渲染属性
    private float sizeWidth = 1.0f;
    private float sizeHeight = 1.0f;
    private float rotationX = 0.0f;
    private float rotationY = 0.0f;
    private float rotationZ = 0.0f;
    private float offsetX = 0.0f;
    private float offsetY = 0.0f;
    private float offsetZ = 0.0f;

    // 元数据
    private long createdTime;
    private long lastModified;

    // 版本号（用于未来升级）
    private int version = 1;

    public SignPictureData() {
        this.createdTime = System.currentTimeMillis();
        this.lastModified = this.createdTime;
    }

    public SignPictureData(@Nonnull String uuid, @Nonnull String url) {
        this();
        this.uuid = uuid;
        this.url = url;
    }

    /**
     * 序列化到NBT
     */
    @Nonnull
    public CompoundTag toNBT() {
        CompoundTag nbt = new CompoundTag();

        // 版本和标识
        nbt.putInt("version", version);
        nbt.putString("uuid", uuid);

        // 图片URL
        nbt.putString("url", url);

        // 渲染属性
        nbt.putFloat("sizeWidth", sizeWidth);
        nbt.putFloat("sizeHeight", sizeHeight);
        nbt.putFloat("rotationX", rotationX);
        nbt.putFloat("rotationY", rotationY);
        nbt.putFloat("rotationZ", rotationZ);
        nbt.putFloat("offsetX", offsetX);
        nbt.putFloat("offsetY", offsetY);
        nbt.putFloat("offsetZ", offsetZ);

        // 元数据
        nbt.putLong("createdTime", createdTime);
        nbt.putLong("lastModified", lastModified);

        return nbt;
    }

    /**
     * 从NBT反序列化
     */
    @Nullable
    public static SignPictureData fromNBT(@Nonnull CompoundTag nbt) {
        try {
            // 验证必需字段
            if (!nbt.contains("uuid") || !nbt.contains("url")) {
                return null;
            }

            SignPictureData data = new SignPictureData();

            // 版本和标识
            data.version = nbt.getInt("version");
            data.uuid = nbt.getString("uuid");

            // 图片URL
            data.url = nbt.getString("url");

            // 渲染属性（带范围限制）
            data.sizeWidth = clamp(nbt.getFloat("sizeWidth"), 0.01f, 10.0f);
            data.sizeHeight = clamp(nbt.getFloat("sizeHeight"), 0.01f, 10.0f);
            data.rotationX = nbt.getFloat("rotationX");
            data.rotationY = nbt.getFloat("rotationY");
            data.rotationZ = nbt.getFloat("rotationZ");
            data.offsetX = nbt.getFloat("offsetX");
            data.offsetY = nbt.getFloat("offsetY");
            data.offsetZ = nbt.getFloat("offsetZ");

            // 元数据
            data.createdTime = nbt.getLong("createdTime");
            data.lastModified = nbt.getLong("lastModified");

            return data;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 限制值在范围内
     */
    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * 更新修改时间
     */
    public void touch() {
        this.lastModified = System.currentTimeMillis();
    }

    /**
     * 创建副本
     */
    @Nonnull
    public SignPictureData copy() {
        SignPictureData copy = new SignPictureData();
        copy.uuid = this.uuid;
        copy.url = this.url;
        copy.sizeWidth = this.sizeWidth;
        copy.sizeHeight = this.sizeHeight;
        copy.rotationX = this.rotationX;
        copy.rotationY = this.rotationY;
        copy.rotationZ = this.rotationZ;
        copy.offsetX = this.offsetX;
        copy.offsetY = this.offsetY;
        copy.offsetZ = this.offsetZ;
        copy.createdTime = this.createdTime;
        copy.lastModified = this.lastModified;
        copy.version = this.version;
        return copy;
    }

    // ========== Getters and Setters ==========

    @Nonnull
    public String getUuid() {
        return uuid;
    }

    public void setUuid(@Nonnull String uuid) {
        this.uuid = uuid;
    }

    @Nonnull
    public String getUrl() {
        return url;
    }

    public void setUrl(@Nonnull String url) {
        this.url = url;
        touch();
    }

    public float getSizeWidth() {
        return sizeWidth;
    }

    public void setSizeWidth(float sizeWidth) {
        this.sizeWidth = clamp(sizeWidth, 0.01f, 10.0f);
        touch();
    }

    public float getSizeHeight() {
        return sizeHeight;
    }

    public void setSizeHeight(float sizeHeight) {
        this.sizeHeight = clamp(sizeHeight, 0.01f, 10.0f);
        touch();
    }

    public void setSize(float width, float height) {
        this.sizeWidth = clamp(width, 0.01f, 10.0f);
        this.sizeHeight = clamp(height, 0.01f, 10.0f);
        touch();
    }

    public float getRotationX() {
        return rotationX;
    }

    public void setRotationX(float rotationX) {
        this.rotationX = rotationX;
        touch();
    }

    public float getRotationY() {
        return rotationY;
    }

    public void setRotationY(float rotationY) {
        this.rotationY = rotationY;
        touch();
    }

    public float getRotationZ() {
        return rotationZ;
    }

    public void setRotationZ(float rotationZ) {
        this.rotationZ = rotationZ;
        touch();
    }

    public void setRotation(float x, float y, float z) {
        this.rotationX = x;
        this.rotationY = y;
        this.rotationZ = z;
        touch();
    }

    public float getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
        touch();
    }

    public float getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
        touch();
    }

    public float getOffsetZ() {
        return offsetZ;
    }

    public void setOffsetZ(float offsetZ) {
        this.offsetZ = offsetZ;
        touch();
    }

    public void setOffset(float x, float y, float z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
        touch();
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public long getLastModified() {
        return lastModified;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return String.format("SignPictureData[uuid=%s, url=%s, size=(%.2f,%.2f), rotation=(%.1f,%.1f,%.1f), offset=(%.2f,%.2f,%.2f)]",
            uuid, url, sizeWidth, sizeHeight, rotationX, rotationY, rotationZ, offsetX, offsetY, offsetZ);
    }
}
