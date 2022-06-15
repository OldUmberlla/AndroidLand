package com.power.base.utils;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * 作者：Gongsensen
 * 日期：2021/11/23
 * 说明：获取设备的IMEI号，10.0系统下有效，获取前需要动态申请权限 android.permission.READ_PHONE_STATE
 */
public class IMEIUtil {
    private static final String TAG = "IMEIUtil";

    public static String getIMEI(Context context) {
        String imei = "";
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                imei = tm.getDeviceId();
            } else {
                Method method = tm.getClass().getMethod("getImei");
                imei = (String) method.invoke(tm);
            }
        } catch (Exception e) {
            Log.i(TAG, "getIMEI: " + e);
            e.printStackTrace();
        }
        Log.i(TAG, "getIMEI: " + imei);
        return imei;
    }
}
