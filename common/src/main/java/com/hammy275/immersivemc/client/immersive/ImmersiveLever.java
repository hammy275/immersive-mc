package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.api.client.ImmersiveConfigScreenInfo;
import com.hammy275.immersivemc.api.common.hitbox.HitboxInfoFactory;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.immersive.info.DragImmersiveInfo;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.NullStorage;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.UsePacket;
import com.hammy275.immersivemc.common.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ImmersiveLever extends AbstractDragImmersive {

    private static final AutoDragSettings settings = new AutoDragSettings(List.of(), false);

    @Override
    protected void hitboxDragged(DragImmersiveInfo info, int controller, int oldIndex, int newIndex) {
        Util.useLever(Minecraft.getInstance().player, info.getBlockPosition());
        Network.INSTANCE.sendToServer(new UsePacket(info.getBlockPosition()));
    }

    @Override
    protected AutoDragSettings autoDragSettings() {
        return settings;
    }

    @Override
    public void tick(DragImmersiveInfo info) {
        boolean powered = Minecraft.getInstance().level.getBlockState(info.getBlockPosition()).getValue(BlockStateProperties.POWERED);
        info.startingHitboxIndex = powered ? 1 : 0;
        super.tick(info);
    }

    @Override
    public DragImmersiveInfo buildInfo(BlockPos pos, Level level) {
        DragImmersiveInfo info = new DragImmersiveInfo(pos);
        BlockState state = level.getBlockState(info.getBlockPosition());
        Vec3 center = Vec3.atCenterOf(info.getBlockPosition());
        AttachFace attachFace = state.getValue(BlockStateProperties.ATTACH_FACE);
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction towardsBaseDir;
        Direction towardsOnDir;
        switch (attachFace) {
            case WALL -> {
                towardsBaseDir = facing.getOpposite();
                towardsOnDir = Direction.DOWN;
            }
            case CEILING -> {
                towardsBaseDir = Direction.UP;
                towardsOnDir = facing;
            }
            case FLOOR -> {
                towardsBaseDir = Direction.DOWN;
                towardsOnDir = facing;
            }
            default -> throw new IllegalStateException("Lever is attached to unknown face " + attachFace.getSerializedName());
        }
        Vec3 towardsBase = towardsBaseDir.getUnitVec3();
        Vec3 towardsOn = towardsOnDir.getUnitVec3();
        center = center.add(towardsBase.scale(0.25));

        Vec3 offPos = center.add(towardsOn.scale(-0.25));
        Vec3 onPos = center.add(towardsOn.scale(0.25));

        info.hitboxes.add(HitboxInfoFactory.instance().interactHitbox(AABB.ofSize(offPos, 0.5, 0.5, 0.5)));
        info.hitboxes.add(HitboxInfoFactory.instance().interactHitbox(AABB.ofSize(onPos, 0.5, 0.5, 0.5)));

        return info;
    }

    @Override
    public ImmersiveHandler<NullStorage> getHandler() {
        return ImmersiveHandlers.leverHandler;
    }

    @Override
    public @Nullable ImmersiveConfigScreenInfo configScreenInfo() {
        return ClientUtil.createConfigScreenInfo("lever", () -> new ItemStack(Items.LEVER),
                config -> config.useLeverImmersive,
                (config, newVal) -> config.useLeverImmersive = newVal);
    }
}
