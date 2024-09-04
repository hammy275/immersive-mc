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
    public static ActiveConfig FROM_SERVER = new ActiveConfig();
    // The settings to actually use in-game. Only used by the client.
    private static ActiveConfig ACTIVE = new ActiveConfig();
    // The settings to actually use in-game for each player. Only used by the server.
    private static final Map<UUID, ActiveConfig> CLIENTS = new HashMap<>();

    public static List<Field> fields;
    // Basic sanity check to make sure server and client have compatible configs.
    public static int fieldsHash = 0;

    // Server authoritative
    public boolean useAnvilImmersion = true;
    public boolean useBrewingImmersion = true;
    public boolean useChestImmersion = true;
    public boolean useCraftingImmersion = true;
    public boolean useFurnaceImmersion = true;
    public boolean useJukeboxImmersion = true;
    public boolean useRangedGrab = true;
    public boolean useButton = true;
    public boolean useETableImmersion = true;
    public boolean useCampfireImmersion = true;
    public boolean useLever = true;
    public boolean useBackpack = true;
    public boolean useRepeaterImmersion = true;
    public boolean useDoorImmersion = true;
    public boolean canPet = true;
    public boolean useArmorImmersion = true;
    public boolean canFeedAnimals = true;
    public boolean useShulkerImmersion = true;
    public boolean canPetAnyLiving = false;
    public boolean immersiveShield = true;
    public int rangedGrabRange = 0;
    public boolean useBeaconImmersion = true;
    public boolean useBarrelImmersion = true;
    public boolean useThrowing = true;
    public boolean allowThrowingBeyondMax = true;
    public boolean useHopperImmersion = true;
    public boolean useSmithingTableImmersion = true;
    public boolean useChiseledBookshelfImmersion = true;
    public boolean useWrittenBookImmersion = true;
    public boolean useCauldronImmersion = true;
    public boolean useIronFurnacesFurnaceImmersion = true;
    public boolean useTinkersConstructCraftingStationImmersion = true;
    public boolean useLecternImmersion = true;

    // Client authoritative
    public boolean crouchBypassImmersion = false;
    public boolean doRumble = true;
    public boolean returnItems = true;
    public boolean disableOutsideVR = false;
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
        DISABLED.setDisabled();
        FROM_SERVER.setDisabled();
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
        ACTIVE.mergeWithServer(FROM_SERVER);
    }

    /**
     * Loads DISABLED config into the ACTIVE slot.
     */
    public static void loadDisabled() {
        ACTIVE = (ActiveConfig) DISABLED.clone();
    }

    public static ActiveConfig getActiveConfigCommon(Player player) {
        return player.level().isClientSide ? active() : getConfigForPlayer(player);
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
    public void mergeWithServer(ActiveConfig other) {
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
        useChiseledBookshelfImmersion = useChiseledBookshelfImmersion && other.useChiseledBookshelfImmersion;
        useWrittenBookImmersion = useWrittenBookImmersion && other.useWrittenBookImmersion;
        useCauldronImmersion = useCauldronImmersion && other.useCauldronImmersion;
        useIronFurnacesFurnaceImmersion = useIronFurnacesFurnaceImmersion && other.useIronFurnacesFurnaceImmersion;
        useTinkersConstructCraftingStationImmersion = useTinkersConstructCraftingStationImmersion && other.useTinkersConstructCraftingStationImmersion;
        useLecternImmersion = useLecternImmersion && other.useLecternImmersion;
    }

    /**
     * Sets this config to its disabled form.
     */
    public void setDisabled() {
        // Synced values
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
        useBarrelImmersion = false;
        useThrowing = false;
        allowThrowingBeyondMax = false;
        useHopperImmersion = false;
        useSmithingTableImmersion = false;
        useChiseledBookshelfImmersion = false;
        useWrittenBookImmersion = false;
        useCauldronImmersion = false;
        useIronFurnacesFurnaceImmersion = false;
        useTinkersConstructCraftingStationImmersion = false;
        useLecternImmersion = false;

        // Client authoritative
        crouchBypassImmersion = false;
        doRumble = false;
        returnItems = false;
        disableOutsideVR = false;
        backpackColor = 11901820;
        rightClickChest = false;
        autoCenterFurnace = false;
        autoCenterBrewing = false;
        backpackMode = BackpackMode.BUNDLE;
        placementGuideMode = PlacementGuideMode.CUBE;
        placementMode = PlacementMode.PLACE_ONE;
        spinCraftingOutput = true;
        rightClickInVR = false;
        resourcePack3dCompat = false;
        itemGuideSize = 1.0;
        itemGuideSelectedSize = 1.0;
        itemGuideColor = new RGBA(0x3300ffffL);
        itemGuideSelectedColor = new RGBA(0x3300ff00L);
        rangedGrabColor = new RGBA(0xff00ffffL);
        disableVanillaGUIs = false;
        reachBehindBackpackMode = ReachBehindBackpackMode.BEHIND_BACK;
    }

    /**
     * Loads the config from the config file into this ActiveConfig instance.
     */
    public void loadFromFile() {
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
        useChiseledBookshelfImmersion = ImmersiveMCConfig.useChiseledBookshelfImmersion.get();
        useWrittenBookImmersion = ImmersiveMCConfig.useWrittenBookImmersion.get();
        useCauldronImmersion = ImmersiveMCConfig.useCauldronImmersion.get();
        useIronFurnacesFurnaceImmersion = ImmersiveMCConfig.useIronFurnacesFurnaceImmersion.get();
        useTinkersConstructCraftingStationImmersion = ImmersiveMCConfig.useTinkersConstructCraftingStationImmersion.get();
        useLecternImmersion = ImmersiveMCConfig.useLecternImmersion.get();

        crouchBypassImmersion = ImmersiveMCConfig.crouchBypassImmersion.get();
        doRumble = ImmersiveMCConfig.doRumble.get();
        returnItems = ImmersiveMCConfig.returnItems.get();
        disableOutsideVR = ImmersiveMCConfig.disableOutsideVR.get();
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

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
