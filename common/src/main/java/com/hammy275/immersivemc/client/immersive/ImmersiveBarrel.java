package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.BarrelInfo;
import com.hammy275.immersivemc.common.immersive.ImmersiveCheckers;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.ChestShulkerOpenPacket;
import com.hammy275.immersivemc.common.network.packet.SwapPacket;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class ImmersiveBarrel extends AbstractBlockEntityImmersive<BarrelBlockEntity, BarrelInfo> {
    public ImmersiveBarrel() {
        super(4);
    }

    @Override
    public BarrelInfo getNewInfo(BlockEntity tileEnt) {
        return new BarrelInfo((BarrelBlockEntity) tileEnt);
    }

    @Override
    public int getTickTime() {
        return ClientConstants.ticksToRenderBarrel;
    }

    @Override
    protected void doTick(BarrelInfo info, boolean isInVR) {
        super.doTick(info, isInVR);
        if (info.updateHitboxes) {
            setHitboxes(info);
            info.updateHitboxes = false;
        }
    }

    @Override
    public boolean shouldRender(BarrelInfo info, boolean isInVR) {
        return info.readyToRender();
    }

    @Override
    protected void render(BarrelInfo info, PoseStack stack, boolean isInVR) {
        float itemSize = ClientConstants.itemScaleSizeChest / info.getItemTransitionCountdown();
        if (info.isOpen) {
            for (int i = 0; i < 27; i++) {
                float renderSize = info.slotHovered == i ? itemSize * 1.25f : itemSize;
                Direction neswDir = info.forward == Direction.UP || info.forward == Direction.DOWN ?
                        getForwardFromPlayer(Minecraft.getInstance().player) : info.forward;
                renderItem(info.items[i], stack, info.getPosition(i),
                        renderSize, neswDir, info.forward, info.getHitbox(i), true, -1);
            }
        }
    }

    @Override
    protected boolean enabledInConfig() {
        return true; // TODO: Replace with config entry
    }

    @Override
    public boolean shouldTrack(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        return ImmersiveCheckers.isBarrel(pos, state, tileEntity, level);
    }

    @Override
    public AbstractImmersive<? extends AbstractImmersiveInfo> getSingleton() {
        return Immersives.immersiveBarrel;
    }

    protected void setHitboxes(BarrelInfo info) {
        BarrelBlockEntity barrel = info.getBlockEntity();
        Direction facing = barrel.getBlockState().getValue(BarrelBlock.FACING);
        info.forward = facing;
        Vec3[] positionsRaw;
        Vec3[] positions = new Vec3[27];
        if (facing == Direction.UP || facing == Direction.DOWN) {
            positionsRaw = get3x3HorizontalGrid(info.getBlockPosition(), ImmersiveChest.spacing);
            if (facing == Direction.DOWN) {
                for (int i = 0; i < positionsRaw.length; i++) {
                    positionsRaw[i] = positionsRaw[i].add(0, -1, 0);
                }
            }
        } else {
            positionsRaw = get3x3VerticalGrid(info.getBlockPosition(), ImmersiveChest.spacing, facing);
        }
        int startIndex = 9 * info.getRowNum();
        float hitboxSize = ClientConstants.itemScaleSizeChest / 3f * 1.1f;
        for (int i = startIndex; i < startIndex + 9; i++) {
            positions[i] = positionsRaw[i % 9];
        }
        for (int i = 0; i < info.getAllPositions().length; i++) {
            info.setPosition(i, positions[i]);
            if (positions[i] == null) {
                info.setHitbox(i, null);
            } else {
                info.setHitbox(i, createHitbox(positions[i], hitboxSize));
            }
        }
    }

    @Override
    protected void initInfo(BarrelInfo info) {
        setHitboxes(info);
    }

    @Override
    public void handleRightClick(AbstractImmersiveInfo bInfo, Player player, int closest, InteractionHand hand) {
        BarrelInfo info = (BarrelInfo) bInfo;
        if (!info.isOpen) return;
        Network.INSTANCE.sendToServer(new SwapPacket(info.getBlockPosition(), closest, hand));
    }

    @Override
    public void onRemove(BarrelInfo info) {
        if (info.isOpen) {
            openBarrel(info);
        }
        super.onRemove(info);
    }

    @Override
    public boolean hitboxesAvailable(AbstractImmersiveInfo info) {
        return ((BarrelInfo) info).isOpen;
    }

    public static void openBarrel(BarrelInfo info) {
        info.isOpen = !info.isOpen;
        Network.INSTANCE.sendToServer(new ChestShulkerOpenPacket(info.getBlockPosition(), info.isOpen));
        if (!info.isOpen) {
            info.remove(); // Remove immersive if we're closing the chest
        }
    }

    public static BarrelInfo findImmersive(BlockEntity barrel) {
        Objects.requireNonNull(barrel);
        for (BarrelInfo info : Immersives.immersiveBarrel.getTrackedObjects()) {
            if (info.getBlockEntity() == barrel) {
                return info;
            }
        }
        return null;
    }
}
