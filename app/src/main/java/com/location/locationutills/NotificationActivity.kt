package com.location.locationutills

import android.app.NotificationManager
import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.location.locationutills.databinding.ActivityNotificationBinding

class NotificationActivity : AppCompatActivity() {

    lateinit var mBinding: ActivityNotificationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this@NotificationActivity, R.layout.activity_notification)
        ChannelBuilder(this).createChannels("11", "Default")
        Log.d("Notification", "Channel created..!!!!!!!!!!!!!!!!!!!!!")
        mBinding.btnNotification.setOnClickListener {
            generateNotification()
        }
        mBinding.btnNotification2.setOnClickListener {
            generateNotification2()
        }
    }

    private fun generateNotification() {
        showNotification("11", "Default", "basic notification usually includes a title, a line of text, and one or more actions the user can perform in response. To provide even more information, you can also create large, expandable notifications by applying one of several notification templates as described on this page.\\n\" +\n" +
                "                \"\\n\" +\n" +
                "                \"To start, build a notification with all the basic content as described in Create a Notification. Then, call setStyle() with a style object and supply information corresponding to each template, as shown below.\\n\" +\n" +
                "                \"\\n\" +\n" +
                "                \"Add a large image\"")
    }

    private fun generateNotification2() {
        val notificationBuilder = NotificationCompat.Builder(this, Notifications.IOS_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText("Ios")
                .setChannelId(Notifications.IOS_CHANNEL_ID)
                .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(44, notificationBuilder.build())
    }

}
