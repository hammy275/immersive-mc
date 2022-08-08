package net.blf02.immersivemc.client.immersive;

import com.mojang.blaze3d.vertex.PoseStack;
import net.blf02.immersivemc.client.config.ClientConstants;
import net.blf02.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import net.blf02.immersivemc.client.immersive.info.BrewingInfo;
import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.packet.SwapPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.Player;
import net.minecraft.tileentity.BrewingStandBlockEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.World;

import java.util.Objects;

public class ImmersiveBrewing extends AbstractBlockEntityImmersive<BrewingStandBlockEntity, BrewingInfo> {

    protected static final ImmersiveBrewing singleton = new ImmersiveBrewing();

    public ImmersiveBrewing() {
        super(2);
    }

    public static ImmersiveBrewing getSingleton() {
        return singleton;
    }

    @Override
    public BrewingInfo getNewInfo(BrewingStandBlockEntity tileEnt) {
        return new BrewingInfo(tileEnt, ClientConstants.ticksToRenderBrewing);
    }

    @Override
    public int getTickTime() {
        return ClientConstants.ticksToRenderBrewing;
    }

    @Override
    public boolean hasValidBlock(BrewingInfo info, World level) {
        return level.getBlockEntity(info.getBlockPosition()) instanceof BrewingStandBlockEntity;
    }

    @Override
    public boolean shouldRender(BrewingInfo info, boolean isInVR) {
        if (Minecraft.getInstance().player == null) {
            return false;
        }
        Direction forward = getForwardFromPlayer(Minecraft.getInstance().player);
        return info.getBlockEntity().getLevel() != null &&
                info.getBlockEntity().getLevel().getBlockState(info.getBlockEntity().getBlockPos().relative(forward)).isAir()
                && info.readyToRender();
    }

    @Override
    protected void initInfo(BrewingInfo info) {
        setHitboxes(info);
    }

    protected void setHitboxes(BrewingInfo info) {
        Objects.requireNonNull(Minecraft.getInstance().player);

        BrewingStandBlockEntity stand = info.getBlockEntity();
        Direction forward = getForwardFromPlayer(Minecraft.getInstance().player);
        Vec3 pos = getDirectlyInFront(forward, stand.getBlockPos());
        Direction left = getLeftOfDirection(forward);

        Vec3 leftOffset = new Vec3(
                left.getNormal().getX() * 0.25, 0, left.getNormal().getZ() * 0.25);
        Vec3 midOffset = new Vec3(
                left.getNormal().getX() * 0.5, 0, left.getNormal().getZ() * 0.5);
        Vec3 rightOffset = new Vec3(
                left.getNormal().getX() * 0.75, 0, left.getNormal().getZ() * 0.75);


        Vec3 posLeftBottle = pos.add(leftOffset).add(0, 1d/3d, 0);
        info.setPosition(0, posLeftBottle);
        double midY = ActiveConfig.autoCenterBrewing ? 1d/3d : 0.25;
        Vec3 posMidBottle = pos.add(midOffset).add(0, midY, 0);
        info.setPosition(1, posMidBottle);
        Vec3 posRightBottle = pos.add(rightOffset).add(0, 1d/3d, 0);
        info.setPosition(2, posRightBottle);
        Vec3 posIngredient;
        Vec3 posFuel;
        if (ActiveConfig.autoCenterBrewing) {
            posIngredient = pos.add(midOffset).add(0, 0.6, 0);
            posFuel = pos.add(midOffset).add(0, 0.85, 0);
        } else {
            posIngredient = pos.add(midOffset).add(0, 0.75, 0);
            posFuel = pos.add(leftOffset).add(0, 0.75, 0);
        }
        info.setPosition(3, posIngredient);
        info.setPosition(4, posFuel);

        float hitboxSize = ClientConstants.itemScaleSizeBrewing / 3f;
        info.setHitbox(0, createHitbox(posLeftBottle, hitboxSize));
        info.setHitbox(1, createHitbox(posMidBottle, hitboxSize));
        info.setHitbox(2, createHitbox(posRightBottle, hitboxSize));
        info.setHitbox(3, createHitbox(posIngredient, hitboxSize));
        info.setHitbox(4, createHitbox(posFuel, hitboxSize));

        info.lastDir = forward;
    }

    @Override
    protected void doTick(BrewingInfo info, boolean isInVR) {
        super.doTick(info, isInVR);

        Direction forward = getForwardFromPlayer(Minecraft.getInstance().player);
        if (forward != info.lastDir) {
            setHitboxes(info);
        }
    }

    @Override
    protected void render(BrewingInfo info, PoseStack stack, boolean isInVR) {
        Direction forward = getForwardFromPlayer(Minecraft.getInstance().player);

        float size = ClientConstants.itemScaleSizeBrewing / info.getItemTransitionCountdown();

        renderItem(info.items[0], stack, info.getPosition(0), size, forward, info.getHitbox(0), false);
        renderItem(info.items[1], stack, info.getPosition(1), size, forward, info.getHitbox(1), false);
        renderItem(info.items[2], stack, info.getPosition(2), size, forward, info.getHitbox(2), false);
        renderItem(info.items[3], stack, info.getPosition(3), size, forward, info.getHitbox(3), true);
        renderItem(info.items[4], stack, info.getPosition(4), size, forward, info.getHitbox(4), true);
    }

    @Override
    protected boolean enabledInConfig() {
        return ActiveConfig.useBrewingImmersion;
    }

    @Override
    public void handleRightClick(AbstractImmersiveInfo info, Player player, int closest, HumanoidArm hand) {
        BrewingInfo infoB = (BrewingInfo) info;
        Network.INSTANCE.sendToServer(new SwapPacket(
                infoB.getBlockEntity().getBlockPos(), closest, hand
        ));
    }
}
