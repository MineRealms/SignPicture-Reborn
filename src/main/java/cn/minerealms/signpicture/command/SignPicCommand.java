package cn.minerealms.signpicture.command;

import cn.minerealms.signpicture.ModConstants;
import cn.minerealms.signpicture.util.ChatBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * SignPicture命令系统
 * /signpic 命令
 */
public class SignPicCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("signpic")
                .then(Commands.literal("version")
                    .executes(SignPicCommand::version))
                .then(Commands.literal("reload")
                    .requires(source -> source.hasPermission(2))
                    .executes(SignPicCommand::reload))
                .then(Commands.literal("clear")
                    .requires(source -> source.hasPermission(2))
                    .executes(SignPicCommand::clear))
                .executes(SignPicCommand::help)
        );
    }
    
    private static int help(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> 
            Component.literal("§6SignPicture Commands:"), false);
        context.getSource().sendSuccess(() -> 
            Component.literal("§e/signpic version §7- Show mod version"), false);
        context.getSource().sendSuccess(() -> 
            Component.literal("§e/signpic reload §7- Reload configuration"), false);
        context.getSource().sendSuccess(() -> 
            Component.literal("§e/signpic clear §7- Clear image cache"), false);
        return 1;
    }
    
    private static int version(CommandContext<CommandSourceStack> context) {
        ChatBuilder.create("§6" + ModConstants.MOD_NAME + " §7v" + ModConstants.VERSION)
            .sendPlayer(context.getSource());
        return 1;
    }
    
    private static int reload(CommandContext<CommandSourceStack> context) {
        // TODO: 重新加载配置
        context.getSource().sendSuccess(() -> 
            Component.literal("§aConfiguration reloaded!"), true);
        return 1;
    }
    
    private static int clear(CommandContext<CommandSourceStack> context) {
        // TODO: 清除缓存
        context.getSource().sendSuccess(() -> 
            Component.literal("§aImage cache cleared!"), true);
        return 1;
    }
}
