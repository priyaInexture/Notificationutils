package com.location

import android.content.Intent
import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.location.easyfingerprint.KeyGeneratorActivity
import com.location.locationutills.R
import com.location.locationutills.databinding.ActivityMain4Binding
import com.location.notification.NotificationActivity

class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityMain4Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main4)
        mainBinding.btnNotifications.setOnClickListener { startActivity(Intent(this@MainActivity, NotificationActivity::class.java)) }
        mainBinding.btnFingerPrint.setOnClickListener { startActivity(Intent(this@MainActivity, KeyGeneratorActivity::class.java)) }
    }
}
