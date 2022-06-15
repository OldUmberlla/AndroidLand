package com.power.http

import com.blankj.utilcode.util.EncryptUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.google.gson.JsonSyntaxException
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import java.io.IOException
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 作者：Gongsensen
 * 日期：2022/5/19
 * 说明：网络请求工具类
 */
object EasyRequest {
    private const val LOG_TAG = "HttpUtils"
    private const val TIME_OUT = 20L //超时时间
    private const val TAG = "HTTP_TAG" //TAG
    private val UTF8 = Charset.forName("UTF-8")

    /**
     * Post请求的MediaType 默认json字符串格式
     */
    private const val jsonMediaType = "application/json; charset=utf-8"

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
        LogUtils.i(
            "-->Request \nrequestTime:$requestTime \nurl:${request.url} \nheaders:$headers \nrequestBody:$requestBody \nresponseBody:$responseBody"
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
     * okHttpClient对象构建
     * 不添加networkInterceptor
     */
    private var okHttpClientNoLog = OkHttpClient.Builder()
        .callTimeout(TIME_OUT, TimeUnit.SECONDS)//完整请求超时时长，从发起到接收返回数据，默认值0，不限定,
        .connectTimeout(TIME_OUT, TimeUnit.SECONDS)//与服务器建立连接的时长，默认10s
        .readTimeout(TIME_OUT, TimeUnit.SECONDS)//读取服务器返回数据的时长
        .writeTimeout(TIME_OUT, TimeUnit.SECONDS)//向服务器写入数据的时长，默认10s
        .retryOnConnectionFailure(true)
        .cookieJar(CookieJar.NO_COOKIES)
        .build()

    /**
     * Post请求 调用票机OpenApi接口时
     * @param url
     * @param tag
     * @param param
     * @param printLog
     */
    fun postForJson(url: String, tag: Any? = TAG, param: Any?): String? {
        try {
            val mediaType = jsonMediaType.toMediaTypeOrNull()
            val data = GsonUtils.toJson(param)
            val requestBody = data.toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .cacheControl(CacheControl.FORCE_NETWORK)
                .tag(tag)
                .build()
            val response = okHttpClient.newCall(request).execute()
            return response.body?.string()
        } catch (e: Exception) {
            LogUtils.e("$TAG post error:$e")
            return e.toString()
        }
    }


    /**
     * 默认添加拦截器打印日志，添加header请求头参数，Post异步请求
     * @param url
     * @param tag
     * @param param
     * @param callback
     * @return 返回的是List
     */
    fun postRequestList(
        url: String,
        tag: Any? = TAG,
        param: Any?,
    ): NetListRsp? {
        try {
            val response = doPostRequest(NetConstants.getUrl() + url, tag, param)
            val json = response.body?.string()
            LogUtils.d("$TAG postRequestList rsp-json:$json")
            val rspList: NetListRsp? = GsonUtils.fromJson(json, NetListRsp::class.java)
            LogUtils.d("$TAG postRequestList NetListRsp:$rspList")
            //网络异常，返回
            if (response.code != NetConstants.RESULT_OK) {
                return NetListRsp(NetConstants.RESULT_NOT_OK, "Network Error", null, false)
            }
            return rspList
        } catch (e: JsonSyntaxException) {
            LogUtils.e("$LOG_TAG postRequest error:$e")
            return NetListRsp(
                NetConstants.RESULT_JSON_SYNTAX_EXCEPTION, "JsonSyntaxException", null, false
            )
        } catch (e: IOException) {
            LogUtils.e("$LOG_TAG postRequest error:$e")
            return NetListRsp(
                NetConstants.RESULT_IO_EXCEPTION, "IOException", null, false
            )
        } catch (e: ClassCastException) {
            LogUtils.e("$LOG_TAG postRequest error:$e")
            return NetListRsp(
                NetConstants.RESULT_CLASS_CAST_EXCEPTION, "ClassCastException", null, false
            )
        } catch (e: Exception) {
            LogUtils.e("$LOG_TAG postRequest error:$e")
            return NetListRsp(
                NetConstants.RESULT_EXCEPTION, "Exception", null, false
            )
        }
    }

    /**
     * 默认添加拦截器打印日志，添加header请求头参数，Post异步请求
     * @param url
     * @param tag
     * @param param
     * @param callback
     * @return 返回的是单独对象
     */
    fun postRequest(
        url: String,
        tag: Any? = TAG,
        param: Any?,
    ): NetRsp? {
        try {
            val response = doPostRequest(NetConstants.getUrl() + url, tag, param)
            val json = response.body?.string()
            LogUtils.d("$TAG postRequest rsp-json:$json")
            val rsp: NetRsp? = GsonUtils.fromJson(json, NetRsp::class.java)
            LogUtils.d("$TAG postRequest NetRsp:$rsp")
            //网络异常，返回
            if (response.code != NetConstants.RESULT_OK) {
                return NetRsp(NetConstants.RESULT_NOT_OK, "Network Error", "", false)
            }
            return rsp
        } catch (e: JsonSyntaxException) {
            LogUtils.e("$LOG_TAG postRequest error:$e")
            return NetRsp(
                NetConstants.RESULT_JSON_SYNTAX_EXCEPTION, "JsonSyntaxException", "", false
            )
        } catch (e: IOException) {
            LogUtils.e("$LOG_TAG postRequest error:$e")
            return NetRsp(
                NetConstants.RESULT_IO_EXCEPTION, "IOException", "", false
            )
        } catch (e: ClassCastException) {
            LogUtils.e("$LOG_TAG postRequest error:$e")
            return NetRsp(
                NetConstants.RESULT_CLASS_CAST_EXCEPTION, "ClassCastException", "", false
            )
        } catch (e: Exception) {
            LogUtils.e("$LOG_TAG postRequest error:$e")
            return NetRsp(
                NetConstants.RESULT_EXCEPTION, "Exception", "", false
            )
        }
    }

    /**
     * 获取数据以String返回
     */
    private fun doPostRequest(
        url: String,
        tag: Any? = TAG,
        param: Any?
    ): Response {
        val mediaType = jsonMediaType.toMediaTypeOrNull()
        val json = GsonUtils.toJson(param)
        val requestBody = json.toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .headers(buildHeaders(json))
            .cacheControl(CacheControl.FORCE_NETWORK)
            .tag(tag)
            .build()
        return okHttpClient.newCall(request).execute()
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
     * 业务内接口校验请求头
     * TODO header参数还未确认，先写死
     */
    private fun buildHeaders(json: String?): Headers {
        val header = Headers.Builder()
        val header1 = ""
        val header2 = " "
        header.add("header1", header1)
        header.add("header2", header2)
        return header.build()
    }

    /**
     * MD5加密
     */
    fun generateEncryptMd5(string: String?): String? {
        return EncryptUtils.encryptMD5ToString(string)
    }

    /**
     * 取消网络请求
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