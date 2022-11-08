package net.blf02.immersivemc.common.vr;

import net.blf02.immersivemc.ImmersiveMC;
import net.blf02.immersivemc.common.config.CommonConstants;
import net.blf02.vrapi.api.IVRAPI;

public class VRPlugin {

    public static IVRAPI API;

    public static void getVRAPI(IVRAPI ivrapi) {
        API = ivrapi;
        VRPluginVerify.hasAPI = true;
        // Verify version
        VersionInfo compatability = vrAPICompatCheck(ivrapi.getVersionArray());
        String error = null;
        if (compatability == VersionInfo.TOO_NEW) {
            error = String.format("mc-vr-api version too new! Please downgrade to mc-vr-api version %s or higher, but below %s!",
                    CommonConstants.vrAPIVersionAsString(), (CommonConstants.minimumVRAPIVersion[0] + 1) + ".0.0");
        } else if (compatability == VersionInfo.TOO_OLD) {
            error = String.format("mc-vr-api version too old! Please upgrade to mc-vr-api version %s or higher, but below %s!",
                    CommonConstants.vrAPIVersionAsString(), (CommonConstants.minimumVRAPIVersion[0] + 1) + ".0.0");
        }

        if (error != null) {
            ImmersiveMC.LOGGER.fatal("!!!!!\nmc-vr-api is on a bad version in comparison to ImmersiveMC! Crashing...\n!!!!!!");
            throw new RuntimeException(error);
        }
    }

    public static VersionInfo vrAPICompatCheck(int[] incomingVersion) {
        // In format major.minor.patch

        if (incomingVersion[0] < CommonConstants.minimumVRAPIVersion[0]
        || incomingVersion[1] < CommonConstants.minimumVRAPIVersion[1]) {
            // Major or minor version less than the one we're using
            return VersionInfo.TOO_OLD;
        } else if (incomingVersion[0] > CommonConstants.minimumVRAPIVersion[0]) {
            // Major version too high (can't depend on backwards compatability in new major versions!)
            return VersionInfo.TOO_NEW;
        }

        return VersionInfo.GOOD;
    }

    public enum VersionInfo {
        TOO_OLD, TOO_NEW, GOOD;
    }
}
