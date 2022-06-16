package com.power.land

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.power.base.utils.LogUtils
import com.power.http.EasyRequest
import com.power.land.location.SampleLocationActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startActivity(Intent(this, SampleLocationActivity::class.java))
    }
}