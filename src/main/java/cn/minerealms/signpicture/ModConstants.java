package cn.minerealms.signpicture;

import javax.annotation.Nonnull;

/**
 * Mod常量定义
 */
public class ModConstants {
    public static final @Nonnull String MOD_ID = "signpicture";
    public static final @Nonnull String MOD_NAME = "SignPicture-Rebornified";
    public static final @Nonnull String VERSION = "${mod_version}";
    
    // 资源路径
    public static final @Nonnull String ASSETS_PATH = "assets/" + MOD_ID + "/";
    public static final @Nonnull String TEXTURES_PATH = ASSETS_PATH + "textures/";
    public static final @Nonnull String LANG_PATH = ASSETS_PATH + "lang/";
    
    // 配置
    public static final @Nonnull String CONFIG_NAME = MOD_ID + ".toml";
}
