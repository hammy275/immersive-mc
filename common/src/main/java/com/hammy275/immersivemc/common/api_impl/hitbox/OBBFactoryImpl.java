package com.hammy275.immersivemc.common.api_impl.hitbox;

import com.hammy275.immersivemc.api.common.hitbox.OBB;
import com.hammy275.immersivemc.api.common.hitbox.OBBFactory;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

public class OBBFactoryImpl implements OBBFactory {

    public static final OBBFactory INSTANCE = new OBBFactoryImpl();

    @Override
    public OBB create(AABB aabb) {
        return new OBBImpl(aabb);
    }

    @Override
    public OBB create(AABB aabb, double pitch, double yaw, double roll) {
        return new OBBImpl(aabb, pitch, yaw, roll);
    }

    @Override
    public OBB create(AABB aabb, Quaternionfc rotation) {
        return new OBBImpl(aabb, new Quaternionf(rotation));
    }
}
