package cn.minerealms.signpicture.network;

import cn.minerealms.signpicture.Log;
import cn.minerealms.signpicture.data.SignPictureData;
import cn.minerealms.signpicture.data.SignPictureDataManagerServer;
import cn.minerealms.signpicture.data.SignPicturePermission;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 更新SignPicture数据包（客户端 → 服务端）
 *
 * 流程：
 * 1. 客户端在GUI中修改属性
 * 2. 发送此包到服务端
 * 3. 服务端更新数据
 * 4. 服务端广播给所有客户端
 */
public class UpdateSignPicturePacket {

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

    public UpdateSignPicturePacket(SignPictureData data) {
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

    public static void encode(UpdateSignPicturePacket packet, FriendlyByteBuf buf) {
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

    public static UpdateSignPicturePacket decode(FriendlyByteBuf buf) {
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

        return new UpdateSignPicturePacket(data);
    }

    public static void handle(UpdateSignPicturePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }

            try {
                // 1. 获取现有数据检查权限
                SignPictureData existingData = SignPictureDataManagerServer.INSTANCE.get(packet.uuid);
                if (existingData == null) {
                    player.sendSystemMessage(Component.literal("§cSignPicture not found: " + packet.uuid));
                    return;
                }

                // 2. 权限检查
                if (!SignPicturePermission.INSTANCE.canEdit(player, existingData)) {
                    player.sendSystemMessage(Component.literal("§cYou don't have permission to edit this SignPicture"));
                    Log.warn("[Server] Player " + player.getName().getString() + " tried to edit SignPicture " + packet.uuid + " without permission");
                    return;
                }

                // 3. 重建数据对象（保留创建者UUID）
                SignPictureData data = new SignPictureData(packet.uuid, packet.url);
                data.setSize(packet.sizeWidth, packet.sizeHeight);
                data.setRotation(packet.rotationX, packet.rotationY, packet.rotationZ);
                data.setOffset(packet.offsetX, packet.offsetY, packet.offsetZ);
                data.setCreatorUUID(existingData.getCreatorUUID()); // 保留创建者

                // 4. 更新服务端数据
                SignPictureDataManagerServer.INSTANCE.update(packet.uuid, data);

                // 5. 广播给所有客户端
                SyncSignPicturePacket syncPacket = new SyncSignPicturePacket(data);
                NetworkHandler.sendToAllClients(syncPacket);

                Log.info("[Server] Updated SignPicture: " + packet.uuid + " by " + player.getName().getString());

            } catch (Exception e) {
                Log.error("[Server] Failed to update SignPicture: " + packet.uuid, e);
            }
        });

        ctx.get().setPacketHandled(true);
    }
}
