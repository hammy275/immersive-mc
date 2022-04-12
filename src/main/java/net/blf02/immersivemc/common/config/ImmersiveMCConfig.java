package net.blf02.immersivemc.common.config;

import net.blf02.immersivemc.common.network.Network;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.ForgeConfigSpec;

public class ImmersiveMCConfig {

    public static final int CONFIG_VERSION = 1; // Increment post-release whenever the config changes

    public static final ForgeConfigSpec GENERAL_SPEC;

    public static ForgeConfigSpec.BooleanValue useBrewingImmersion;
    public static ForgeConfigSpec.BooleanValue useChestImmersion;
    public static ForgeConfigSpec.BooleanValue useCraftingImmersion;
    public static ForgeConfigSpec.BooleanValue useFurnaceImmersion;
    public static ForgeConfigSpec.BooleanValue useJukeboxImmersion;
    public static ForgeConfigSpec.BooleanValue useRangedGrab;

    static {
        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
        setupConfig(configBuilder);
        GENERAL_SPEC = configBuilder.build();
    }

    protected static void setupConfig(ForgeConfigSpec.Builder builder) {
        useBrewingImmersion = builder
                .comment("Whether immersives on brewing stands should be allowed")
                .define("brewing_immersion", true);
        useChestImmersion = builder
                .comment("Whether immersives on chests should be allowed (VR only)")
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
    }

    public static void encode(PacketBuffer buffer) {
        buffer.writeInt(Network.PROTOCOL_VERSION).writeInt(CONFIG_VERSION).
                writeBoolean(useBrewingImmersion.get())
                .writeBoolean(useChestImmersion.get()).writeBoolean(useCraftingImmersion.get())
                .writeBoolean(useFurnaceImmersion.get()).writeBoolean(useJukeboxImmersion.get())
                .writeBoolean(useRangedGrab.get());
    }


}
