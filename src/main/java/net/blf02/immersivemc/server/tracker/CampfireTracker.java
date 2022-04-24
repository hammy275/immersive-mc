package net.blf02.immersivemc.server.tracker;

import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.tracker.AbstractTracker;
import net.blf02.immersivemc.common.vr.VRPlugin;
import net.blf02.immersivemc.common.vr.VRPluginVerify;
import net.blf02.vrapi.api.data.IVRData;
import net.blf02.vrapi.api.data.IVRPlayer;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CampfireCookingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

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
    protected void tick(PlayerEntity player) {
        CookInfo info = cookTime.get(player.getGameProfile().getName());
        if (info == null) return;
        for (int c = 0; c <= 1; c++) {
            ItemStack toSmelt = c == 0 ? player.getItemInHand(Hand.MAIN_HAND) : player.getItemInHand(Hand.OFF_HAND);
            Optional<CampfireCookingRecipe> recipe =
                    player.level.getRecipeManager().getRecipeFor(IRecipeType.CAMPFIRE_COOKING, new Inventory(toSmelt), player.level);
            if (recipe.isPresent() && info.get(c) >= recipe.get().getCookingTime() / 2) { // Attempt to smelt the held controller's item if we reach cook time.
                boolean didGive = player.inventory.add(recipe.get().getResultItem());
                if (didGive) {
                    toSmelt.shrink(1);
                }
                cookTime.remove(player.getGameProfile().getName());
            } else if (recipe.isPresent() &&
                    ThreadLocalRandom.current().nextInt(4) == 0) { // Not ready to smelt yet, show particle
                Vector3d pos = VRPlugin.API.getVRPlayer(player).getController(c).position();
                if (player.level instanceof ServerWorld) {
                    ServerWorld serverLevel = (ServerWorld) player.level;
                    serverLevel.sendParticles(ParticleTypes.SMOKE, pos.x, pos.y, pos.z,
                            1, 0.01, 0.01, 0.01, 0);
                }
            }
        }
    }

    @Override
    protected boolean shouldTick(PlayerEntity player) {
        if (!ActiveConfig.useCampfireImmersion) return false;
        if (!VRPluginVerify.hasAPI) return false;
        if (!VRPlugin.API.playerInVR(player)) return false;
        IVRPlayer vrPlayer = VRPlugin.API.getVRPlayer(player);
        boolean mainRes = false;
        boolean offRes = false;
        for (int c = 0; c <= 1; c++) {
            IVRData controller = vrPlayer.getController(c);
            BlockPos pos = new BlockPos(controller.position());
            if (player.level.getBlockState(pos).getBlock() instanceof CampfireBlock ||
                    player.level.getBlockState(pos.below()).getBlock() instanceof CampfireBlock) {
                BlockState campfire;
                if (player.level.getBlockState(pos).getBlock() instanceof CampfireBlock) {
                    campfire = player.level.getBlockState(pos);
                } else {
                    campfire = player.level.getBlockState(pos.below());
                } // Get campfire state
                if (!campfire.getValue(CampfireBlock.LIT)) continue; // Immediately continue if no campfire is lit
                ItemStack stackNew;
                if (c == 0) {
                    stackNew = player.getItemInHand(Hand.MAIN_HAND);
                } else {
                    stackNew = player.getItemInHand(Hand.OFF_HAND);
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
