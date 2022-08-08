package net.blf02.immersivemc.client.immersive;

import com.mojang.blaze3d.vertex.PoseStack;
import net.blf02.immersivemc.client.config.ClientConstants;
import net.blf02.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import net.blf02.immersivemc.client.immersive.info.JukeboxInfo;
import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.packet.SwapPacket;
import net.blf02.immersivemc.common.vr.VRPluginVerify;
import net.minecraft.core.Direction;
import net.minecraft.entity.player.Player;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ImmersiveJukebox extends AbstractBlockEntityImmersive<JukeboxBlockEntity, JukeboxInfo> {
    
    protected static ImmersiveJukebox singleton = new ImmersiveJukebox();

    public ImmersiveJukebox() {
        super(1);
    }

    public static ImmersiveJukebox getSingleton() {
        return singleton;
    }

    @Override
    protected void initInfo(JukeboxInfo info) {
        Vec3 topCenter = getTopCenterOfBlock(info.getBlockEntity().getBlockPos());
        info.setPosition(0, topCenter);
        // North and south
        AABB hitbox = createHitbox(topCenter, 1f/16f);
        hitbox = hitbox.inflate(0, 0, 0.25); // Rectangular hitbox that covers disc slot
        info.setHitbox(0, hitbox);
    }

    @Override
    protected void render(JukeboxInfo info, PoseStack stack, boolean isInVR) {
        renderHitbox(stack, info.getHitbox(0), info.getPosition(0));
    }

    @Override
    protected boolean slotShouldRenderHelpHitbox(JukeboxInfo info, int slotNum) {
        return false;
    }

    @Override
    protected boolean enabledInConfig() {
        return ActiveConfig.useJukeboxImmersion;
    }

    @Override
    public void handleRightClick(AbstractImmersiveInfo info, Player player, int closest, HumanoidArm hand) {
        if (!VRPluginVerify.clientInVR) return;
        Network.INSTANCE.sendToServer(new SwapPacket(info.getBlockPosition(), 0, hand));
    }

    @Override
    public JukeboxInfo getNewInfo(JukeboxBlockEntity tileEnt) {
        return new JukeboxInfo(tileEnt, ClientConstants.ticksToHandleJukebox);
    }

    @Override
    public int getTickTime() {
        return ClientConstants.ticksToHandleJukebox;
    }

    @Override
    public boolean hasValidBlock(JukeboxInfo info, Level level) {
        return level.getBlockEntity(info.getBlockPosition()) instanceof JukeboxBlockEntity;
    }

    @Override
    public boolean shouldRender(JukeboxInfo info, boolean isInVR) {
        return info.getBlockEntity().getLevel() != null &&
                info.getBlockEntity().getLevel().getBlockState(info.getBlockEntity().getBlockPos().relative(Direction.UP)).isAir() &&
                info.readyToRender();
    }
}
