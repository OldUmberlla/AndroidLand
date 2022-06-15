package com.power.traffic.entity

/**
 * 作者：Gongsensen
 * 日期：2022/2/21
 * 说明：
 */
object TrafficConst {
    //本地化存储数据路径
    const val NET_TRAFFIC_FILE_PATH = "/traffic_lib/file/Traffic_"

    //本地存储的SP的名称
    const val SP_NAME = "TRAFFIC_SP"

    //关机情况下用来做消耗记录判断条件的缓存值：WiFi状态下的流量消耗
    const val SP_KEY_WIFI_TRAFFIC = "wifiTraffic"

    //关机情况下用来做消耗记录判断条件的缓存值：手机网络状态下的流量消耗
    const val SP_KEY_MOBILE_TRAFFIC = "mobileTraffic"

    //关机情况下用来做消耗记录判断条件的缓存值：设备全部流量消耗统计（不区分网络环境）
    const val SP_KEY_ALL_TRAFFIC = "allTraffic"

    //关机情况下用来做消耗记录判断条件的缓存值：当前应用的流量消耗
    const val SP_KEY_APP_TRAFFIC = "appTraffic"

}