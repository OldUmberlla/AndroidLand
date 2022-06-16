package com.power.land.location

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.power.base.utils.CrashHandler
import com.power.base.utils.LogUtils
import com.power.land.R
import com.power.location.LocationUtils

/**
 * 作者：Gongsensen
 * 日期：2022/6/15
 * 说明：
 */
class SampleLocationActivity : AppCompatActivity() {
    companion object {
        const val TAG = "SampleLocationActivity"
    }

    private var flag = false

    private var locationGetGPS: Button? = null
    private var locationGetNetGPS: Button? = null
    private var locationGetBestGPS: Button? = null
    private var locationShowTv: TextView? = null

    private var tvTextList = mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        locationGetGPS = findViewById(R.id.locationGetGPS)
        locationGetNetGPS = findViewById(R.id.locationGetNetGPS)
        locationGetBestGPS = findViewById(R.id.locationGetBestGPS)
        locationShowTv = findViewById(R.id.locationShowTv)

        locationGetGPS?.setOnClickListener {
            getGPSLocation()
        }

        locationGetNetGPS?.setOnClickListener {
            getNetworkLocation()
        }

        locationGetBestGPS?.setOnClickListener {
            getBestLocation()
        }
        initLocationListener()

        CrashHandler.testCrash()
    }

    /**
     * 通过GPS获取定位信息
     */
    private fun getGPSLocation() {
        val gps = LocationUtils.getGPSLocation(this)
        if (gps == null) {
            showTv("getGPSLocation gps location is null")
            LogUtils.d(TAG, "getGPSLocation gps location is null")
        } else {
            showTv("getGPSLocation lat==${gps.latitude} lng==${gps.longitude}")
            LogUtils.d(TAG, "getGPSLocation lat==${gps.latitude} lng==${gps.longitude}")
        }
    }

    /**
     * 通过网络等获取定位信息
     */
    private fun getNetworkLocation() {
        val net = LocationUtils.getNetWorkLocation(this)
        if (net == null) {
            showTv("getNetworkLocation gps location is null")
            LogUtils.d(TAG, "getNetworkLocation gps location is null")
        } else {
            showTv("getNetworkLocation lat==${net.latitude} lng==${net.longitude}")
            LogUtils.d(TAG, "getNetworkLocation lat==${net.latitude} lng==${net.longitude}")
        }
    }

    /**
     * 采用最好的方式获取定位信息
     */
    private fun getBestLocation() {
        val best = LocationUtils.getBestLocation(this)
        if (best == null) {
            showTv("getBestLocation gps location is null")
            LogUtils.d(TAG, "getBestLocation gps location is null")
        } else {
            showTv("getNetworkLocation lat==${best.latitude} lng==${best.longitude}")
            LogUtils.d(TAG, "getNetworkLocation lat==${best.latitude} lng==${best.longitude}")
        }
    }

    override fun onResume() {
        super.onResume()
        initPermission();//针对6.0以上版本做权限适配
    }

    override fun onRestart() {
        super.onRestart()
        initLocationListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocationUtils.unRegisterListener(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            flag =
                grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED
            if (flag) {
                initLocationListener()
            }
        }
    }

    private fun initLocationListener() {
        LocationUtils.addLocationListener(
            this,
            LocationManager.GPS_PROVIDER,
            object : LocationUtils.ILocationListener {
                override fun onSuccessLocation(location: Location?) {
                    if (location != null) {
                        showTv("onSuccessLocation lat==${location.latitude} lng==${location.longitude}")
                        LogUtils.d(
                            TAG,
                            "onSuccessLocation lat==${location.latitude} lng==${location.longitude}"
                        )
                    } else {
                        showTv("onSuccessLocation gps location is null")
                        LogUtils.d(TAG, "onSuccessLocation gps location is null")
                    }
                }
            })

    }

    private fun initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //检查权限
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //请求权限
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    1
                )
            } else {
                flag = true
            }
        } else {
            flag = true
        }
    }

    private fun showTv(string: String) {
        if (tvTextList.size >= 15) {
            tvTextList.clear()
            locationShowTv?.text = ""
        }
        locationShowTv?.append("$string \n")
        tvTextList.add(string)


    }
}