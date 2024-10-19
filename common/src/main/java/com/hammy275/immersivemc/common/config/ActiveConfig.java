package com.hammy275.immersivemc.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ActiveConfig implements Cloneable {
    // The settings representing a disabled config.
    public static final ActiveConfig DISABLED = new ActiveConfig();


    // The settings from the server config file. Used by both the server and client.
    public static ActiveConfig FILE_SERVER;
    // The settings from the client config file. Only used by the client.
    public static ClientActiveConfig FILE_CLIENT;
    // The settings from the server. Only used by the client.
    public static ActiveConfig FROM_SERVER;
    // The settings to actually use in-game. Only used by the client.
    private static ClientActiveConfig ACTIVE;
    // The settings to actually use in-game for each player. Only used by the server.
    private static final Map<UUID, ClientActiveConfig> CLIENTS = new HashMap<>();

    public static List<Field> fields;
    // Basic sanity check to make sure server and client have compatible configs.
    public static int fieldsHash = 0;

    protected static final Gson GSON = new Gson();
    protected static final Gson GSON_PRETTY = new GsonBuilder().setPrettyPrinting().create();

    public boolean useAnvilImmersive = true;
    public boolean useBrewingStandImmersive = true;
    public boolean useChestImmersive = true;
    public boolean useCraftingTableImmersive = true;
    public boolean useFurnaceImmersive = true;
    public boolean useJukeboxImmersive = true;
    public boolean useRangedGrabImmersive = true;
    public boolean useButtonImmersive = true;
    public boolean useEnchantingTableImmersive = true;
    public boolean useCampfireImmersive = true;
    public boolean useLeverImmersive = true;
    public boolean useBagImmersive = true;
    public boolean useRepeaterImmersive = true;
    public boolean useDoorImmersive = true;
    public boolean useTrapdoorImmersive = true;
    public boolean allowPetting = true;
    public boolean useArmorImmersive = true;
    public boolean useFeedingAnimalsImmersive = true;
    public boolean useShulkerImmersive = true;
    public boolean allowPettingAnythingLiving = false;
    public boolean useShieldImmersive = true;
    public int rangedGrabRange = 8;
    public boolean useBeaconImmersive = true;
    public boolean useBarrelImmersive = true;
    public boolean useThrowingImmersive = true;
    public boolean allowThrowingBeyondVanillaMaxRange = true;
    public boolean useHopperImmersive = true;
    public boolean useSmithingTableImmersive = true;
    public boolean useChiseledBookshelfImmersive = true;
    public boolean useWrittenBookImmersive = true;
    public boolean useCauldronImmersive = true;
    public boolean useIronFurnacesFurnaceImmersive = true;
    public boolean useTinkersConstructCraftingStationImmersive = true;
    public boolean useLecternImmersive = true;
    public boolean useBucketAndBottleImmersive = true;

    static {
        DISABLED.setDisabled();
        FROM_SERVER = new ClientActiveConfig();
        FROM_SERVER.setDisabled();
        loadFilesToMemory();
        ACTIVE = new ClientActiveConfig();
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
    }

    /**
     * Get the ActiveConfig for a player. For server only!
     * @param player Player to get config of.
     * @return Config for player, or a disabled config if the player does not have a config.
     */
    public static ClientActiveConfig getConfigForPlayer(Player player) {
        ClientActiveConfig config = CLIENTS.getOrDefault(player.getUUID(), ClientActiveConfig.DISABLED);
        // If not in VR and user wants ImmersiveMC disabled outside VR, return DISABLED config.
        if (config.disableImmersiveMCOutsideVR && !VRPluginVerify.playerInVR((ServerPlayer) player)) {
            return ClientActiveConfig.DISABLED;
        }
        return config;
    }

    /**
     * Get the ActiveConfig for the client. For client only!
     * @return Config for the local player, or a disabled config if not in VR and the setting to disable ImmersiveMC
     * outside VR is enabled.
     */
    public static ClientActiveConfig active() {
        if (FILE_CLIENT.disableImmersiveMCOutsideVR && !VRPluginVerify.clientInVR()) {
            return ClientActiveConfig.DISABLED;
        }
        return ACTIVE;
    }

    /**
     * Gets the ACTIVE config. For client only, and should only be used for changing the values in the active config.
     * All other methods should use active().
     * @return The ACTIVE config.
     */
    public static ClientActiveConfig activeRaw() {
        return ACTIVE;
    }

    /**
     * Register config for player
     * @param player Player to register config for.
     * @param config Config from the player.
     */
    public static void registerPlayerConfig(Player player, ClientActiveConfig config) {
        CLIENTS.put(player.getUUID(), config);
    }

    public static ActiveConfig getFileConfig(ConfigType type) {
        return type == ConfigType.CLIENT ? FILE_CLIENT : FILE_SERVER;
    }

    public ActiveConfig() {

    }

    public static ActiveConfig readConfigFile(ConfigType type) {
        if (!type.configFile.exists() || !type.configFile.canRead()) {
            return new ClientActiveConfig();
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(type.configFile))) {
            ActiveConfig config = GSON.fromJson(reader, type.configClass);
            config.validateConfig();
            return config;
        } catch (IOException | JsonParseException ignored) {
            return new ClientActiveConfig();
        }
    }

    public boolean writeConfigFile(ConfigType type) {
        try {
            if (!type.configFile.exists()) {
                boolean createdFile = type.configFile.createNewFile();
                if (!createdFile) {
                    return false;
                }
            }
            if (type.configFile.canWrite()) {
                try (FileWriter writer = new FileWriter(type.configFile)) {
                    writer.write(GSON_PRETTY.toJson(this));
                    return true;
                }
            }
        } catch (IOException ignored) {}
        return false;
    }

    public static void createDefaultConfigFilesIfNotFound() {
        for (ConfigType type : ConfigType.values()) {
            if (type.neededOnSide() && !type.configFile.exists()) {
                type.getDefaultConfig().writeConfigFile(type);
            }
        }
    }

        /**
         * Set ACTIVE config to be the merge of the FILE config and the FROM_SERVER config if connected to a server.
         * Should only be called by the client.
         */
    public static void loadActive() {
        ACTIVE = ((ClientActiveConfig) FILE_CLIENT.clone());
        ACTIVE.mergeWithServer(FROM_SERVER);
    }

    /**
     * Loads DISABLED config into the ACTIVE slot.
     */
    public static void loadDisabled() {
        ACTIVE = (ClientActiveConfig) ClientActiveConfig.DISABLED.clone();
    }

    public static ActiveConfig getActiveConfigCommon(Player player) {
        return player.level().isClientSide ? active() : getConfigForPlayer(player);
    }

    /**
     * Re-merges all player configs with the server config. Should only be called by the server.
     */
    public static void remergeAllConfigs() {
        CLIENTS.values().forEach(config -> config.mergeWithServer(FILE_SERVER));
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
        useAnvilImmersive = useAnvilImmersive && other.useAnvilImmersive;
        useBrewingStandImmersive = useBrewingStandImmersive && other.useBrewingStandImmersive;
        useChestImmersive = useChestImmersive && other.useChestImmersive;
        useCraftingTableImmersive = useCraftingTableImmersive && other.useCraftingTableImmersive;
        useFurnaceImmersive = useFurnaceImmersive && other.useFurnaceImmersive;
        useJukeboxImmersive = useJukeboxImmersive && other.useJukeboxImmersive;
        useRangedGrabImmersive = useRangedGrabImmersive && other.useRangedGrabImmersive;
        useButtonImmersive = useButtonImmersive && other.useButtonImmersive;
        useEnchantingTableImmersive = useEnchantingTableImmersive && other.useEnchantingTableImmersive;
        useCampfireImmersive = useCampfireImmersive && other.useCampfireImmersive;
        useLeverImmersive = useLeverImmersive && other.useLeverImmersive;
        useBagImmersive = useBagImmersive && other.useBagImmersive;
        useRepeaterImmersive = useRepeaterImmersive && other.useRepeaterImmersive;
        useDoorImmersive = useDoorImmersive && other.useDoorImmersive;
        useTrapdoorImmersive = useTrapdoorImmersive && other.useTrapdoorImmersive;
        allowPetting = allowPetting && other.allowPetting;
        useArmorImmersive = useArmorImmersive && other.useArmorImmersive;
        useFeedingAnimalsImmersive = useFeedingAnimalsImmersive && other.useFeedingAnimalsImmersive;
        useShulkerImmersive = useShulkerImmersive && other.useShulkerImmersive;
        allowPettingAnythingLiving = allowPettingAnythingLiving && other.allowPettingAnythingLiving;
        useShieldImmersive = useShieldImmersive && other.useShieldImmersive;
        rangedGrabRange = Math.min(rangedGrabRange, other.rangedGrabRange);
        useBeaconImmersive = useBeaconImmersive && other.useBeaconImmersive;
        useBarrelImmersive = useBarrelImmersive && other.useBarrelImmersive;
        useThrowingImmersive = useThrowingImmersive && other.useThrowingImmersive;
        allowThrowingBeyondVanillaMaxRange = allowThrowingBeyondVanillaMaxRange && other.allowThrowingBeyondVanillaMaxRange;
        useHopperImmersive = useHopperImmersive && other.useHopperImmersive;
        useSmithingTableImmersive = useSmithingTableImmersive && other.useSmithingTableImmersive;
        useChiseledBookshelfImmersive = useChiseledBookshelfImmersive && other.useChiseledBookshelfImmersive;
        useWrittenBookImmersive = useWrittenBookImmersive && other.useWrittenBookImmersive;
        useCauldronImmersive = useCauldronImmersive && other.useCauldronImmersive;
        useIronFurnacesFurnaceImmersive = useIronFurnacesFurnaceImmersive && other.useIronFurnacesFurnaceImmersive;
        useTinkersConstructCraftingStationImmersive = useTinkersConstructCraftingStationImmersive && other.useTinkersConstructCraftingStationImmersive;
        useLecternImmersive = useLecternImmersive && other.useLecternImmersive;
        useBucketAndBottleImmersive = useBucketAndBottleImmersive && other.useBucketAndBottleImmersive;
    }

    /**
     * Sets this config to its disabled form.
     */
    public void setDisabled() {
        // Synced values
        useAnvilImmersive = false;
        useBrewingStandImmersive = false;
        useChestImmersive = false;
        useCraftingTableImmersive = false;
        useFurnaceImmersive = false;
        useJukeboxImmersive = false;
        useRangedGrabImmersive = false;
        useButtonImmersive = false;
        useEnchantingTableImmersive = false;
        useCampfireImmersive = false;
        useLeverImmersive = false;
        useBagImmersive = false;
        useRepeaterImmersive = false;
        useDoorImmersive = false;
        useTrapdoorImmersive = false;
        allowPetting = false;
        useArmorImmersive = false;
        useFeedingAnimalsImmersive = false;
        useShulkerImmersive = false;
        allowPettingAnythingLiving = false;
        useShieldImmersive = false;
        rangedGrabRange = 0;
        useBeaconImmersive = false;
        useBarrelImmersive = false;
        useThrowingImmersive = false;
        allowThrowingBeyondVanillaMaxRange = false;
        useHopperImmersive = false;
        useSmithingTableImmersive = false;
        useChiseledBookshelfImmersive = false;
        useWrittenBookImmersive = false;
        useCauldronImmersive = false;
        useIronFurnacesFurnaceImmersive = false;
        useTinkersConstructCraftingStationImmersive = false;
        useLecternImmersive = false;
        useBucketAndBottleImmersive = false;
    }

    /**
     * Loads the configs from the config files.
     */
    public static void loadFilesToMemory() {
        FILE_SERVER = readConfigFile(ConfigType.SERVER);
        if (Platform.isClient()) {
            FILE_CLIENT = (ClientActiveConfig) readConfigFile(ConfigType.CLIENT);
        }
        ConfigConverter.maybeDoConversion();
    }

    /**
     * Encodes this ActiveConfig instance into the buffer.
     * @param buffer Buffer to encode into.
     */
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this instanceof ClientActiveConfig);
        buffer.writeInt(fieldsHash);
        buffer.writeUtf(GSON.toJson(this));
    }

    /**
     * Decodes a buffer into a (Client)ActiveConfig instance.
     * @param buffer Buffer to decode from.
     */
    public static ActiveConfig decode(FriendlyByteBuf buffer) {
        Class<? extends ActiveConfig> configClass = buffer.readBoolean() ? ClientActiveConfig.class : ActiveConfig.class;
        int hashFromBuffer = buffer.readInt();
        if (hashFromBuffer != fieldsHash) {
            // Version mismatch, return disabled clone.
            return (ActiveConfig) DISABLED.clone();
        }
        return GSON.fromJson(buffer.readUtf(), configClass);
    }

    /**
     * Modifies the config to ensure all values are within valid ranges.
     */
    public void validateConfig() {
        rangedGrabRange = Mth.clamp(rangedGrabRange, -1, 12);
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    protected <T extends Enum<T>> T firstEnumIfNull(T val, Class<T> clazz) {
        return val == null ? clazz.getEnumConstants()[0] : val;
    }

    protected <T> T defaultIfNull(T val, T def) {
        return val == null ? def : val;
    }
}
