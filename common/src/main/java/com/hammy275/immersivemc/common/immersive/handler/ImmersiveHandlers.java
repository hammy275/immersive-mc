package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.api.common.immersive.BlockBasedImmersiveHandler;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.api.common.immersive.PlayerAttachmentImmersiveHandler;
import com.hammy275.immersivemc.common.api_impl.ImmersiveMCRegistrationImpl;
import com.hammy275.immersivemc.common.compat.IronFurnaces;
import com.hammy275.immersivemc.common.compat.TinkersConstruct;
import com.hammy275.immersivemc.common.compat.apotheosis.Apoth;
import com.hammy275.immersivemc.common.compat.util.CompatModule;
import com.hammy275.immersivemc.common.immersive.CommonBookData;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.AnvilStorage;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.ApothSalvagingTableStorage;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.BeaconStorage;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.CraftingTableStorage;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.GrindstoneStorage;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.SmithingTableStorage;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.*;

import java.util.ArrayList;
import java.util.List;

public class ImmersiveHandlers {

    public static final List<ImmersiveHandler> ALL_HANDLERS = new ArrayList<>();
    public static final List<BlockBasedImmersiveHandler<?>> BLOCK_HANDLERS = new ArrayList<>();
    public static final List<PlayerAttachmentImmersiveHandler<?>> ATTACHMENT_HANDLERS = new ArrayList<>();


    public static final WorldStorageHandler<AnvilStorage> anvilHandler = new AnvilHandler();
    public static final WorldStorageHandler<ApothSalvagingTableStorage> apothSalvagingTableHandler = CompatModule.create(new ApothSalvagingTableHandler(), WorldStorageHandler.class, Apoth.compatData);
    public static final PlayerAttachmentImmersiveHandler<BagStorage> bagHandler = new BagHandler();
    public static final BlockBasedImmersiveHandler<?> barrelHandler = new BarrelHandler();
    public static final WorldStorageHandler<BeaconStorage> beaconHandler = new BeaconHandler();
    public static final BlockBasedImmersiveHandler<?> brewingStandHandler = new BrewingStandHandler();
    public static final BlockBasedImmersiveHandler<ListOfItemsStorage> chestHandler = new ChestHandler();
    public static final WorldStorageHandler<CraftingTableStorage> craftingHandler = new CraftingHandler();
    public static final BlockBasedImmersiveHandler<NullStorage> doorHandler = new DoorHandler();
    public static final WorldStorageHandler<ETableStorage> enchantingTableHandler = new ETableHandler();
    public static final BlockBasedImmersiveHandler<?> furnaceHandler = new FurnaceHandler();
    public static final WorldStorageHandler<GrindstoneStorage> grindstoneHandler = new GrindstoneHandler();
    public static final PlayerAttachmentImmersiveHandler<NullStorage> hitboxesHandler = new HitboxesHandler();
    public static final BlockBasedImmersiveHandler<?> hopperHandler = new HopperHandler();
    public static final BlockBasedImmersiveHandler<?> ironFurnacesFurnaceHandler = CompatModule.create(new IronFurnacesFurnaceHandler(), IronFurnaces.compatData);
    public static final BlockBasedImmersiveHandler<?> jukeboxHandler = new JukeboxHandler();
    public static final BlockBasedImmersiveHandler<LecternData<CommonBookData>> lecternHandler = new LecternHandler();
    public static final BlockBasedImmersiveHandler<NullStorage> leverHandler = new LeverHandler();
    public static final BlockBasedImmersiveHandler<NullStorage> repeaterHandler = new RepeaterHandler();
    public static final BlockBasedImmersiveHandler<?> shulkerBoxHandler = new ShulkerBoxHandler();
    public static final WorldStorageHandler<SmithingTableStorage> smithingTableHandler = new SmithingTableHandler();
    public static final BlockBasedImmersiveHandler<NullStorage> trapdoorHandler = new TrapdoorHandler();
    public static final BlockBasedImmersiveHandler<?> tcCraftingStationHandler = CompatModule.create(new TCCraftingStationHandler(), TinkersConstruct.compatData);
    public static final BlockBasedImmersiveHandler<?> visualWorkbenchHandler = new VisualWorkbenchHandler();

    static {
        ImmersiveMCRegistrationImpl.doImmersiveRegistration((handler) -> {
            if (!ALL_HANDLERS.contains(handler)) {
                ALL_HANDLERS.add(handler);
                if (handler instanceof BlockBasedImmersiveHandler<?> blockHandler) {
                    BLOCK_HANDLERS.add(blockHandler);
                }
                if (handler instanceof PlayerAttachmentImmersiveHandler<?> attachmentHandler) {
                    ATTACHMENT_HANDLERS.add(attachmentHandler);
                }
            }
        });
    }
}
