package com.power.traffic.entity

/**
 * 作者：Gongsensen
 * 日期：2022/2/21
 * 说明：流量统计数据本地化保存格式
 */

data class TrafficInfoBean(
    val allTraffic: Long?,//设备全部流量消耗统计（不区分网络环境）
    val appTraffic: Long?,//当前应用的流量消耗
    val currentTime: String?,//当天的时间：年月日
    val mobileTraffic: Long?, //手机网络状态下的流量消耗：kb
    val statisticsTime: String?,//统计数据的时间：年月日时分秒
    val wifiTraffic: Long?,//WiFi状态下的流量消耗：kb
)
