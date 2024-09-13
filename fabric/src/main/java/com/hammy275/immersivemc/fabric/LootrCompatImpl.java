package com.hammy275.immersivemc.fabric;

import com.hammy275.immersivemc.common.compat.Lootr;
import com.hammy275.immersivemc.common.compat.lootr.LootrCompat;
import com.hammy275.immersivemc.common.compat.lootr.LootrNullImpl;
import com.hammy275.immersivemc.common.compat.util.CompatModule;
import com.hammy275.immersivemc.mixin.ChestLidControllerAccessor;
import com.hammy275.immersivemc.server.ChestToOpenSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.zestyblaze.lootr.api.LootrAPI;
import net.zestyblaze.lootr.api.blockentity.ILootBlockEntity;
import net.zestyblaze.lootr.api.inventory.ILootrInventory;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class LootrCompatImpl implements LootrCompat {

    private static final Class<?> lootrBarrelBlockEntityClass;
    private static final Method barrelStartOpen;
    private static final Method barrelStopOpen;
    private static final Class<?> lootrChestBlockEntityClass;
    private static final Field chestChestLidController;
    private static final Class<?> lootrShulkerBlockEntityClass;
    private static final Method shulkerBoxStartOpen;
    private static final Method shulkerBoxStopOpen;
    private static final Method shulkerBoxIsClosed;

    static {
        lootrBarrelBlockEntityClass = getLootrClassMaybeRenamed("net.zestyblaze.lootr.blocks.entities.LootrBarrelBlockEntity");
        barrelStartOpen = getMethod(lootrBarrelBlockEntityClass, "startOpen", Player.class);
        barrelStopOpen = getMethod(lootrBarrelBlockEntityClass, "stopOpen", Player.class);

        lootrChestBlockEntityClass = getLootrClassMaybeRenamed("net.zestyblaze.lootr.blocks.entities.LootrChestBlockEntity");
        chestChestLidController = getDeclaredField(lootrChestBlockEntityClass, "chestLidController");

        lootrShulkerBlockEntityClass = getLootrClassMaybeRenamed("net.zestyblaze.lootr.blocks.entities.LootrShulkerBlockEntity");
        shulkerBoxStartOpen = getMethod(lootrShulkerBlockEntityClass, "startOpen", Player.class);
        shulkerBoxStopOpen = getMethod(lootrShulkerBlockEntityClass, "stopOpen", Player.class);
        shulkerBoxIsClosed = getMethod(lootrShulkerBlockEntityClass, "isClosed");
    }

    private LootrCompatImpl() {}

    public static LootrCompat makeCompatImpl() {
        return CompatModule.create(new LootrCompatImpl(), LootrCompat.class, "Lootr",
                (config, newValue) -> {
                    Lootr.lootrImpl = new LootrNullImpl();
                });
    }

    @Override
    public @Nullable Container getContainer(ServerPlayer player, BlockPos pos) {
        Level level = player.level();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ILootBlockEntity lootBE &&
            blockEntity instanceof RandomizableContainerBlockEntity rcbe) {
            MenuProvider provider = LootrAPI.getModdedMenu(level, lootBE.getTileId(), pos,
                    player, rcbe, lootBE::unpackLootTable, lootBE::getTable,
                    lootBE::getSeed);
            if (provider instanceof ILootrInventory inventory) {
                return inventory;
            }
        }
        return null;
    }

    @Override
    public void markOpener(Player player, BlockPos pos) {
        BlockEntity blockEntity = player.level().getBlockEntity(pos);
        if (blockEntity instanceof ILootBlockEntity lootBE) {
            if (player.level().isClientSide) {
                lootBE.setOpened(true);
            } else {
                lootBE.getOpeners().add(player.getUUID());
                blockEntity.setChanged();
            }
        }
    }

    @Override
    public boolean isLootrContainer(BlockPos pos, Level level) {
        return level.getBlockState(pos).is(LootrCompat.BLOCK_TAG);
    }

    @Override
    public boolean openLootrBarrel(BlockPos pos, Player player, boolean nowOpen) {
        BlockEntity barrel = player.level().getBlockEntity(pos);
        if (lootrBarrelBlockEntityClass.isInstance(barrel)) {
            if (nowOpen) {
                invoke(barrelStartOpen, barrel, player);
                ChestToOpenSet.openChest(player, pos);
            } else {
                invoke(barrelStopOpen, barrel, player);
                ChestToOpenSet.closeChest(player, pos);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean openLootrShulkerBox(BlockPos pos, Player player, boolean nowOpen) {
        BlockEntity be = player.level().getBlockEntity(pos);
        if (lootrShulkerBlockEntityClass.isInstance(be)) {
            if (nowOpen) {
                invoke(shulkerBoxStartOpen, be, player);
                ChestToOpenSet.openChest(player, pos);
            } else {
                invoke(shulkerBoxStopOpen, be, player);
                ChestToOpenSet.closeChest(player, pos);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isOpen(BlockPos pos, Player player) {
        BlockEntity be = player.level().getBlockEntity(pos);
        if (lootrShulkerBlockEntityClass.isInstance(be)) {
            return !((boolean) invoke(shulkerBoxIsClosed, be));
        } else if (lootrBarrelBlockEntityClass.isInstance(be)) {
            return player.level().getBlockState(pos).getValue(BarrelBlock.OPEN);
        } else if (lootrChestBlockEntityClass.isInstance(be)) {
            try {
                return ((ChestLidControllerAccessor) chestChestLidController.get(be)).getShouldBeOpen();
            } catch (IllegalAccessException ignored) {}
        }
        return false;
    }

    @Nullable
    private static Class<?> getLootrClassMaybeRenamed(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ignored) {
            try {
                return Class.forName(name.replace(".blocks.", ".block."));
            } catch (ClassNotFoundException ignored2) {
                return null;
            }
        }
    }

    @Nullable
    private static Method getMethod(@Nullable Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        if (clazz == null) {
            return null;
        }
        try {
            return clazz.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    @Nullable
    private static Field getDeclaredField(@Nullable Class<?> clazz, String fieldName) {
        if (clazz == null) {
            return null;
        }
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException ignored) {
            return null;
        }
    }

    private static Object invoke(Method method, Object obj, Object... args) {
        if (method == null) {
            return null;
        }
        try {
            return method.invoke(obj, args);
        } catch (InvocationTargetException | IllegalAccessException ignored) {
            return null;
        }
    }
}
