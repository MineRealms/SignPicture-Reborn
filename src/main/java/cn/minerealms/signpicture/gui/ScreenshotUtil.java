package cn.minerealms.signpicture.gui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 截图工具类
 * 提供游戏内截图功能
 */
public class ScreenshotUtil {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

    /**
     * 截取当前屏幕
     *
     * @return 截图的BufferedImage
     */
    @Nonnull
    public static BufferedImage takeScreenshot() {
        Minecraft mc = Minecraft.getInstance();
        int width = mc.getWindow().getWidth();
        int height = mc.getWindow().getHeight();

        // 读取帧缓冲
        NativeImage nativeImage = Screenshot.takeScreenshot(mc.getMainRenderTarget());

        // 转换为BufferedImage
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = nativeImage.getPixelRGBA(x, y);
                bufferedImage.setRGB(x, y, pixel);
            }
        }

        nativeImage.close();
        return bufferedImage;
    }

    /**
     * 截取屏幕并保存到文件
     *
     * @param directory 保存目录
     * @return 保存的文件
     */
    @Nonnull
    public static File takeAndSaveScreenshot(@Nonnull File directory) throws IOException {
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = "screenshot_" + DATE_FORMAT.format(new Date()) + ".png";
        File file = new File(directory, fileName);

        Minecraft mc = Minecraft.getInstance();
        Screenshot.grab(directory, fileName, mc.getMainRenderTarget(),
                (component) -> {
                    // 截图完成回调
                });

        return file;
    }

    /**
     * 截取指定区域
     *
     * @param x      起始X坐标
     * @param y      起始Y坐标
     * @param width  宽度
     * @param height 高度
     * @return 截图的BufferedImage
     */
    @Nonnull
    public static BufferedImage takeRegionScreenshot(int x, int y, int width, int height) {
        BufferedImage fullScreenshot = takeScreenshot();

        // 裁剪指定区域
        int x1 = Math.max(0, x);
        int y1 = Math.max(0, y);
        int x2 = Math.min(fullScreenshot.getWidth(), x + width);
        int y2 = Math.min(fullScreenshot.getHeight(), y + height);

        return fullScreenshot.getSubimage(x1, y1, x2 - x1, y2 - y1);
    }
}
