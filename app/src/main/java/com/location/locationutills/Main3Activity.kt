package com.location.locationutills

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.widget.TextView
import android.widget.Toast

class Main3Activity : AppCompatActivity() {

    private val mGpsListener = MyLocationListener()

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            bindLocationListener()
        } else {
            Toast.makeText(this, "This sample requires Location access", Toast.LENGTH_LONG).show()
        }
    }

    private fun bindLocationListener() {
        LocationTrack.bindLocationListenerIn(this, mGpsListener, applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    REQUEST_LOCATION_PERMISSION_CODE)
        } else {
            bindLocationListener()
        }
    }

    private inner class MyLocationListener : LocationListener {
        @SuppressLint("SetTextI18n")
        override fun onLocationChanged(location: Location) {
            val textView = findViewById<TextView>(R.id.location)
            textView.text = location.latitude.toString() + ", " + location.longitude
            Log.d("location", "location...." + location)
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

        override fun onProviderEnabled(provider: String) {
            Toast.makeText(this@Main3Activity,
                    "Provider enabled: " + provider, Toast.LENGTH_SHORT).show()
        }

        override fun onProviderDisabled(provider: String) {}
    }

    companion object {

        private val REQUEST_LOCATION_PERMISSION_CODE = 1
    }
}
