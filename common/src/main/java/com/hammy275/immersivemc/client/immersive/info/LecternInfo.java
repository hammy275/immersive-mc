package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.api.client.immersive.ImmersiveInfo;
import com.hammy275.immersivemc.api.common.hitbox.HitboxInfo;
import com.hammy275.immersivemc.api.common.hitbox.HitboxInfoFactory;
import com.hammy275.immersivemc.client.immersive_item.info.ClientBookData;
import com.hammy275.immersivemc.common.util.PosRot;
import com.hammy275.immersivemc.common.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.List;

public class LecternInfo implements ImmersiveInfo {

    public ClientBookData bookData = new ClientBookData();
    public long tickCount = 0;
    public PosRot posRot;
    protected BlockPos pos;

    public LecternInfo(BlockPos pos, Level level) {
        this.pos = pos;
        Direction direction = level.getBlockState(pos).getValue(BlockStateProperties.HORIZONTAL_FACING);

        Vec3 renderPos = Vec3.atBottomCenterOf(pos).add(0, 1, 0)
                .add(Vec3.atLowerCornerOf(direction.getNormal()).scale(0.1));
        posRot = new PosRot(renderPos, Util.getLookAngle((float) -Math.PI / 8f, (float) -Math.toRadians(direction.getOpposite().toYRot())),
                22.5f,
                direction.getOpposite().toYRot(), 0);
    }

    @Override
    public List<? extends HitboxInfo> getAllHitboxes() {
        return Arrays.stream(bookData.pageTurnBoxes)
                .map(obb -> HitboxInfoFactory.instance().interactHitbox(obb))
                .toList();
    }

    @Override
    public boolean hasHitboxes() {
        return bookData.pageTurnBoxes[2] != null;
    }

    @Override
    public BlockPos getBlockPosition() {
        return pos;
    }

    @Override
    public void setSlotHovered(int hitboxIndex, int handIndex) {
        // Intentional no-op. No slots can be hovered, since we don't hold any items.
    }

    @Override
    public int getSlotHovered(int handIndex) {
        return -1;
    }

    @Override
    public long getTicksExisted() {
        return tickCount;
    }
}
