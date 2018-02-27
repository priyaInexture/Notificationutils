package com.location.locationutills

import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.support.v4.app.NotificationCompat
import android.util.Log
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL

fun Context.showNotification(
        channel_id: String,
        notification_title: String
) {
    val notificationBuilder = NotificationCompat.Builder(this, channel_id)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(notification_title)
            .setChannelId(channel_id)
            .setAutoCancel(true)

    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    notificationManager.notify(Math.random().toInt(), notificationBuilder.build())

}

fun Context.showExpandedNotification(
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


fun Context.showNotificationwithImageURL(

        channel_id: String,
        notification_title: String,
        notification_content: String,
        url: String
) {

    val notificationBuilder = NotificationCompat.Builder(this, channel_id)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(notification_title)
            .setContentText(notification_content)
            .setAutoCancel(true)


    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    class ImageDownloadAsyncTask : AsyncTask<String, Void, Bitmap>() {
        override fun doInBackground(vararg params: String): Bitmap? {
            try {
                val url2 = URL(params[0])
                val connection = url2.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input = connection.inputStream
                return BitmapFactory.decodeStream(input)
            } catch (e: Exception) {
                Log.d(ContentValues.TAG, e.message)
            }
            return null
        }

        override fun onPostExecute(result: Bitmap) {
            notificationBuilder.setLargeIcon(result)
            notificationBuilder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(result))
            notificationManager.notify(Math.random().toInt(), notificationBuilder.build())

        }
    }
    ImageDownloadAsyncTask().execute(url)
}


fun Context.showNotificationwithImage(

        channel_id: String,
        notification_title: String,
        notification_content: String,
        notification_img: Int
) {

    val notificationBuilder = NotificationCompat.Builder(this, channel_id)
            .setLargeIcon(BitmapFactory.decodeResource(resources,
                    notification_img))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(notification_title)
            .setContentText(notification_content)
            .setStyle(NotificationCompat.BigPictureStyle().bigPicture(BitmapFactory.decodeResource(resources,
                    notification_img)))
            .setAutoCancel(true)


    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(Math.random().toInt(), notificationBuilder.build())

}

fun Context.showNotificationwithInboxstyle(
        channel_id: String,
        notification_title: String,
        notification_content: String,
        strings: List<String>,
        contentTitle: String,
        summaryText: String


) {
    var addString = StringBuilder()
    for (i in strings) {
        addString.append(i)
    }

    val notificationBuilder = NotificationCompat.Builder(this, channel_id)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(notification_title)
            .setChannelId(channel_id)
            .setContentText(notification_content)
            .setAutoCancel(true)
    var notificationCompact = NotificationCompat.InboxStyle().setBigContentTitle(contentTitle).setSummaryText(summaryText)

    for (i in strings)
        notificationCompact.addLine(i)

    notificationBuilder.setStyle(notificationCompact)

    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    notificationManager.notify(Math.random().toInt(), notificationBuilder.build())

}