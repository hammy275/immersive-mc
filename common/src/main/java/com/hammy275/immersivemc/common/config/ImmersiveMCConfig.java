package com.hammy275.immersivemc.common.config;

import com.hammy275.immersivemc.common.network.Network;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.ForgeConfigSpec;

public class ImmersiveMCConfig {


    public static final int MAJOR_CONFIG_VERSION = 2; // Increment whenever a change is made that requires ImmersiveMC to do config adjustments
    public static final int CONFIG_VERSION = 4; // Increment post-release whenever the server+client config changes

    public static final ForgeConfigSpec GENERAL_SPEC;

    // Synced values
    public static ForgeConfigSpec.BooleanValue useAnvilImmersion;
    public static ForgeConfigSpec.BooleanValue useBrewingImmersion;
    public static ForgeConfigSpec.BooleanValue useChestImmersion;
    public static ForgeConfigSpec.BooleanValue useCraftingImmersion;
    public static ForgeConfigSpec.BooleanValue useFurnaceImmersion;
    public static ForgeConfigSpec.BooleanValue useJukeboxImmersion;
    public static ForgeConfigSpec.BooleanValue useRangedGrab;
    public static ForgeConfigSpec.BooleanValue useButton;
    public static ForgeConfigSpec.BooleanValue useETableImmersion;
    public static ForgeConfigSpec.BooleanValue useCampfireImmersion;
    public static ForgeConfigSpec.BooleanValue useLever;
    public static ForgeConfigSpec.BooleanValue useBackpack;
    public static ForgeConfigSpec.BooleanValue useRepeaterImmersion;
    public static ForgeConfigSpec.BooleanValue useDoorImmersion;
    public static ForgeConfigSpec.BooleanValue canPet;
    public static ForgeConfigSpec.BooleanValue useArmorImmersion;
    public static ForgeConfigSpec.BooleanValue canFeedAnimals;
    public static ForgeConfigSpec.BooleanValue useShulkerImmersion;
    public static ForgeConfigSpec.BooleanValue canPetAnyLiving;
    public static ForgeConfigSpec.BooleanValue immersiveShield;
    public static ForgeConfigSpec.IntValue rangedGrabRange;
    public static ForgeConfigSpec.BooleanValue useBeaconImmersion;
    public static ForgeConfigSpec.BooleanValue useBarrelImmersion;
    public static ForgeConfigSpec.BooleanValue useThrowing;
    public static ForgeConfigSpec.BooleanValue allowThrowingBeyondMax;
    public static ForgeConfigSpec.BooleanValue useHopperImmersion;
    public static ForgeConfigSpec.BooleanValue useSmithingTableImmersion;
    public static ForgeConfigSpec.BooleanValue useWrittenBookImmersion;
    public static ForgeConfigSpec.BooleanValue useCauldronImmersion;

    // C2S Only Sync
    public static ForgeConfigSpec.BooleanValue crouchBypassImmersion;
    public static ForgeConfigSpec.BooleanValue doRumble;
    public static ForgeConfigSpec.BooleanValue returnItems;

    // Non-synced values
    public static ForgeConfigSpec.IntValue backpackColor;
    public static ForgeConfigSpec.BooleanValue rightClickChest;
    public static ForgeConfigSpec.BooleanValue autoCenterFurnace;
    public static ForgeConfigSpec.BooleanValue autoCenterBrewing;
    public static ForgeConfigSpec.IntValue backpackMode;
    public static ForgeConfigSpec.IntValue placementGuideMode;
    public static ForgeConfigSpec.IntValue itemPlacementMode;
    public static ForgeConfigSpec.BooleanValue spinCraftingOutput;
    public static ForgeConfigSpec.BooleanValue rightClickInVR;
    public static ForgeConfigSpec.BooleanValue resourcePack3dCompat;
    public static ForgeConfigSpec.LongValue itemGuideColor;
    public static ForgeConfigSpec.LongValue itemGuideSelectedColor;
    public static ForgeConfigSpec.LongValue rangedGrabColor;
    public static ForgeConfigSpec.BooleanValue disableVanillaGUIs;
    public static ForgeConfigSpec.BooleanValue reachBehindBackpack;



    // Used to track config updates that require special intervention by ImmersiveMC to process (such as the anvil
    // smithing table split requiring the moving of the anvil key to the smithing table one).
    // Increment MAJOR_CONFIG_VERSION at the top of this file to update
    public static ForgeConfigSpec.IntValue configVersion;
    static {
        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
        setupConfig(configBuilder);
        GENERAL_SPEC = configBuilder.build();
    }

    protected static void setupConfig(ForgeConfigSpec.Builder builder) {
        // Synced Values
        useAnvilImmersion = builder
                .comment("Whether immersives on anvils should be allowed")
                .define("anvil_immersion", true);
        useBrewingImmersion = builder
                .comment("Whether immersives on brewing stands should be allowed")
                .define("brewing_immersion", true);
        useChestImmersion = builder
                .comment("Whether immersives on all types of chests should be allowed. Unless users enable right_click_chest, this is VR only.")
                .define("chest_immersion", true);
        useCraftingImmersion = builder
                .comment("Whether immersives on crafting tables should be allowed")
                .define("crafting_immersion", true);
        useFurnaceImmersion = builder
                .comment("Whether immersives on furnaces should be allowed")
                .define("furnace_immersion", true);
        useJukeboxImmersion = builder
                .comment("Whether immersives on jukeboxes should be allowed (VR only)")
                .define("jukebox_immersion", true);
        useRangedGrab = builder
                .comment("Allow VR users to grab items at a distance.")
                .define("ranged_grab", true);
        useButton = builder
                .comment("Whether VR users can physically push buttons")
                .define("button_immersion", true);
        useETableImmersion = builder
                .comment("Whether immersives on Enchanting Tables should be allowed")
                .define("enchant_table_immersion", true);
        useCampfireImmersion = builder
                .comment("Whether VR users can hold items above a campfire to cook them")
                .define("campfire_immersion", true);
        useLever = builder
                .comment("Whether VR users can physically toggle levers")
                .define("lever_immersion", true);
        useBackpack = builder
                .comment("Allow VR players to use a bag to manage their inventory")
                .define("bag_inventory", true);
        useRepeaterImmersion = builder
                .comment("Whether VR users can adjust the delay of repeaters using their hands")
                .define("repeater_immersion", true);
        useDoorImmersion = builder
                .comment("Whether VR users can open/close doors and fence gates using their hands")
                .define("door_immersion", true);
        canPet = builder
                .comment("Whether VR users can pet animals they've tamed (or animals that are tamed for all once tamed, like horses)")
                .define("can_pet", true);
        useArmorImmersion = builder
                .comment("Whether VR users can equip armor by placing the armor on them")
                .define("armor_immersion", true);
        canFeedAnimals = builder
                .comment("Whether VR users can feed animals using both hands")
                .define("feed_animals", true);
        useShulkerImmersion = builder
                .comment("Whether immersives on Shulker Boxes should be allowed")
                .define("shulker_box_immersion", true);
        canPetAnyLiving = builder
                .comment("Whether VR players can pet any living entity instead of just pets that they're owners of. Requires 'can_pet' to be enabled!")
                .define("can_pet_any_living", false);
        immersiveShield = builder
                .comment("Whether VR users can use a more immersive shield. They only need to hold a shield in the direction to block, no need to right click!")
                .define("immersive_shield", true);
        rangedGrabRange = builder
                .comment("Range in blocks that VR players can pick up items using ranged grab. Set to -1 to use Minecraft's pick range.")
                .defineInRange("ranged_grab_range", 8, -1, 12);
        useBeaconImmersion = builder
                .comment("Whether immersives on beacons should be allowed")
                .define("beacon_immersion", true);
        useBarrelImmersion = builder
                .comment("Whether immersives on barrels should be allowed")
                .define("barrel_immersion", true);
        useThrowing = builder
                .comment("Whether VR users can throw items based on the speed of their hand")
                .define("use_throwing", true);
        allowThrowingBeyondMax = builder
                .comment("Whether VR throwing users can throw up to 5% beyond the velocity of non-VR users depending on how hard they throw")
                .define("allow_throwing_beyond_max", true);
        useHopperImmersion = builder
                .comment("Whether immersives on hoppers should be allowed.")
                .define("hopper_immersion", true);
        useSmithingTableImmersion = builder
                .comment("Whether immersives for smithing tables should be allowed.")
                .define("smithing_table_immersion", true);
        useWrittenBookImmersion = builder
                .comment("Whether immersives for written books should be allowed.")
                .define("written_book_immersion", true);
        useCauldronImmersion = builder
                .comment("Whether immersives for cauldrons should be allowed.")
                .define("cauldron_immersion", true);

        // C2S Only Sync
        crouchBypassImmersion = builder
                .comment("Allow users to crouch + right-click to bypass immersives.")
                .define("crouch_bypass_immersion", true);
        doRumble = builder
                .comment("Do controller rumble for ImmersiveMC actions.")
                .define("do_rumble", true);
        returnItems = builder
                .comment("Return items to oneself when walking away from immersives that items tend to not be stored in.")
                .define("return_items", true);

        // Non-synced Values
        backpackColor = builder
                .comment("Color for the bag as a base-10 RGB number.")
                .defineInRange("bag_color", 11901820, 0, 0xFFFFFF);
        rightClickChest = builder
                .comment("Allows for right-clicking chests to use their immersive. Works outside of VR!")
                .define("right_click_chest", false);
        autoCenterFurnace = builder
                .comment("Makes the furnace immersive more centered instead of similar to the vanilla GUI")
                .define("center_furnace", false);
        autoCenterBrewing = builder
                .comment("Makes the brewing stand more centered instead of similar to the vanilla GUI")
                .define("center_brewing", false);
        backpackMode = builder
                .comment("Which bag type to display")
                .defineInRange("bag_mode", 0, 0, BackpackMode.values().length -1);
        placementGuideMode = builder
                .comment("The mode for the placement guide")
                .defineInRange("placement_guide_mode", 0, 0, PlacementGuideMode.values().length - 1);
        itemPlacementMode = builder
                .comment("Integer representation for the mode to use when placing items using ImmersiveMC")
                .defineInRange("placement_mode", 0, 0, PlacementMode.values().length - 1);
        spinCraftingOutput = builder
                .comment("Whether the item output of a crafting table should spin")
                .define("spin_crafting_output", true);
        rightClickInVR = builder
                .comment("Allow right-clicking immersives in VR, like how NonVR users do.")
                .define("right_click_in_vr", false);
        resourcePack3dCompat = builder
                .comment("Enables compatability for 3D resource packs like Classic 3D (16x)")
                .define("resource_pack_3d_compat", false);
        itemGuideColor = builder
                .comment("Color for the item guides that help with item placement (aqua by default).")
                .defineInRange("item_guide_color", 0x3300ffffL, 0, 0xFFFFFFFFL);
        itemGuideSelectedColor = builder
                .comment("Color for the item guides that help with item placement when hovered over (green by default).")
                .defineInRange("item_guide_selected_color", 0x3300ff00L, 0, 0xFFFFFFFFL);
        rangedGrabColor = builder
                .comment("Color for the particles shown when grabbing items from range (aqua by default).")
                .defineInRange("ranged_grab_color", 0xff00ffffL, 0, 0xFFFFFFFFL);
        disableVanillaGUIs = builder
                .comment("Disable vanilla GUIs when their respective immersive is enabled")
                .define("disable_vanilla_interactions", false);
        reachBehindBackpack = builder
                .comment("Allow reaching behind you to grab your bag. Disables similar functionatliy from regular Vivecraft.")
                .define("reach_behind_backpack", false);



        // Config version (not synced!)
        // Note: We set the default value to 1 here, meaning the first time someone launches ImmersiveMC
        // and generates a config file, it will be "upgraded". Have to do this since version 1
        // doesn't have a config number.
        configVersion = builder
                .comment("!!!!DON'T TOUCH!!!! Version number for this configuration file! Do not change! It is used by ImmersiveMC " +
                        "to track config updates so it can automatically update your configuration files when needed!")
                .defineInRange("config_version", 1, 1, Integer.MAX_VALUE);
    }

    public static void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(Network.PROTOCOL_VERSION).writeInt(CONFIG_VERSION)
                .writeBoolean(useAnvilImmersion.get())
                .writeBoolean(useBrewingImmersion.get())
                .writeBoolean(useChestImmersion.get()).writeBoolean(useCraftingImmersion.get())
                .writeBoolean(useFurnaceImmersion.get()).writeBoolean(useJukeboxImmersion.get())
                .writeBoolean(useRangedGrab.get())
                .writeBoolean(useButton.get())
                .writeBoolean(useETableImmersion.get())
                .writeBoolean(useCampfireImmersion.get())
                .writeBoolean(useLever.get())
                .writeBoolean(useBackpack.get())
                .writeBoolean(useRepeaterImmersion.get())
                .writeBoolean(useDoorImmersion.get())
                .writeBoolean(canPet.get())
                .writeBoolean(useArmorImmersion.get())
                .writeBoolean(canFeedAnimals.get())
                .writeBoolean(useShulkerImmersion.get())
                .writeBoolean(canPetAnyLiving.get())
                .writeBoolean(immersiveShield.get())
                .writeInt(rangedGrabRange.get())
                .writeBoolean(useBeaconImmersion.get())
                .writeBoolean(useBarrelImmersion.get())
                .writeBoolean(useThrowing.get())
                .writeBoolean(allowThrowingBeyondMax.get())
                .writeBoolean(useHopperImmersion.get())
                .writeBoolean(useSmithingTableImmersion.get())
                .writeBoolean(useWrittenBookImmersion.get())
                .writeBoolean(useCauldronImmersion.get());
    }

    public static void resetToDefault() {
        // Synced defaults
        useAnvilImmersion.set(true);
        useBrewingImmersion.set(true);
        useChestImmersion.set(true);
        useCraftingImmersion.set(true);
        useFurnaceImmersion.set(true);
        useJukeboxImmersion.set(true);
        useRangedGrab.set(true);
        useButton.set(true);
        useETableImmersion.set(true);
        useCampfireImmersion.set(true);
        useLever.set(true);
        useBackpack.set(true);
        useRepeaterImmersion.set(true);
        useDoorImmersion.set(true);
        canPet.set(true);
        useArmorImmersion.set(true);
        canFeedAnimals.set(true);
        useShulkerImmersion.set(true);
        canPetAnyLiving.set(false);
        immersiveShield.set(true);
        rangedGrabRange.set(rangedGrabRange.getDefault());
        useBeaconImmersion.set(true);
        useBarrelImmersion.set(true);
        useThrowing.set(true);
        allowThrowingBeyondMax.set(true);
        useHopperImmersion.set(true);
        useSmithingTableImmersion.set(true);
        useWrittenBookImmersion.set(true);
        useCauldronImmersion.set(true);

        // C2S Synced Values
        crouchBypassImmersion.set(true);
        doRumble.set(true);
        returnItems.set(true);

        // Non-synced defaults
        backpackColor.set(11901820);
        rightClickChest.set(false);
        autoCenterFurnace.set(false);
        autoCenterBrewing.set(false);
        backpackMode.set(0);
        placementGuideMode.set(0);
        itemPlacementMode.set(0);
        spinCraftingOutput.set(false);
        rightClickInVR.set(false);
        resourcePack3dCompat.set(false);
        itemGuideColor.set(0x3300ffffL);
        itemGuideSelectedColor.set(0x3300ff00L);
        rangedGrabColor.set(0xff00ffffL);
        disableVanillaGUIs.set(false);
        reachBehindBackpack.set(false);

    }


}
