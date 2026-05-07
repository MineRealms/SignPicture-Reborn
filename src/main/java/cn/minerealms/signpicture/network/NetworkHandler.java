package cn.minerealms.signpicture.network;

import cn.minerealms.signpicture.ModConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * 网络包管理器
 */
public class NetworkHandler {

    private static final String PROTOCOL_VERSION = "2";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.tryParse(ModConstants.MOD_ID + ":main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        // 旧的UpdateSignPacket（保留兼容性）
        INSTANCE.registerMessage(
                id(),
                UpdateSignPacket.class,
                UpdateSignPacket::encode,
                UpdateSignPacket::decode,
                UpdateSignPacket::handle
        );

        // 新的SignPicture网络包
        INSTANCE.messageBuilder(CreateSignPicturePacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(CreateSignPicturePacket::encode)
                .decoder(CreateSignPicturePacket::decode)
                .consumerMainThread(CreateSignPicturePacket::handle)
                .add();

        INSTANCE.messageBuilder(SyncSignPicturePacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncSignPicturePacket::encode)
                .decoder(SyncSignPicturePacket::decode)
                .consumerMainThread(SyncSignPicturePacket::handle)
                .add();

        INSTANCE.messageBuilder(RequestSignPicturePacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(RequestSignPicturePacket::encode)
                .decoder(RequestSignPicturePacket::decode)
                .consumerMainThread(RequestSignPicturePacket::handle)
                .add();

        INSTANCE.messageBuilder(ResponseSignPicturePacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ResponseSignPicturePacket::encode)
                .decoder(ResponseSignPicturePacket::decode)
                .consumerMainThread(ResponseSignPicturePacket::handle)
                .add();

        INSTANCE.messageBuilder(UpdateSignPicturePacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(UpdateSignPicturePacket::encode)
                .decoder(UpdateSignPicturePacket::decode)
                .consumerMainThread(UpdateSignPicturePacket::handle)
                .add();

        INSTANCE.messageBuilder(DeleteSignPicturePacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(DeleteSignPicturePacket::encode)
                .decoder(DeleteSignPicturePacket::decode)
                .consumerMainThread(DeleteSignPicturePacket::handleClient)
                .add();
    }

    /**
     * 发送包到服务端
     */
    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    /**
     * 发送包到指定客户端
     */
    public static <MSG> void sendToClient(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    /**
     * 广播包给所有客户端
     */
    public static <MSG> void sendToAllClients(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }
}

