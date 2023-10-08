package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.BarrelInfo;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.immersive.ImmersiveCheckers;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.ChestShulkerOpenPacket;
import com.hammy275.immersivemc.common.network.packet.SwapPacket;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import com.hammy275.immersivemc.common.vr.VRRumble;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class ImmersiveBarrel extends AbstractBlockEntityImmersive<BarrelBlockEntity, BarrelInfo> {
    public static final double MOVE_THRESHOLD = 0.045;

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
        if (info.placeItemCooldown > 0) {
            info.placeItemCooldown--;
        }
        if (info.updateHitboxes) {
            setHitboxes(info);
            info.updateHitboxes = false;
        }

        if (!info.isOpen && info.pullHitbox != null && VRPluginVerify.clientInVR()
            && VRPlugin.API.apiActive(Minecraft.getInstance().player)) {
            for (int c = 0; c <= 1; c++) {
                Vec3 current = VRPlugin.API.getVRPlayer(Minecraft.getInstance().player).getController(c).position();
                if (info.lastControllerPos[c] != null && info.lastPlayerPos != null) {
                    Vec3 last = info.lastControllerPos[c];
                    Vec3 change = new Vec3(
                      current.x - last.x,
                      current.y - last.y,
                      current.z - last.z
                    );
                    Vec3 playerPos = playerPos();
                    Vec3 playerPosChange = new Vec3(
                            playerPos.x - info.lastPlayerPos.x,
                            playerPos.y - info.lastPlayerPos.y,
                            playerPos.z - info.lastPlayerPos.z
                    );
                    change = change.subtract(playerPosChange);
                    Direction dir = ClientUtil.getClosestDirection(change);
                    double moveAmount = Math.max(
                            Math.abs(change.x),
                            Math.max(Math.abs(change.y), Math.abs(change.z)));
                    if (info.pullHitbox.contains(current) &&
                            dir == info.forward && moveAmount >= MOVE_THRESHOLD) {
                        info.placeItemCooldown = 10; // Used since barrel handle is by an item spot
                        VRRumble.rumbleIfVR(null, c, CommonConstants.vibrationTimeWorldInteraction);
                        openBarrel(info);
                    }
                }
                info.lastControllerPos[c] = current;
                info.lastPlayerPos = playerPos();
            }
        }
    }

    @Override
    public BlockPos getLightPos(BarrelInfo info) {
        return info.getBlockPosition().relative(info.forward);
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
                        renderSize, neswDir, info.forward, info.getHitbox(i), true, -1, info.light);
            }
        } else {
            if (info.pullHitbox != null) {
                renderHitbox(stack, info.pullHitbox, info.pullHitbox.getCenter());
            }
        }
    }

    @Override
    public boolean enabledInConfig() {
        return ActiveConfig.useBarrelImmersion;
    }

    @Override
    public boolean shouldTrack(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        return ImmersiveCheckers.isBarrel(pos, state, tileEntity, level);
    }

    @Override
    public boolean shouldBlockClickIfEnabled(AbstractImmersiveInfo info) {
        return true;
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

            Vec3 topCenter = getTopCenterOfBlock(info.getBlockPosition());
            // Handle always on the west side. Also bump up a bit since handle is off-center
            info.pullHitbox = AABB.ofSize(topCenter.add(-0.25, 0.15, 1d/16d),
                    0.35, 0.5, 0.35);
        } else {
            positionsRaw = get3x3VerticalGrid(info.getBlockPosition(), ImmersiveChest.spacing, facing);
            Vec3 forwardBotLeft = getDirectlyInFront(facing, info.getBlockPosition());
            Direction handleDir = facing.getCounterClockWise();
            Vec3 handleDirVec = new Vec3(handleDir.getNormal().getX(), handleDir.getNormal().getY(),
                    handleDir.getNormal().getZ());
            Vec3 forwardVec = new Vec3(facing.getNormal().getX(), facing.getNormal().getY(),
                    facing.getNormal().getZ());
            Vec3 pos = forwardBotLeft.add(handleDirVec.scale(0.75))
                    .add(0, 0.5625, 0).add(forwardVec.scale(0.15));
            double xSize = facing.getAxis() == Direction.Axis.X ? 0.5 : 0.35;
            double zSize = facing.getAxis() == Direction.Axis.Z ? 0.5 : 0.35;
            info.pullHitbox = AABB.ofSize(pos, xSize, 0.35, zSize);

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
        if (!info.isOpen || info.placeItemCooldown > 0) return;
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
