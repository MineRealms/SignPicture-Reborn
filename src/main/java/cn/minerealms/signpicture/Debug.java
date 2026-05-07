package cn.minerealms.signpicture;

import javax.annotation.Nonnull;

/**
 * 调试工具类
 * 包含各种调试和测试方法
 */
public class Debug {
    
    /**
     * 检查Entry ID是否可以放置在告示牌上
     * 告示牌有4行，每行最多15个字符
     */
    public static boolean isPlaceable(final @Nonnull String entryId) {
        return entryId.length() <= 15 * 4;
    }
    
    /**
     * 将长ID字符串分割成告示牌的4行
     */
    public static void toStrings(final @Nonnull String[] sign, final @Nonnull String id) {
        final int length = id.length();
        for (int i = 0; i < 4; i++) {
            int start = 15 * i;
            int end = Math.min(15 * (i + 1), length);
            sign[i] = id.substring(start, end);
        }
    }
    
    /**
     * 将InputStream转换为String
     */
    public static String convertStreamToString(final @Nonnull java.io.InputStream is) {
        final java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
