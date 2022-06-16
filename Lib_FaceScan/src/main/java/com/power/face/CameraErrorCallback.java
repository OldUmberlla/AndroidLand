package com.power.face;

import android.hardware.Camera;
import android.util.Log;

/**
 * 作者：Gongsensen
 * 日期：2022/6/16
 * 说明：
 */
public class CameraErrorCallback implements Camera.ErrorCallback {

    private static final String TAG = "CameraErrorCallback";

    @Override
    public void onError(int error, Camera camera) {
        Log.e(TAG, "Encountered an unexpected camera error: " + error);
    }
}