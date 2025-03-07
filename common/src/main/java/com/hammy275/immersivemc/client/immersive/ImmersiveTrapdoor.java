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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ImmersiveTrapdoor extends AbstractDragImmersive {

    private static final AutoDragSettings settings = new AutoDragSettings(List.of(2), true);

    @Override
    protected void hitboxDragged(DragImmersiveInfo info, int controller, int oldIndex, int newIndex) {
        if (newIndex == 1) {
            Util.useTrapdoor(Minecraft.getInstance().player, Minecraft.getInstance().level, info.getBlockPosition());
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
        for (int i = 0; i < 3; i++) {
            info.hitboxes.add(null);
        }
        info.startingHitboxIndex = 0;
        makeHitboxes(info, level);
        return info;
    }

    @Override
    public ImmersiveHandler<NullStorage> getHandler() {
        return ImmersiveHandlers.trapdoorHandler;
    }

    @Override
    public @Nullable ImmersiveConfigScreenInfo configScreenInfo() {
        return ClientUtil.createConfigScreenInfo("trapdoor",
                () -> new ItemStack(Items.OAK_TRAPDOOR),
                config -> config.useTrapdoorImmersive,
                (config, newVal) -> config.useTrapdoorImmersive = newVal);
    }

    @Override
    protected void makeHitboxes(DragImmersiveInfo info, Level level) {
        BlockState state = level.getBlockState(info.getBlockPosition());
        Direction trapdoorWallDir = state.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite();
        boolean closesTop = state.getValue(BlockStateProperties.HALF) == Half.TOP;
        boolean isOpen = state.getValue(BlockStateProperties.OPEN);

        VoxelShape currentShape = state.getShape(level, info.getBlockPosition());
        AABB start = currentShape.bounds().move(info.getBlockPosition()).inflate(0.0001);

        Vec3 endPos = Vec3.atBottomCenterOf(info.getBlockPosition());
        AABB end;
        if (isOpen) {
            if (closesTop) {
                endPos = endPos.add(0, 13d/16d, 0);
            } else {
                endPos = endPos.add(0, 3d/16d, 0);
            }
            end = AABB.ofSize(endPos, 1, 6d/16d, 1);
        } else {
            endPos = endPos.add(trapdoorWallDir.getUnitVec3().scale(5d/16d)).add(0, 0.5, 0);
            double xSize = trapdoorWallDir.getAxis() == Direction.Axis.X ? 6d/16d : 1;
            double zSize = xSize == 1 ? 6d/16d : 1;
            end = AABB.ofSize(endPos, xSize, 1, zSize);
        }
        end = end.inflate(0.0001);

        AABB bounds = new AABB(info.getBlockPosition()).inflate(0.001);

        info.hitboxes.set(0, HitboxInfoFactory.instance().interactHitbox(start));
        info.hitboxes.set(1, HitboxInfoFactory.instance().interactHitbox(end));
        info.hitboxes.set(2, HitboxInfoFactory.instance().interactHitbox(bounds));
    }
}
