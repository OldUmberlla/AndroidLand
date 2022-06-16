package com.power.http.upload;


import okhttp3.OkHttpClient;

/**
 * 作者：Gongsensen
 * 日期：2022/6/16
 * 说明：HttpClient工厂类
 */
public class HttpClientFactory {
    public static OkHttpClient build() {
        OkHttpClient.Builder builder = HttpClientBuilder.buildOKHttpClient();
        return builder.build();
    }
}