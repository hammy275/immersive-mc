package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.ImmersiveCheckers;
import com.hammy275.immersivemc.common.network.Network;
import com.mojang.blaze3d.vertex.PoseStack;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.BrewingInfo;
import com.hammy275.immersivemc.common.network.packet.SwapPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class ImmersiveBrewing extends AbstractBlockEntityImmersive<BrewingStandBlockEntity, BrewingInfo> {

    public ImmersiveBrewing() {
        super(2);
    }

    @Override
    public BrewingInfo getNewInfo(BlockEntity tileEnt) {
        return new BrewingInfo((BrewingStandBlockEntity) tileEnt, ClientConstants.ticksToRenderBrewing);
    }

    @Override
    public int getTickTime() {
        return ClientConstants.ticksToRenderBrewing;
    }

    @Override
    public boolean shouldRender(BrewingInfo info, boolean isInVR) {
        if (Minecraft.getInstance().player == null) {
            return false;
        }
        Direction forward = getForwardFromPlayer(Minecraft.getInstance().player);
        return info.getBlockEntity().getLevel() != null &&
                info.getBlockEntity().getLevel().getBlockState(info.getBlockEntity().getBlockPos().relative(forward)).canBeReplaced()
                && info.readyToRender();
    }

    @Override
    protected void initInfo(BrewingInfo info) {
        setHitboxes(info);
    }

    protected void setHitboxes(BrewingInfo info) {
        Objects.requireNonNull(Minecraft.getInstance().player);

        BrewingStandBlockEntity stand = info.getBlockEntity();
        Direction forward = getForwardFromPlayer(Minecraft.getInstance().player);
        Vec3 pos = getDirectlyInFront(forward, stand.getBlockPos());
        Direction left = getLeftOfDirection(forward);

        Vec3 leftOffset = new Vec3(
                left.getNormal().getX() * 0.25, 0, left.getNormal().getZ() * 0.25);
        Vec3 midOffset = new Vec3(
                left.getNormal().getX() * 0.5, 0, left.getNormal().getZ() * 0.5);
        Vec3 rightOffset = new Vec3(
                left.getNormal().getX() * 0.75, 0, left.getNormal().getZ() * 0.75);


        Vec3 posLeftBottle = pos.add(leftOffset).add(0, 1d/3d, 0);
        info.setPosition(0, posLeftBottle);
        double midY = ActiveConfig.autoCenterBrewing ? 1d/3d : 0.25;
        Vec3 posMidBottle = pos.add(midOffset).add(0, midY, 0);
        info.setPosition(1, posMidBottle);
        Vec3 posRightBottle = pos.add(rightOffset).add(0, 1d/3d, 0);
        info.setPosition(2, posRightBottle);
        Vec3 posIngredient;
        Vec3 posFuel;
        if (ActiveConfig.autoCenterBrewing) {
            posIngredient = pos.add(midOffset).add(0, 0.6, 0);
            posFuel = pos.add(midOffset).add(0, 0.85, 0);
        } else {
            posIngredient = pos.add(midOffset).add(0, 0.75, 0);
            posFuel = pos.add(leftOffset).add(0, 0.75, 0);
        }
        info.setPosition(3, posIngredient);
        info.setPosition(4, posFuel);

        float hitboxSize = ClientConstants.itemScaleSizeBrewing / 3f;
        info.setHitbox(0, createHitbox(posLeftBottle, hitboxSize));
        info.setHitbox(1, createHitbox(posMidBottle, hitboxSize));
        info.setHitbox(2, createHitbox(posRightBottle, hitboxSize));
        info.setHitbox(3, createHitbox(posIngredient, hitboxSize));
        info.setHitbox(4, createHitbox(posFuel, hitboxSize));

        info.lastDir = forward;
    }

    @Override
    protected void doTick(BrewingInfo info, boolean isInVR) {
        super.doTick(info, isInVR);

        Direction forward = getForwardFromPlayer(Minecraft.getInstance().player);
        if (forward != info.lastDir) {
            setHitboxes(info);
        }
    }

    @Override
    public BlockPos getLightPos(BrewingInfo info) {
        return info.getBlockPosition().relative(info.lastDir);
    }

    @Override
    protected void render(BrewingInfo info, PoseStack stack, boolean isInVR) {
        Direction forward = getForwardFromPlayer(Minecraft.getInstance().player);

        float itemSize = ClientConstants.itemScaleSizeBrewing / info.getItemTransitionCountdown();

        for (int i = 0; i <= 4; i++) {
            float renderSize = info.slotHovered == i ? itemSize * 1.25f : itemSize;
            renderItem(info.items[i], stack, info.getPosition(i), renderSize, forward, info.getHitbox(i), i >= 3, info.light);
        }
    }

    @Override
    public boolean enabledInConfig() {
        return ActiveConfig.useBrewingImmersion;
    }

    @Override
    public boolean shouldTrack(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        return ImmersiveCheckers.isBrewingStand(pos, state, tileEntity, level);
    }

    @Override
    public boolean shouldBlockClickIfEnabled(AbstractImmersiveInfo info) {
        return true;
    }

    @Override
    public void handleRightClick(AbstractImmersiveInfo info, Player player, int closest, InteractionHand hand) {
        BrewingInfo infoB = (BrewingInfo) info;
        Network.INSTANCE.sendToServer(new SwapPacket(
                infoB.getBlockEntity().getBlockPos(), closest, hand
        ));
    }
}
