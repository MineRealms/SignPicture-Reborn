package cn.minerealms.signpicture.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 更新告示牌文本的网络包
 */
public class UpdateSignPacket {

    private final BlockPos pos;
    private final String[] lines;
    private final boolean isFrontText;

    public UpdateSignPacket(BlockPos pos, String[] lines, boolean isFrontText) {
        this.pos = pos;
        this.lines = lines;
        this.isFrontText = isFrontText;
    }

    public static void encode(UpdateSignPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeBoolean(packet.isFrontText);
        for (int i = 0; i < 4; i++) {
            buf.writeUtf(packet.lines[i], 384);
        }
    }

    public static UpdateSignPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        boolean isFrontText = buf.readBoolean();
        String[] lines = new String[4];
        for (int i = 0; i < 4; i++) {
            lines[i] = buf.readUtf(384);
        }
        return new UpdateSignPacket(pos, lines, isFrontText);
    }

    public static void handle(UpdateSignPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            BlockEntity blockEntity = player.level().getBlockEntity(packet.pos);
            if (blockEntity instanceof SignBlockEntity sign) {
                // 更新告示牌文本
                // 使用反射访问私有方法
                try {
                    var textField = packet.isFrontText ?
                        SignBlockEntity.class.getDeclaredField("frontText") :
                        SignBlockEntity.class.getDeclaredField("backText");
                    textField.setAccessible(true);

                    var text = packet.isFrontText ? sign.getFrontText() : sign.getBackText();

                    for (int i = 0; i < 4; i++) {
                        text = text.setMessage(i, net.minecraft.network.chat.Component.literal(packet.lines[i]));
                    }

                    textField.set(sign, text);

                    sign.setChanged();
                    player.level().sendBlockUpdated(packet.pos, sign.getBlockState(), sign.getBlockState(), 3);
                } catch (Exception e) {
                    // 记录错误但不崩溃
                    e.printStackTrace();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
