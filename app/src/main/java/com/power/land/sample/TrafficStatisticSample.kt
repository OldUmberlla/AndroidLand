package com.power.land.sample

import android.content.Context
import com.power.traffic.TrafficHelper
import com.power.traffic.TrafficStatsInfo
import com.power.traffic.utils.TrafficTools

/**
 * 作者：Gongsensen
 * 日期：2022/6/15
 * 说明：TrafficStatistic流量统计示例类
 */
class TrafficStatisticSample {
    //需要在程序打开时进行注册，SDK会主动统计流量消耗
    fun registerTraffic(context: Context) {
        TrafficHelper.INSTANCE.init(
            context,
            "设备唯一编码/标识",
            "设备说明",
        ).start()
    }

    //可以通过TrafficStatsInfo主动获取当前消耗流量
    fun getTrafficInfo(context: Context) {
        val statsInfo = TrafficStatsInfo()
            .apply {
                //上传/下载所有流量
                getAllBytes()
                //获取设备接收到的总流量
                getAllRxBytes()
                //获取设备接收到的总流量
                getAllRxBytes()
                //获取设备发送的总流量
                getAllTxBytes()
                //移动网上传/下载所有流量
                getAllBytesMobile()
                //获取设备移动网络下接收到的总流量（非wifi）
                getAllRxBytesMobile()
                //获取设备移动网络下发送的总流量（非wifi）
                getAllTxBytesMobile()
                //WIFI 上传/下载所有流量
                getAllBytesWifi()
                //设备接收的总流量 - 设备移动网接收总流量 = 设备wifi下接收的总流量
                getAllRxBytesWifi()
                //设备发送的总流量 - 设备移动网发送总流量 = 设备wifi下发送的总流量
                getAllTxBytesWifi()
                //指定应用下所接收总流量
                getPackageRxBytes(TrafficTools.getUid(context = context))
                //指定应用下发送总流量
                getPackageTxBytes(TrafficTools.getUid(context = context))
                //指定应用所消耗的全部流量（移动+wifi）
                getUidAllBytes(TrafficTools.getUid(context = context))
            }
    }
}