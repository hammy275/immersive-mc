package com.hammy275.immersivemc.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
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
        if (config.disableOutsideVR && !VRPluginVerify.playerInVR((ServerPlayer) player)) {
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
        if (FILE_CLIENT.disableOutsideVR && !VRPluginVerify.clientInVR()) {
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

    /**
     * Set ACTIVE config to be the merge of the FILE config and the FROM_SERVER config if connected to a server.
     * Should only be called by the client.
     */
    public static void loadActive() {
        loadFilesToMemory();
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
    }

    /**
     * Loads the configs from the config files.
     */
    public static void loadFilesToMemory() {
        FILE_SERVER = readConfigFile(ConfigType.SERVER);
        if (Platform.getEnvironment() == Env.CLIENT) {
            FILE_CLIENT = (ClientActiveConfig) readConfigFile(ConfigType.CLIENT);
        }
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
