package com.hammy275.immersivemc.server.ticker;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.ticker.AbstractTicker;
import com.hammy275.immersivemc.common.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.api.VRAPI;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.api.data.VRPoseHistory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class CampfireTicker extends AbstractTicker {

    public static final Map<String, CookInfo> cookTime = new HashMap<>();

    @Override
    protected void tick(Player playerIn, VRPose pose, VRPoseHistory poseHistory) {
        ServerPlayer player = (ServerPlayer) playerIn;
        CookInfo info = cookTime.get(player.getGameProfile().getName());
        if (info == null) return;
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack toSmelt = player.getItemInHand(hand);
            Optional<RecipeHolder<CampfireCookingRecipe>> recipe =
                    player.level().getRecipeManager().getRecipeFor(RecipeType.CAMPFIRE_COOKING, new SingleRecipeInput(toSmelt), player.level());
            if (recipe.isPresent() && info.get(hand.ordinal()) >= recipe.get().value().getCookingTime() / 2) { // Smelt the held controller's item if we reach cook time.
                toSmelt.shrink(1);
                boolean didGive = player.getInventory().add(recipe.get().value().getResultItem(player.level().registryAccess()).copy());
                if (!didGive) {
                    Util.placeLeftovers(player, recipe.get().value().getResultItem(player.level().registryAccess()).copy());
                }
                cookTime.remove(player.getGameProfile().getName());
            } else if (recipe.isPresent() &&
                    ThreadLocalRandom.current().nextInt(4) == 0) { // Not ready to smelt yet, show particle
                Vec3 pos = VRAPI.instance().getVRPose(player).getHand(hand).getPos();
                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SMOKE, pos.x, pos.y, pos.z,
                            1, 0.01, 0.01, 0.01, 0);
                }
            }
        }
    }

    @Override
    protected boolean shouldTick(Player player, VRPose pose, VRPoseHistory poseHistory) {
        if (!ActiveConfig.getConfigForPlayer(player).useCampfireImmersive) return false;
        VRPose vrPose = VRAPI.instance().getVRPose(player);
        boolean mainRes = false;
        boolean offRes = false;
        for (InteractionHand hand : InteractionHand.values()) {
            VRBodyPartData controller = vrPose.getHand(hand);
            BlockPos pos = BlockPos.containing(controller.getPos());
            if (player.level().getBlockState(pos).getBlock() instanceof CampfireBlock ||
                    player.level().getBlockState(pos.below()).getBlock() instanceof CampfireBlock) {
                BlockState campfire;
                if (player.level().getBlockState(pos).getBlock() instanceof CampfireBlock) {
                    campfire = player.level().getBlockState(pos);
                } else {
                    campfire = player.level().getBlockState(pos.below());
                } // Get campfire state
                if (!campfire.getValue(CampfireBlock.LIT)) continue; // Immediately continue if no campfire is lit
                ItemStack stackNew = player.getItemInHand(hand);
                // Get info instance ready
                CookInfo info = cookTime.get(player.getGameProfile().getName());
                if (info == null) {
                    info = new CookInfo();
                    cookTime.put(player.getGameProfile().getName(), info);
                }
                ItemStack stackOld = info.getStack(hand.ordinal());
                if (stackNew == stackOld || stackOld.isEmpty()) { // If what we're holding is either new or what we were holding last tick
                    info.add(hand.ordinal(), 1); // Add 1 to the count
                    if (hand == InteractionHand.MAIN_HAND) {
                        mainRes = true;
                    } else {
                        offRes = true;
                    }
                }
                info.setStack(hand.ordinal(), stackNew); // Set the old stack to our new stack
            }
        }
        return mainRes || offRes; // A result has occurred from either hand
    }

    @Override
    public void onPlayerDisconnect(Player player) {
        super.onPlayerDisconnect(player);
        cookTime.remove(player.getGameProfile().getName());
    }

    public static class CookInfo {
        protected int mainHand = 0;
        protected int offHand = 0;
        public ItemStack stackHeldMain = ItemStack.EMPTY;
        public ItemStack stackHeldOff = ItemStack.EMPTY;

        public void set(int controller, int value) {
            if (controller == 0) {
                mainHand = value;
            } else {
                offHand = value;
            }
        }

        public int get(int controller) {
            return controller == 0 ? mainHand : offHand;
        }

        public void add(int controller, int amount) {
            set(controller, get(controller) + amount);
        }

        public ItemStack getStack(int controller) {
            return controller == 0 ? stackHeldMain : stackHeldOff;
        }

        public void setStack(int controller, ItemStack stack) {
            if (controller == 0) {
                stackHeldMain = stack;
            } else {
                stackHeldOff = stack;
            }
        }

        @Override
        public String toString() {
            return "Main Hand: " + stackHeldMain + " w/ " + mainHand + " ticks" +
                    "\nOff Hand: " + stackHeldOff + " w/ " + offHand + " ticks";
        }
    }

}
