package com.hammy275.immersivemc.common.obb;

import org.joml.Quaternionf;

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
     * @return A copy of the rotations that make this rotation list.
     */
    public List<OBBRot> getRotations() {
        return new ArrayList<>(this.rotList);
    }

    /**
     * @return This {@link OBBRotList} as a Quaternion
     */
    public Quaternionf asQuaternion() {
        Quaternionf quaternion = new Quaternionf();
        for (OBBRot rot : rotList) {
            switch (rot.rotType()) {
                case PITCH -> quaternion.rotateX(-rot.rot());
                case YAW -> quaternion.rotateY(-rot.rot());
                case ROLL -> quaternion.rotateZ(rot.rot());
            }
        }
        return quaternion;
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
