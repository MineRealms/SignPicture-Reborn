package cn.minerealms.signpicture.network;

import cn.minerealms.signpicture.Log;
import cn.minerealms.signpicture.data.SignPictureData;
import cn.minerealms.signpicture.data.SignPictureDataManagerServer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 请求SignPicture数据包（客户端 → 服务端）
 *
 * 流程：
 * 1. 客户端渲染时发现没有元数据缓存
 * 2. 发送此包请求服务端
 * 3. 服务端响应ResponseSignPicturePacket
 */
public class RequestSignPicturePacket {

    private final String uuid;

    public RequestSignPicturePacket(String uuid) {
        this.uuid = uuid;
    }

    public static void encode(RequestSignPicturePacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.uuid, 10);
    }

    public static RequestSignPicturePacket decode(FriendlyByteBuf buf) {
        String uuid = buf.readUtf(10);
        return new RequestSignPicturePacket(uuid);
    }

    public static void handle(RequestSignPicturePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }

            try {
                // 从服务端加载数据
                SignPictureData data = SignPictureDataManagerServer.INSTANCE.get(packet.uuid);

                if (data != null) {
                    // 发送给请求的客户端
                    ResponseSignPicturePacket response = new ResponseSignPicturePacket(data);
                    NetworkHandler.sendToClient(response, player);

                    Log.info("[Server] Sent SignPicture data: " + packet.uuid + " to " + player.getName().getString());
                } else {
                    Log.warn("[Server] SignPicture not found: " + packet.uuid);
                }

            } catch (Exception e) {
                Log.error("[Server] Failed to handle request for: " + packet.uuid, e);
            }
        });

        ctx.get().setPacketHandled(true);
    }
}
