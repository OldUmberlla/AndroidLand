package com.power.traffic.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import com.blankj.utilcode.util.*
import com.power.traffic.entity.TrafficConst
import com.power.traffic.entity.TrafficInfoBean
import com.power.traffic.TrafficStatsInfo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException

/**
 * 作者：Gongsensen
 * 日期：2022/2/21
 * 说明：
 */
object TrafficTools {

    /**
     * 获取应用的Uid
     */
    @SuppressLint("WrongConstant")
    fun getUid(context: Context?): Int {
        if (context == null) {
            return 0
        }
        try {
            val ai = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_ACTIVITIES
            )
            return ai.uid
        } catch (e: Exception) {
            LogUtils.e("getUid 获取应用uid失败:${e.message}")
            e.printStackTrace()
            return 0
        }
    }

    /**
     * 将统计的数据写入文件中，文件以当天日期命名
     */
    fun createData2File(
        statsInfo: TrafficStatsInfo?,
        context: Context?,
        invoke: (state: Boolean) -> Unit
    ) {
        GlobalScope.launch {
            if (statsInfo == null || context == null) {
                LogUtils.e("createData2File statsInfo:$statsInfo context:$context")
                invoke.invoke(false)
            }
            //当天的时间：年月日
            val currentTime = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd"))
            //统计数据的时间：年月日时分秒
            val statisticsTime = TimeUtils.getNowString()
            //文件路径
            val putFilePath = getFilePath(currentTime)
            LogUtils.d("createData2File putFilePath:$putFilePath")
            //记录数据实体
            var info: TrafficInfoBean? = null

            //每天的第一次开启第一次记录
            if (FileUtils.isFileExists(putFilePath)) {
                //如果当前已有文件且文件内已有记录
                val fileData = readTrafficFile(currentTime)
                LogUtils.d("createData2File fileData:$fileData")
                if (fileData == null) {
                    //有文件但是读取失败说明文件损坏，这种情况直接保存记录
                    info = getSimpleSaveData(statsInfo, currentTime, statisticsTime, context)
                    LogUtils.d("createData2File 2. info:$info")
                } else {
                    //当天关机并重启后，计算的数据记录
                    info = getRestartOpenData(
                        statsInfo,
                        currentTime,
                        statisticsTime,
                        context,
                        fileData
                    )
                    LogUtils.d("createData2File 3. info:$info")
                }
            } else {
                //直接保存记录
                info = getSimpleSaveData(statsInfo, currentTime, statisticsTime, context)
                LogUtils.d("createData2File 1. info:$info")
            }
            try {
                //不管有没有，先删除上一次的文件
                FileUtils.createFileByDeleteOldFile(putFilePath)
                //生成新数据
                val json = GsonUtils.toJson(info)
                //数据写入到文件中
                FileIOUtils.writeFileFromString(putFilePath, json, false)
                LogUtils.i("createData2File 数据写入成功")
                invoke.invoke(true)
            } catch (e: Exception) {
                e.printStackTrace()
                LogUtils.e("createData2File onFailure:${e.message}")
                invoke.invoke(false)
            }
        }
    }


    /**
     * 关机重启后，计算统计的数据
     */
    private fun getRestartOpenData(
        statsInfo: TrafficStatsInfo?,
        currentTime: String?,
        statisticsTime: String?,
        context: Context?,
        fileInfo: TrafficInfoBean?
    ): TrafficInfoBean {
        //实时获取的
        val currentWifiTraffic: Long? = statsInfo?.getAllBytesWifi()
        val currentMobileTraffic: Long? = statsInfo?.getAllBytesMobile()
        val currentAllTraffic: Long? = statsInfo?.getAllBytes()
        val currentAppTraffic: Long? = statsInfo?.getUidAllBytes(getUid(context))
        LogUtils.d("getRestartOpenData->currentWifiTraffic:$currentWifiTraffic currentMobileTraffic:$currentMobileTraffic currentAllTraffic:$currentAllTraffic currentAppTraffic:$currentAppTraffic")
        //要计算的
        var allTraffic: Long? = currentAllTraffic
        var wifiTraffic: Long? = currentWifiTraffic
        var mobileTraffic: Long? = currentMobileTraffic
        var appTraffic: Long? = currentAppTraffic
        LogUtils.d("getRestartOpenData->allTraffic 1:$allTraffic wifiTraffic:$wifiTraffic mobileTraffic:$mobileTraffic appTraffic:$appTraffic")
        if (fileInfo?.allTraffic != null && currentAllTraffic != null) {
            //设备当天关过机，如果本地记录 A > 当前记录 B; 保存当前记录为缓存 C; 新的记录 D = （B - C）+ A
            if (fileInfo.allTraffic > currentAllTraffic) {
                //D = （B - C）+ A
                allTraffic =
                    (currentAllTraffic - getCacheAllTraffic()) + fileInfo.allTraffic
                //刷新缓存值
                getSP().put(TrafficConst.SP_KEY_ALL_TRAFFIC, currentAllTraffic)
            }
        }

        if (fileInfo?.wifiTraffic != null && currentWifiTraffic != null) {
            if (fileInfo.wifiTraffic > currentWifiTraffic) {
                //D = （B - C）+ A
                wifiTraffic =
                    (currentWifiTraffic - getCacheWifiTraffic()) + fileInfo.wifiTraffic
                //刷新缓存值
                getSP().put(TrafficConst.SP_KEY_WIFI_TRAFFIC, currentWifiTraffic)
            }
        }

        if (fileInfo?.mobileTraffic != null && currentMobileTraffic != null) {
            if (fileInfo.mobileTraffic > currentMobileTraffic) {
                //D = （B - C）+ A
                mobileTraffic =
                    (currentMobileTraffic - getCacheMobileTraffic()) + fileInfo.mobileTraffic
                //刷新缓存值
                getSP().put(TrafficConst.SP_KEY_MOBILE_TRAFFIC, currentMobileTraffic)
            }
        }

        if (fileInfo?.appTraffic != null && currentAppTraffic != null) {
            if (fileInfo.appTraffic > currentAppTraffic) {
                //D = （B - C）+ A
                appTraffic =
                    (currentAppTraffic - getCacheAppTraffic()) + fileInfo.appTraffic
                //刷新缓存值
                getSP().put(TrafficConst.SP_KEY_APP_TRAFFIC, currentAppTraffic)
            }
        }

        LogUtils.d("getRestartOpenData->allTraffic 2:$allTraffic wifiTraffic:$wifiTraffic mobileTraffic:$mobileTraffic appTraffic:$appTraffic")

        //将数据写入文件(这里直接存对象，会自动序列化为json，如果转成json字符串再存的话，默认保存的数据是字符，取出来转json的话会有问题)
        return TrafficInfoBean(
            allTraffic,
            appTraffic,
            currentTime,
            mobileTraffic,
            statisticsTime,
            wifiTraffic
        )

    }

    /**
     * 每天的第一次开启情况下流量消耗记录生成
     */
    private fun getSimpleSaveData(
        statsInfo: TrafficStatsInfo?,
        currentTime: String?,
        statisticsTime: String?,
        context: Context?
    ): TrafficInfoBean {
        //WiFi状态下的流量消耗：kb
        val wifiTraffic: Long? = statsInfo?.getAllBytesWifi()
        //手机网络状态下的流量消耗：kb
        val mobileTraffic: Long? = statsInfo?.getAllBytesMobile()
        //设备全部流量消耗统计（不区分网络环境）
        val allTraffic: Long? = statsInfo?.getAllBytes()
        //当前应用的流量消耗
        val appTraffic: Long? = statsInfo?.getUidAllBytes(getUid(context))
        LogUtils.d("getSimpleSaveData allTraffic:$allTraffic wifiTraffic:$wifiTraffic mobileTraffic:$mobileTraffic appTraffic:$appTraffic")
        //将数据写入文件(这里直接存对象，会自动序列化为json，如果转成json字符串再存的话，默认保存的数据是字符，取出来转json的话会有问题)
        return TrafficInfoBean(
            allTraffic,
            appTraffic,
            currentTime,
            mobileTraffic,
            statisticsTime,
            wifiTraffic
        )
    }

    /**
     * 根据填入的日期获取文件并读取
     */
    fun readTrafficFile(date: String?): TrafficInfoBean? {
        try {
            val filePath = getFilePath(date)
            LogUtils.d("readTrafficFile 创建filePath:$filePath")
//            val json = FileIOUtils.readFile2String(File(filePath))
            val json = readFileContentStr(filePath)
            LogUtils.d("readTrafficFile 取出json:$json")
            val info = GsonUtils.fromJson(json.toString(), TrafficInfoBean::class.java)
            LogUtils.d("readTrafficFile 序列化后info:$info")
            return info
        } catch (e: Exception) {
            e.printStackTrace()
            LogUtils.e("readTrafficFile() 读取文件失败，有可能找不到前一天的文件：${e.message}")
            return null
        }
    }

    /**
     * 文件生成规则：
     */
    private fun getFilePath(date: String?): String {
        return "${PathUtils.getExternalStoragePath()}${TrafficConst.NET_TRAFFIC_FILE_PATH}$date.txt"
    }

    /**
     * 缓存值：AllTraffic
     */
    private fun getCacheAllTraffic(): Long {
        val v = getSP().getLong(TrafficConst.SP_KEY_ALL_TRAFFIC, 0)
        LogUtils.d("getCache-> getCacheAllTraffic v:$v")
        return if (v <= 0) {
            0
        } else {
            v
        }
    }


    /**
     * 缓存值：WifiTraffic
     */
    private fun getCacheWifiTraffic(): Long {
        val v = getSP().getLong(TrafficConst.SP_KEY_WIFI_TRAFFIC)
        LogUtils.d("getCache-> getCacheWifiTraffic v:$v")
        return if (v <= 0) {
            0
        } else {
            v
        }
    }

    /**
     *  缓存值：MobileTraffic
     */
    private fun getCacheMobileTraffic(): Long {
        val v = getSP().getLong(TrafficConst.SP_KEY_MOBILE_TRAFFIC, 0)
        LogUtils.d("getCache-> getCacheMobileTraffic v:$v")
        return if (v <= 0) {
            0
        } else {
            v
        }
    }

    /**
     * 缓存值：AppTraffic
     */
    private fun getCacheAppTraffic(): Long {
        val v = getSP().getLong(TrafficConst.SP_KEY_APP_TRAFFIC, 0)
        LogUtils.d("getCache-> getCacheAppTraffic v:$v")
        return if (v <= 0) {
            0
        } else {
            v
        }
    }

    /**
     * 缓存数据重置
     */
    fun resetSPData() {
        getSP().put(TrafficConst.SP_KEY_WIFI_TRAFFIC, 0.toLong())
        getSP().put(TrafficConst.SP_KEY_MOBILE_TRAFFIC, 0.toLong())
        getSP().put(TrafficConst.SP_KEY_ALL_TRAFFIC, 0.toLong())
        getSP().put(TrafficConst.SP_KEY_APP_TRAFFIC, 0.toLong())
    }

    private fun getSP(): SPUtils {
        return SPUtils.getInstance(TrafficConst.SP_NAME)
    }

    private fun readFileContentStr(fullFilename: String): String? {
        var readOutStr: String? = null
        try {
            val dis = DataInputStream(FileInputStream(fullFilename))
            readOutStr = try {
                val len = File(fullFilename).length()
                if (len > Int.MAX_VALUE) throw IOException("File $fullFilename too large, was $len bytes.")
                val bytes = ByteArray(len.toInt())
                dis.readFully(bytes)
                String(bytes)
            } finally {
                dis.close()
            }
        } catch (e: IOException) {
            readOutStr = null
            LogUtils.d("readTrafficFile readFileContentStr error:${e.message}")
        }
        return readOutStr
    }
}