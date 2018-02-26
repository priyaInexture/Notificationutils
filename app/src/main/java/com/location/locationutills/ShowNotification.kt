package com.location.locationutills

import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.support.v4.app.NotificationCompat


/**
 * Created by Android on 2/26/2018.
 */

fun Context.showNotification(channel_id: String, notification_title: String) {
    createNotification(channel_id, notification_title, "")
}

fun Context.showNotificationwithImage(channel_id: String, notification_title: String, notification_content: String, notification_img: Bitmap) {
    createPictureNotification(channel_id, notification_title, notification_content, notification_img)
}

fun Context.showNotificationwithInboxstyle(channel_id: String, notification_title: String, notification_img: Bitmap) {}

fun Context.showNotificationExpanded(channel_id: String, notification_title: String, notification_content: String) {
    createNotification(channel_id, notification_title, notification_content)
}

private fun Context.createNotification(

        channel_id: String,
        notification_title: String,
        notification_content: String
) {
    val notificationBuilder = NotificationCompat.Builder(this, channel_id)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(notification_title)
            .setChannelId(channel_id)
            .setContentText(notification_content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notification_content))
            .setAutoCancel(true)

    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    notificationManager.notify(Math.random().toInt(), notificationBuilder.build())

}


private fun Context.createPictureNotification(

        channel_id: String,
        notification_title: String,
        notification_content: String,
        notification_img: Bitmap
) {
    val notificationBuilder = NotificationCompat.Builder(this, channel_id)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(notification_title)
            .setChannelId(channel_id)
            .setContentText(notification_content)
            .setStyle(NotificationCompat.BigPictureStyle().bigPicture(notification_img))
            .setAutoCancel(true)

    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    notificationManager.notify(Math.random().toInt(), notificationBuilder.build())

}

fun Context.showInboxstyleNotification(
        channel_id: String,
        notification_title: String,
        notification_content: String,
        strings: List<String>,
        contentTitle : String,
        summaryText : String


) {
    val notificationBuilder = NotificationCompat.Builder(this, channel_id)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(notification_title)
            .setChannelId(channel_id)
            .setContentText(notification_content)
            .setStyle(NotificationCompat.InboxStyle().addLine().setBigContentTitle(contentTitle).setSummaryText(summaryText))
            .setAutoCancel(true)

    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    notificationManager.notify(Math.random().toInt(), notificationBuilder.build())

}