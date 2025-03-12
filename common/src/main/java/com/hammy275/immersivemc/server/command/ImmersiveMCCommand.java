package com.hammy275.immersivemc.server.command;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.network.packet.ConfigSyncPacket;
import com.hammy275.immersivemc.server.storage.world.ImmersiveMCPlayerStorages;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ImmersiveMCCommand {

    public static void createCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("immersivemc")
                        .requires(source -> source.hasPermission(2))
                        .then(
                                Commands.literal("enable")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes(context ->
                                                                enableDisable(context.getSource(), EntityArgument.getPlayer(context, "player"), true))
                                        )
                        )
                        .then(
                                Commands.literal("disable")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes(context ->
                                                                enableDisable(context.getSource(), EntityArgument.getPlayer(context, "player"), false))
                                        )
                        )
        );
    }

    private static int enableDisable(CommandSourceStack source, ServerPlayer player, boolean nowEnabled) {
        boolean currentlyEnabled = !ImmersiveMCPlayerStorages.isPlayerDisabled(player);
        if (currentlyEnabled == nowEnabled) {
            source.sendFailure(Component.translatableWithFallback("commands." + ImmersiveMC.MOD_ID + ".enable_disable.already_" + nowEnabled,
                    "This player already has ImmersiveMC %s!".formatted(nowEnabled ? "enabled" : "disabled")));
            return 0;
        }
        if (nowEnabled) {
            ImmersiveMCPlayerStorages.setPlayerEnabled(player);
        } else {
            ImmersiveMCPlayerStorages.setPlayerDisabled(player);
        }
        ConfigSyncPacket.syncConfigToPlayer(player);
        source.sendSuccess(() -> Component.translatableWithFallback("commands." + ImmersiveMC.MOD_ID + ".enable_disable." + nowEnabled,
                "ImmersiveMC now %s for the provided player!".formatted(nowEnabled ? "enabled" : "disabled")), true);
        return 1;
    }
}
