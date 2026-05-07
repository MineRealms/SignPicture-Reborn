package cn.minerealms.signpicture.network;

import cn.minerealms.signpicture.Log;
import cn.minerealms.signpicture.data.SignPictureData;
import cn.minerealms.signpicture.data.SignPictureDataManagerServer;
import cn.minerealms.signpicture.data.SignPictureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 创建SignPicture数据包（客户端 → 服务端）
 *
 * 流程：
 * 1. 客户端在GUI中创建SignPicture
 * 2. 发送此包到服务端
 * 3. 服务端生成UUID，保存数据
 * 4. 服务端更新告示牌
 * 5. 服务端广播给所有客户端
 */
public class CreateSignPicturePacket {

    private final BlockPos pos;
    private final String url;
    private final float sizeWidth;
    private final float sizeHeight;
    private final float rotationX;
    private final float rotationY;
    private final float rotationZ;
    private final float offsetX;
    private final float offsetY;
    private final float offsetZ;

    public CreateSignPicturePacket(BlockPos pos, String url,
                                   float sizeWidth, float sizeHeight,
                                   float rotationX, float rotationY, float rotationZ,
                                   float offsetX, float offsetY, float offsetZ) {
        this.pos = pos;
        this.url = url;
        this.sizeWidth = sizeWidth;
        this.sizeHeight = sizeHeight;
        this.rotationX = rotationX;
        this.rotationY = rotationY;
        this.rotationZ = rotationZ;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
    }

    public static void encode(CreateSignPicturePacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
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

    public static CreateSignPicturePacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        String url = buf.readUtf(1000);
        float sizeWidth = buf.readFloat();
        float sizeHeight = buf.readFloat();
        float rotationX = buf.readFloat();
        float rotationY = buf.readFloat();
        float rotationZ = buf.readFloat();
        float offsetX = buf.readFloat();
        float offsetY = buf.readFloat();
        float offsetZ = buf.readFloat();

        return new CreateSignPicturePacket(pos, url,
            sizeWidth, sizeHeight,
            rotationX, rotationY, rotationZ,
            offsetX, offsetY, offsetZ);
    }

    public static void handle(CreateSignPicturePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }

            try {
                // 1. 验证URL
                if (!validateUrl(packet.url)) {
                    Log.error("[Server] Invalid URL: " + packet.url);
                    return;
                }

                // 2. 生成UUID
                String uuid = SignPictureDataManagerServer.INSTANCE.generateUUID();

                // 3. 创建数据
                SignPictureData data = new SignPictureData(uuid, packet.url);
                data.setSize(packet.sizeWidth, packet.sizeHeight);
                data.setRotation(packet.rotationX, packet.rotationY, packet.rotationZ);
                data.setOffset(packet.offsetX, packet.offsetY, packet.offsetZ);

                // 4. 保存到服务端
                SignPictureDataManagerServer.INSTANCE.create(uuid, data);

                // 5. 更新告示牌
                BlockEntity be = player.level().getBlockEntity(packet.pos);
                if (be instanceof SignBlockEntity sign) {
                    SignPictureHelper.setSignPicture(sign, uuid);
                    sign.setChanged();
                    player.level().sendBlockUpdated(packet.pos, sign.getBlockState(), sign.getBlockState(), 3);
                }

                // 6. 广播给所有客户端
                SyncSignPicturePacket syncPacket = new SyncSignPicturePacket(data);
                NetworkHandler.sendToAllClients(syncPacket);

                Log.info("[Server] Created SignPicture: " + uuid + " at " + packet.pos);

            } catch (Exception e) {
                Log.error("[Server] Failed to create SignPicture", e);
            }
        });

        ctx.get().setPacketHandled(true);
    }

    private static boolean validateUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        return url.startsWith("http://") || url.startsWith("https://");
    }
}
