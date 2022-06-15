package com.power.traffic

import android.net.TrafficStats


/**
 * 使用TrafficStats获取流量消耗统计数据
 * TrafficStats只能统计设备从开机后所消耗的流量，如果设备关机重启，下次统计数据会重置
 * @author Gss
 */
class TrafficStatsInfo {
    //上传/下载所有流量
    fun getAllBytes(): Long {
        return pretreatmentBytes(getAllRxBytes() + getAllTxBytes())
    }

    //获取设备接收到的总流量
    fun getAllRxBytes(): Long {
        return pretreatmentBytes(TrafficStats.getTotalRxBytes())
    }

    //获取设备发送的总流量
    fun getAllTxBytes(): Long {
        return pretreatmentBytes(TrafficStats.getTotalTxBytes())
    }

    //移动网上传/下载所有流量
    fun getAllBytesMobile(): Long {
        return pretreatmentBytes(getAllRxBytesMobile() + getAllTxBytesMobile())
    }

    //获取设备移动网络下接收到的总流量（非wifi）
    fun getAllRxBytesMobile(): Long {
        return pretreatmentBytes(TrafficStats.getMobileRxBytes())
    }

    //获取设备移动网络下发送的总流量（非wifi）
    fun getAllTxBytesMobile(): Long {
        return pretreatmentBytes(TrafficStats.getMobileTxBytes())
    }

    //WIFI 上传/下载所有流量
    fun getAllBytesWifi(): Long {
        return pretreatmentBytes(getAllRxBytesWifi() + getAllTxBytesWifi())
    }

    //设备接收的总流量 - 设备移动网接收总流量 = 设备wifi下接收的总流量
    fun getAllRxBytesWifi(): Long {
        return pretreatmentBytes(TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes())
    }

    //设备发送的总流量 - 设备移动网发送总流量 = 设备wifi下发送的总流量
    fun getAllTxBytesWifi(): Long {
        return pretreatmentBytes(TrafficStats.getTotalTxBytes() - TrafficStats.getMobileTxBytes())
    }

    //指定应用下所接收总流量
    fun getPackageRxBytes(uid: Int): Long {
        return pretreatmentBytes(TrafficStats.getUidRxBytes(uid))
    }

    //指定应用下发送总流量
    fun getPackageTxBytes(uid: Int): Long {
        return pretreatmentBytes(TrafficStats.getUidTxBytes(uid))
    }

    //指定应用所消耗的全部流量（移动+wifi）
    fun getUidAllBytes(uid: Int): Long {
        return pretreatmentBytes(getPackageRxBytes(uid) + getPackageTxBytes(uid))
    }

    /**
     * 预处理一下数据，负数值默认返回0
     */
    private fun pretreatmentBytes(l: Long?): Long {
        if (l == null) {
            return 0
        }
        if (l >= 0) {
            return l
        } else {
            return 0
        }
    }
}