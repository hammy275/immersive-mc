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

public class ImmersiveCrafting extends AbstractImmersive<CraftingInfo> {

    public static final ImmersiveCrafting singleton = new ImmersiveCrafting();

    @Override
    protected void handleImmersion(CraftingInfo info, MatrixStack stack) {
        super.handleImmersion(info, stack);
        Direction forward = getForwardFromPlayer(Minecraft.getInstance().player);
        Vector3d pos = getDirectlyInFront(forward, info.tablePos);
        Direction left = getLeftOfDirection(forward);

        Vector3d leftOffset = new Vector3d(
                left.getNormal().getX() * 0.25, 0, left.getNormal().getZ() * 0.25);
        Vector3d midXOffset = new Vector3d(
                left.getNormal().getX() * 0.5, 0, left.getNormal().getZ() * 0.5);
        Vector3d rightOffset = new Vector3d(
                left.getNormal().getX() * 0.75, 0, left.getNormal().getZ() * 0.75);

        Vector3d topOffset = new Vector3d(0, 0.75, 0);
        Vector3d midYOffset = new Vector3d(0, 0.5, 0);
        Vector3d botOffset = new Vector3d(0, 0.25, 0);


        List<ItemStack> slots = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            slots.add(ClientStorage.craftingStorage.getItem(i));
        }

        Vector3d[] positions = new Vector3d[]{
                pos.add(leftOffset).add(topOffset), pos.add(midXOffset).add(topOffset), pos.add(rightOffset).add(topOffset),
                pos.add(leftOffset).add(midYOffset), pos.add(midXOffset).add(midYOffset), pos.add(rightOffset).add(midYOffset),
                pos.add(leftOffset).add(botOffset), pos.add(midXOffset).add(botOffset), pos.add(rightOffset).add(botOffset)
        };

        float itemSize = ClientConfig.itemScaleSizeCrafting / info.getCountdown();
        float hitboxSize = ClientConfig.itemScaleSizeCrafting / 3f;

        for (int i = 0; i < 9; i++) {
            info.setHitbox(i, createHitbox(positions[i], hitboxSize));
            renderItem(ClientStorage.craftingStorage.getItem(i), stack, positions[i],
                    itemSize, forward, info.getHibtox(i));
        }

    }

    @Override
    public boolean shouldHandleImmersion(CraftingInfo info) {
        if (Minecraft.getInstance().player == null) return false;
        Direction forward = getForwardFromPlayer(Minecraft.getInstance().player);
        World level = Minecraft.getInstance().level;
        return level != null && level.getBlockState(info.tablePos.relative(forward)).isAir();
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
