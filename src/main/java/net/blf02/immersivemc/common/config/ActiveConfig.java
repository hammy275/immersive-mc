package net.blf02.immersivemc.common.config;

import net.blf02.immersivemc.ImmersiveMC;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.packet.ConfigSyncPacket;
import net.minecraft.network.FriendlyByteBuf;

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
    public static boolean useRepeaterImmersion = false;
    public static boolean useDoorImmersion = false;
    public static boolean useHoeImmersion = false;

    // Non-synced values
    public static int backpackColor = 11901820;
    public static boolean leftHandedBackpack = false;
    public static boolean rightClickChest = false;
    public static boolean autoCenterFurnace = false;
    public static boolean autoCenterBrewing = false;
    public static boolean useLowDetailBackpack = false;
    public static boolean showPlacementGuide = true;
    public static PlacementMode placementMode = PlacementMode.PLACE_ONE;

    public static void loadConfigFromPacket(FriendlyByteBuf buffer) {
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
        loadConfigFromFile(true);
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
        useRepeaterImmersion = buffer.readBoolean() && useRepeaterImmersion;
        useDoorImmersion = buffer.readBoolean() && useDoorImmersion;
        useHoeImmersion = buffer.readBoolean() && useHoeImmersion;
        ImmersiveMC.LOGGER.debug("Loaded config from network: \n" + asString());

    }

    public static void loadConfigFromFile() {
        loadConfigFromFile(false);
    }

    public static void loadConfigFromFile(boolean forceLoadServerSettings) {
        // Synced values (only loaded if we're not in a server)
        if (forceLoadServerSettings) {
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
            useRepeaterImmersion = ImmersiveMCConfig.useRepeaterImmersion.get();
            useDoorImmersion = ImmersiveMCConfig.useDoorImmersion.get();
            useHoeImmersion = ImmersiveMCConfig.useHoeImmersion.get();
        } else {
            ImmersiveMC.LOGGER.debug("Not re-loading immersive options since we're in a world!");
        }

        // Non-synced values
        backpackColor = ImmersiveMCConfig.backpackColor.get();
        leftHandedBackpack = ImmersiveMCConfig.leftHandedBackpack.get();
        rightClickChest = ImmersiveMCConfig.rightClickChest.get();
        autoCenterFurnace = ImmersiveMCConfig.autoCenterFurnace.get();
        autoCenterBrewing = ImmersiveMCConfig.autoCenterBrewing.get();
        useLowDetailBackpack = ImmersiveMCConfig.useLowDetailBackpack.get();
        showPlacementGuide = ImmersiveMCConfig.showPlacementGuide.get();
        placementMode = PlacementMode.fromInt(ImmersiveMCConfig.itemPlacementMode.get());
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
        useRepeaterImmersion = false;
        useDoorImmersion = false;
        useHoeImmersion = false;
        ImmersiveMC.LOGGER.debug("Loaded 'disabled' config: \n" + asString());
    }

    public static FriendlyByteBuf encodeServerOnlyConfig(FriendlyByteBuf buffer) {
        buffer.writeBoolean(ActiveConfig.useButton).writeBoolean(ActiveConfig.useCampfireImmersion)
                .writeBoolean(ActiveConfig.useLever).writeBoolean(ActiveConfig.useRangedGrab)
                .writeBoolean(ActiveConfig.useDoorImmersion).writeBoolean(ActiveConfig.useHoeImmersion);
        return buffer;
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
                "Use right click chest: " + rightClickChest + "\n" +
                "Use repeater immersion: " + useRepeaterImmersion + "\n" +
                "Auto-center furnace: " + autoCenterFurnace + "\n" +
                "Auto-center brewing: " + autoCenterBrewing + "\n" +
                "Use low detailed bag: " + useLowDetailBackpack + "\n" +
                "Show placement guide: " + showPlacementGuide + "\n" +
                "Placement mode: " + placementMode + "\n" +
                "Use door immersion: " + useDoorImmersion + "\n" +
                "Use hoe immersion: " + useHoeImmersion;
        return stringOut;
    }
}
