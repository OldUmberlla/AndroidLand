package com.power.http

import com.blankj.utilcode.util.GsonUtils
import com.power.base.BaseSingleton
import com.power.base.utils.LogUtils
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import java.io.IOException
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit


/**
 * 作者：Gongsensen
 * 日期：2022/5/19
 * 说明：网络请求工具类
 */
class EasyRequest {
    companion object {
        private const val TAG = "EasyRequest"

        //超时时间
        private const val TIME_OUT = 20L
        private val UTF8 = Charset.forName("UTF-8")

        //Post请求的MediaType 默认json字符串格式
        private const val jsonMediaType = "application/json; charset=utf-8"

        fun getInstance(): EasyRequest {
            return object : BaseSingleton<EasyRequest>() {
                override fun newInstance(): EasyRequest {
                    return EasyRequest()
                }
            }.instance
        }
    }


    /**
     * 网络拦截器
     */
    private val networkInterceptor = Interceptor { chain ->
        val startTime = System.currentTimeMillis()
        val request = chain.request()
        val response = chain.proceed(request)
        val requestBody = requestBody(request)
        val headers = request.headers
        val responseBody = responseBody(response)
        val requestTime = System.currentTimeMillis() - startTime
        LogUtils.d(
            TAG,
            "-->Request \nRequestTime:$requestTime \nURL:${request.url} \nHeaders:$headers \nRequestBody:$requestBody \nResponseBody:$responseBody"
        )
        response
    }

    /**
     * okHttpClient对象构建
     */
    private var okHttpClient = OkHttpClient.Builder()
        .callTimeout(TIME_OUT, TimeUnit.SECONDS)//完整请求超时时长，从发起到接收返回数据，默认值0，不限定,
        .connectTimeout(TIME_OUT, TimeUnit.SECONDS)//与服务器建立连接的时长，默认10s
        .readTimeout(TIME_OUT, TimeUnit.SECONDS)//读取服务器返回数据的时长
        .writeTimeout(TIME_OUT, TimeUnit.SECONDS)//向服务器写入数据的时长，默认10s
        .retryOnConnectionFailure(true)
        .cookieJar(CookieJar.NO_COOKIES)
        .addNetworkInterceptor(networkInterceptor)//添加网络拦截器，可以对okhttp的网络请求做拦截处理，不同于应用拦截器，这里能感知所有网络状态，比如重定向。
        .build()

    /**
     * Get请求
     * @param url
     * @param tag
     * @param param
     */
    fun get(url: String, tag: Any? = TAG): String? {
        try {
            val request = Request.Builder()
                .url(url)
                .get()
                .cacheControl(CacheControl.FORCE_NETWORK)
                .tag(tag)
                .build()
            val response = okHttpClient.newCall(request).execute()
            return response.body?.string()
        } catch (e: Exception) {
            LogUtils.e(TAG, "get error:$e")
            return e.toString()
        }
    }

    /**
     * Post请求 json格式参数
     * @param url 接口地址
     * @param tag 请求标识，可根据tag主动取消请求
     * @param entity 对象序列化为json类型参数
     */
    fun postJson(url: String, tag: Any? = TAG, entity: Any?): String? {
        try {
            val mediaType = jsonMediaType.toMediaTypeOrNull()
            val json = GsonUtils.toJson(entity)
            val requestBody = json.toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .cacheControl(CacheControl.FORCE_NETWORK)
                .tag(tag)
                .build()
            val response = okHttpClient.newCall(request).execute()
            return response.body?.string()
        } catch (e: Exception) {
            LogUtils.e(TAG, "postJson error:$e")
            return e.toString()
        }
    }


    /**
     * Post请求 Form表单形式
     * @param url
     * @param tag
     * @param map 表单数据
     */
    fun postForm(url: String, tag: Any? = TAG, map: Map<String, String>?): String? {
        try {
            val builder = FormBody.Builder()
            map?.iterator()?.forEach {
                builder.add(it.key, it.key)
            }
            val formBody: RequestBody = builder.build()
            val request = Request.Builder()
                .url(url)
                .post(formBody)
                .cacheControl(CacheControl.FORCE_NETWORK)
                .tag(tag)
                .build()
            val response = okHttpClient.newCall(request).execute()
            return response.body?.string()
        } catch (e: Exception) {
            LogUtils.e(TAG, "postForm error:$e")
            return e.toString()
        }
    }

    /**
     * 打印响应体的返回参数
     * @param response
     */
    private fun responseBody(response: Response): String? {
        return try {
            val source = response.body?.source()
            source?.request(Long.MAX_VALUE)
            val buffer = source?.buffer()
            buffer?.clone()?.readString(UTF8)
        } catch (e: Exception) {
            e.toString()
        }
    }

    /**
     * 打印请求体request
     * @param request
     */
    private fun requestBody(request: Request?): String? {
        return try {
            val copy = request?.newBuilder()?.build()
            val buffer = Buffer()
            copy?.body?.writeTo(buffer)
            buffer.readUtf8()
        } catch (e: IOException) {
            e.toString()
        }
    }

    /**
     * 添加 header
     */
    fun buildHeaders(map: HashMap<String, String>?): Headers {
        val header = Headers.Builder()
        map?.iterator()?.forEach {
            header.add(it.key, it.value)
        }
        return header.build()
    }

    /**
     * 取消指定网络请求
     * @param tag
     */
    fun cancel(tag: Any? = TAG) {
        for (call in okHttpClient.dispatcher.runningCalls()) {
            call.request().tag().let {
                if (it == tag) {
                    call.cancel()
                }
            }
        }
        for (call in okHttpClient.dispatcher.queuedCalls()) {
            call.request().tag().let {
                if (it == tag) {
                    call.cancel()
                }
            }
        }
    }

    /**
     * 取消全部网络请求
     */
    fun cancelAll() {
        for (call in okHttpClient.dispatcher.runningCalls()) {
            call.cancel()
        }
        for (call in okHttpClient.dispatcher.queuedCalls()) {
            call.cancel()
        }
    }
}