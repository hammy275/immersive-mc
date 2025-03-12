package com.hammy275.immersivemc.server.command;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.network.packet.ConfigSyncPacket;
import com.hammy275.immersivemc.server.storage.world.ImmersiveMCPlayerStorages;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class ImmersiveMCCommand {

    public static void createCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("immersivemc")
                        .requires(source -> source.hasPermission(2))
                        .then(
                                Commands.literal("enable")
                                        .then(
                                                Commands.argument("players", EntityArgument.players())
                                                        .executes(context ->
                                                                enableDisable(context.getSource(), EntityArgument.getPlayers(context, "players"), true))
                                        )
                        )
                        .then(
                                Commands.literal("disable")
                                        .then(
                                                Commands.argument("players", EntityArgument.players())
                                                        .executes(context ->
                                                                enableDisable(context.getSource(), EntityArgument.getPlayers(context, "players"), false))
                                        )
                        )
        );
    }

    private static int enableDisable(CommandSourceStack source, Collection<ServerPlayer> targets, boolean nowEnabled) {
        for (ServerPlayer player : targets) {
            if (nowEnabled) {
                ImmersiveMCPlayerStorages.setPlayerEnabled(player);
            } else {
                ImmersiveMCPlayerStorages.setPlayerDisabled(player);
            }
            ConfigSyncPacket.syncConfigToPlayer(player);
        }
        source.sendSuccess(new TranslatableComponent("commands." + ImmersiveMC.MOD_ID + ".enable_disable." + nowEnabled), true);
        return targets.size();
    }
}
