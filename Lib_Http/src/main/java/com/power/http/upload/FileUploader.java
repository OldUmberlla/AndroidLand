package com.power.http.upload;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.LogUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

/**
 * 作者：Gongsensen
 * 日期：2022/6/16
 * 说明：文件上传类封装
 */
public class FileUploader {
    private static final String TAG = FileUploader.class.getSimpleName();
    private static final OkHttpClient sOKHttpClient = HttpClientFactory.build();


    public static String uploadFileData(@NotNull String url,
                                        @NotNull String filePath,
                                        String name) {
        try {
            Response response = uploadFileSync(url, file2RequestBody(new File(filePath), name));
            if (response != null) {
                String rspJson = response.body().string();
                return rspJson;
            }
            return "response is null!";
        } catch (Exception e) {
            Log.e(TAG, "uploadFileData:" + e);
            return e.toString();
        }
    }

    /**
     * File转RequestBody
     */
    private static RequestBody file2RequestBody(@NonNull File file, String name) {
        return new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("image/png"), file))
                .addFormDataPart("name", name)
                .build();
    }

    /**
     * 同步上传File
     */
    @Nullable
    public static Response uploadFileSync(@NonNull String url,
                                          @NonNull RequestBody requestBody) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        LogUtils.d(TAG + " uploadFileSync requestBody" + requestBody(request));
        return sOKHttpClient.newCall(request).execute();
    }

    /**
     * 打印 requestBody
     *
     * @param request
     * @return
     */
    private static String requestBody(Request request) {
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (Exception e) {
            return e.toString();
        }
    }
}
