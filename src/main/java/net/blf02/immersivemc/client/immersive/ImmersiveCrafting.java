package net.blf02.immersivemc.client.immersive;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.blf02.immersivemc.client.config.ClientConstants;
import net.blf02.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import net.blf02.immersivemc.client.immersive.info.CraftingInfo;
import net.blf02.immersivemc.client.immersive.info.InfoTriggerHitboxes;
import net.blf02.immersivemc.client.storage.ClientStorage;
import net.blf02.immersivemc.client.swap.ClientSwap;
import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.config.CommonConstants;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.packet.CraftPacket;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ImmersiveCrafting extends AbstractImmersive<CraftingInfo> {

    public static final ImmersiveCrafting singleton = new ImmersiveCrafting();
    private final double spacing = 3d/16d;

    protected int noInfosCooldown = 0;

    public ImmersiveCrafting() {
        super(-1);
    }

    @Override
    public void noInfosTick() {
        super.noInfosTick();
        if (noInfosCooldown >= 200) {
            Arrays.fill(ClientStorage.craftingStorage, ItemStack.EMPTY);
            ClientStorage.craftingOutput = ItemStack.EMPTY;
        } else {
            noInfosCooldown++;
        }
    }

    @Override
    protected void initInfo(CraftingInfo info) {
        setHitboxes(info);
    }

    protected void setHitboxes(CraftingInfo info) {
        Objects.requireNonNull(Minecraft.getInstance().player);

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
        float hitboxSize = ClientConstants.itemScaleSizeCrafting / 3f;
        for (int i = 0; i < 9; i++) {
            info.setPosition(i, positions[i]);
            info.setHitbox(i, createHitbox(positions[i], hitboxSize));
        }

        info.resultPosition = info.getPosition(4).add(0, 0.5, 0);
        info.resultHitbox = createHitbox(info.resultPosition, hitboxSize * 3);

        info.lastDir = forward;
    }

    @Override
    protected void doTick(CraftingInfo info, boolean isInVR) {
        super.doTick(info, isInVR);
        Objects.requireNonNull(Minecraft.getInstance().player);

        if (info.tablePos != null &&
                Minecraft.getInstance().player.distanceToSqr(Vector3d.atCenterOf(info.tablePos)) >
                        CommonConstants.distanceSquaredToRemoveImmersive) {
            info.remove();
        }

        Direction forward = getForwardFromPlayer(Minecraft.getInstance().player);
        if (info.lastDir != forward) {
            setHitboxes(info);
        }

    }

    @Override
    public void handleTriggerHitboxRightClick(InfoTriggerHitboxes info, PlayerEntity player, int hitboxNum) {
        Network.INSTANCE.sendToServer(new CraftPacket(
                ClientStorage.craftingStorage, ((CraftingInfo) info).tablePos, false
        ));

        // Clear items that we don't have anymore and retrieve recipe to match
        ClientStorage.removeLackingIngredientsFromTable(player);
        Network.INSTANCE.sendToServer(new CraftPacket(
                ClientStorage.craftingStorage, ((CraftingInfo) info).tablePos, true
        ));

        ((CraftingInfo) info).setTicksLeft(ClientConstants.ticksToRenderCrafting); // Reset count if we craft
    }

    @Override
    protected void render(CraftingInfo info, MatrixStack stack, boolean isInVR) {
        float itemSize = ClientConstants.itemScaleSizeCrafting / info.getCountdown();
        Direction forward = getForwardFromPlayer(Minecraft.getInstance().player);

        for (int i = 0; i < 9; i++) {
            renderItem(ClientStorage.craftingStorage[i], stack, info.getPosition(i),
                    itemSize, forward, Direction.UP, info.getHibtox(i), false);
        }
        renderItem(ClientStorage.craftingOutput, stack, info.resultPosition,
                itemSize * 3, forward, info.resultHitbox, true);
    }

    @Override
    protected boolean enabledInConfig() {
        return ActiveConfig.useCraftingImmersion;
    }

    @Override
    public void handleRightClick(AbstractImmersiveInfo info, PlayerEntity player, int closest, Hand hand) {
        ClientSwap.craftingSwap(closest, hand, info.getBlockPosition());
    }

    @Override
    public boolean hasValidBlock(CraftingInfo info, World level) {
        return level.getBlockState(info.getBlockPosition()) .getBlock() == Blocks.CRAFTING_TABLE;
    }

    @Override
    public boolean shouldRender(CraftingInfo info, boolean isInVR) {
        if (Minecraft.getInstance().player == null) return false;
        World level = Minecraft.getInstance().level;
        return level != null && level.getBlockState(info.tablePos.above()).isAir()
                && info.readyToRender();
    }

    public void trackObject(BlockPos tablePos) {
        for (CraftingInfo info : getTrackedObjects()) {
            if (info.tablePos.equals(tablePos)) {
                info.setTicksLeft(ClientConstants.ticksToRenderCrafting);
                return;
            }
        }
        this.noInfosCooldown = 0;
        infos.add(new CraftingInfo(tablePos, ClientConstants.ticksToRenderCrafting));
    }

    @Override
    public int getCooldownVR() {
        return 5;
    }
}
