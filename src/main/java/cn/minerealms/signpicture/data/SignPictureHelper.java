package cn.minerealms.signpicture.data;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * SignPicture工具类
 * 处理告示牌的读写操作
 */
public class SignPictureHelper {

    private static final String MARKER = "[SignPicture]";
    private static final String UUID_PREFIX = "#";

    /**
     * 检查告示牌是否是SignPicture
     */
    public static boolean isSignPicture(@Nonnull SignBlockEntity sign) {
        try {
            SignText frontText = sign.getFrontText();
            String line1 = frontText.getMessage(0, false).getString().trim();
            return MARKER.equals(line1);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从告示牌提取UUID
     * @return UUID，如果不是SignPicture返回null
     */
    @Nullable
    public static String getUUID(@Nonnull SignBlockEntity sign) {
        try {
            if (!isSignPicture(sign)) {
                return null;
            }

            SignText frontText = sign.getFrontText();
            String line2 = frontText.getMessage(1, false).getString().trim();

            // 移除#前缀
            if (line2.startsWith(UUID_PREFIX)) {
                return line2.substring(UUID_PREFIX.length());
            }

            return line2.isEmpty() ? null : line2;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 设置告示牌为SignPicture
     * @param sign 告示牌实体
     * @param uuid UUID
     */
    public static void setSignPicture(@Nonnull SignBlockEntity sign, @Nonnull String uuid) {
        try {
            // 构建文本
            String[] lines = formatSignText(uuid);

            // 更新告示牌文本
            sign.updateText(signText -> {
                SignText newText = signText;
                for (int i = 0; i < 4; i++) {
                    newText = newText.setMessage(i, Component.literal(lines[i]));
                }
                return newText;
            }, true); // true = 正面

            sign.setChanged();

        } catch (Exception e) {
            throw new RuntimeException("Failed to set SignPicture on sign", e);
        }
    }

    /**
     * 格式化告示牌文本
     * @param uuid UUID
     * @return 4行文本
     */
    @Nonnull
    public static String[] formatSignText(@Nonnull String uuid) {
        return new String[]{
            MARKER,           // 行1: [SignPicture]
            UUID_PREFIX + uuid,  // 行2: #a3f9c2
            "",               // 行3: 空
            ""                // 行4: 空
        };
    }

    /**
     * 清除告示牌的SignPicture标记
     * @param sign 告示牌实体
     */
    public static void clearSignPicture(@Nonnull SignBlockEntity sign) {
        try {
            sign.updateText(signText -> {
                SignText newText = signText;
                for (int i = 0; i < 4; i++) {
                    newText = newText.setMessage(i, Component.literal(""));
                }
                return newText;
            }, true);

            sign.setChanged();

        } catch (Exception e) {
            throw new RuntimeException("Failed to clear SignPicture on sign", e);
        }
    }

    /**
     * 从旧格式（文本URL）迁移到新格式（UUID）
     * @param sign 告示牌实体
     * @return 新的UUID，如果迁移失败返回null
     */
    @Nullable
    public static String migrateFromOldFormat(@Nonnull SignBlockEntity sign) {
        try {
            // 检查是否已经是新格式
            if (isSignPicture(sign)) {
                return getUUID(sign);
            }

            // 提取旧格式的URL
            String url = extractOldFormatUrl(sign);
            if (url == null || url.isEmpty()) {
                return null;
            }

            // 创建新数据
            String uuid = SignPictureDataManager.INSTANCE.create(url);

            // 更新告示牌为新格式
            setSignPicture(sign, uuid);

            return uuid;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从旧格式告示牌提取URL
     */
    @Nullable
    private static String extractOldFormatUrl(@Nonnull SignBlockEntity sign) {
        try {
            SignText frontText = sign.getFrontText();
            StringBuilder url = new StringBuilder();

            for (int i = 0; i < 4; i++) {
                String line = frontText.getMessage(i, false).getString().trim();
                if (!line.isEmpty()) {
                    url.append(line);
                }
            }

            String result = url.toString();

            // 验证是否是有效URL
            if (result.startsWith("http://") || result.startsWith("https://") || result.contains(".")) {
                return result;
            }

            return null;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取SignPicture数据（自动处理旧格式迁移）
     * @param sign 告示牌实体
     * @return 数据对象，如果不存在返回null
     */
    @Nullable
    public static SignPictureData getData(@Nonnull SignBlockEntity sign) {
        // 尝试获取UUID
        String uuid = getUUID(sign);

        // 如果是新格式，直接获取数据
        if (uuid != null) {
            return SignPictureDataManager.INSTANCE.get(uuid);
        }

        // 尝试从旧格式迁移
        uuid = migrateFromOldFormat(sign);
        if (uuid != null) {
            return SignPictureDataManager.INSTANCE.get(uuid);
        }

        return null;
    }
}
