package com.power.http

import com.blankj.utilcode.BuildConfig


/**
 * 作者：Gongsensen
 * 日期：2022/5/18
 * 说明：
 */
object NetConstants {

    const val RESULT_OK = 200
    const val RESULT_SUCCESS = true
    const val RESULT_NOT_OK = "0000"
    const val RESULT_JSON_SYNTAX_EXCEPTION = "0001"
    const val RESULT_IO_CANCEL_EXCEPTION = "0002"
    const val RESULT_IO_EXCEPTION = "0003"
    const val RESULT_CLASS_CAST_EXCEPTION = "0004"
    const val RESULT_EXCEPTION = "0005"

    //测试环境
    private const val HOST_QA = "https://"

    //正式环境
    private const val HOST_PRODUCT = "https://"

    //获取域名
    fun getUrl(): String {
        if (BuildConfig.DEBUG) {
            return HOST_QA
        } else {
            return HOST_PRODUCT
        }
    }

}