package com.hammy275.immersivemc.common.compat;

import com.hammy275.immersivemc.common.compat.lootr.LootrCompat;
import com.hammy275.immersivemc.common.compat.lootr.LootrNullImpl;

public class Lootr {
    // Currently active LootrCompat instance. Should be set by modloader-specific subprojects.
    public static LootrCompat lootrImpl = new LootrNullImpl();
    // CompatData info for Lootr
    public static CompatData compatData = new CompatData("Lootr",
            (config, newValue) -> Lootr.lootrImpl = new LootrNullImpl());
}
