package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.JukeboxInfo;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.ImmersiveCheckers;
import com.hammy275.immersivemc.common.network.Network;
import com.mojang.blaze3d.vertex.PoseStack;
import com.hammy275.immersivemc.common.network.packet.SwapPacket;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ImmersiveJukebox extends AbstractBlockEntityImmersive<JukeboxBlockEntity, JukeboxInfo> {

    public ImmersiveJukebox() {
        super(1);
    }

    @Override
    public boolean isVROnly() {
        return true;
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
    protected boolean inputSlotShouldRenderHelpHitbox(JukeboxInfo info, int slotNum) {
        return false;
    }

    @Override
    public boolean shouldTrack(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        return ImmersiveCheckers.isJukebox(pos, state, tileEntity, level);
    }

    @Override
    public boolean shouldBlockClickIfEnabled(AbstractImmersiveInfo info) {
        if (Minecraft.getInstance().level.getBlockEntity(info.getBlockPosition()) instanceof JukeboxBlockEntity) {
            // Check hand, since client can't see record in jukebox
            // Side effects: Mods that don't use RecordItem won't be blocked and holding a record makes it impossible
            // to take one out.
            return Minecraft.getInstance().player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof RecordItem;
        }
        return false; // Not actually a jukebox?!
    }

    @Override
    public boolean enabledInConfig() {
        return ActiveConfig.useJukeboxImmersion;
    }

    @Override
    public void handleRightClick(AbstractImmersiveInfo info, Player player, int closest, InteractionHand hand) {
        if (!VRPluginVerify.clientInVR()) return;
        Network.INSTANCE.sendToServer(new SwapPacket(info.getBlockPosition(), 0, hand));
    }

    @Override
    public BlockPos getLightPos(JukeboxInfo info) {
        return info.getBlockPosition().above();
    }

    @Override
    public JukeboxInfo getNewInfo(BlockEntity tileEnt) {
        return new JukeboxInfo((JukeboxBlockEntity) tileEnt, getTickTime());
    }

    @Override
    public int getTickTime() {
        return ClientConstants.ticksToHandleJukebox;
    }

    @Override
    public boolean shouldRender(JukeboxInfo info, boolean isInVR) {
        return info.getBlockEntity().getLevel() != null &&
                info.getBlockEntity().getLevel().getBlockState(info.getBlockEntity().getBlockPos().relative(Direction.UP)).canBeReplaced() &&
                info.readyToRender();
    }
}
