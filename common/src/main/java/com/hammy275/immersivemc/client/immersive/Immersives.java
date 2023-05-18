package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.AbstractWorldStorageInfo;

import java.util.LinkedList;
import java.util.List;

public class Immersives {

    public static final List<AbstractImmersive<? extends AbstractImmersiveInfo>> IMMERSIVES =
            new LinkedList<>();

    public static final List<AbstractWorldStorageImmersive<? extends AbstractWorldStorageInfo>> WS_IMMERSIVES =
            new LinkedList<>();

    public static final ImmersiveAnvil immersiveAnvil = new ImmersiveAnvil();
    public static final ImmersiveBackpack immersiveBackpack = new ImmersiveBackpack();
    public static final ImmersiveBarrel immersiveBarrel = new ImmersiveBarrel();
    public static final ImmersiveBeacon immersiveBeacon = new ImmersiveBeacon();
    public static final ImmersiveBrewing immersiveBrewing = new ImmersiveBrewing();
    public static final ImmersiveChest immersiveChest = new ImmersiveChest();
    public static final ImmersiveCrafting immersiveCrafting = new ImmersiveCrafting();
    public static final ImmersiveETable immersiveETable = new ImmersiveETable();
    public static final ImmersiveFurnace immersiveFurnace = new ImmersiveFurnace();
    public static final ImmersiveHitboxes immersiveHitboxes = new ImmersiveHitboxes();
    public static final ImmersiveHopper immersiveHopper = new ImmersiveHopper();
    public static final ImmersiveJukebox immersiveJukebox = new ImmersiveJukebox();
    public static final ImmersiveRepeater immersiveRepeater = new ImmersiveRepeater();
    public static final ImmersiveShulker immersiveShulker = new ImmersiveShulker();
    public static final ImmersiveSmithingTable immersiveSmithingTable = new ImmersiveSmithingTable();

}
