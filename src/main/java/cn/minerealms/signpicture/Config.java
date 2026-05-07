package cn.minerealms.signpicture;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * SignPicture配置系统
 * 使用Forge 1.20.1的ForgeConfigSpec
 */
public class Config {
    
    public static final ClientConfig CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;
    
    public static final CommonConfig COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;
    
    static {
        Pair<ClientConfig, ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT = clientPair.getLeft();
        CLIENT_SPEC = clientPair.getRight();
        
        Pair<CommonConfig, ForgeConfigSpec> commonPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
        COMMON = commonPair.getLeft();
        COMMON_SPEC = commonPair.getRight();
    }
    
    /**
     * 客户端配置
     */
    public static class ClientConfig {
        // === 渲染设置 ===
        public final ForgeConfigSpec.BooleanValue renderOverlayPanel;
        public final ForgeConfigSpec.BooleanValue renderGuiOverlay;
        public final ForgeConfigSpec.BooleanValue renderUseMipmap;
        public final ForgeConfigSpec.BooleanValue renderMipmapTypeNearest;
        public final ForgeConfigSpec.DoubleValue renderSeeOpacity;
        public final ForgeConfigSpec.DoubleValue renderPreviewFixedOpacity;
        public final ForgeConfigSpec.DoubleValue renderPreviewFloatedOpacity;
        
        // === 聊天图片设置 ===
        public final ForgeConfigSpec.BooleanValue chatpicEnable;
        public final ForgeConfigSpec.IntValue chatpicLine;
        public final ForgeConfigSpec.IntValue chatpicStackTick;
        
        // === 调试设置 ===
        public final ForgeConfigSpec.BooleanValue debugLog;
        
        public ClientConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Render Settings").push("render");
            
            renderOverlayPanel = builder
                    .comment("Overlay signpic!online")
                    .define("overlayPanel", true);
            
            renderGuiOverlay = builder
                    .comment("Overlay on GUI")
                    .define("guiOverlay", true);
            
            renderUseMipmap = builder
                    .comment("Use Mipmap (Requires OpenGL 3.0 or later)")
                    .define("useMipmap", true);
            
            renderMipmapTypeNearest = builder
                    .comment("Mipmap type: true = Nearest, false = Linear")
                    .define("mipmapTypeNearest", false);
            
            builder.pop();
            
            builder.comment("Render Opacity Settings").push("opacity");
            
            renderSeeOpacity = builder
                    .comment("Opacity when viewing sign")
                    .defineInRange("viewSign", 0.5, 0.0, 1.0);
            
            renderPreviewFixedOpacity = builder
                    .comment("Opacity for preview fixed sign")
                    .defineInRange("previewFixed", 0.7, 0.0, 1.0);
            
            renderPreviewFloatedOpacity = builder
                    .comment("Opacity for preview floated sign")
                    .defineInRange("previewFloated", 0.49, 0.0, 1.0);
            
            builder.pop();
            
            builder.comment("Chat Picture Settings").push("chatpicture");
            
            chatpicEnable = builder
                    .comment("Enable ChatPicture extension")
                    .define("enable", true);
            
            chatpicLine = builder
                    .comment("How many lines does image use")
                    .defineInRange("imageLines", 4, 1, 20);
            
            chatpicStackTick = builder
                    .comment("Stack chat lines within interval ticks")
                    .defineInRange("stackTicks", 50, 0, 200);
            
            builder.pop();
            
            builder.comment("Debug Settings").push("debug");
            
            debugLog = builder
                    .comment("Output debug log")
                    .define("debugLog", false);
            
            builder.pop();
        }
    }
    
    /**
     * 通用配置
     */
    public static class CommonConfig {
        // === 常规设置 ===
        public final ForgeConfigSpec.ConfigValue<String> signpicDir;
        public final ForgeConfigSpec.BooleanValue signTooltip;
        
        // === 图片设置 ===
        public final ForgeConfigSpec.IntValue imageWidthLimit;
        public final ForgeConfigSpec.IntValue imageHeightLimit;
        public final ForgeConfigSpec.BooleanValue imageResizeFast;
        public final ForgeConfigSpec.BooleanValue imageAnimationGif;
        
        // === Entry管理 ===
        public final ForgeConfigSpec.IntValue entryGCtick;
        
        // === HTTP设置 ===
        public final ForgeConfigSpec.IntValue communicateThreads;
        public final ForgeConfigSpec.IntValue communicateDLTimedout;
        
        // === Content管理 ===
        public final ForgeConfigSpec.IntValue contentLoadThreads;
        public final ForgeConfigSpec.IntValue contentMaxByte;
        public final ForgeConfigSpec.IntValue contentGCtick;
        public final ForgeConfigSpec.IntValue contentLoadTick;
        public final ForgeConfigSpec.IntValue contentSyncTick;
        public final ForgeConfigSpec.IntValue contentMaxRetry;
        
        // === 版本更新 ===
        public final ForgeConfigSpec.BooleanValue informationNotice;
        public final ForgeConfigSpec.BooleanValue informationJoinBeta;
        public final ForgeConfigSpec.BooleanValue informationUpdateGui;
        public final ForgeConfigSpec.BooleanValue informationTryNew;
        
        // === 多人游戏反作弊 ===
        public final ForgeConfigSpec.BooleanValue multiplayPAAS;
        public final ForgeConfigSpec.IntValue multiplayPAASMinEditTime;
        public final ForgeConfigSpec.IntValue multiplayPAASMinLineTime;
        public final ForgeConfigSpec.IntValue multiplayPAASMinCharTime;
        
        // === API设置 ===
        public final ForgeConfigSpec.ConfigValue<String> apiUploaderType;
        public final ForgeConfigSpec.ConfigValue<String> apiUploaderKey;
        public final ForgeConfigSpec.ConfigValue<String> apiShortenerType;
        public final ForgeConfigSpec.ConfigValue<String> apiShortenerKey;
        
        public CommonConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("General Settings").push("general");
            
            signpicDir = builder
                    .comment("SignPicture directory (empty = default)")
                    .define("signpicDir", "");
            
            signTooltip = builder
                    .comment("Add tooltip line to sign")
                    .define("signTooltip", false);
            
            builder.pop();
            
            builder.comment("Image Settings").push("image");
            
            imageWidthLimit = builder
                    .comment("Image width limit in pixels")
                    .defineInRange("widthLimit", 512, 1, 4096);
            
            imageHeightLimit = builder
                    .comment("Image height limit in pixels")
                    .defineInRange("heightLimit", 512, 1, 4096);
            
            imageResizeFast = builder
                    .comment("Use fast resize algorithm")
                    .define("fastResize", false);
            
            imageAnimationGif = builder
                    .comment("Animate GIF images")
                    .define("animateGif", true);
            
            builder.pop();
            
            builder.comment("Entry (sign text parse cache) Management").push("entry");
            
            entryGCtick = builder
                    .comment("Garbage collection delay in ticks")
                    .defineInRange("gcDelayTick", 15 * 20, 0, Integer.MAX_VALUE);
            
            builder.pop();
            
            builder.comment("HTTP Settings").push("http");
            
            communicateThreads = builder
                    .comment("Parallel processing number for downloading")
                    .defineInRange("httpThreads", 3, 1, 32);
            
            communicateDLTimedout = builder
                    .comment("Download timeout in milliseconds (0 = infinity)")
                    .defineInRange("downloadTimeout", 15000, 0, 300000);
            
            builder.pop();
            
            builder.comment("Content Data Management").push("content");
            
            contentLoadThreads = builder
                    .comment("Parallel processing number for image loading")
                    .defineInRange("loadThreads", 3, 1, 32);
            
            contentMaxByte = builder
                    .comment("Size limit before downloading in bytes (0 = infinity)")
                    .defineInRange("maxByte", 32 * 1024 * 1024, 0, Integer.MAX_VALUE);
            
            contentGCtick = builder
                    .comment("Garbage collection delay in ticks")
                    .defineInRange("gcDelayTick", 15 * 20, 0, Integer.MAX_VALUE);
            
            contentLoadTick = builder
                    .comment("Load process starting delay in ticks")
                    .defineInRange("loadStartIntervalTick", 0, 0, 100);
            
            contentSyncTick = builder
                    .comment("Sync process interval in ticks")
                    .defineInRange("syncLoadIntervalTick", 0, 0, 100);
            
            contentMaxRetry = builder
                    .comment("Retry count limit (0 = infinity)")
                    .defineInRange("maxRetry", 3, 0, 10);
            
            builder.pop();
            
            builder.comment("Version Update Settings").push("version");
            
            informationNotice = builder
                    .comment("Show update notifications")
                    .define("notice", true);
            
            informationJoinBeta = builder
                    .comment("Join beta channel")
                    .define("joinBeta", false);
            
            informationUpdateGui = builder
                    .comment("Show update GUI")
                    .define("updateGui", true);
            
            informationTryNew = builder
                    .comment("Try new features")
                    .define("tryNew", false);
            
            builder.pop();
            
            builder.comment("Prevent Anti-AutoSign Plugin (e.g., NoCheatPlus)").push("preventAntiAutoSign");
            
            multiplayPAAS = builder
                    .comment("Enable anti-auto-sign prevention")
                    .define("enable", true);
            
            multiplayPAASMinEditTime = builder
                    .comment("Minimum edit time in milliseconds")
                    .defineInRange("minEditTime", 150, 0, 10000);
            
            multiplayPAASMinLineTime = builder
                    .comment("Minimum time per line in milliseconds")
                    .defineInRange("minLineTime", 50, 0, 5000);
            
            multiplayPAASMinCharTime = builder
                    .comment("Minimum time per character in milliseconds")
                    .defineInRange("minCharTime", 50, 0, 1000);
            
            builder.pop();
            
            builder.comment("API Upload Settings").push("api");
            
            apiUploaderType = builder
                    .comment("Uploader type")
                    .define("uploaderType", "");
            
            apiUploaderKey = builder
                    .comment("Uploader API key")
                    .define("uploaderKey", "");
            
            apiShortenerType = builder
                    .comment("URL shortener type")
                    .define("shortenerType", "");
            
            apiShortenerKey = builder
                    .comment("URL shortener API key")
                    .define("shortenerKey", "");
            
            builder.pop();
        }
    }
}
