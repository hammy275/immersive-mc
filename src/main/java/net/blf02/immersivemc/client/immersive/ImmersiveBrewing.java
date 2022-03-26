package net.blf02.immersivemc.client.immersive;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.blf02.immersivemc.client.config.ClientConfig;
import net.blf02.immersivemc.client.immersive.info.BrewingInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.BrewingStandTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

public class ImmersiveBrewing extends AbstractTileEntityImmersive<BrewingStandTileEntity, BrewingInfo> {

    protected static final ImmersiveBrewing singleton = new ImmersiveBrewing();

    public static ImmersiveBrewing getSingleton() {
        return singleton;
    }

    @Override
    public BrewingInfo getNewInfo(BrewingStandTileEntity tileEnt) {
        return new BrewingInfo(tileEnt, ClientConfig.ticksToRenderBrewing);
    }

    @Override
    public int getTickTime() {
        return ClientConfig.ticksToRenderBrewing;
    }

    @Override
    public boolean shouldHandleImmersion(BrewingInfo info) {
        if (Minecraft.getInstance().player == null) {
            return false;
        }
        Direction forward = getForwardFromPlayer(Minecraft.getInstance().player);
        return info.getTileEntity().getLevel() != null &&
                info.getTileEntity().getLevel().getBlockState(info.getTileEntity().getBlockPos().relative(forward)).isAir();
    }

    @Override
    protected void handleImmersion(BrewingInfo info, MatrixStack stack) {
        super.handleImmersion(info, stack);

        BrewingStandTileEntity stand = info.getTileEntity();
        Direction forward = getForwardFromPlayer(Minecraft.getInstance().player);
        Vector3d pos = getDirectlyInFront(forward, stand.getBlockPos());
        Direction left = getLeftOfDirection(forward);


        Vector3d leftOffset = new Vector3d(
                left.getNormal().getX() * 0.25, 0, left.getNormal().getZ() * 0.25);
        Vector3d midOffset = new Vector3d(
                left.getNormal().getX() * 0.5, 0, left.getNormal().getZ() * 0.5);
        Vector3d rightOffset = new Vector3d(
                left.getNormal().getX() * 0.75, 0, left.getNormal().getZ() * 0.75);

        ItemStack[] bottles = new ItemStack[]{stand.getItem(0), stand.getItem(1), stand.getItem(2)};
        ItemStack ingredient = stand.getItem(3);
        ItemStack fuel = stand.getItem(4);

        float size = ClientConfig.itemScaleSizeBrewing / info.getCountdown();

        Vector3d posLeftBottle = pos.add(leftOffset).add(0, 1d/3d, 0);
        Vector3d posMidBottle = pos.add(midOffset).add(0, 0.25, 0);
        Vector3d posRightBottle = pos.add(rightOffset).add(0, 1d/3d, 0);
        Vector3d posIngredient = pos.add(midOffset).add(0, 0.75, 0);
        Vector3d posFuel = pos.add(leftOffset).add(0, 0.75, 0);

        float hitboxSize = ClientConfig.itemScaleSizeBrewing / 3f;
        info.setHitbox(0, createHitbox(posLeftBottle, hitboxSize));
        info.setHitbox(1, createHitbox(posMidBottle, hitboxSize));
        info.setHitbox(2, createHitbox(posRightBottle, hitboxSize));
        info.setHitbox(3, createHitbox(posIngredient, hitboxSize));
        info.setHitbox(4, createHitbox(posFuel, hitboxSize));

        renderItem(bottles[0], stack, posLeftBottle, size, forward, info.getHibtox(0));
        renderItem(bottles[1], stack, posMidBottle, size, forward, info.getHibtox(1));
        renderItem(bottles[2], stack, posRightBottle, size, forward, info.getHibtox(2));
        renderItem(ingredient, stack, posIngredient, size, forward, info.getHibtox(3));
        renderItem(fuel, stack, posFuel, size, forward, info.getHibtox(4));
    }
}
