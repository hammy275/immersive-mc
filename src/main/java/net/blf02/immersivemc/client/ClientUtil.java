package net.blf02.immersivemc.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientUtil {

    @OnlyIn(Dist.CLIENT)
    public static void setRightClickCooldown() {
        Minecraft.getInstance().rightClickDelay = 4;
    }
}
