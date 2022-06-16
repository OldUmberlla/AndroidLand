package com.power.face;

import android.content.Context;

import com.power.base.utils.LogUtils;

import java.io.File;
import java.util.List;

/**
 * 作者：Gongsensen
 * 日期：2022/5/5
 * 说明：
 */
public class LuBanCompressImg {
    private static final String TAG = LuBanCompressImg.class.getSimpleName();

    public static File compressImg(Context context, String filePath) {
        try {
//            List<File> files = Luban.with(context)
//                    .load(new File(filePath))
//                    .ignoreBy(100)
//                    .get();
//            LogUtils.INSTANCE.d(TAG, " compressImg files:" + files);
//            if (files != null && files.size() != 0) {
//                return files.get(0);
//            } else {
                return null;
//            }
        } catch (Exception e) {
            LogUtils.INSTANCE.e(TAG, " compressImg error:" + e);
            return null;
        }
    }
}
