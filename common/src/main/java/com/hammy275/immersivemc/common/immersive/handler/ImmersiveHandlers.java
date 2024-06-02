package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.AnvilStorage;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.BeaconStorage;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.CraftingTableStorage;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.SmithingTableStorage;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.ETableStorage;

import java.util.ArrayList;
import java.util.List;

public class ImmersiveHandlers {

    public static final List<ImmersiveHandler<?>> HANDLERS = new ArrayList<>();


    public static final ImmersiveHandler<AnvilStorage> anvilHandler = new AnvilHandler();
    public static final ImmersiveHandler<?> barrelHandler = new BarrelHandler();
    public static final ImmersiveHandler<BeaconStorage> beaconHandler = new BeaconHandler();
    public static final ImmersiveHandler<?> brewingStandHandler = new BrewingStandHandler();
    public static final ImmersiveHandler<?> chestHandler = new ChestHandler();
    public static final ImmersiveHandler<?> chiseledBookshelfHandler = new ChiseledBookshelfHandler();
    public static final ImmersiveHandler<CraftingTableStorage> craftingHandler = new CraftingHandler();
    public static final ImmersiveHandler<ETableStorage> enchantingTableHandler = new ETableHandler();
    public static final ImmersiveHandler<?> furnaceHandler = new FurnaceHandler();
    public static final ImmersiveHandler<?> hopperHandler = new HopperHandler();
    public static final ImmersiveHandler<?> ironFurnacesFurnaceHandler = new IronFurnacesFurnaceHandler();
    public static final ImmersiveHandler<?> jukeboxHandler = new JukeboxHandler();
    public static final ImmersiveHandler<?> shulkerBoxHandler = new ShulkerBoxHandler();
    public static final ImmersiveHandler<SmithingTableStorage> smithingTableHandler = new SmithingTableHandler();
    public static final ImmersiveHandler<?> tcCraftingStationHandler = new TCCraftingStationHandler();

    static {
        HANDLERS.add(anvilHandler);
        HANDLERS.add(barrelHandler);
        HANDLERS.add(beaconHandler);
        HANDLERS.add(brewingStandHandler);
        HANDLERS.add(chestHandler);
        HANDLERS.add(chiseledBookshelfHandler);
        HANDLERS.add(craftingHandler);
        HANDLERS.add(enchantingTableHandler);
        HANDLERS.add(furnaceHandler);
        HANDLERS.add(hopperHandler);
        HANDLERS.add(ironFurnacesFurnaceHandler);
        HANDLERS.add(jukeboxHandler);
        HANDLERS.add(shulkerBoxHandler);
        HANDLERS.add(smithingTableHandler);
        HANDLERS.add(tcCraftingStationHandler);
    }
}
