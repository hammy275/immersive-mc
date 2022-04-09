package net.blf02.immersivemc.client.immersive;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.blf02.immersivemc.client.config.ClientConfig;
import net.blf02.immersivemc.client.immersive.info.CraftingInfo;
import net.blf02.immersivemc.client.storage.ClientStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ImmersiveCrafting extends AbstractImmersive<CraftingInfo> {

    public static final ImmersiveCrafting singleton = new ImmersiveCrafting();
    private final double spacing = 3d/16d;

    @Override
    public void tick(CraftingInfo info, boolean isInVR) {
        super.tick(info, isInVR);
        Objects.requireNonNull(Minecraft.getInstance().player);

        if (info.tablePos != null &&
                Minecraft.getInstance().player.distanceToSqr(Vector3d.atCenterOf(info.tablePos)) >
                        ClientConfig.distanceSquaredToRemoveImmersive) {
            info.remove();
        }

        Direction forward = getForwardFromPlayer(Minecraft.getInstance().player);
        Vector3d pos = getTopCenterOfBlock(info.tablePos);
        Direction left = getLeftOfDirection(forward);

        List<ItemStack> slots = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            slots.add(ClientStorage.craftingStorage[i]);
        }

        Vector3d leftOffset = new Vector3d(
                left.getNormal().getX() * spacing, 0, left.getNormal().getZ() * spacing);
        Vector3d rightOffset = new Vector3d(
                left.getNormal().getX() * -spacing, 0, left.getNormal().getZ() * -spacing);

        Vector3d topOffset = new Vector3d(
                forward.getNormal().getX() * -spacing, 0, forward.getNormal().getZ() * -spacing);
        Vector3d botOffset = new Vector3d(
                forward.getNormal().getX() * spacing, 0, forward.getNormal().getZ() * spacing);


        Vector3d[] positions = new Vector3d[]{
                pos.add(leftOffset).add(topOffset), pos.add(topOffset), pos.add(rightOffset).add(topOffset),
                pos.add(leftOffset), pos, pos.add(rightOffset),
                pos.add(leftOffset).add(botOffset), pos.add(botOffset), pos.add(rightOffset).add(botOffset)
        };
        float hitboxSize = ClientConfig.itemScaleSizeCrafting / 3f;
        for (int i = 0; i < 9; i++) {
            info.setPosition(i, positions[i]);
            info.setHitbox(i, createHitbox(positions[i], hitboxSize));
        }

    }

    @Override
    protected void render(CraftingInfo info, MatrixStack stack, boolean isInVR) {
        float itemSize = ClientConfig.itemScaleSizeCrafting / info.getCountdown();
        Direction forward = getForwardFromPlayer(Minecraft.getInstance().player);

        for (int i = 0; i < 9; i++) {
            renderItem(ClientStorage.craftingStorage[i], stack, info.getPosition(i),
                    itemSize, forward, Direction.UP, info.getHibtox(i));
        }

    }

    @Override
    public boolean shouldRender(CraftingInfo info, boolean isInVR) {
        if (Minecraft.getInstance().player == null) return false;
        Direction forward = getForwardFromPlayer(Minecraft.getInstance().player);
        World level = Minecraft.getInstance().level;
        return level != null && level.getBlockState(info.tablePos.relative(forward)).isAir()
                && info.readyToRender();
    }

    public void trackObject(BlockPos tablePos) {
        for (CraftingInfo info : getTrackedObjects()) {
            if (info.tablePos.equals(tablePos)) {
                info.setTicksLeft(ClientConfig.ticksToRenderCrafting);
                return;
            }
        }
        infos.add(new CraftingInfo(tablePos, ClientConfig.ticksToRenderCrafting));
    }
}
