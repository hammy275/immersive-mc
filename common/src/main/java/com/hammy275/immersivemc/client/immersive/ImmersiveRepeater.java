package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.ImmersiveCheckers;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandler;
import com.hammy275.immersivemc.common.immersive.storage.network.NetworkStorage;
import com.hammy275.immersivemc.common.network.Network;
import com.mojang.blaze3d.vertex.PoseStack;
import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.RepeaterInfo;
import com.hammy275.immersivemc.common.network.packet.SetRepeaterPacket;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class ImmersiveRepeater extends AbstractImmersive<RepeaterInfo> {

    public ImmersiveRepeater() {
        super(-1);
    }

    @Override
    public boolean isVROnly() {
        return true;
    }

    @Override
    public boolean clientAuthoritative() {
        return true; // Doesn't require swap or items, so the client can just detect it and send repeater updates when needed.
    }

    @Override
    public @Nullable ImmersiveHandler getHandler() {
        return null;
    }

    @Override
    protected void initInfo(RepeaterInfo info) {
        Objects.requireNonNull(Minecraft.getInstance().level);
        BlockState state = Minecraft.getInstance().level.getBlockState(info.getBlockPosition());

        Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
        Direction forwardDir = facing.getOpposite();
        Vec3 forward = Vec3.atLowerCornerOf(forwardDir.getNormal());
        Vec3 centerPos = getTopCenterOfBlock(info.getBlockPosition()).add(0, -0.675, 0);

        info.setPosition(0, centerPos.add(forward.multiply(1d/16d, 0, 1d/16d)));
        info.setPosition(1, centerPos.add(forward.multiply(-1d/16d, 0, -1d/16d)));
        info.setPosition(2, centerPos.add(forward.multiply(-3d/16d, 0, -3d/16d)));
        info.setPosition(3, centerPos.add(forward.multiply(-5d/16d, 0, -5d/16d)));

        for (int i = 0; i <= 3; i++) {
            info.setHitbox(i, createHitbox(info.getPosition(i), 1f/14f).inflate(0, 0.2, 0));
        }
    }

    @Override
    protected void doTick(RepeaterInfo info, boolean isInVR) {
        super.doTick(info, isInVR);

        if (!(Minecraft.getInstance().level.getBlockState(info.getBlockPosition()).getBlock() instanceof RepeaterBlock)) {
            info.remove();
            return;
        }

        if (isInVR) {
            BlockState state = Minecraft.getInstance().level.getBlockState(info.getBlockPosition());
            for (int c = 0; c <= 1; c++) {
                Vec3 pos = VRPlugin.API.getVRPlayer(Minecraft.getInstance().player).getController(c).position();
                Optional<Integer> hit = Util.getClosestIntersect(pos, info.getAllHitboxes(), info.getAllPositions());
                int repeaterValue = state.getValue(RepeaterBlock.DELAY);
                if (hit.isPresent()) {
                    int delayHit = hit.get() + 1;
                    if (delayHit == repeaterValue) {
                        info.grabbedCurrent[c] = true;
                    } else if (info.grabbedCurrent[c]) {
                        Util.setRepeater(Minecraft.getInstance().level, info.getBlockPosition(), delayHit);
                        Network.INSTANCE.sendToServer(new SetRepeaterPacket(info.getBlockPosition(), delayHit));
                    }
                } else {
                    info.grabbedCurrent[c] = false;
                }
            }
        }

    }

    @Override
    public BlockPos getLightPos(RepeaterInfo info) {
        return info.getBlockPosition().above();
    }

    @Override
    public boolean shouldRender(RepeaterInfo info, boolean isInVR) {
        if (Minecraft.getInstance().player == null) return false;
        Level level = Minecraft.getInstance().level;
        return level != null && level.getBlockState(info.getBlockPosition().above()).getMaterial().isReplaceable()
                && info.readyToRender();
    }

    @Override
    protected void render(RepeaterInfo info, PoseStack stack, boolean isInVR) {
        for (int i = 0; i <= 3; i++) {
            renderHitbox(stack, info.getHitbox(i), info.getPosition(i));
        }
    }

    @Override
    public boolean enabledInConfig() {
        return ActiveConfig.active().useRepeaterImmersion;
    }

    @Override
    protected boolean inputSlotShouldRenderHelpHitbox(RepeaterInfo info, int slotNum) {
        return false;
    }

    @Override
    public boolean shouldTrack(BlockPos pos, Level level) {
        return ImmersiveCheckers.isRepeater(pos, level);
    }

    @Override
    public RepeaterInfo refreshOrTrackObject(BlockPos pos, Level level) {
        for (RepeaterInfo info : getTrackedObjects()) {
            if (info.getBlockPosition().equals(pos)) {
                info.setTicksLeft(ClientConstants.ticksToRenderRepeater);
                return info;
            }
        }
        RepeaterInfo newInfo = new RepeaterInfo(pos);
        infos.add(newInfo);
        return newInfo;
    }

    @Override
    public boolean shouldBlockClickIfEnabled(AbstractImmersiveInfo info) {
        return true; // VR Only check is done before this is called
    }

    @Override
    public void handleRightClick(AbstractImmersiveInfo info, Player player, int closest, InteractionHand hand) {
        // NOOP. Handled in doTick().
    }

    @Override
    public void processStorageFromNetwork(AbstractImmersiveInfo info, NetworkStorage storage) {
        // Intentional NO-OP
    }
}
