package com.location.locationutills

import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by Android on 2/27/2018.
 */

 class SendHttpRequestTask : AsyncTask<String, Void, Bitmap>() {
    override fun doInBackground(vararg params: String): Bitmap? {
        try {
            val url = URL("http://xxx.xxx.xxx/image.jpg")
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            return BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            Log.d(TAG, e.message)
        }

        return null
    }

    override fun onPostExecute(result: Bitmap) {

    }
}
