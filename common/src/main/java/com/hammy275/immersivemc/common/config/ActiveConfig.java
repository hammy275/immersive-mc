package com.hammy275.immersivemc.common.config;

import com.hammy275.immersivemc.common.util.RGBA;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public final class ActiveConfig implements Cloneable {
    // The settings representing a disabled config.
    public static final ActiveConfig DISABLED = new ActiveConfig();


    // The settings for this server/client before combining. This is a direct reflection of the config file.
    public static ActiveConfig FILE = new ActiveConfig();
    // The settings from the server. Only used by the client.
    public static ActiveConfig FROM_SERVER = (ActiveConfig) DISABLED.clone();
    // The settings to actually use in-game. Only used by the client.
    private static ActiveConfig ACTIVE = new ActiveConfig();
    // The settings to actually use in-game for each player. Only used by the server.
    private static final Map<UUID, ActiveConfig> CLIENTS = new HashMap<>();

    public static List<Field> fields;
    // Basic sanity check to make sure server and client have compatible configs.
    public static int fieldsHash = 0;

    // Synced values
    public boolean useAnvilImmersion = false;
    public boolean useBrewingImmersion = false;
    public boolean useChestImmersion = false;
    public boolean useCraftingImmersion = false;
    public boolean useFurnaceImmersion = false;
    public boolean useJukeboxImmersion = false;
    public boolean useRangedGrab = false;
    public boolean useButton = false;
    public boolean useETableImmersion = false;
    public boolean useCampfireImmersion = false;
    public boolean useLever = false;
    public boolean useBackpack = false;
    public boolean useRepeaterImmersion = false;
    public boolean useDoorImmersion = false;
    public boolean canPet = false;
    public boolean useArmorImmersion = false;
    public boolean canFeedAnimals = false;
    public boolean useShulkerImmersion = false;
    public boolean canPetAnyLiving = false;
    public boolean immersiveShield = false;
    public int rangedGrabRange = 0;
    public boolean useBeaconImmersion = false;
    public boolean useBarrelImmersion = false;
    public boolean useThrowing = false;
    public boolean allowThrowingBeyondMax = false;
    public boolean useHopperImmersion = false;
    public boolean useSmithingTableImmersion = false;
    public boolean useWrittenBookImmersion = false;
    public boolean useCauldronImmersion = false;
    public boolean useIronFurnacesFurnaceImmersion = false;
    public boolean useTinkersConstructCraftingStationImmersion = false;
    public boolean useLecternImmersion = false;

    // C2S Synced values
    public boolean crouchBypassImmersion = false;
    public boolean doRumble = false;
    public boolean returnItems = false;
    public boolean disableOutsideVR = false;

    // Non-synced values
    public int backpackColor = 11901820;
    public boolean rightClickChest = false;
    public boolean autoCenterFurnace = false;
    public boolean autoCenterBrewing = false;
    public BackpackMode backpackMode = BackpackMode.BUNDLE;
    public PlacementGuideMode placementGuideMode = PlacementGuideMode.CUBE;
    public PlacementMode placementMode = PlacementMode.PLACE_ONE;
    public boolean spinCraftingOutput = true;
    public boolean rightClickInVR = false;
    public boolean resourcePack3dCompat = false;
    public double itemGuideSize = 1.0;
    public double itemGuideSelectedSize = 1.0;
    public RGBA itemGuideColor = new RGBA(0x3300ffffL);
    public RGBA itemGuideSelectedColor = new RGBA(0x3300ff00L);
    public RGBA rangedGrabColor = new RGBA(0xff00ffffL);
    public boolean disableVanillaGUIs = false;
    public ReachBehindBackpackMode reachBehindBackpackMode = ReachBehindBackpackMode.BEHIND_BACK;

    static {
        Field[] fieldsArr = ActiveConfig.class.getDeclaredFields();
        // Java doesn't guarantee order of getDeclaredFields(), so we sort it.
        fields = Arrays.stream(fieldsArr)
                .sorted(Comparator.comparing(Field::getName))
                .filter((field) -> !Modifier.isStatic(field.getModifiers()))
                .toList();

        // Create a "good enough" hash of all the fields to sanity check config syncs.
        for (Field f : fields) {
            fieldsHash += f.getName().hashCode();
        }

        ActiveConfig.FILE.loadFromFile();
    }

    /**
     * Get the ActiveConfig for a player. For server only!
     * @param player Player to get config of.
     * @return Config for player, or a disabled config if the player does not have a config.
     */
    public static ActiveConfig getConfigForPlayer(Player player) {
        ActiveConfig config = CLIENTS.getOrDefault(player.getUUID(), DISABLED);
        // If not in VR and user wants ImmersiveMC disabled outside VR, return DISABLED config.
        if (config.disableOutsideVR && !VRPluginVerify.playerInVR((ServerPlayer) player)) {
            return DISABLED;
        }
        return config;
    }

    /**
     * Get the ActiveConfig for the client. For client only!
     * @return Config for the local player, or a disabled config if not in VR and the setting to disable ImmersiveMC
     * outside VR is enabled.
     */
    public static ActiveConfig active() {
        if (FILE.disableOutsideVR && !VRPluginVerify.clientInVR()) {
            return DISABLED;
        }
        return ACTIVE;
    }

    /**
     * Gets the ACTIVE config. For client only, and should only be used for changing the values in the active config.
     * All other methods should use active().
     * @return The ACTIVE config.
     */
    public static ActiveConfig activeRaw() {
        return ACTIVE;
    }

    /**
     * Register config for player
     * @param player Player to register config for.
     * @param config Config from the player.
     */
    public static void registerPlayerConfig(Player player, ActiveConfig config) {
        CLIENTS.put(player.getUUID(), config);
    }

    public ActiveConfig() {

    }

    /**
     * Set ACTIVE config to be the merge of the FILE config and the FROM_SERVER config if connected to a server.
     * Should only be called by the client.
     */
    public static void loadActive() {
        ACTIVE = ((ActiveConfig) FILE.clone());
        ACTIVE.mergeWithOther(FROM_SERVER);
    }

    /**
     * Loads DISABLED config into the ACTIVE slot.
     */
    public static void loadDisabled() {
        ACTIVE = (ActiveConfig) DISABLED.clone();
    }

    public static ActiveConfig getActiveConfigCommon(Player player) {
        return player.level.isClientSide ? active() : getConfigForPlayer(player);
    }

    /**
     * Merges this config with the config provided.
     * This will only update values that both the client and server get a say in (Synced values). For example, both the
     * server and the client can declare useAnvilImmersion to be false. If at least one of them declares it such, it
     * will be false in this instance after merging.
     * For values only one side gets a say in (C2S synced values and Non-synced values), the value in this instance
     * will not change.
     * The values in the other config will never change.
     * @param other The other config
     */
    public void mergeWithOther(ActiveConfig other) {
        useAnvilImmersion = useAnvilImmersion && other.useAnvilImmersion;
        useBrewingImmersion = useBrewingImmersion && other.useBrewingImmersion;
        useChestImmersion = useChestImmersion && other.useChestImmersion;
        useCraftingImmersion = useCraftingImmersion && other.useCraftingImmersion;
        useFurnaceImmersion = useFurnaceImmersion && other.useFurnaceImmersion;
        useJukeboxImmersion = useJukeboxImmersion && other.useJukeboxImmersion;
        useRangedGrab = useRangedGrab && other.useRangedGrab;
        useButton = useButton && other.useButton;
        useETableImmersion = useETableImmersion && other.useETableImmersion;
        useCampfireImmersion = useCampfireImmersion && other.useCampfireImmersion;
        useLever = useLever && other.useLever;
        useBackpack = useBackpack && other.useBackpack;
        useRepeaterImmersion = useRepeaterImmersion && other.useRepeaterImmersion;
        useDoorImmersion = useDoorImmersion && other.useDoorImmersion;
        canPet = canPet && other.canPet;
        useArmorImmersion = useArmorImmersion && other.useArmorImmersion;
        canFeedAnimals = canFeedAnimals && other.canFeedAnimals;
        useShulkerImmersion = useShulkerImmersion && other.useShulkerImmersion;
        canPetAnyLiving = canPetAnyLiving && other.canPetAnyLiving;
        immersiveShield = immersiveShield && other.immersiveShield;
        rangedGrabRange = Math.min(rangedGrabRange, other.rangedGrabRange);
        useBeaconImmersion = useBeaconImmersion && other.useBeaconImmersion;
        useBarrelImmersion = useBarrelImmersion && other.useBarrelImmersion;
        useThrowing = useThrowing && other.useThrowing;
        allowThrowingBeyondMax = allowThrowingBeyondMax && other.allowThrowingBeyondMax;
        useHopperImmersion = useHopperImmersion && other.useHopperImmersion;
        useSmithingTableImmersion = useSmithingTableImmersion && other.useSmithingTableImmersion;
        useWrittenBookImmersion = useWrittenBookImmersion && other.useWrittenBookImmersion;
        useCauldronImmersion = useCauldronImmersion && other.useCauldronImmersion;
        useIronFurnacesFurnaceImmersion = useIronFurnacesFurnaceImmersion && other.useIronFurnacesFurnaceImmersion;
        useTinkersConstructCraftingStationImmersion = useTinkersConstructCraftingStationImmersion && other.useTinkersConstructCraftingStationImmersion;
        useLecternImmersion = useLecternImmersion && other.useLecternImmersion;
    }

    /**
     * Loads the config from the config file into this ActiveConfig instance.
     */
    public void loadFromFile() {
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
        useIronFurnacesFurnaceImmersion = ImmersiveMCConfig.useIronFurnacesFurnaceImmersion.get();
        useTinkersConstructCraftingStationImmersion = ImmersiveMCConfig.useTinkersConstructCraftingStationImmersion.get();
        useLecternImmersion = ImmersiveMCConfig.useLecternImmersion.get();

        // C2S synced values
        crouchBypassImmersion = ImmersiveMCConfig.crouchBypassImmersion.get();
        doRumble = ImmersiveMCConfig.doRumble.get();
        returnItems = ImmersiveMCConfig.returnItems.get();
        disableOutsideVR = ImmersiveMCConfig.disableOutsideVR.get();

        // Not synced values
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
        itemGuideSize = ImmersiveMCConfig.itemGuideSize.get();
        itemGuideSelectedSize = ImmersiveMCConfig.itemGuideSelectedSize.get();
        itemGuideColor = new RGBA(ImmersiveMCConfig.itemGuideColor.get());
        itemGuideSelectedColor = new RGBA(ImmersiveMCConfig.itemGuideSelectedColor.get());
        rangedGrabColor = new RGBA(ImmersiveMCConfig.rangedGrabColor.get());
        disableVanillaGUIs = ImmersiveMCConfig.disableVanillaGUIs.get();
        reachBehindBackpackMode = ReachBehindBackpackMode.values()[ImmersiveMCConfig.reachBehindBackpackMode.get()];
    }

    /**
     * Encodes this ActiveConfig instance into the buffer.
     * @param buffer Buffer to encode into.
     */
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(fieldsHash);

        try {
            for (Field field : fields) {
                Class<?> type = field.getType();
                if (type == boolean.class) {
                    buffer.writeBoolean(field.getBoolean(this));
                } else if (type == int.class) {
                    buffer.writeInt(field.getInt(this));
                } else if (type == long.class) {
                    buffer.writeLong(field.getLong(this));
                } else if (type == float.class) {
                    buffer.writeFloat(field.getFloat(this));
                } else if (type == double.class) {
                    buffer.writeDouble(field.getDouble(this));
                } else if (type.isEnum()) {
                    Object[] enums = type.getEnumConstants();
                    for (int i = 0; i < enums.length; i++) {
                        if (enums[i] == field.get(this)) {
                            buffer.writeInt(i);
                            break;
                        }
                    }
                } else if (type == RGBA.class) {
                    RGBA rgba = (RGBA) field.get(this);
                    buffer.writeLong(rgba.toLong());
                } else {
                    throw new IllegalArgumentException("Encoding for type " + type.getName() + " not supported!");
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to encode config!", e);
        }
    }

    /**
     * Decodes a buffer into this ActiveConfig instance.
     * @param buffer Buffer to decode from.
     */
    public void decode(FriendlyByteBuf buffer) {
        int hashFromBuffer = buffer.readInt();
        if (hashFromBuffer != fieldsHash) {
            // Version mismatch, load disabled.
            ACTIVE = (ActiveConfig) ActiveConfig.DISABLED.clone();
            return;
        }

        try {
            for (Field field : fields) {
                Class<?> type = field.getType();
                if (type == boolean.class) {
                    field.setBoolean(this, buffer.readBoolean());
                } else if (type == int.class) {
                    field.setInt(this, buffer.readInt());
                } else if (type == long.class) {
                    field.setLong(this, buffer.readLong());
                } else if (type == float.class) {
                    field.setFloat(this, buffer.readFloat());
                } else if (type == double.class) {
                    field.setDouble(this, buffer.readDouble());
                } else if (type.isEnum()) {
                    Object[] enums = type.getEnumConstants();
                    field.set(this, enums[buffer.readInt()]);
                } else if (type == RGBA.class) {
                    field.set(this, new RGBA(buffer.readLong()));
                } else {
                    throw new IllegalArgumentException("Decoding for type " + type.getName() + " not supported!");
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to decode config!", e);
        }
    }

    public String asString() {
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
                "Item Guide Size: " + itemGuideSize + '\n' +
                "Hovered Over Item Guide Size: " + itemGuideSelectedSize + '\n' +
                "Item Guide Color: " + itemGuideColor + "\n" +
                "Item Guide Selected Color: " + itemGuideSelectedColor + "\n" +
                "Ranged Grab Color: " + rangedGrabColor + "\n" +
                "Use Hopper Immersion: " + useHopperImmersion + "\n" +
                "Disable Vanilla GUIs: " + disableVanillaGUIs + "\n" +
                "Use Smithing Table Immersion: " + useSmithingTableImmersion + "\n" +
                "Do Rumble: " + doRumble + "\n" +
                "Return Items: " + returnItems + "\n" +
                "Use Written Book Immersion: " + useWrittenBookImmersion + "\n" +
                "Use Cauldron Immersion: " + useCauldronImmersion + "\n" +
                "Reach Behind Backpack Mode: " + reachBehindBackpackMode + "\n" +
                "Use Iron Furnaces Furnace Immersion: " + useIronFurnacesFurnaceImmersion + "\n" +
                "Use Tinkers' Construct Crafting Station Immersion: " + useTinkersConstructCraftingStationImmersion + "\n" +
                "Disable ImmersiveMC When not in VR: " + disableOutsideVR + "\n" +
                "Use Lectern Immersion: " + useLecternImmersion;
        return stringOut;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
