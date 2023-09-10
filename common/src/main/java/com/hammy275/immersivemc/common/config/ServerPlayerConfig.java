package com.hammy275.immersivemc.common.config;

import net.minecraft.network.FriendlyByteBuf;

public class ServerPlayerConfig {

    public static final ServerPlayerConfig EMPTY_CONFIG = new ServerPlayerConfig(false,
            false, false, false, false,
            false, false, false, false, 0,
            false, false, false, false);

    public boolean useButtons;
    public boolean useCampfire;
    public boolean useLevers;
    public boolean useRangedGrab;
    public boolean useDoorImmersion;
    public boolean canPet;
    public boolean useArmorImmersion;
    public boolean canFeedAnimals;
    public boolean canPetAnyLiving;
    public int rangedGrabRange;
    public boolean crouchBypassImmersion;
    public boolean doRumble;
    public boolean returnItems;
    public boolean useCauldronImmersion;

    public ServerPlayerConfig(boolean useButtons, boolean useCampfire, boolean useLevers,
                              boolean useRangedGrab, boolean useDoorImmersion,
                              boolean canPet, boolean useArmorImmersion, boolean canFeedAnimals,
                              boolean canPetAnyLiving, int rangedGrabRange, boolean crouchBypassImmersion,
                              boolean doRumble, boolean returnItems, boolean useCauldronImmersion) {
        this.useButtons = useButtons && ActiveConfig.useButton;
        this.useCampfire = useCampfire && ActiveConfig.useCampfireImmersion;
        this.useLevers = useLevers && ActiveConfig.useLever;
        this.useRangedGrab = useRangedGrab && ActiveConfig.useRangedGrab;
        this.useDoorImmersion = useDoorImmersion && ActiveConfig.useDoorImmersion;
        this.canPet = canPet && ActiveConfig.canPet;
        this.useArmorImmersion = useArmorImmersion && ActiveConfig.useArmorImmersion;
        this.canFeedAnimals = canFeedAnimals && ActiveConfig.canFeedAnimals;
        this.canPetAnyLiving = canPetAnyLiving && ActiveConfig.canPetAnyLiving;
        this.rangedGrabRange = Math.min(rangedGrabRange, ActiveConfig.rangedGrabRange);
        this.crouchBypassImmersion = crouchBypassImmersion && ActiveConfig.crouchBypassImmersion;
        this.doRumble = doRumble; // Don't check server for this one, no need to allow admins to disable rumble.
        this.returnItems = returnItems;
        this.useCauldronImmersion = useCauldronImmersion && ActiveConfig.useCauldronImmersion;
    }

    public ServerPlayerConfig(FriendlyByteBuf buffer) {
        this(buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(),
                buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(),
                buffer.readBoolean(), buffer.readBoolean(), buffer.readInt(), buffer.readBoolean(),
                buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean());
        buffer.release();
    }
}
