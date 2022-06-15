package com.power.traffic

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.blankj.utilcode.util.*
import com.power.traffic.utils.TrafficTools

/**
 * 作者：Gongsensen
 * 日期：2022/2/16
 * 说明：
 */
class TrafficHelper private constructor() {
    companion object {
        private const val TAG = "NetTrafficHelper"

        //双重校验锁式单例
        val INSTANCE: TrafficHelper by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            TrafficHelper()
        }
    }

    private var mContext: Context? = null

    //设备编号，设备唯一标识
    private var deviceCode: String? = ""

    //设备说明
    private var deviceDesc: String? = ""

    //TrafficStats：获取流量消耗功能
    private var trafficStatsInfo: TrafficStatsInfo? = null

    //本地统计记录间隔：1分钟
    private val saveTime: Long = 1 * (1000 * 60)

    private val mHandler: Handler = Handler(Looper.getMainLooper())

    //计时任务：流量消耗数据本地化存储
    private val timerSaveTraffic = Runnable {
        saveTraffic()
    }

    /**
     * 数据初始化
     */
    fun init(
        context: Context,
        deviceCode: String?,
        deviceDesc: String?,
    ): TrafficHelper {
        this.mContext = context
        //数据初始化
        initData(deviceCode, deviceDesc)
        //重置缓存数据
        TrafficTools.resetSPData()
        //初始化TrafficStats对象用来获取流量消耗信息
        trafficStatsInfo = TrafficStatsInfo()
        LogUtils.d("$TAG init() Successful!")
        return INSTANCE
    }

    /**
     * 功能初始化
     */
    fun start(): TrafficHelper {
        //开启之后需要先统计一次
        saveTraffic()
        LogUtils.d("$TAG start() Successful!")
        return INSTANCE
    }

    private fun initData(
        deviceCode: String?,
        deviceDesc: String?,
    ) {
        this.deviceCode = deviceCode
        this.deviceDesc = deviceDesc
    }

    //对外提供TrafficStats对象可获取更多流量统计类型数据
    fun getTrafficStats(): TrafficStatsInfo? {
        return trafficStatsInfo
    }

    /**
     * 将统计的数据记录做本地化存储
     */
    private fun saveTraffic() {
        if (trafficStatsInfo == null || mContext == null) {
            return
        }
        TrafficTools.createData2File(trafficStatsInfo, mContext) {
            //在回调中开启计时统计任务
            mHandler.removeCallbacks(timerSaveTraffic)
            mHandler.postDelayed(timerSaveTraffic, saveTime)
        }
    }

}