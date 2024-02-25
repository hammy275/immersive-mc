package com.hammy275.immersivemc.common.immersive.handler;

import java.util.ArrayList;
import java.util.List;

public class ImmersiveHandlers {

    public static final List<ImmersiveHandler> HANDLERS = new ArrayList<>();


    public static final ImmersiveHandler anvilHandler = new AnvilHandler();
    public static final ImmersiveHandler barrelHandler = new BarrelHandler();
    public static final ImmersiveHandler beaconHandler = new BeaconHandler();
    public static final ImmersiveHandler brewingStandHandler = new BrewingStandHandler();
    public static final ImmersiveHandler chestHandler = new ChestHandler();
    public static final ImmersiveHandler craftingHandler = new CraftingHandler();
    public static final ImmersiveHandler enchantingTableHandler = new ETableHandler();
    public static final ImmersiveHandler furnaceHandler = new FurnaceHandler();
    public static final ImmersiveHandler hopperHandler = new HopperHandler();
    public static final ImmersiveHandler ironFurnacesFurnaceHandler = new IronFurnacesFurnaceHandler();
    public static final ImmersiveHandler jukeboxHandler = new JukeboxHandler();
    public static final ImmersiveHandler shulkerBoxHandler = new ShulkerBoxHandler();
    public static final ImmersiveHandler smithingTableHandler = new SmithingTableHandler();
    public static final ImmersiveHandler tcCraftingStationHandler = new TCCraftingStationHandler();

    static {
        HANDLERS.add(anvilHandler);
        HANDLERS.add(barrelHandler);
        HANDLERS.add(beaconHandler);
        HANDLERS.add(brewingStandHandler);
        HANDLERS.add(chestHandler);
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
