package com.power.face;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.power.base.utils.LogUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 作者：Gongsensen
 * 日期：2022/4/15
 * 说明：
 */
public class FileUtils {
    public static final String TAG = "FileUtils";
    public static final String imagePath = "Face-OCR/imgs/";


    /**
     * 获取保存在本地的所有图片数组
     *
     * @param context
     * @return 没有文件情况下返回空数据组, 异常情况下返回null
     */
    public static List<String> getImgPathList(Context context) {
        try {
            List<String> list = new ArrayList<>();
            File file = new File(context.getExternalFilesDir(null), imagePath);
            //文件夹不存在直接返回
            if (!file.exists()) {
                return list;
            }
            File[] fileList = file.listFiles();
            //没有文件直接返回
            if (fileList == null || fileList.length <= 0) {
                return list;
            }
            for (int i = 0; i < fileList.length; i++) {
                list.add(fileList[i].getPath());
            }
            return list;
        } catch (Exception e) {
            LogUtils.INSTANCE.i(TAG, "FileUtils getImgPathList :" + e);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 读取文件为Bitmap
     *
     * @param filePath
     * @return
     * @throws FileNotFoundException
     */
    public static Bitmap getBitmapFromFile(String filePath) {
        try {
            InputStream is = new FileInputStream(filePath);
            return BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            LogUtils.INSTANCE.i(TAG, "FileUtils getBitmapFromFile :" + e);
            e.printStackTrace();
        }
        return null;
    }

    public static boolean deleteFile(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        File file = new File(path);
        return file.delete();
    }

    /**
     * 保存Bitmap到程序内部目录内，此目录保存用户无法从系统相册内看到
     *
     * @param context 上下文
     * @param bitmap  要保存的图片
     * @param bitName 文件名
     * @return
     */
    public static String saveBitmapToFile(Context context, Bitmap bitmap, String bitName) {
        //首次进入需要检查是否有二级目录，没有则创建后再保存
        if (checkFileDirs(context)) {
            String path = imagePath + bitName + ".png";
            File file = new File(context.getExternalFilesDir(null), path);
            try {
                //文件输出流
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                //压缩图片，如果要保存png，就用Bitmap.CompressFormat.PNG，要保存jpg就用Bitmap.CompressFormat.JPEG,质量是100%，表示不压缩
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
                //返回保存的图片的路径
                return file.getPath();
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.INSTANCE.i(TAG, "FileUtils saveBitmapToFile :" + e);
                return "";
            }
        } else {
            return "";
        }
    }

    /**
     * 判断文件夹是否存在
     *
     * @param context
     * @return
     */
    private static boolean checkFileDirs(Context context) {
        try {
            File file = new File(context.getExternalFilesDir(null), imagePath);
            if (!file.exists()) {
                return file.mkdirs();
            } else {
                return true;
            }
        } catch (Exception e) {
            LogUtils.INSTANCE.i(TAG,"FileUtils checkFileDirs :" + e);
            return false;
        }
    }


    public static String createFileName() {
        return "FaceOCR_" + System.currentTimeMillis();
    }
}
