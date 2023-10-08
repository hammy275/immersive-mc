package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.ImmersiveCheckers;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.storage.ImmersiveStorage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.AbstractWorldStorageInfo;
import com.hammy275.immersivemc.client.immersive.info.CraftingInfo;
import com.hammy275.immersivemc.client.immersive.info.InfoTriggerHitboxes;
import com.hammy275.immersivemc.common.network.packet.GetRecipePacket;
import com.hammy275.immersivemc.common.network.packet.InteractPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class ImmersiveCrafting extends AbstractWorldStorageImmersive<CraftingInfo> {
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
        Objects.requireNonNull(Minecraft.getInstance().level);

        info.isTinkersTable = Minecraft.getInstance().level.getBlockEntity(info.getBlockPosition()) != null;

        Direction forward;
        try { // try-catch prevents some funny race, I think? See #117
            forward = info.isTinkersTable ?
                    Minecraft.getInstance().level.getBlockState(info.getBlockPosition()).getValue(BlockStateProperties.HORIZONTAL_FACING) :
                    getForwardFromPlayer(Minecraft.getInstance().player);
        } catch (IllegalArgumentException e) {
            forward = getForwardFromPlayer(Minecraft.getInstance().player);
        }

        Vec3[] positions = get3x3HorizontalGrid(info.getBlockPosition(), spacing, forward, ActiveConfig.resourcePack3dCompat);
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

        if (info.isTinkersTable && info.ticksActive % 4 == 0) { // Retrieve recipe to prevent de-syncs with tinkers
            Network.INSTANCE.sendToServer(new GetRecipePacket(info.getBlockPosition()));
        }

    }

    @Override
    public BlockPos getLightPos(CraftingInfo info) {
        return info.getBlockPosition().above();
    }

    @Override
    public void handleRightClick(AbstractImmersiveInfo info, Player player, int closest, InteractionHand hand) {
        Network.INSTANCE.sendToServer(new InteractPacket(info.getBlockPosition(), closest, hand));
    }

    @Override
    public void handleTriggerHitboxRightClick(InfoTriggerHitboxes info, Player player, int hitboxNum) {
        AbstractImmersiveInfo aInfo = (AbstractImmersiveInfo) info;
        Network.INSTANCE.sendToServer(new InteractPacket(aInfo.getBlockPosition(), 9, InteractionHand.MAIN_HAND));
        ((CraftingInfo) info).setTicksLeft(ClientConstants.ticksToRenderCrafting); // Reset count if we craft
    }

    @Override
    protected void render(CraftingInfo info, PoseStack stack, boolean isInVR) {
        float itemSize = ClientConstants.itemScaleSizeCrafting / info.getItemTransitionCountdown();
        Direction forward = getForwardFromPlayer(Minecraft.getInstance().player);

        for (int i = 0; i < 9; i++) {
            float renderSize = info.slotHovered == i ? itemSize * 1.25f : itemSize;
            renderItem(info.items[i], stack, info.getPosition(i),
                    renderSize, forward, Direction.UP, info.getHitbox(i), true, -1, info.light);
        }
        int degreesRotation = (int) (info.ticksActive % 100d * 3.6d);
        renderItem(info.outputItem, stack, info.outputPosition,
                itemSize * 3, forward, null, info.outputHitbox, true,
                ActiveConfig.spinCraftingOutput ? degreesRotation : -1, info.light); // * 3.6 = * 360/100
    }

    @Override
    public boolean enabledInConfig() {
        return ActiveConfig.useCraftingImmersion;
    }

    @Override
    public boolean shouldTrack(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        return ImmersiveCheckers.isCraftingTable(pos, state, tileEntity, level);
    }

    @Override
    public boolean shouldBlockClickIfEnabled(AbstractImmersiveInfo info) {
        return true;
    }

    @Override
    public void processStorageFromNetwork(AbstractWorldStorageInfo info, ImmersiveStorage storageIn) {
        for (int i = 0; i <= 8; i++) {
            info.items[i] = storageIn.getItem(i);
        }
        CraftingInfo cInfo = (CraftingInfo) info;
        cInfo.outputItem = storageIn.getItem(9);
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
    public boolean shouldRender(CraftingInfo info, boolean isInVR) {
        if (Minecraft.getInstance().player == null) return false;
        Level level = Minecraft.getInstance().level;
        return level != null && level.getBlockState(info.getBlockPosition().above()).canBeReplaced()
                && info.readyToRender();
    }

    @Override
    public int getCooldownVR() {
        return 7;
    }
}
