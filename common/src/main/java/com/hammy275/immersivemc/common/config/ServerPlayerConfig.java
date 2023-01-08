package com.hammy275.immersivemc.common.config;

import net.minecraft.network.FriendlyByteBuf;

public class ServerPlayerConfig {

    public static final ServerPlayerConfig EMPTY_CONFIG = new ServerPlayerConfig(false,
            false, false, false, false, false,
            false, false, false, false, 0, false);

    public boolean useButtons;
    public boolean useCampfire;
    public boolean useLevers;
    public boolean useRangedGrab;
    public boolean useDoorImmersion;
    public boolean useHoeImmersion;
    public boolean canPet;
    public boolean useArmorImmersion;
    public boolean canFeedAnimals;
    public boolean canPetAnyLiving;
    public int rangedGrabRange;
    public boolean crouchBypassImmersion;

    public ServerPlayerConfig(boolean useButtons, boolean useCampfire, boolean useLevers,
                              boolean useRangedGrab, boolean useDoorImmersion, boolean useHoeImmersion,
                              boolean canPet, boolean useArmorImmersion, boolean canFeedAnimals,
                              boolean canPetAnyLiving, int rangedGrabRange, boolean crouchBypassImmersion) {
        this.useButtons = useButtons && ActiveConfig.useButton;
        this.useCampfire = useCampfire && ActiveConfig.useCampfireImmersion;
        this.useLevers = useLevers && ActiveConfig.useLever;
        this.useRangedGrab = useRangedGrab && ActiveConfig.useRangedGrab;
        this.useDoorImmersion = useDoorImmersion && ActiveConfig.useDoorImmersion;
        this.useHoeImmersion = useHoeImmersion && ActiveConfig.useHoeImmersion;
        this.canPet = canPet && ActiveConfig.canPet;
        this.useArmorImmersion = useArmorImmersion && ActiveConfig.useArmorImmersion;
        this.canFeedAnimals = canFeedAnimals && ActiveConfig.canFeedAnimals;
        this.canPetAnyLiving = canPetAnyLiving && ActiveConfig.canPetAnyLiving;
        this.rangedGrabRange = Math.min(rangedGrabRange, ActiveConfig.rangedGrabRange);
        this.crouchBypassImmersion = crouchBypassImmersion && ActiveConfig.crouchBypassImmersion;
    }

    public ServerPlayerConfig(FriendlyByteBuf buffer) {
        this(buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(),
                buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(),
                buffer.readBoolean(), buffer.readBoolean(), buffer.readInt(), buffer.readBoolean());
        buffer.release();
    }
}
