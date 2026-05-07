package cn.minerealms.signpicture.network;

import cn.minerealms.signpicture.Log;
import cn.minerealms.signpicture.data.SignPictureData;
import cn.minerealms.signpicture.data.SignPictureDataManagerClient;
import cn.minerealms.signpicture.entry.content.ContentId;
import cn.minerealms.signpicture.entry.content.ContentManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 响应SignPicture数据包（服务端 → 客户端）
 *
 * 流程：
 * 1. 服务端响应RequestSignPicturePacket
 * 2. 客户端接收并保存元数据
 * 3. 客户端触发图片下载
 */
public class ResponseSignPicturePacket {

    private final String uuid;
    private final String url;
    private final float sizeWidth;
    private final float sizeHeight;
    private final float rotationX;
    private final float rotationY;
    private final float rotationZ;
    private final float offsetX;
    private final float offsetY;
    private final float offsetZ;

    public ResponseSignPicturePacket(SignPictureData data) {
        this.uuid = data.getUuid();
        this.url = data.getUrl();
        this.sizeWidth = data.getSizeWidth();
        this.sizeHeight = data.getSizeHeight();
        this.rotationX = data.getRotationX();
        this.rotationY = data.getRotationY();
        this.rotationZ = data.getRotationZ();
        this.offsetX = data.getOffsetX();
        this.offsetY = data.getOffsetY();
        this.offsetZ = data.getOffsetZ();
    }

    public static void encode(ResponseSignPicturePacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.uuid, 10);
        buf.writeUtf(packet.url, 1000);
        buf.writeFloat(packet.sizeWidth);
        buf.writeFloat(packet.sizeHeight);
        buf.writeFloat(packet.rotationX);
        buf.writeFloat(packet.rotationY);
        buf.writeFloat(packet.rotationZ);
        buf.writeFloat(packet.offsetX);
        buf.writeFloat(packet.offsetY);
        buf.writeFloat(packet.offsetZ);
    }

    public static ResponseSignPicturePacket decode(FriendlyByteBuf buf) {
        String uuid = buf.readUtf(10);
        String url = buf.readUtf(1000);
        float sizeWidth = buf.readFloat();
        float sizeHeight = buf.readFloat();
        float rotationX = buf.readFloat();
        float rotationY = buf.readFloat();
        float rotationZ = buf.readFloat();
        float offsetX = buf.readFloat();
        float offsetY = buf.readFloat();
        float offsetZ = buf.readFloat();

        SignPictureData data = new SignPictureData(uuid, url);
        data.setSize(sizeWidth, sizeHeight);
        data.setRotation(rotationX, rotationY, rotationZ);
        data.setOffset(offsetX, offsetY, offsetZ);

        return new ResponseSignPicturePacket(data);
    }

    public static void handle(ResponseSignPicturePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 只在客户端执行
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(packet));
        });

        ctx.get().setPacketHandled(true);
    }

    private static void handleClient(ResponseSignPicturePacket packet) {
        try {
            // 1. 重建数据对象
            SignPictureData data = new SignPictureData(packet.uuid, packet.url);
            data.setSize(packet.sizeWidth, packet.sizeHeight);
            data.setRotation(packet.rotationX, packet.rotationY, packet.rotationZ);
            data.setOffset(packet.offsetX, packet.offsetY, packet.offsetZ);

            // 2. 保存元数据到客户端本地缓存
            SignPictureDataManagerClient.INSTANCE.saveMetadata(packet.uuid, data);

            // 3. 触发图片下载
            ContentId contentId = ContentId.from(packet.url);
            ContentManager.instance.get(contentId);

            Log.info("[Client] Received SignPicture data: " + packet.uuid);

        } catch (Exception e) {
            Log.error("[Client] Failed to handle response", e);
        }
    }
}
