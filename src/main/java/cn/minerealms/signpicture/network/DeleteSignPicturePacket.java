package cn.minerealms.signpicture.network;

import cn.minerealms.signpicture.Log;
import cn.minerealms.signpicture.data.SignPictureDataManagerClient;
import cn.minerealms.signpicture.data.SignPictureDataManagerServer;
import cn.minerealms.signpicture.render.SignHandlerV2;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 删除SignPicture数据包（双向）
 *
 * 客户端 → 服务端：
 * 1. 客户端破坏告示牌
 * 2. 发送此包到服务端
 * 3. 服务端删除数据
 * 4. 服务端广播给所有客户端
 *
 * 服务端 → 客户端：
 * 1. 服务端广播删除
 * 2. 客户端清理缓存和纹理
 */
public class DeleteSignPicturePacket {

    private final String uuid;

    public DeleteSignPicturePacket(String uuid) {
        this.uuid = uuid;
    }

    public static void encode(DeleteSignPicturePacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.uuid, 10);
    }

    public static DeleteSignPicturePacket decode(FriendlyByteBuf buf) {
        String uuid = buf.readUtf(10);
        return new DeleteSignPicturePacket(uuid);
    }

    /**
     * 服务端处理（接收客户端的删除请求）
     */
    public static void handleServer(DeleteSignPicturePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }

            try {
                // 1. 删除服务端数据
                SignPictureDataManagerServer.INSTANCE.delete(packet.uuid);

                // 2. 广播给所有客户端
                NetworkHandler.sendToAllClients(packet);

                Log.info("[Server] Deleted SignPicture: " + packet.uuid);

            } catch (Exception e) {
                Log.error("[Server] Failed to delete SignPicture: " + packet.uuid, e);
            }
        });

        ctx.get().setPacketHandled(true);
    }

    /**
     * 客户端处理（接收服务端的删除广播）
     */
    public static void handleClient(DeleteSignPicturePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 只在客户端执行
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClientInternal(packet));
        });

        ctx.get().setPacketHandled(true);
    }

    private static void handleClientInternal(DeleteSignPicturePacket packet) {
        try {
            // 1. 从客户端缓存删除（可选，保留也可以）
            // SignPictureDataManagerClient.INSTANCE.deleteMetadata(packet.uuid);

            // 2. 清理纹理
            SignHandlerV2.INSTANCE.releaseTexture(packet.uuid);

            Log.info("[Client] Deleted SignPicture: " + packet.uuid);

        } catch (Exception e) {
            Log.error("[Client] Failed to delete SignPicture: " + packet.uuid, e);
        }
    }
}
