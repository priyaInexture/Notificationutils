package com.location.locationutills

import android.databinding.DataBindingUtil
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.location.locationutills.databinding.ActivityNotificationBinding

class NotificationActivity : AppCompatActivity() {

    lateinit var mBinding: ActivityNotificationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this@NotificationActivity, R.layout.activity_notification)
//        ChannelBuilder(this).createChannels("11", "Default")
        Log.d("Notification", "Channel created..!!!!!!!!!!!!!!!!!!!!!")
        mBinding.btnNotification.setOnClickListener {
            showNotification("11", "Simple Notification")
        }
        mBinding.btnNotification2.setOnClickListener {
            showExpandedNotification("11", "Expanded Notification", "gfadgfjgadjfjkadgfjkadgfjkadgfadjgfjkagfkagfasjfgjkagfjkagfjkasgfjgajgfjagfjgasjkfgjaskgfjasgfjgajkfgjagfjagfjgafgjagfjagfajgfhagfgahgfhagfhagfjagfjagjfgsdjhfgjsdfjsdgfjsdgfsdgfjsgfjsdgfjgsdfgsdjfgsjhgfjhsgfsfsgf")
        }
        mBinding.btnNotification3.setOnClickListener {
            showNotificationwithImageResourse("11", "Image Notification", "Hello image", R.drawable.download)
        }
        mBinding.btnNotification4.setOnClickListener {
            showNotificationwithInboxstyle("11", "Input Notification", "hhhhhh", listOf("one", "two", "threee"), "cdcshch", "+3image")
        }
        mBinding.btnNotification5.setOnClickListener {
            showNotificationwithImageUrl("11", "URL Notification", "content", "https://www.gstatic.com/webp/gallery3/1.png")
        }
        mBinding.btnNotification6.setOnClickListener {
            showNotificationwithImageDrawable("11", "URL Notification", "content", BitmapFactory.decodeResource(resources, R.drawable.download))
        }

    }


}
