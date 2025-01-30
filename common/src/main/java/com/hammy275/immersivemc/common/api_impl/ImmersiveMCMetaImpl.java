package com.hammy275.immersivemc.common.api_impl;

import com.hammy275.immersivemc.api.common.ImmersiveMCMeta;
import com.hammy275.immersivemc.common.config.CommonConstants;

public class ImmersiveMCMetaImpl implements ImmersiveMCMeta {

    public static final ImmersiveMCMeta INSTANCE = new ImmersiveMCMetaImpl();

    @Override
    public boolean compatibleWithAPIVersion(String builtVersion) throws IllegalArgumentException {
        // We only use MAJOR.MINOR.PATCH, so no need to deal with pre-release versioning or build metadata.
        String[] vers = builtVersion.split("\\.");
        if (vers.length != 2) {
            throwBadVersion();
        }
        try {
            int major = Integer.parseInt(vers[0]);
            int minor = Integer.parseInt(vers[1]);
            return major == getMajorAPIVersion() && minor >= getMinorAPIVersion();
        } catch (NumberFormatException e) {
            throwBadVersion();
        }
        return false;
    }

    private void throwBadVersion() {
        throw new IllegalArgumentException("Version string must be of the format x.y where x and y are integers.");
    }

    @Override
    public String getAPIVersion() {
        return "%d.%d".formatted(getMajorAPIVersion(), getMinorAPIVersion());
    }

    @Override
    public int getMajorAPIVersion() {
        return CommonConstants.API_MAJOR_VERSION;
    }

    @Override
    public int getMinorAPIVersion() {
        return CommonConstants.API_MINOR_VERSION;
    }
}
