package net.blf02.immersivemc.common.config;

import net.blf02.immersivemc.ImmersiveMC;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.packet.ConfigSyncPacket;
import net.minecraft.network.PacketBuffer;

public class ActiveConfig {

    // Synced values
    public static boolean useAnvilImmersion = false;
    public static boolean useBrewingImmersion = false;
    public static boolean useChestImmersion = false;
    public static boolean useCraftingImmersion = false;
    public static boolean useFurnaceImmersion = false;
    public static boolean useJukeboxImmersion = false;
    public static boolean useRangedGrab = false;
    public static boolean useButton = false;
    public static boolean useETableImmersion = false;
    public static boolean useCampfireImmersion = false;
    public static boolean useLever = false;
    public static boolean useBackpack = false;
    public static boolean useEnderChestImmersion = false;

    // Non-synced values
    public static int backpackColor = 11901820;
    public static boolean leftHandedBackpack = false;

    public static void loadConfigFromPacket(PacketBuffer buffer) {
        int serverNetworkVersion = buffer.readInt();
        if (serverNetworkVersion != Network.PROTOCOL_VERSION) {
            Network.INSTANCE.sendToServer(ConfigSyncPacket.getKickMePacket());
        }
        int serverConfigVersion = buffer.readInt();
        if (serverConfigVersion != ImmersiveMCConfig.CONFIG_VERSION) { // Kick if we have a different config version
            Network.INSTANCE.sendToServer(ConfigSyncPacket.getKickMePacket());
        }
        // We combine client config with server, so if a user doesn't want to use an immersion, they don't
        // even if a server is OK with it.
        loadConfigFromFile();
        useAnvilImmersion = buffer.readBoolean() && useAnvilImmersion;
        useBrewingImmersion = buffer.readBoolean() && useBrewingImmersion;
        useChestImmersion = buffer.readBoolean() && useChestImmersion;
        useCraftingImmersion = buffer.readBoolean() && useCraftingImmersion;
        useFurnaceImmersion = buffer.readBoolean() && useFurnaceImmersion;
        useJukeboxImmersion = buffer.readBoolean() && useJukeboxImmersion;
        useRangedGrab = buffer.readBoolean() && useRangedGrab;
        useButton = buffer.readBoolean() && useButton;
        useETableImmersion = buffer.readBoolean() && useETableImmersion;
        useCampfireImmersion = buffer.readBoolean() && useCampfireImmersion;
        useLever = buffer.readBoolean() && useLever;
        useBackpack = buffer.readBoolean() && useBackpack;
        useEnderChestImmersion = buffer.readBoolean() && useEnderChestImmersion;
        ImmersiveMC.LOGGER.debug("Loaded config from network: \n" + asString());

    }

    public static void loadConfigFromFile() {
        // Synced values
        useAnvilImmersion = ImmersiveMCConfig.useAnvilImmersion.get();
        useBrewingImmersion = ImmersiveMCConfig.useBrewingImmersion.get();
        useChestImmersion = ImmersiveMCConfig.useChestImmersion.get();
        useCraftingImmersion = ImmersiveMCConfig.useCraftingImmersion.get();
        useFurnaceImmersion = ImmersiveMCConfig.useFurnaceImmersion.get();
        useJukeboxImmersion = ImmersiveMCConfig.useJukeboxImmersion.get();
        useRangedGrab = ImmersiveMCConfig.useRangedGrab.get();
        useButton = ImmersiveMCConfig.useButton.get();
        useETableImmersion = ImmersiveMCConfig.useETableImmersion.get();
        useCampfireImmersion = ImmersiveMCConfig.useCampfireImmersion.get();
        useLever = ImmersiveMCConfig.useLever.get();
        useBackpack = ImmersiveMCConfig.useBackpack.get();
        useEnderChestImmersion = ImmersiveMCConfig.useEnderChestImmersion.get();

        // Non-synced values
        backpackColor = ImmersiveMCConfig.backpackColor.get();
        leftHandedBackpack = ImmersiveMCConfig.leftHandedBackpack.get();
        ImmersiveMC.LOGGER.debug("Loaded config from file: \n" + asString());
    }

    public static void loadOffConfig() {
        useAnvilImmersion = false;
        useBrewingImmersion = false;
        useChestImmersion = false;
        useCraftingImmersion = false;
        useFurnaceImmersion = false;
        useJukeboxImmersion = false;
        useRangedGrab = false;
        useButton = false;
        useETableImmersion = false;
        useCampfireImmersion = false;
        useLever = false;
        useBackpack = false;
        useEnderChestImmersion = false;
        ImmersiveMC.LOGGER.debug("Loaded 'disabled' config: \n" + asString());
    }

    public static String asString() {
        String stringOut = "Use anvil immersion: " + useAnvilImmersion + "\n" +
                "Use brewing immersion: " + useBrewingImmersion + "\n" +
                "Use chest immersion: " + useChestImmersion + "\n" +
                "Use crafting immersion: " + useCraftingImmersion + "\n" +
                "Use furnace immersion: " + useFurnaceImmersion + "\n" +
                "Use jukebox immersion: " + useJukeboxImmersion + "\n" +
                "Use ranged grab: " + useRangedGrab + "\n" +
                "Use button: " + useButton + "\n" +
                "Use enchanting table: " + useETableImmersion + "\n" +
                "Use campfire immersion: " + useCampfireImmersion + "\n" +
                "Use lever: " + useLever + "\n" +
                "Use backpack: " + useBackpack + "\n" +
                "Backpack color: " + backpackColor + "\n" +
                "Left handed backpack: " + leftHandedBackpack + "\n" +
                "Use ender chest immersion: " + useEnderChestImmersion;
        return stringOut;
    }
}
