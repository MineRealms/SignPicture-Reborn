package cn.minerealms.signpicture.data;

import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SignPicture权限管理器（服务端）
 *
 * 权限规则：
 * 1. 创建者可以编辑自己的SignPicture
 * 2. OP可以使用 /signpicture edit on 开启编辑所有人的权限
 * 3. /signpicture edit off 关闭特权
 */
public class SignPicturePermission {

    public static final SignPicturePermission INSTANCE = new SignPicturePermission();

    // 存储开启了编辑特权的OP玩家UUID
    private final Map<UUID, Boolean> opEditMode = new ConcurrentHashMap<>();

    private SignPicturePermission() {
    }

    /**
     * 检查玩家是否可以编辑指定的SignPicture
     *
     * @param player 玩家
     * @param data SignPicture数据
     * @return true=可以编辑, false=无权限
     */
    public boolean canEdit(@Nonnull ServerPlayer player, @Nonnull SignPictureData data) {
        if (player == null || data == null) {
            return false;
        }

        String playerUUID = player.getStringUUID();
        String creatorUUID = data.getCreatorUUID();

        // 1. 创建者可以编辑
        if (creatorUUID != null && creatorUUID.equals(playerUUID)) {
            return true;
        }

        // 2. OP开启了编辑模式
        if (player.hasPermissions(2)) { // OP level 2+
            UUID uuid = player.getUUID();
            Boolean editMode = opEditMode.get(uuid);
            if (editMode != null && editMode) {
                return true;
            }
        }

        return false;
    }

    /**
     * 设置OP编辑模式
     *
     * @param player OP玩家
     * @param enabled true=开启, false=关闭
     */
    public void setOpEditMode(@Nonnull ServerPlayer player, boolean enabled) {
        if (player == null) {
            return;
        }

        UUID uuid = player.getUUID();
        if (enabled) {
            opEditMode.put(uuid, true);
        } else {
            opEditMode.remove(uuid);
        }
    }

    /**
     * 获取OP编辑模式状态
     *
     * @param player OP玩家
     * @return true=已开启, false=未开启
     */
    public boolean isOpEditMode(@Nonnull ServerPlayer player) {
        if (player == null) {
            return false;
        }

        UUID uuid = player.getUUID();
        Boolean editMode = opEditMode.get(uuid);
        return editMode != null && editMode;
    }

    /**
     * 清理玩家退出时的数据
     *
     * @param playerUUID 玩家UUID
     */
    public void cleanupPlayer(@Nonnull UUID playerUUID) {
        opEditMode.remove(playerUUID);
    }

    /**
     * 清理所有数据（服务器关闭时）
     */
    public void clear() {
        opEditMode.clear();
    }
}
