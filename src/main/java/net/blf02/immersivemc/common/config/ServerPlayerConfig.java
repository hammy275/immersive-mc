package net.blf02.immersivemc.common.config;

import net.minecraft.network.FriendlyByteBuf;

public class ServerPlayerConfig {

    public static final ServerPlayerConfig EMPTY_CONFIG = new ServerPlayerConfig(false,
            false, false, false, false, false, false);

    public boolean useButtons;
    public boolean useCampfire;
    public boolean useLevers;
    public boolean useRangedGrab;
    public boolean useDoorImmersion;
    public boolean useHoeImmersion;
    public boolean canPet;

    public ServerPlayerConfig(boolean useButtons, boolean useCampfire, boolean useLevers,
                              boolean useRangedGrab, boolean useDoorImmersion, boolean useHoeImmersion,
                              boolean canPet) {
        this.useButtons = useButtons;
        this.useCampfire = useCampfire;
        this.useLevers = useLevers;
        this.useRangedGrab = useRangedGrab;
        this.useDoorImmersion = useDoorImmersion;
        this.useHoeImmersion = useHoeImmersion;
        this.canPet = canPet;
    }

    public ServerPlayerConfig(FriendlyByteBuf buffer) {
        this(buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(),
                buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean());
    }
}
