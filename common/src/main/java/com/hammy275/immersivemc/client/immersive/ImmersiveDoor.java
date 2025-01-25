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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ImmersiveDoor extends AbstractDragImmersive {

    private static final AutoDragSettings settings = new AutoDragSettings(List.of(), true);

    @Override
    protected void hitboxDragged(DragImmersiveInfo info, int controller, int oldIndex, int newIndex) {
        if (newIndex == 1) {
            Util.useDoor(Minecraft.getInstance().player, Minecraft.getInstance().level, info.getBlockPosition());
            Network.INSTANCE.sendToServer(new UsePacket(info.getBlockPosition()));
        }
    }

    @Override
    protected AutoDragSettings autoDragSettings() {
        return settings;
    }

    @Override
    public DragImmersiveInfo buildInfo(BlockPos pos, Level level) {
        DragImmersiveInfo info = new DragImmersiveInfo(pos);
        for (int i = 0; i < 2; i++) {
            info.hitboxes.add(null);
        }
        info.startingHitboxIndex = 0;
        makeHitboxes(info, level);
        return info;
    }

    @Override
    protected void makeHitboxes(DragImmersiveInfo info, Level level) {
        BlockPos pos = info.getBlockPosition();
        BlockState state = level.getBlockState(pos);

        VoxelShape currentShape = state.getShape(level, pos);
        AABB start = currentShape.bounds().move(pos).inflate(0.1);
        AABB outer = new AABB(pos).inflate(0.0001);
        info.hitboxes.set(0, HitboxInfoFactory.instance().interactHitbox(start));
        info.hitboxes.set(1, HitboxInfoFactory.instance().interactHitbox(outer));
    }

    @Override
    public ImmersiveHandler<NullStorage> getHandler() {
        return ImmersiveHandlers.doorHandler;
    }

    @Override
    @Nullable
    public ImmersiveConfigScreenInfo configScreenInfo() {
        return ClientUtil.createConfigScreenInfo("door",
                () -> new ItemStack(Items.OAK_DOOR),
                config -> config.useDoorImmersive,
                (config, newVal) -> config.useDoorImmersive = newVal);
    }
}
