package com.hammy275.immersivemc.common.config;

import com.hammy275.immersivemc.common.util.RGBA;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.BiConsumer;

/**
 * Handles converting the old config format to the new one. Kept in a separate file so it doesn't clog up
 * ActiveConfig.
 */
public class ConfigConverter {

    private static final File OLD_CONFIG_FILE = Paths.get(Platform.getConfigFolder().toString(), "immersive_mc.toml").toFile();
    private static Map<String, OldConfigData<?, ActiveConfig>> OLD_CONFIG_MAP_SHARED;
    private static Map<String, OldConfigData<?, ClientActiveConfig>> OLD_CONFIG_MAP_CLIENT_ONLY;

    // Used to prevent checking for a conversion if we know we already did one or know we don't need to.
    private static boolean definitelyDontConvert = false;

    public static void maybeDoConversion() {
        if (!definitelyDontConvert) {
            if (!OLD_CONFIG_FILE.exists() || ConfigType.SERVER.configFile.exists() || ConfigType.CLIENT.configFile.exists()) {
                definitelyDontConvert = true;
                return;
            }
            try {
                doConversion();
            } finally {
                definitelyDontConvert = true;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void doConversion() {
        initConversionMap();
        // Conversion is written assuming we have a client config. Add one in here so the server is happy.
        if (ActiveConfig.FILE_CLIENT == null) {
            ActiveConfig.FILE_CLIENT = new ClientActiveConfig();
        }
        try (Scanner scanner = new Scanner(OLD_CONFIG_FILE)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (!line.startsWith("#")) {
                    int equalsIndex = line.indexOf('=');
                    if (equalsIndex > -1) {
                        String key = line.substring(0, equalsIndex).strip();
                        OldConfigData<?, ActiveConfig> dataShared = OLD_CONFIG_MAP_SHARED.get(key);
                        OldConfigData<?, ClientActiveConfig> dataClient = OLD_CONFIG_MAP_CLIENT_ONLY.get(key);
                        if (dataShared != null || dataClient != null) {
                            String value = line.substring(equalsIndex + 1).strip();
                            Class<?> clazz = dataShared != null ? dataShared.clazz : dataClient.clazz;
                            try {
                                if (clazz == Integer.class) {
                                    int val = Integer.parseInt(value);
                                    if (dataShared != null) {
                                        ((BiConsumer<ActiveConfig, Integer>) dataShared.setter).accept(ActiveConfig.FILE_CLIENT, val);
                                        ((BiConsumer<ActiveConfig, Integer>) dataShared.setter).accept(ActiveConfig.FILE_SERVER, val);
                                    } else {
                                        ((BiConsumer<ClientActiveConfig, Integer>) dataClient.setter).accept(ActiveConfig.FILE_CLIENT, val);
                                    }
                                } else if (clazz == Long.class) {
                                    long val = Long.parseLong(value);
                                    if (dataShared != null) {
                                        ((BiConsumer<ActiveConfig, Long>) dataShared.setter).accept(ActiveConfig.FILE_CLIENT, val);
                                        ((BiConsumer<ActiveConfig, Long>) dataShared.setter).accept(ActiveConfig.FILE_SERVER, val);
                                    } else {
                                        ((BiConsumer<ClientActiveConfig, Long>) dataClient.setter).accept(ActiveConfig.FILE_CLIENT, val);
                                    }
                                } else if (clazz == Double.class) {
                                    double val = Double.parseDouble(value);
                                    if (dataShared != null) {
                                        ((BiConsumer<ActiveConfig, Double>) dataShared.setter).accept(ActiveConfig.FILE_CLIENT, val);
                                        ((BiConsumer<ActiveConfig, Double>) dataShared.setter).accept(ActiveConfig.FILE_SERVER, val);
                                    } else {
                                        ((BiConsumer<ClientActiveConfig, Double>) dataClient.setter).accept(ActiveConfig.FILE_CLIENT, val);
                                    }
                                } else if (clazz == Boolean.class) {
                                    boolean val = value.startsWith("t") || value.startsWith("T");
                                    if (dataShared != null) {
                                        ((BiConsumer<ActiveConfig, Boolean>) dataShared.setter).accept(ActiveConfig.FILE_CLIENT, val);
                                        ((BiConsumer<ActiveConfig, Boolean>) dataShared.setter).accept(ActiveConfig.FILE_SERVER, val);
                                    } else {
                                        ((BiConsumer<ClientActiveConfig, Boolean>) dataClient.setter).accept(ActiveConfig.FILE_CLIENT, val);
                                    }
                                } else {
                                    throw new UnsupportedOperationException("Class " + clazz + " does not have a written conversion to the new config format!");
                                }
                            } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {}
                        }
                    }
                }
            }
        } catch (IOException ignored) {
            // Intentionally empty
        } finally {
            OLD_CONFIG_FILE.delete();
            ActiveConfig.FILE_SERVER.writeConfigFile(ConfigType.SERVER);
            if (Platform.getEnv() == EnvType.CLIENT) {
                ActiveConfig.FILE_CLIENT.writeConfigFile(ConfigType.CLIENT);
            } else {
                ActiveConfig.FILE_CLIENT = null; // Was set before since conversion assumes client. Make it null again.
            }
        }
    }

    private static <T> void addBothConversion(String key, Class<T> clazz, BiConsumer<ActiveConfig, T> setter) {
        OLD_CONFIG_MAP_SHARED.put(key, new OldConfigData<>(clazz, setter));
    }

    private static <T> void addClientConversion(String key, Class<T> clazz, BiConsumer<ClientActiveConfig, T> setter) {
        OLD_CONFIG_MAP_CLIENT_ONLY.put(key, new OldConfigData<>(clazz, setter));
    }

    private static void initConversionMap() {
        OLD_CONFIG_MAP_SHARED = new HashMap<>();
        OLD_CONFIG_MAP_CLIENT_ONLY = new HashMap<>();
        addClientConversion("bag_color", Integer.class, (config, val) -> config.backpackColor = val);
        addBothConversion("anvil_immersion", Boolean.class, (config, val) -> config.useAnvilImmersion = val);
        addBothConversion("brewing_immersion", Boolean.class, (config, val) -> config.useBrewingImmersion = val);
        addBothConversion("chest_immersion", Boolean.class, (config, val) -> config.useChestImmersion = val);
        addBothConversion("crafting_immersion", Boolean.class, (config, val) -> config.useCraftingImmersion = val);
        addBothConversion("furnace_immersion", Boolean.class, (config, val) -> config.useFurnaceImmersion = val);
        addBothConversion("jukebox_immersion", Boolean.class, (config, val) -> config.useJukeboxImmersion = val);
        addBothConversion("ranged_grab", Boolean.class, (config, val) -> config.useRangedGrab = val);
        addBothConversion("button_immersion", Boolean.class, (config, val) -> config.useButton = val);
        addBothConversion("enchant_table_immersion", Boolean.class, (config, val) -> config.useETableImmersion = val);
        addBothConversion("campfire_immersion", Boolean.class, (config, val) -> config.useCampfireImmersion = val);
        addBothConversion("lever_immersion", Boolean.class, (config, val) -> config.useLever = val);
        addBothConversion("bag_inventory", Boolean.class, (config, val) -> config.useBackpack = val);
        addBothConversion("repeater_immersion", Boolean.class, (config, val) -> config.useRepeaterImmersion = val);
        addBothConversion("door_immersion", Boolean.class, (config, val) -> config.useDoorImmersion = val);
        addBothConversion("can_pet", Boolean.class, (config, val) -> config.canPet = val);
        addBothConversion("armor_immersion", Boolean.class, (config, val) -> config.useArmorImmersion = val);
        addBothConversion("feed_animals", Boolean.class, (config, val) -> config.canFeedAnimals = val);
        addBothConversion("shulker_box_immersion", Boolean.class, (config, val) -> config.useShulkerImmersion = val);
        addBothConversion("can_pet_any_living", Boolean.class, (config, val) -> config.canPetAnyLiving = val);
        addBothConversion("immersive_shield", Boolean.class, (config, val) -> config.immersiveShield = val);
        addBothConversion("ranged_grab_range", Integer.class, (config, val) -> config.rangedGrabRange = val);
        addBothConversion("beacon_immersion", Boolean.class, (config, val) -> config.useBeaconImmersion = val);
        addBothConversion("barrel_immersion", Boolean.class, (config, val) -> config.useBarrelImmersion = val);
        addBothConversion("use_throwing", Boolean.class, (config, val) -> config.useThrowing = val);
        addBothConversion("allow_throwing_beyond_max", Boolean.class, (config, val) -> config.allowThrowingBeyondMax = val);
        addBothConversion("hopper_immersion", Boolean.class, (config, val) -> config.useHopperImmersion = val);
        addBothConversion("smithing_table_immersion", Boolean.class, (config, val) -> config.useSmithingTableImmersion = val);
        addBothConversion("written_book_immersion", Boolean.class, (config, val) -> config.useWrittenBookImmersion = val);
        addBothConversion("cauldron_immersion", Boolean.class, (config, val) -> config.useCauldronImmersion = val);
        addClientConversion("crouch_bypass_immersion", Boolean.class, (config, val) -> config.crouchBypassImmersion = val);
        addClientConversion("do_rumble", Boolean.class, (config, val) -> config.doRumble = val);
        addClientConversion("return_items", Boolean.class, (config, val) -> config.returnItems = val);
        addClientConversion("right_click_chest", Boolean.class, (config, val) -> config.rightClickChest = val);
        addClientConversion("center_furnace", Boolean.class, (config, val) -> config.autoCenterFurnace = val);
        addClientConversion("center_brewing", Boolean.class, (config, val) -> config.autoCenterBrewing = val);
        addClientConversion("bag_mode", Integer.class, (config, val) -> config.backpackMode = BackpackMode.values()[val]);
        addClientConversion("placement_guide_mode", Integer.class, (config, val) -> config.placementGuideMode = PlacementGuideMode.values()[val]);
        addClientConversion("placement_mode", Integer.class, (config, val) -> config.placementMode = PlacementMode.values()[val]);
        addClientConversion("spin_crafting_output", Boolean.class, (config, val) -> config.spinCraftingOutput = val);
        addClientConversion("right_click_in_vr", Boolean.class, (config, val) -> config.rightClickInVR = val);
        addClientConversion("resource_pack_3d_compat", Boolean.class, (config, val) -> config.resourcePack3dCompat = val);
        addClientConversion("item_guide_color", Long.class, (config, val) -> config.itemGuideColor = new RGBA(val));
        addClientConversion("item_guide_selected_color", Long.class, (config, val) -> config.itemGuideSelectedColor = new RGBA(val));
        addClientConversion("ranged_grab_color", Long.class, (config, val) -> config.rangedGrabColor = new RGBA(val));
        addClientConversion("disable_vanilla_interactions", Boolean.class, (config, val) -> config.disableVanillaGUIs = val);
        addClientConversion("reach_behind_bag_mode", Integer.class, (config, val) -> config.reachBehindBackpackMode = ReachBehindBackpackMode.values()[val]);
        addBothConversion("iron_furnaces_furnace_immersion", Boolean.class, (config, val) -> config.useIronFurnacesFurnaceImmersion = val);
        addBothConversion("tinkers_construct_crafting_station_immersion", Boolean.class, (config, val) -> config.useTinkersConstructCraftingStationImmersion = val);
        addClientConversion("item_guide_size", Double.class, (config, val) -> config.itemGuideSize = val);
        addClientConversion("item_guide_selected_size", Double.class, (config, val) -> config.itemGuideSelectedSize = val);
        addClientConversion("disable_outside_vr", Boolean.class, (config, val) -> config.disableOutsideVR = val);
        addBothConversion("lectern_immersion", Boolean.class, (config, val) -> config.useLecternImmersion = val);
        addBothConversion("chiseled_bookshelf_immersion", Boolean.class, (config, val) -> config.useChiseledBookshelfImmersion = val);
    }

    private record OldConfigData<T, C extends ActiveConfig> (Class<T> clazz, BiConsumer<C, T> setter) {}
}
