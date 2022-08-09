package net.blf02.immersivemc.server;

import net.blf02.immersivemc.common.config.ServerPlayerConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public class PlayerConfigs {

    public static final Map<String, ServerPlayerConfig> CONFIGS = new HashMap<>();

    public static void registerConfig(Player player, FriendlyByteBuf buffer) {
        CONFIGS.put(player.getGameProfile().getName(), new ServerPlayerConfig(buffer));
    }

    public static ServerPlayerConfig getConfig(Player player) {
        return CONFIGS.getOrDefault(player.getGameProfile().getName(), ServerPlayerConfig.EMPTY_CONFIG);
    }
}
