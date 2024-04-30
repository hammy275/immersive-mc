package com.hammy275.immersivemc.common.obb;

import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class OBBRotList {

    private final List<OBBRot> rotList = new ArrayList<>();

    private OBBRotList() {

    }

    private OBBRotList(OBBRot... rotations) {
        for (OBBRot rot : rotations) {
            if (rot == null) {
                throw new NullPointerException("Cannot add null rotation to rotation list.");
            } else if (rot.rot() != 0) {
                rotList.add(rot);
            }
        }
    }

    private OBBRotList(List<OBBRot> rotList) {
        this.rotList.addAll(rotList);
    }

    /**
     * Add the given rotation to this OBBRotList.
     * @param rot Rotation to add.
     * @param rotType Rotation type to add.
     * @return this.
     */
    public OBBRotList addRot(float rot, RotType rotType) {
        if (rot != 0) {
            rotList.add(new OBBRot(rot, rotType));
        }
        return this;
    }

    /**
     * Add the given rotation to this OBBRotList.
     * @param rot Rotation to add.
     * @param rotType Rotation type to add.
     * @return this.
     */
    public OBBRotList addRot(double rot, RotType rotType) {
        return addRot((float) rot, rotType);
    }

    /**
     * Rotates the provided Vec3 with the rotations in this OBBRotList.
     * @param toRot The Vec3 (such as a position) to rotate.
     * @param negative Whether to negate the rotation amount before rotating.
     * @return The rotated vector.
     */
    public Vec3 rotate(Vec3 toRot, boolean negative) {
        float mult = negative ? -1 : 1;
        for (OBBRot rot : this.rotList) {
            switch (rot.rotType()) {
                case PITCH -> toRot = toRot.xRot(rot.rot() * mult);
                case YAW -> toRot = toRot.yRot(rot.rot() * mult);
                case ROLL -> toRot = toRot.zRot(-rot.rot() * mult);
            }
        }
        return toRot;
    }

    /**
     * @return A copy of the rotations that make this rotation list.
     */
    public List<OBBRot> getRotations() {
        return new ArrayList<>(this.rotList);
    }

    /**
     * @return A copy of this OBBRotList
     */
    public OBBRotList copy() {
        return new OBBRotList(this.rotList);
    }

    /**
     * Creates an OBBRotList with no rotations.
     * @return A new OBBRotList instance.
     */
    public static OBBRotList create() {
        return new OBBRotList();
    }

    /**
     * Creates an OBBRotList with one or more rotations.
     * @param rotations One or more non-null rotations to initialize this OBBRotList with.
     * @return A new OBBRotList instance.
     */
    public static OBBRotList create(OBBRot... rotations) {
        return new OBBRotList(rotations);
    }
}
