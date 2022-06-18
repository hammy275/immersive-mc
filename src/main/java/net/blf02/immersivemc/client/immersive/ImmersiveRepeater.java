package net.blf02.immersivemc.client.immersive;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.blf02.immersivemc.client.config.ClientConstants;
import net.blf02.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import net.blf02.immersivemc.client.immersive.info.RepeaterInfo;
import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.packet.SetRepeaterPacket;
import net.blf02.immersivemc.common.util.Util;
import net.blf02.immersivemc.common.vr.VRPlugin;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.Objects;
import java.util.Optional;

public class ImmersiveRepeater extends AbstractImmersive<RepeaterInfo> {

    public static final ImmersiveRepeater singelton = new ImmersiveRepeater();

    public ImmersiveRepeater() {
        super(2); // You really only interact with one repeater at a time, so 2 at most makes sense
    }

    @Override
    protected void initInfo(RepeaterInfo info) {
        Objects.requireNonNull(Minecraft.getInstance().level);
        BlockState state = Minecraft.getInstance().level.getBlockState(info.getBlockPosition());

        Direction facing = state.getValue(HorizontalBlock.FACING);
        Direction forwardDir = facing.getOpposite();
        Vector3d forward = Vector3d.atLowerCornerOf(forwardDir.getNormal());
        Vector3d centerPos = getTopCenterOfBlock(info.getBlockPosition()).add(0, -0.675, 0);

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
                Vector3d pos = VRPlugin.API.getVRPlayer(Minecraft.getInstance().player).getController(c).position();
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
    public boolean hasValidBlock(RepeaterInfo info, World level) {
        return level.getBlockState(info.getBlockPosition()).getBlock() instanceof RepeaterBlock;
    }

    @Override
    public boolean shouldRender(RepeaterInfo info, boolean isInVR) {
        if (Minecraft.getInstance().player == null) return false;
        World level = Minecraft.getInstance().level;
        return level != null && level.getBlockState(info.getBlockPosition().above()).isAir()
                && info.readyToRender();
    }

    @Override
    protected void render(RepeaterInfo info, MatrixStack stack, boolean isInVR) {
        for (int i = 0; i <= 3; i++) {
            renderHitbox(stack, info.getHibtox(i), info.getPosition(i));
        }
    }

    @Override
    protected boolean enabledInConfig() {
        return ActiveConfig.useRepeaterImmersion;
    }

    @Override
    protected boolean inputSlotHasItem(RepeaterInfo info, int slotNum) {
        return false;
    }

    @Override
    public void handleRightClick(AbstractImmersiveInfo info, PlayerEntity player, int closest, Hand hand) {
        // NOOP. Handled in doTick().
    }

    public void trackObject(BlockPos pos) {
        for (RepeaterInfo info : getTrackedObjects()) {
            if (info.getBlockPosition().equals(pos)) {
                info.setTicksLeft(ClientConstants.ticksToRenderRepeater);
                return;
            }
        }
        infos.add(new RepeaterInfo(pos));
    }
}
