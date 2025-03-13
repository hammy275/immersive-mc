package com.hammy275.immersivemc.client.compat.ipn;

import com.hammy275.immersivemc.common.compat.CompatData;

public class IPN {
    public static IPNCompat ipnCompat = new IPNNullImpl();
    public static CompatData compatData = new CompatData("Inventory Profiles Next",
            (config, newValue) -> IPN.ipnCompat = new IPNNullImpl());
}
