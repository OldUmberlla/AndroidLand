package com.power.base.utils

import android.text.TextUtils
import android.util.Log

/**
 * 作者：Gongsensen
 * 日期：2022/6/16
 * 说明：log日志打印工具类
 */
object LogUtils {
    enum class LogLevel(val level: Int) {
        L_D(0),
        L_I(1),
        L_W(2),
        L_E(3),
    }

    fun d(tag: String, msg: String) {
        log(LogLevel.L_D, tag, msg)
    }

    fun i(tag: String, msg: String) {
        log(LogLevel.L_I, tag, msg)
    }

    fun w(tag: String, msg: String) {
        log(LogLevel.L_W, tag, msg)
    }

    fun e(tag: String, msg: String) {
        log(LogLevel.L_E, tag, msg)
    }

    /**
     * 截断输出日志
     * @param msg
     */
    private fun log(level: LogLevel, tag: String, msg: String) {
        if (TextUtils.isEmpty(tag) || TextUtils.isEmpty(msg)) {
            return
        }
        var logMsg = msg
        val segmentSize = 3 * 1024
        val length = logMsg.length.toLong()
        // 长度小于等于限制直接打印
        if (length <= segmentSize) {
            logSwitch(level, tag, logMsg)
        } else {
            // 循环分段打印日志
            while (logMsg.length > segmentSize) {
                val logContent = logMsg.substring(0, segmentSize)
                logMsg = logMsg.replace(logContent, "")
                logSwitch(level, tag, logContent)
            }
            // 打印剩余日志
            logSwitch(level, tag, logMsg)
        }
    }

    private fun logSwitch(level: LogLevel, tag: String, msg: String) {
        val logMsg = String(msg.toByteArray(Charsets.UTF_8), Charsets.UTF_8)
        if (TextUtils.isEmpty(logMsg)) {
            return
        }
        when (level.level) {
            LogLevel.L_D.level -> {
                Log.d(tag, logMsg)
            }
            LogLevel.L_I.level -> {
                Log.i(tag, logMsg)
            }
            LogLevel.L_W.level -> {
                Log.w(tag, logMsg)
            }
            LogLevel.L_E.level -> {
                Log.e(tag, logMsg)
            }
        }
    }
}