package com.lomoment.serialportsample;

import android.os.Build;

import java.io.File;

/**
 * @author libin
 * @date 2019/4/23
 * @Description
 */

public class VendingMachineDevicesUtils {

    public static String SU_PATH_BIN = "/system/bin/su";
    public static String SU_PATH_XBIN = "/system/xbin/su";

    public static String YUXIAN_MACHINE_MODEL = "yx_rk3288";
    public static String YUXIAN_MACHINE_MODEL_4K = "rk3288_box";

    /**
     * 根据不同设备获取SU路径
     *
     * @return
     */
    public static String getSuPathFromDevices() {
        File file = new File(SU_PATH_XBIN);
        if (file.exists()) {
            return SU_PATH_XBIN;
        }
        return SU_PATH_BIN;
    }

    /**
     * 根据不同设备获取RS232路径
     *
     * @return
     */
    public static String getRs232PathFromDevices() {
        if (YUXIAN_MACHINE_MODEL.equals(Build.MODEL)) {
            return "/dev/ttyS4";
        } else if (YUXIAN_MACHINE_MODEL_4K.equals(Build.MODEL)) {
            return "/dev/ttyS4";
        } else {
            return "/dev/ttyS0";
        }
    }
}
