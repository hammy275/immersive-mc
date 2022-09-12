package net.blf02.quilt;

import net.blf02.ExampleMod;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class ImmersiveMCQuilt implements ModInitializer {
    @Override
    public void onInitialize(ModContainer mod) {
        ExampleMod.init();
    }
}
