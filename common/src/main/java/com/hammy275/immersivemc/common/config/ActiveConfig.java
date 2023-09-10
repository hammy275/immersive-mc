package com.hammy275.immersivemc.common.config;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.ConfigSyncPacket;
import com.hammy275.immersivemc.common.util.RGBA;
import net.minecraft.client.Minecraft;
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
    public static boolean canPet = false;
    public static boolean useArmorImmersion = false;
    public static boolean canFeedAnimals = false;
    public static boolean useShulkerImmersion = false;
    public static boolean canPetAnyLiving = false;
    public static boolean immersiveShield = false;
    public static int rangedGrabRange = 0;
    public static boolean useBeaconImmersion = false;
    public static boolean useBarrelImmersion = false;
    public static boolean useThrowing = false;
    public static boolean allowThrowingBeyondMax = false;
    public static boolean useHopperImmersion = false;
    public static boolean useSmithingTableImmersion = false;
    public static boolean useWrittenBookImmersion = false;
    public static boolean useCauldronImmersion = false;

    // C2S Synced values
    public static boolean crouchBypassImmersion = false;
    public static boolean doRumble = false;
    public static boolean returnItems = false;

    // Non-synced values
    public static int backpackColor = 11901820;
    public static boolean rightClickChest = false;
    public static boolean autoCenterFurnace = false;
    public static boolean autoCenterBrewing = false;
    public static BackpackMode backpackMode = BackpackMode.BUNDLE;
    public static PlacementGuideMode placementGuideMode = PlacementGuideMode.CUBE;
    public static PlacementMode placementMode = PlacementMode.PLACE_ONE;
    public static boolean spinCraftingOutput = true;
    public static boolean rightClickInVR = false;
    public static boolean resourcePack3dCompat = false;
    public static RGBA itemGuideColor = new RGBA(0x3300ffffL);
    public static RGBA itemGuideSelectedColor = new RGBA(0x3300ff00L);
    public static RGBA rangedGrabColor = new RGBA(0xff00ffffL);
    public static boolean disableVanillaGUIs = false;
    public static boolean reachBehindBackpack = false;

    // For changing config values in-game
    public static FriendlyByteBuf serverCopy = null;

    // On a singleplayer world, the server and client share this value.
    // When set to true when the client-side changes a config value, the server sees
    // this as true, and reloads the config globally, so we can sync it to other
    // LAN players.
    public static boolean clientForceServerReloadForLAN = false;


    public static void loadConfigFromPacket(FriendlyByteBuf buffer) {
        int serverNetworkVersion = buffer.readInt();
        if (serverNetworkVersion != Network.PROTOCOL_VERSION) {
            Network.INSTANCE.sendToServer(ConfigSyncPacket.getKickMePacket());
        }
        int serverConfigVersion = buffer.readInt();
        if (serverConfigVersion != ImmersiveMCConfig.CONFIG_VERSION) { // Kick if we have a different config version
            Network.INSTANCE.sendToServer(ConfigSyncPacket.getKickMePacket());
        }
        serverCopy = new FriendlyByteBuf(buffer.copy()); // Store copy of server configuration
        serverCopy.retain();
        loadFromByteBuffer(buffer);
        buffer.release();
        ImmersiveMC.LOGGER.debug("Loaded config from network: \n" + asString());

    }

    private static void loadFromByteBuffer(FriendlyByteBuf buffer) {
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
        canPet = buffer.readBoolean() && canPet;
        useArmorImmersion = buffer.readBoolean() && useArmorImmersion;
        canFeedAnimals = buffer.readBoolean() && canFeedAnimals;
        useShulkerImmersion = buffer.readBoolean() && useShulkerImmersion;
        canPetAnyLiving = buffer.readBoolean() && canPetAnyLiving;
        immersiveShield = buffer.readBoolean() && immersiveShield;
        // Always use minimum value between client and server
        rangedGrabRange = Math.min(buffer.readInt(), rangedGrabRange);
        useBeaconImmersion = buffer.readBoolean() && useBeaconImmersion;
        useBarrelImmersion = buffer.readBoolean() && useBarrelImmersion;
        useThrowing = buffer.readBoolean() && useThrowing;
        allowThrowingBeyondMax = buffer.readBoolean() && allowThrowingBeyondMax;
        useHopperImmersion = buffer.readBoolean() && useHopperImmersion;
        useSmithingTableImmersion = buffer.readBoolean() && useSmithingTableImmersion;
        useWrittenBookImmersion = buffer.readBoolean() && useWrittenBookImmersion;
        useCauldronImmersion = buffer.readBoolean() && useCauldronImmersion;

    }

    public static void loadConfigFromFile() {
        loadConfigFromFile(false);
    }

    public static void loadConfigFromFile(boolean forceLoadServerSettings) {
        // Synced values (only loaded if we're not in a server, or we are the server)
        upgradeConfigIfNeeded(ImmersiveMCConfig.configVersion.get(), ImmersiveMCConfig.MAJOR_CONFIG_VERSION);
        if (forceLoadServerSettings || Minecraft.getInstance().level == null) {
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
            canPet = ImmersiveMCConfig.canPet.get();
            useArmorImmersion = ImmersiveMCConfig.useArmorImmersion.get();
            canFeedAnimals = ImmersiveMCConfig.canFeedAnimals.get();
            useShulkerImmersion = ImmersiveMCConfig.useShulkerImmersion.get();
            canPetAnyLiving = ImmersiveMCConfig.canPetAnyLiving.get();
            immersiveShield = ImmersiveMCConfig.immersiveShield.get();
            rangedGrabRange = ImmersiveMCConfig.rangedGrabRange.get();
            useBeaconImmersion = ImmersiveMCConfig.useBeaconImmersion.get();
            useBarrelImmersion = ImmersiveMCConfig.useBarrelImmersion.get();
            useThrowing = ImmersiveMCConfig.useThrowing.get();
            allowThrowingBeyondMax = ImmersiveMCConfig.allowThrowingBeyondMax.get();
            useHopperImmersion = ImmersiveMCConfig.useHopperImmersion.get();
            useSmithingTableImmersion = ImmersiveMCConfig.useSmithingTableImmersion.get();
            useWrittenBookImmersion = ImmersiveMCConfig.useWrittenBookImmersion.get();
            useCauldronImmersion = ImmersiveMCConfig.useCauldronImmersion.get();
        } else {
            ImmersiveMC.LOGGER.debug("Not re-loading immersive options since we're in a world!");
        }

        // C2S Synced values
        crouchBypassImmersion = ImmersiveMCConfig.crouchBypassImmersion.get();
        doRumble = ImmersiveMCConfig.doRumble.get();
        returnItems = ImmersiveMCConfig.returnItems.get();

        // Non-synced values
        backpackColor = ImmersiveMCConfig.backpackColor.get();
        rightClickChest = ImmersiveMCConfig.rightClickChest.get();
        autoCenterFurnace = ImmersiveMCConfig.autoCenterFurnace.get();
        autoCenterBrewing = ImmersiveMCConfig.autoCenterBrewing.get();
        backpackMode = BackpackMode.values()[ImmersiveMCConfig.backpackMode.get()];
        placementGuideMode = PlacementGuideMode.values()[ImmersiveMCConfig.placementGuideMode.get()];
        placementMode = PlacementMode.fromInt(ImmersiveMCConfig.itemPlacementMode.get());
        spinCraftingOutput = ImmersiveMCConfig.spinCraftingOutput.get();
        rightClickInVR = ImmersiveMCConfig.rightClickInVR.get();
        resourcePack3dCompat = ImmersiveMCConfig.resourcePack3dCompat.get();
        itemGuideColor = new RGBA(ImmersiveMCConfig.itemGuideColor.get());
        itemGuideSelectedColor = new RGBA(ImmersiveMCConfig.itemGuideSelectedColor.get());
        rangedGrabColor = new RGBA(ImmersiveMCConfig.rangedGrabColor.get());
        disableVanillaGUIs = ImmersiveMCConfig.disableVanillaGUIs.get();
        reachBehindBackpack = ImmersiveMCConfig.reachBehindBackpack.get();
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
        canPet = false;
        useArmorImmersion = false;
        canFeedAnimals = false;
        useShulkerImmersion = false;
        canPetAnyLiving = false;
        immersiveShield = false;
        rangedGrabRange = 0;
        useBeaconImmersion = false;
        crouchBypassImmersion = false;
        useBarrelImmersion = false;
        useThrowing = false;
        allowThrowingBeyondMax = false;
        useHopperImmersion = false;
        useSmithingTableImmersion = false;
        useWrittenBookImmersion = false;
        useCauldronImmersion = false;
        ImmersiveMC.LOGGER.debug("Loaded 'disabled' config: \n" + asString());
    }

    public static FriendlyByteBuf encodeServerOnlyConfig(FriendlyByteBuf buffer) {
        buffer.writeBoolean(ActiveConfig.useButton).writeBoolean(ActiveConfig.useCampfireImmersion)
                .writeBoolean(ActiveConfig.useLever).writeBoolean(ActiveConfig.useRangedGrab)
                .writeBoolean(ActiveConfig.useDoorImmersion)
                .writeBoolean(ActiveConfig.canPet).writeBoolean(ActiveConfig.useArmorImmersion)
                .writeBoolean(ActiveConfig.canFeedAnimals).writeBoolean(ActiveConfig.canPetAnyLiving)
                .writeInt(ActiveConfig.rangedGrabRange).writeBoolean(ActiveConfig.crouchBypassImmersion)
                .writeBoolean(ActiveConfig.doRumble).writeBoolean(ActiveConfig.returnItems)
                .writeBoolean(ActiveConfig.useCauldronImmersion);
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
                "Use right click chest: " + rightClickChest + "\n" +
                "Use repeater immersion: " + useRepeaterImmersion + "\n" +
                "Auto-center furnace: " + autoCenterFurnace + "\n" +
                "Auto-center brewing: " + autoCenterBrewing + "\n" +
                "Backpack mode: " + backpackMode + "\n" +
                "Placement Guide mode: " + placementGuideMode + "\n" +
                "Placement mode: " + placementMode + "\n" +
                "Use door immersion: " + useDoorImmersion + "\n" +
                "Spin crafting output: " + spinCraftingOutput + "\n" +
                "Can pet: " + canPet + "\n" +
                "Use armor immersion: " + useArmorImmersion + "\n" +
                "Can feed animals: " + canFeedAnimals + "\n" +
                "Use Shulker Box Immersion: " + useShulkerImmersion + "\n" +
                "Can pet any living: " + canPetAnyLiving + "\n" +
                "Use immersive shield: " + immersiveShield + "\n" +
                "Ranged grab range: " + rangedGrabRange + "\n" +
                "Right click in VR: " + rightClickInVR + "\n" +
                "3D resource pack compatability: " + resourcePack3dCompat + "\n" +
                "Use beacon immersion: " + useBeaconImmersion + "\n" +
                "Crouch bypass immersion: " + crouchBypassImmersion + "\n" +
                "Use barrel immersion: " + useBarrelImmersion + "\n" +
                "Use throwing: " + useThrowing + "\n" +
                "Allow throwing beyond max: " + allowThrowingBeyondMax + "\n" +
                "Item Guide Color: " + itemGuideColor + "\n" +
                "Item Guide Selected Color: " + itemGuideSelectedColor + "\n" +
                "Ranged Grab Color: " + rangedGrabColor + "\n" +
                "Use Hopper Immersion: " + useHopperImmersion + "\n" +
                "Disable Vanilla GUIs: " + disableVanillaGUIs + "\n" +
                "Reach Behind Backpack: " + reachBehindBackpack + "\n" +
                "Use Smithing Table Immersion: " + useSmithingTableImmersion + "\n" +
                "Do Rumble: " + doRumble + "\n" +
                "Return Items: " + returnItems + "\n" +
                "Use Written Book Immersion: " + useWrittenBookImmersion + "\n" +
                "Use Cauldron Immersion: " + useCauldronImmersion;
        return stringOut;
    }

    public static void reloadAfterServer() {
        // Only call on client!
        if (serverCopy != null) {
            int oldIndex = serverCopy.readerIndex();
            loadFromByteBuffer(serverCopy);
            serverCopy.readerIndex(oldIndex);
            ImmersiveMC.LOGGER.debug("Reloaded config while in-game: \n" + asString());
            Network.INSTANCE.sendToServer(ConfigSyncPacket.getToServerConfigPacket()); // Also send our new config to server

            // If we're the host of an SP game, reload config server-side. This re-syncs configs
            // with all clients connected on a LAN world.
            ActiveConfig.clientForceServerReloadForLAN = true;
        }
    }

    public static void upgradeConfigIfNeeded(int configFileVersion, int configLatestVersion) {
        while (configFileVersion < configLatestVersion) {
            if (configFileVersion == 1) {
                ImmersiveMC.LOGGER.info("Upgrading ImmersiveMC config to version 2 (set Smithing Table config value to same as anvil).");
                ImmersiveMC.LOGGER.info("If you just installed ImmersiveMC, you can ignore the above message!");
                ImmersiveMCConfig.useSmithingTableImmersion.set(ImmersiveMCConfig.useAnvilImmersion.get());
            }
            ImmersiveMCConfig.configVersion.set(++configFileVersion);
        }
    }
}
