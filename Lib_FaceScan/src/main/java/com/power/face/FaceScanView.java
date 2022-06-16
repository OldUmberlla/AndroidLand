package com.power.face;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.power.base.utils.LogUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者：Gongsensen
 * 日期：2022/4/19
 * 说明：
 */
public class FaceScanView implements
        SurfaceHolder.Callback,
        Camera.PreviewCallback {
    // Number of Cameras in device.
//    private int numberOfCameras;

    public static final String TAG = FaceScanView.class.getSimpleName();

//    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private Camera mCamera;
    private int cameraId = 0;

    // 让我们跟踪显示旋转和方向：
    private int mDisplayRotation;
    private int mDisplayOrientation;

    private int previewWidth;
    private int previewHeight;

    // 相机数据的表面视图
    private SurfaceView surfaceView;

    // 绘制矩形和其他花哨的东西
    private FaceOverlayView faceOverlayView;

    //所有的错误回调
    private final CameraErrorCallback mErrorCallback = new CameraErrorCallback();

    private static final int MAX_FACE = 10;
    private boolean isThreadWorking = false;
    private Handler handler;
    private FaceDetectThread detectThread = null;
    private int prevSettingWidth;
    private int prevSettingHeight;
    private FaceDetector fdet;

    private FaceResult faces[];
    private FaceResult faces_previous[];
    private int Id = 0;

    private HashMap<Integer, Integer> facesCount = new HashMap<>();

    private final Context mContext;
    private final Activity activity;

    private String vcode = "";
    private String lineId = "";
    private String vehicleNo = "";


    public FaceScanView(@NonNull Context context, @NonNull Activity activity, Map<String, Object> params) {
        this.mContext = context;
        this.activity = activity;
        initView();
        initHandler();
        initSetting();
        startPreview();
        getFromFlutterParam(params);
    }

    /**
     * 获取flutter端widget传来的值
     *
     * @param params
     */
    private void getFromFlutterParam(Map<String, Object> params) {
        LogUtils.INSTANCE.i(TAG, "getFromFlutterParam params=" + params.toString());
        //商户号和线路id在图片上传中需要用到
        if (params.containsKey("vcode")) {
            this.vcode = (String) params.get("vcode");
            LogUtils.INSTANCE.d(TAG, "getFromFlutterParam vcode=" + vcode);
        }
        if (params.containsKey("lineId")) {
            this.lineId = (String) params.get("lineId");
            LogUtils.INSTANCE.d(TAG, "getFromFlutterParam lineId=" + lineId);
        }
        //车牌号
        if (params.containsKey("vehicleNo")) {
            this.vehicleNo = (String) params.get("vehicleNo");
            LogUtils.INSTANCE.d(TAG, "getFromFlutterParam vehicleNo=" + vehicleNo);
        }

    }

    /**
     * 初始化view及其它必要的功能和数据
     */
    private void initView() {
        surfaceView = new SurfaceView(mContext);
        faceOverlayView = new FaceOverlayView(mContext);
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        activity.addContentView(faceOverlayView, new ViewGroup.LayoutParams(350, 350));
    }

    private void initSetting() {
        faces = new FaceResult[MAX_FACE];
        faces_previous = new FaceResult[MAX_FACE];
        for (int i = 0; i < MAX_FACE; i++) {
            faces[i] = new FaceResult();
            faces_previous[i] = new FaceResult();
        }
        //权限检查
        checkCameraPermission();
        //生命周期处理
        registerLifecycle();
    }

    private void initHandler() {
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == 0) {
                    startPreview();
                }
            }
        };
    }

    //在访问相机之前检查相机权限。如果尚未授予权限，请请求权限
    private void checkCameraPermission() {
        SurfaceHolder holder = surfaceView.getHolder();
        holder.addCallback(this);
        holder.setFormat(ImageFormat.NV21);
    }

    /**
     * activity生命周期处理
     */
    private void registerLifecycle() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                @Override
                public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

                }

                @Override
                public void onActivityStarted(@NonNull Activity activity) {

                }

                @Override
                public void onActivityResumed(@NonNull Activity activity) {
                    startPreview();
                }

                @Override
                public void onActivityPaused(@NonNull Activity activity) {
                    if (mCamera != null) {
                        mCamera.stopPreview();
                    }
                }

                @Override
                public void onActivityStopped(@NonNull Activity activity) {

                }

                @Override
                public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

                }

                @Override
                public void onActivityDestroyed(@NonNull Activity activity) {
                    faceOverlayView = null;
                    surfaceView = null;
                    mCamera = null;
                    handler = null;
                    detectThread = null;
                }
            });
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        //查找可用的相机总数
//        numberOfCameras = Camera.getNumberOfCameras();
        try {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    if (cameraId == 0) cameraId = i;
                }
            }
            mCamera = Camera.open(cameraId);
            Camera.getCameraInfo(cameraId, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                faceOverlayView.setFront(true);
            }
            mCamera.setPreviewDisplay(surfaceView.getHolder());
        } catch (Exception e) {
            LogUtils.INSTANCE.e(TAG, " surfaceCreated error:" + e);
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        // 没有表面，立即返回
        if (surfaceHolder.getSurface() == null) {
            return;
        }
        // 尝试停止当前预览：
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            LogUtils.INSTANCE.e(TAG, "surfaceChanged stopPreview error:" + e);
        }

        configureCamera(width, height);
        setDisplayOrientation();
        setErrorCallback();

        try {
            // 创建 media.FaceDetector
            float aspect = (float) previewHeight / (float) previewWidth;
            fdet = new FaceDetector(prevSettingWidth, (int) (prevSettingWidth * aspect), MAX_FACE);
        } catch (Exception e) {
            LogUtils.INSTANCE.e(TAG, "surfaceChanged error:" + e);
        }
        // 一切都配置好了！最后再次启动相机预览：
        startPreview();
    }

    private void setErrorCallback() {
        if (mCamera != null) {
            mCamera.setErrorCallback(mErrorCallback);
        }
    }

    private void setDisplayOrientation() {
        try {
            // 现在设置显示方向
            mDisplayRotation = Util.getDisplayRotation(activity);
            mDisplayOrientation = Util.getDisplayOrientation(mDisplayRotation, cameraId);

            mCamera.setDisplayOrientation(mDisplayOrientation);

            if (faceOverlayView != null) {
                faceOverlayView.setDisplayOrientation(mDisplayOrientation);
            }
        } catch (Exception e) {
            LogUtils.INSTANCE.e(TAG, " setDisplayOrientation error:" + e);
        }
    }

    private void configureCamera(int width, int height) {
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            // 设置 PreviewSize 和 AutoFocus
            setOptimalPreviewSize(parameters, width, height);
            setAutoFocus(parameters);
            // 并设置参数
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            LogUtils.INSTANCE.e(TAG, " configureCamera error:" + e);
        }

    }

    private void setOptimalPreviewSize(Camera.Parameters cameraParameters, int width, int height) {
        List<Camera.Size> previewSizes = cameraParameters.getSupportedPreviewSizes();
        float targetRatio = (float) width / height;
        Camera.Size previewSize = Util.getOptimalPreviewSize(activity, previewSizes, targetRatio);
        previewWidth = previewSize.width;
        previewHeight = previewSize.height;

        LogUtils.INSTANCE.d(TAG, "previewWidth:" + previewWidth);
        LogUtils.INSTANCE.d(TAG, "previewHeight:" + previewHeight);

        /**
         * 计算大小以将全帧位图缩放为更小的位图在缩放位图中检测人脸比全位图具有更高的性能。
         * 较小的图像尺寸->检测速度更快，但检测面部的距离更短，因此根据您的目的计算尺寸
         */
        if (previewWidth / 4 > 360) {
            prevSettingWidth = 360;
            prevSettingHeight = 270;
        } else if (previewWidth / 4 > 320) {
            prevSettingWidth = 320;
            prevSettingHeight = 240;
        } else if (previewWidth / 4 > 240) {
            prevSettingWidth = 240;
            prevSettingHeight = 160;
        } else {
            prevSettingWidth = 160;
            prevSettingHeight = 120;
        }

        cameraParameters.setPreviewSize(previewSize.width, previewSize.height);

        faceOverlayView.setPreviewWidth(previewWidth);
        faceOverlayView.setPreviewHeight(previewHeight);
    }

    private void setAutoFocus(Camera.Parameters cameraParameters) {
        List<String> focusModes = cameraParameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
            cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
    }

    private void startPreview() {
        if (mCamera != null) {
            isThreadWorking = false;
            mCamera.startPreview();
            mCamera.setPreviewCallback(this);
            counter = 0;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (mCamera != null) {
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.setErrorCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onPreviewFrame(byte[] _data, Camera _camera) {
        if (!isThreadWorking) {
            if (counter == 0)
                start = System.currentTimeMillis();

            isThreadWorking = true;
            waitForFdetThreadComplete();
//            LogUtils.d(TAG + " onPreviewFrame detectThread");
            detectThread = new FaceDetectThread(handler);
            detectThread.setData(_data);
            detectThread.start();
        }
    }

    private void waitForFdetThreadComplete() {
        if (detectThread == null) {
            return;
        }

        if (detectThread.isAlive()) {
            try {
                detectThread.join();
                detectThread = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    // fps 检测人脸（不是相机的 FPS）
    long start, end;
    int counter = 0;
    double fps;

    /**
     * 在线程中进行面部检测
     */
    private class FaceDetectThread extends Thread {
        private Handler handler;
        private byte[] data = null;

        public FaceDetectThread(Handler handler) {
            this.handler = handler;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        public void run() {
            float aspect = (float) previewHeight / (float) previewWidth;
            int w = prevSettingWidth;
            int h = (int) (prevSettingWidth * aspect);

            Bitmap bitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.RGB_565);
            // 人脸检测：首先将图像从NV21转换为RGB_565
            YuvImage yuv = new YuvImage(data, ImageFormat.NV21,
                    bitmap.getWidth(), bitmap.getHeight(), null);
            // 使 rect 成为成员并将其用于上面的宽度和高度值
            Rect rectImage = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

            // 使用线程选项或循环缓冲区来转换流？
            //see http://ostermiller.org/convert_java_outputstream_inputstream.html
            ByteArrayOutputStream baout = new ByteArrayOutputStream();
            if (!yuv.compressToJpeg(rectImage, 100, baout)) {
                LogUtils.INSTANCE.e(TAG, "compressToJpeg failed");
            }

            BitmapFactory.Options bfo = new BitmapFactory.Options();
            bfo.inPreferredConfig = Bitmap.Config.RGB_565;
            bitmap = BitmapFactory.decodeStream(
                    new ByteArrayInputStream(baout.toByteArray()), null, bfo);

            Bitmap bmp = Bitmap.createScaledBitmap(bitmap, w, h, false);

            float xScale = (float) previewWidth / (float) prevSettingWidth;
            float yScale = (float) previewHeight / (float) h;

            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, info);
            int rotate = mDisplayOrientation;
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT && mDisplayRotation % 180 == 0) {
                if (rotate + 180 > 360) {
                    rotate = rotate - 180;
                } else {
                    rotate = rotate + 180;
                }
            }

            switch (rotate) {
                case 90:
                    bmp = ImageUtils.rotate(bmp, 90);
                    xScale = (float) previewHeight / bmp.getWidth();
                    yScale = (float) previewWidth / bmp.getHeight();
                    break;
                case 180:
                    bmp = ImageUtils.rotate(bmp, 180);
                    break;
                case 270:
                    bmp = ImageUtils.rotate(bmp, 270);
                    xScale = (float) previewHeight / (float) h;
                    yScale = (float) previewWidth / (float) prevSettingWidth;
                    break;
            }

            fdet = new FaceDetector(bmp.getWidth(), bmp.getHeight(), MAX_FACE);

            FaceDetector.Face[] fullResults = new FaceDetector.Face[MAX_FACE];
            fdet.findFaces(bmp, fullResults);

            for (int i = 0; i < MAX_FACE; i++) {
                if (fullResults[i] == null) {
                    faces[i].clear();
                } else {
                    PointF mid = new PointF();
                    fullResults[i].getMidPoint(mid);

                    mid.x *= xScale;
                    mid.y *= yScale;

                    float eyesDis = fullResults[i].eyesDistance() * xScale;
                    float confidence = fullResults[i].confidence();
                    float pose = fullResults[i].pose(FaceDetector.Face.EULER_Y);
                    int idFace = Id;

                    Rect rect = new Rect(
                            (int) (mid.x - eyesDis * 1.20f),
                            (int) (mid.y - eyesDis * 0.55f),
                            (int) (mid.x + eyesDis * 1.20f),
                            (int) (mid.y + eyesDis * 1.85f));

                    //仅检测人脸尺寸 > 100x100
                    if (rect.height() * rect.width() > 100 * 100) {
                        for (int j = 0; j < MAX_FACE; j++) {
                            float eyesDisPre = faces_previous[j].eyesDistance();
                            PointF midPre = new PointF();
                            faces_previous[j].getMidPoint(midPre);

                            RectF rectCheck = new RectF(
                                    (midPre.x - eyesDisPre * 1.5f),
                                    (midPre.y - eyesDisPre * 1.15f),
                                    (midPre.x + eyesDisPre * 1.5f),
                                    (midPre.y + eyesDisPre * 1.85f));

                            if (rectCheck.contains(mid.x, mid.y) && (System.currentTimeMillis() - faces_previous[j].getTime()) < 1000) {
                                idFace = faces_previous[j].getId();
                                break;
                            }
                        }

                        if (idFace == Id) Id++;

                        faces[i].setFace(idFace, mid, eyesDis, confidence, pose, System.currentTimeMillis());

                        faces_previous[i].set(faces[i].getId(), faces[i].getMidEye(), faces[i].eyesDistance(), faces[i].getConfidence(), faces[i].getPose(), faces[i].getTime());

                        // 如果焦点在面部 8 帧中 -> 由于某些第一帧质量低，在 RecyclerView 中拍照面部显示
                        if (facesCount.get(idFace) == null) {
                            facesCount.put(idFace, 0);
                        } else {
                            int count = facesCount.get(idFace) + 1;
                            if (count <= 8) {
                                facesCount.put(idFace, count);
                            }

                            //识别到人脸
                            if (count == 8) {
                                handler.post(() -> {
                                    //暂停预览
                                    mCamera.stopPreview();
                                });

                                if (bitmap != null) {
                                    //压缩
//                                    operationBitmap(bitmap);
                                    //不压缩
                                    originalOperationImg(bitmap);
                                }
                            }
                        }
                    }
                }
            }

            handler.post(() -> {
                //将人脸发送到 FaceView 以绘制矩形
                faceOverlayView.setFaces(faces);
                //计算FPS
                end = System.currentTimeMillis();
                counter++;
                double time = (double) (end - start) / 1000;
                if (time != 0)
                    fps = counter / time;
                faceOverlayView.setFPS(fps);
                if (counter == (Integer.MAX_VALUE - 1000))
                    counter = 0;
                isThreadWorking = false;
            });
        }
    }

    //压缩图片
//    private void operationBitmap(Bitmap bitmap) {
//        //先保存原图
//        String originalImgPath = FileUtils.saveBitmapToFile(mContext, bitmap, FileUtils.createFileName());
//        //开始压缩
//        File compressImg = LuBanCompressImg.compressImg(mContext, originalImgPath);
//        //最后删除原图片
//        FileUtils.deleteFile(originalImgPath);
//        //2秒后再通知handler打开相机预览
//        handler.sendEmptyMessageDelayed(0, 2000);
//    }

    //原图不压缩图片
    private void originalOperationImg(Bitmap bitmap) {
        //先保存原图
        String originalImgPath = FileUtils.saveBitmapToFile(mContext, bitmap, FileUtils.createFileName());
        //2秒后再通知handler打开相机预览
        handler.sendEmptyMessageDelayed(0, 2000);
    }

}
