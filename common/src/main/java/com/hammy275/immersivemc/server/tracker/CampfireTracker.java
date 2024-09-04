package com.hammy275.immersivemc.server.tracker;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.tracker.AbstractTracker;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import net.blf02.vrapi.api.data.IVRData;
import net.blf02.vrapi.api.data.IVRPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class CampfireTracker extends AbstractTracker {

    public static final Map<String, CookInfo> cookTime = new HashMap<>();

    public CampfireTracker() {
        ServerTrackerInit.playerTrackers.add(this);
    }

    @Override
    protected void tick(Player player) {
        CookInfo info = cookTime.get(player.getGameProfile().getName());
        if (info == null) return;
        for (int c = 0; c <= 1; c++) {
            ItemStack toSmelt = c == 0 ? player.getItemInHand(InteractionHand.MAIN_HAND) : player.getItemInHand(InteractionHand.OFF_HAND);
            Optional<RecipeHolder<CampfireCookingRecipe>> recipe =
                    player.level().getRecipeManager().getRecipeFor(RecipeType.CAMPFIRE_COOKING, new SimpleContainer(toSmelt), player.level());
            if (recipe.isPresent() && info.get(c) >= recipe.get().value().getCookingTime() / 2) { // Smelt the held controller's item if we reach cook time.
                toSmelt.shrink(1);
                boolean didGive = player.getInventory().add(recipe.get().value().getResultItem(player.level().registryAccess()).copy());
                if (!didGive) {
                    Util.placeLeftovers(player, recipe.get().value().getResultItem(player.level().registryAccess()).copy());
                }
                cookTime.remove(player.getGameProfile().getName());
            } else if (recipe.isPresent() &&
                    ThreadLocalRandom.current().nextInt(4) == 0) { // Not ready to smelt yet, show particle
                Vec3 pos = VRPlugin.API.getVRPlayer(player).getController(c).position();
                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SMOKE, pos.x, pos.y, pos.z,
                            1, 0.01, 0.01, 0.01, 0);
                }
            }
        }
    }

    @Override
    protected boolean shouldTick(Player player) {
        if (!ActiveConfig.FILE_SERVER.useCampfireImmersion) return false;
        if (!VRPluginVerify.hasAPI) return false;
        if (!VRPlugin.API.playerInVR(player)) return false;
        if (!ActiveConfig.getConfigForPlayer(player).useCampfireImmersion) return false;
        IVRPlayer vrPlayer = VRPlugin.API.getVRPlayer(player);
        boolean mainRes = false;
        boolean offRes = false;
        for (int c = 0; c <= 1; c++) {
            IVRData controller = vrPlayer.getController(c);
            BlockPos pos = BlockPos.containing(controller.position());
            if (player.level().getBlockState(pos).getBlock() instanceof CampfireBlock ||
                    player.level().getBlockState(pos.below()).getBlock() instanceof CampfireBlock) {
                BlockState campfire;
                if (player.level().getBlockState(pos).getBlock() instanceof CampfireBlock) {
                    campfire = player.level().getBlockState(pos);
                } else {
                    campfire = player.level().getBlockState(pos.below());
                } // Get campfire state
                if (!campfire.getValue(CampfireBlock.LIT)) continue; // Immediately continue if no campfire is lit
                ItemStack stackNew;
                if (c == 0) {
                    stackNew = player.getItemInHand(InteractionHand.MAIN_HAND);
                } else {
                    stackNew = player.getItemInHand(InteractionHand.OFF_HAND);
                }
                // Get info instance ready
                CookInfo info = cookTime.get(player.getGameProfile().getName());
                if (info == null) {
                    info = new CookInfo();
                    cookTime.put(player.getGameProfile().getName(), info);
                }
                ItemStack stackOld = info.getStack(c);
                if (stackNew == stackOld || stackOld.isEmpty()) { // If what we're holding is either new or what we were holding last tick
                    info.add(c, 1); // Add 1 to the count
                    if (c == 0) {
                        mainRes = true;
                    } else {
                        offRes = true;
                    }
                }
                info.setStack(c, stackNew); // Set the old stack to our new stack
            }
        }
        return mainRes || offRes; // A result has occurred from either hand
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
