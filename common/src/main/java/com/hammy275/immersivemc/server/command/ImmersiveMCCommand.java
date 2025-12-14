package com.hammy275.immersivemc.server.command;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.network.packet.ConfigSyncPacket;
import com.hammy275.immersivemc.server.storage.world.ImmersiveMCPlayerStorages;
import com.hammy275.immersivemc.test.Tests;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

public class ImmersiveMCCommand {

    public static void createCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("immersivemc")
                        .requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS)))
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
                        .then(
                                Commands.literal("test")
                                        .executes(context -> beginTesting(context.getSource()))
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

    private static int beginTesting(CommandSourceStack source) {
        if (!source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.OWNERS))) {
            source.sendFailure(Component.translatable("commands." + ImmersiveMC.MOD_ID + ".test.no_permission"));
            return 0;
        } else if (!source.isPlayer()) {
            source.sendFailure(Component.translatable("commands." + ImmersiveMC.MOD_ID + ".test.not_a_player"));
            return 0;
        }
        new Tests(source.getPlayer()).runTests();
        return 1;
    }
}
