package cn.minerealms.signpicture.network;

import cn.minerealms.signpicture.ModConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * 网络包管理器
 */
public class NetworkHandler {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ModConstants.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        INSTANCE.registerMessage(
                id(),
                UpdateSignPacket.class,
                UpdateSignPacket::encode,
                UpdateSignPacket::decode,
                UpdateSignPacket::handle
        );
    }
}
