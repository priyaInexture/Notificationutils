package com.location.locationutills

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.util.Log

/**
 * Created by Android on 2/26/2018.
 */

class Notifications(base: Context) : ContextWrapper(base) {

    private var mManager: NotificationManager? = null

    private val manager: NotificationManager
        get() {
            if (mManager == null) {
                mManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            return mManager!!
        }

    init {
        createChannels()
    }

    fun createChannels() {

        // create android channel
        var androidChannel: NotificationChannel? = null
        var androidChannelSilent: NotificationChannel? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            androidChannel = NotificationChannel(ANDROID_CHANNEL_ID,
                    ANDROID_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            androidChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            manager.createNotificationChannel(androidChannel)
            Log.d(TAG, "createChannels: Channel created.........................")

            androidChannelSilent = NotificationChannel(ANDROID_CHANNEL_SILENT_ID,
                    ANDROID_CHANNEL_SILENT_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            androidChannelSilent.lockscreenVisibility = Notification.VISIBILITY_SECRET
            manager.createNotificationChannel(androidChannelSilent)
            Log.d(TAG, "createChannels: Channel created.........................")


            /*IOS channel */


            // create android channel
            var iosChannel: NotificationChannel? = null
            var iosChannelSilent: NotificationChannel? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                iosChannel = NotificationChannel(IOS_CHANNEL_ID,
                        IOS_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
                iosChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
                manager.createNotificationChannel(iosChannel)
                Log.d(TAG, "createChannels: Channel created.........................")

                iosChannelSilent = NotificationChannel(IOS_CHANNEL_SILENT_ID,
                        IOS_CHANNEL_SILENT_NAME, NotificationManager.IMPORTANCE_DEFAULT)
                iosChannelSilent.lockscreenVisibility = Notification.VISIBILITY_SECRET
                manager.createNotificationChannel(iosChannelSilent)
                Log.d(TAG, "createChannels: Channel created.........................")
            }
        }
    }

    companion object {
        val ANDROID_CHANNEL_ID = "com.android.notification"
        val ANDROID_CHANNEL_NAME = "Android"
        val ANDROID_CHANNEL_SILENT_ID = "com.android.notification.silent"
        val ANDROID_CHANNEL_SILENT_NAME = "Android Silent"
        val IOS_CHANNEL_ID = "com.ios.notification"
        val IOS_CHANNEL_NAME = "Ios"
        val IOS_CHANNEL_SILENT_ID = "com.ios.notification.silent"
        val IOS_CHANNEL_SILENT_NAME = "Ios Silent"
    }
}
