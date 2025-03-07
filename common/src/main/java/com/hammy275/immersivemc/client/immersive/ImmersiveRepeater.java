package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.api.client.ImmersiveConfigScreenInfo;
import com.hammy275.immersivemc.api.common.hitbox.HitboxInfoFactory;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.immersive.info.DragImmersiveInfo;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.NullStorage;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.SetRepeaterPacket;
import com.hammy275.immersivemc.common.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ImmersiveRepeater extends AbstractDragImmersive {

    private static final AutoDragSettings settings = new AutoDragSettings(List.of(), false);

    @Override
    protected void hitboxDragged(DragImmersiveInfo info, int controller, int oldIndex, int newIndex) {
        int newDelay = newIndex + 1;
        Util.setRepeater(Minecraft.getInstance().level, info.getBlockPosition(), newDelay);
        Network.INSTANCE.sendToServer(new SetRepeaterPacket(info.getBlockPosition(), newDelay));
    }


    @Override
    protected AutoDragSettings autoDragSettings() {
        return settings;
    }

    @Override
    public void tick(DragImmersiveInfo info) {
        BlockState state = Minecraft.getInstance().level.getBlockState(info.getBlockPosition());
        int repeaterValue = state.getValue(RepeaterBlock.DELAY);
        info.startingHitboxIndex = repeaterValue - 1; // Delay is ranged 1-4, but hitboxes are indexed 0-3.
        super.tick(info);
    }

    @Override
    public ImmersiveHandler<NullStorage> getHandler() {
        return ImmersiveHandlers.repeaterHandler;
    }

    @Override
    protected void makeHitboxes(DragImmersiveInfo info, Level level) {
        BlockState state = Minecraft.getInstance().level.getBlockState(info.getBlockPosition());
        Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
        Direction forwardDir = facing.getOpposite();
        Vec3 forward = forwardDir.getUnitVec3();
        Vec3 centerPos = Vec3.upFromBottomCenterOf(info.getBlockPosition(), 1).add(0, -0.675, 0);

        info.hitboxes.add(HitboxInfoFactory.instance().interactHitbox(
                AABB.ofSize(centerPos.add(forward.multiply(1d/16d, 0, 1d/16d)),
                1f/7f, 1f/7f, 1f/7f).inflate(0, 0.2, 0)));
        info.hitboxes.add(HitboxInfoFactory.instance().interactHitbox(
                AABB.ofSize(centerPos.add(forward.multiply(-1d/16d, 0, -1d/16d)),
                1f/7f, 1f/7f, 1f/7f).inflate(0, 0.2, 0)));
        info.hitboxes.add(HitboxInfoFactory.instance().interactHitbox(
                AABB.ofSize(centerPos.add(forward.multiply(-3d/16d, 0, -3d/16d)),
                1f/7f, 1f/7f, 1f/7f).inflate(0, 0.2, 0)));
        info.hitboxes.add(HitboxInfoFactory.instance().interactHitbox(
                AABB.ofSize(centerPos.add(forward.multiply(-5d/16d, 0, -5d/16d)),
                1f/7f, 1f/7f, 1f/7f).inflate(0, 0.2, 0)));
    }

    @Override
    public DragImmersiveInfo buildInfo(BlockPos pos, Level level) {
        DragImmersiveInfo info = new DragImmersiveInfo(pos);
        makeHitboxes(info, level);
        return info;
    }

    @Override
    public @Nullable ImmersiveConfigScreenInfo configScreenInfo() {
        return ClientUtil.createConfigScreenInfo("repeater", () -> new ItemStack(Items.REPEATER),
                config -> config.useRepeaterImmersive,
                (config, newVal) -> config.useRepeaterImmersive = newVal);
    }
}
