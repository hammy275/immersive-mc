package net.blf02.immersivemc.client.immersive;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.blf02.immersivemc.client.config.ClientConstants;
import net.blf02.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import net.blf02.immersivemc.client.immersive.info.AbstractWorldStorageInfo;
import net.blf02.immersivemc.client.immersive.info.CraftingInfo;
import net.blf02.immersivemc.client.immersive.info.InfoTriggerHitboxes;
import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.packet.InteractPacket;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.Objects;

public class ImmersiveCrafting extends AbstractWorldStorageImmersive<CraftingInfo> {

    public static final ImmersiveCrafting singleton = new ImmersiveCrafting();
    private final double spacing = 3d/16d;


    public ImmersiveCrafting() {
        super(2); // We don't expect to use many crafting tables at once
    }

    @Override
    protected void initInfo(CraftingInfo info) {
        setHitboxes(info);
    }

    protected void setHitboxes(CraftingInfo info) {
        Objects.requireNonNull(Minecraft.getInstance().player);

        Direction forward = getForwardFromPlayer(Minecraft.getInstance().player);
        Vector3d pos = getTopCenterOfBlock(info.getBlockPosition());
        Direction left = getLeftOfDirection(forward);

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
        float hitboxSize = ClientConstants.itemScaleSizeCrafting / 3f;
        for (int i = 0; i < 9; i++) {
            info.setPosition(i, positions[i]);
            info.setHitbox(i, createHitbox(positions[i], hitboxSize));
        }

        info.outputPosition = info.getPosition(4).add(0, 0.5, 0);
        info.outputHitbox = createHitbox(info.outputPosition, hitboxSize * 3);

        info.lastDir = forward;
    }

    @Override
    protected void doTick(CraftingInfo info, boolean isInVR) {
        super.doTick(info, isInVR);
        Objects.requireNonNull(Minecraft.getInstance().player);

        Direction forward = getForwardFromPlayer(Minecraft.getInstance().player);
        if (info.lastDir != forward) {
            setHitboxes(info);
        }

    }

    @Override
    public void handleRightClick(AbstractImmersiveInfo info, PlayerEntity player, int closest, Hand hand) {
        Network.INSTANCE.sendToServer(new InteractPacket(info.getBlockPosition(), closest, hand));
    }

    @Override
    public void handleTriggerHitboxRightClick(InfoTriggerHitboxes info, PlayerEntity player, int hitboxNum) {
        AbstractImmersiveInfo aInfo = (AbstractImmersiveInfo) info;
        Network.INSTANCE.sendToServer(new InteractPacket(aInfo.getBlockPosition(), 9, Hand.MAIN_HAND));
        ((CraftingInfo) info).setTicksLeft(ClientConstants.ticksToRenderCrafting); // Reset count if we craft
    }

    @Override
    protected void render(CraftingInfo info, MatrixStack stack, boolean isInVR) {
        float itemSize = ClientConstants.itemScaleSizeCrafting / info.getItemTransitionCountdown();
        Direction forward = getForwardFromPlayer(Minecraft.getInstance().player);

        for (int i = 0; i < 9; i++) {
            renderItem(info.items[i], stack, info.getPosition(i),
                    itemSize, forward, Direction.UP, info.getHitbox(i), true);
        }
        renderItem(info.outputItem, stack, info.outputPosition,
                itemSize * 3, forward, info.outputHitbox, true);
    }

    @Override
    protected boolean enabledInConfig() {
        return ActiveConfig.useCraftingImmersion;
    }

    @Override
    public void processItems(AbstractWorldStorageInfo info, ItemStack[] items) {
        for (int i = 0; i <= 8; i++) {
            info.items[i] = items[i];
        }
        CraftingInfo cInfo = (CraftingInfo) info;
        cInfo.outputItem = items[9];
    }

    @Override
    public CraftingInfo getNewInfo(BlockPos pos) {
        return new CraftingInfo(pos, getTickTime());
    }

    @Override
    public int getTickTime() {
        return ClientConstants.ticksToRenderCrafting;
    }

    @Override
    public boolean hasValidBlock(CraftingInfo info, World level) {
        return level.getBlockState(info.getBlockPosition()) .getBlock() == Blocks.CRAFTING_TABLE;
    }

    @Override
    public boolean shouldRender(CraftingInfo info, boolean isInVR) {
        if (Minecraft.getInstance().player == null) return false;
        World level = Minecraft.getInstance().level;
        return level != null && level.getBlockState(info.getBlockPosition().above()).isAir()
                && info.readyToRender();
    }

    @Override
    public int getCooldownVR() {
        return 5;
    }
}
