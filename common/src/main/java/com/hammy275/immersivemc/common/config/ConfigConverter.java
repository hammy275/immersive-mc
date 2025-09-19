package com.hammy275.immersivemc.common.config;

import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.common.util.RGBA;

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

    // Temporary storage to read these in from the old config
    private static long readItemGuideColor = -1;
    private static long readItemGuideSelectedColor = -1;
    private static long readRangedGrabColor = -1;

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
            // Conversion of item guide colors to new system
            if (readItemGuideColor == 0x3300ffffL && readItemGuideSelectedColor == 0x3300ff00L && readRangedGrabColor == 0xff00ffffL) {
                ActiveConfig.FILE_CLIENT.itemGuidePreset = ItemGuidePreset.CLASSIC;
            } else if (readItemGuideColor == 0x638b8b8bL && readItemGuideSelectedColor == 0x7fc5c5c5L && readRangedGrabColor == 0xffc5c5c5L) {
                ActiveConfig.FILE_CLIENT.itemGuidePreset = ItemGuidePreset.GRAY;
            } else if (readItemGuideColor != -1 && readItemGuideSelectedColor != -1 && readRangedGrabColor != -1) {
                ActiveConfig.FILE_CLIENT.itemGuidePreset = ItemGuidePreset.CUSTOM;
                ActiveConfig.FILE_CLIENT.itemGuideCustomColorData = ItemGuideColorData.of(() -> new RGBA(readItemGuideColor), () -> new RGBA(readItemGuideSelectedColor), () -> new RGBA(readRangedGrabColor));
            }

            // Delete old config and save
            OLD_CONFIG_FILE.delete();
            ActiveConfig.FILE_SERVER.writeConfigFile(ConfigType.SERVER);
            if (Platform.isClient()) {
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
        addClientConversion("bag_color", Integer.class, (config, val) -> config.bagColor = val);
        addBothConversion("anvil_immersion", Boolean.class, (config, val) -> config.useAnvilImmersive = val);
        addBothConversion("brewing_immersion", Boolean.class, (config, val) -> config.useBrewingStandImmersive = val);
        addBothConversion("chest_immersion", Boolean.class, (config, val) -> config.useChestImmersive = val);
        addBothConversion("crafting_immersion", Boolean.class, (config, val) -> config.useCraftingTableImmersive = val);
        addBothConversion("furnace_immersion", Boolean.class, (config, val) -> config.useFurnaceImmersive = val);
        addBothConversion("jukebox_immersion", Boolean.class, (config, val) -> config.useJukeboxImmersive = val);
        addBothConversion("ranged_grab", Boolean.class, (config, val) -> config.useRangedGrabImmersive = val);
        addBothConversion("button_immersion", Boolean.class, (config, val) -> config.useButtonImmersive = val);
        addBothConversion("enchant_table_immersion", Boolean.class, (config, val) -> config.useEnchantingTableImmersive = val);
        addBothConversion("campfire_immersion", Boolean.class, (config, val) -> config.useCampfireImmersive = val);
        addBothConversion("lever_immersion", Boolean.class, (config, val) -> config.useLeverImmersive = val);
        addBothConversion("bag_inventory", Boolean.class, (config, val) -> config.useBagImmersive = val);
        addBothConversion("repeater_immersion", Boolean.class, (config, val) -> config.useRepeaterImmersive = val);
        addBothConversion("door_immersion", Boolean.class, (config, val) -> config.useDoorImmersive = val);
        addBothConversion("can_pet", Boolean.class, (config, val) -> config.allowPetting = val);
        addBothConversion("armor_immersion", Boolean.class, (config, val) -> config.useArmorImmersive = val);
        addBothConversion("feed_animals", Boolean.class, (config, val) -> config.useFeedingAnimalsImmersive = val);
        addBothConversion("shulker_box_immersion", Boolean.class, (config, val) -> config.useShulkerImmersive = val);
        addBothConversion("can_pet_any_living", Boolean.class, (config, val) -> config.allowPettingAnythingLiving = val);
        addBothConversion("ranged_grab_range", Integer.class, (config, val) -> config.rangedGrabRange = val);
        addBothConversion("beacon_immersion", Boolean.class, (config, val) -> config.useBeaconImmersive = val);
        addBothConversion("barrel_immersion", Boolean.class, (config, val) -> config.useBarrelImmersive = val);
        addBothConversion("use_throwing", Boolean.class, (config, val) -> config.useThrowingImmersive = val);
        addBothConversion("allow_throwing_beyond_max", Boolean.class, (config, val) -> config.allowThrowingBeyondVanillaMaxRange = val);
        addBothConversion("hopper_immersion", Boolean.class, (config, val) -> config.useHopperImmersive = val);
        addBothConversion("smithing_table_immersion", Boolean.class, (config, val) -> config.useSmithingTableImmersive = val);
        addBothConversion("written_book_immersion", Boolean.class, (config, val) -> config.useWrittenBookImmersive = val);
        addBothConversion("cauldron_immersion", Boolean.class, (config, val) -> config.useCauldronImmersive = val);
        addClientConversion("crouch_bypass_immersion", Boolean.class, (config, val) -> config.crouchMode = val ? CrouchMode.BYPASS_IMMERSIVE : CrouchMode.SWAP_ALL);
        addClientConversion("do_rumble", Boolean.class, (config, val) -> config.doVRControllerRumble = val);
        addClientConversion("return_items", Boolean.class, (config, val) -> config.returnItemsWhenLeavingImmersives = val);
        addClientConversion("right_click_chest", Boolean.class, (config, val) -> config.rightClickChestInteractions = val);
        addClientConversion("center_furnace", Boolean.class, (config, val) -> config.autoCenterFurnaceImmersive = val);
        addClientConversion("center_brewing", Boolean.class, (config, val) -> config.autoCenterBrewingStandImmersive = val);
        addClientConversion("bag_mode", Integer.class, (config, val) -> config.bagMode = BackpackMode.values()[val]);
        addClientConversion("placement_guide_mode", Integer.class, (config, val) -> config.placementGuideMode = PlacementGuideMode.values()[val]);
        addClientConversion("spin_crafting_output", Boolean.class, (config, val) -> config.spinSomeImmersiveOutputs = val);
        addClientConversion("right_click_in_vr", Boolean.class, (config, val) -> config.rightClickImmersiveInteractionsInVR = val);
        addClientConversion("resource_pack_3d_compat", Boolean.class, (config, val) -> config.compatFor3dResourcePacks = val);
        addClientConversion("item_guide_color", Long.class, (config, val) -> readItemGuideColor = val);
        addClientConversion("item_guide_selected_color", Long.class, (config, val) -> readItemGuideSelectedColor = val);
        addClientConversion("ranged_grab_color", Long.class, (config, val) -> readRangedGrabColor = val);
        addClientConversion("disable_vanilla_interactions", Boolean.class, (config, val) -> config.disableVanillaInteractionsForSupportedImmersives = val);
        addClientConversion("reach_behind_bag_mode", Integer.class, (config, val) -> config.reachBehindBagMode = ReachBehindBackpackMode.values()[val]);
        addBothConversion("iron_furnaces_furnace_immersion", Boolean.class, (config, val) -> config.useIronFurnacesFurnaceImmersive = val);
        addBothConversion("tinkers_construct_crafting_station_immersion", Boolean.class, (config, val) -> config.useTinkersConstructCraftingStationImmersive = val);
        addClientConversion("item_guide_size", Double.class, (config, val) -> config.itemGuideSize = val);
        addClientConversion("item_guide_selected_size", Double.class, (config, val) -> config.itemGuideSelectedSize = val);
        addClientConversion("disable_outside_vr", Boolean.class, (config, val) -> config.disableImmersiveMCOutsideVR = val);
        addBothConversion("lectern_immersion", Boolean.class, (config, val) -> config.useLecternImmersive = val);
        addBothConversion("chiseled_bookshelf_immersion", Boolean.class, (config, val) -> config.useChiseledBookshelfImmersive = val);
    }

    private record OldConfigData<T, C extends ActiveConfig> (Class<T> clazz, BiConsumer<C, T> setter) {}
}
