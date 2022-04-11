package net.blf02.immersivemc.client.immersive;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.blf02.immersivemc.client.config.ClientConstants;
import net.blf02.immersivemc.client.immersive.info.BrewingInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.BrewingStandTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

public class ImmersiveBrewing extends AbstractTileEntityImmersive<BrewingStandTileEntity, BrewingInfo> {

    protected static final ImmersiveBrewing singleton = new ImmersiveBrewing();

    public ImmersiveBrewing() {
        super(2);
    }

    public static ImmersiveBrewing getSingleton() {
        return singleton;
    }

    @Override
    public BrewingInfo getNewInfo(BrewingStandTileEntity tileEnt) {
        return new BrewingInfo(tileEnt, ClientConstants.ticksToRenderBrewing);
    }

    @Override
    public int getTickTime() {
        return ClientConstants.ticksToRenderBrewing;
    }

    @Override
    public boolean shouldRender(BrewingInfo info, boolean isInVR) {
        if (Minecraft.getInstance().player == null) {
            return false;
        }
        Direction forward = getForwardFromPlayer(Minecraft.getInstance().player);
        return info.getTileEntity().getLevel() != null &&
                info.getTileEntity().getLevel().getBlockState(info.getTileEntity().getBlockPos().relative(forward)).isAir()
                && info.readyToRender();
    }

    @Override
    public void tick(BrewingInfo info, boolean isInVR) {
        super.tick(info, isInVR);
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


        Vector3d posLeftBottle = pos.add(leftOffset).add(0, 1d/3d, 0);
        info.setPosition(0, posLeftBottle);
        Vector3d posMidBottle = pos.add(midOffset).add(0, 0.25, 0);
        info.setPosition(1, posMidBottle);
        Vector3d posRightBottle = pos.add(rightOffset).add(0, 1d/3d, 0);
        info.setPosition(2, posRightBottle);
        Vector3d posIngredient = pos.add(midOffset).add(0, 0.75, 0);
        info.setPosition(3, posIngredient);
        Vector3d posFuel = pos.add(leftOffset).add(0, 0.75, 0);
        info.setPosition(4, posFuel);

        float hitboxSize = ClientConstants.itemScaleSizeBrewing / 3f;
        info.setHitbox(0, createHitbox(posLeftBottle, hitboxSize));
        info.setHitbox(1, createHitbox(posMidBottle, hitboxSize));
        info.setHitbox(2, createHitbox(posRightBottle, hitboxSize));
        info.setHitbox(3, createHitbox(posIngredient, hitboxSize));
        info.setHitbox(4, createHitbox(posFuel, hitboxSize));
    }

    @Override
    protected void render(BrewingInfo info, MatrixStack stack, boolean isInVR) {
        Direction forward = getForwardFromPlayer(Minecraft.getInstance().player);

        float size = ClientConstants.itemScaleSizeBrewing / info.getCountdown();

        renderItem(info.items[0], stack, info.getPosition(0), size, forward, info.getHibtox(0));
        renderItem(info.items[1], stack, info.getPosition(1), size, forward, info.getHibtox(1));
        renderItem(info.items[2], stack, info.getPosition(2), size, forward, info.getHibtox(2));
        renderItem(info.items[3], stack, info.getPosition(3), size, forward, info.getHibtox(3));
        renderItem(info.items[4], stack, info.getPosition(4), size, forward, info.getHibtox(4));
    }
}
