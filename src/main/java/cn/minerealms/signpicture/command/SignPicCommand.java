package cn.minerealms.signpicture.command;

import cn.minerealms.signpicture.Config;
import cn.minerealms.signpicture.Log;
import cn.minerealms.signpicture.ModConstants;
import cn.minerealms.signpicture.entry.EntryManager;
import cn.minerealms.signpicture.entry.content.ContentManager;
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
                .then(Commands.literal("debug")
                    .requires(source -> source.hasPermission(2))
                    .executes(SignPicCommand::toggleDebug))
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
        context.getSource().sendSuccess(() ->
            Component.literal("§e/signpic debug §7- Toggle debug logging"), false);
        return 1;
    }

    private static int version(CommandContext<CommandSourceStack> context) {
        ChatBuilder.create("§6" + ModConstants.MOD_NAME + " §7v" + ModConstants.VERSION)
            .sendPlayer(context.getSource());
        return 1;
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        try {
            Log.info("Reloading configuration...");

            // 重新加载配置文件
            Config.CLIENT_SPEC.save();
            Config.COMMON_SPEC.save();

            Log.info("Configuration reloaded successfully");
            context.getSource().sendSuccess(() ->
                Component.literal("§aConfiguration reloaded!"), true);
            return 1;
        } catch (Exception e) {
            Log.error("Failed to reload configuration", e);
            context.getSource().sendFailure(
                Component.literal("§cFailed to reload configuration: " + e.getMessage()));
            return 0;
        }
    }

    private static int clear(CommandContext<CommandSourceStack> context) {
        try {
            Log.info("Clearing image cache...");

            // 清除Entry缓存
            int entryCount = EntryManager.instance.clear();

            // 清除Content缓存
            int contentCount = ContentManager.instance.clear();

            // 强制GC
            System.gc();

            Log.info("Cache cleared: {} entries, {} contents", entryCount, contentCount);
            context.getSource().sendSuccess(() ->
                Component.literal(String.format("§aImage cache cleared! (%d entries, %d contents)",
                    entryCount, contentCount)), true);
            return 1;
        } catch (Exception e) {
            Log.error("Failed to clear cache", e);
            context.getSource().sendFailure(
                Component.literal("§cFailed to clear cache: " + e.getMessage()));
            return 0;
        }
    }

    private static int toggleDebug(CommandContext<CommandSourceStack> context) {
        boolean currentDebug = Config.CLIENT.debugLog.get();
        boolean newDebug = !currentDebug;

        Config.CLIENT.debugLog.set(newDebug);
        Config.CLIENT_SPEC.save();

        Log.info("Debug logging " + (newDebug ? "enabled" : "disabled"));
        context.getSource().sendSuccess(() ->
            Component.literal("§aDebug logging " + (newDebug ? "§2enabled" : "§cdisabled")), true);
        return 1;
    }
}
