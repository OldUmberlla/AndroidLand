package com.power.land

import android.app.Application
import com.power.base.utils.CrashHandler

/**
 * 作者：Gongsensen
 * 日期：2022/6/16
 * 说明：
 */
class MyApplication:Application() {
    override fun onCreate() {
        super.onCreate()
        CrashHandler.init(this)

    }
}