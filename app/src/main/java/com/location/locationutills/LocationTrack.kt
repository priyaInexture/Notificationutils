package com.location.locationutills

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.location.Criteria
import android.location.LocationManager
import android.util.Log

/**
 * Created by Android on 2/14/2018.
 */
object LocationTrack {
    fun bindLocationListenerIn(lifecycleOwner: LifecycleOwner,
                               listener: android.location.LocationListener, context: Context) {
        BoundLocationListener(lifecycleOwner, listener, context)
    }

    internal class BoundLocationListener(lifecycleOwner: LifecycleOwner,
                                         private val mListener: android.location.LocationListener, private val mContext: Context) : LifecycleObserver {
        private var mLocationManager: LocationManager? = null

        init {
            lifecycleOwner.lifecycle.addObserver(this)
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        private fun addLocationListener() {
            // Note: Use the Fused Location Provider from Google Play Services instead.
            // https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderApi

            mLocationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            val locationCritera = Criteria()
            val providerName = mLocationManager!!.getBestProvider(locationCritera,
                    true)

            // Force an update with the last location, if available.
            val lastLocation = mLocationManager!!.getLastKnownLocation(providerName)
            if (lastLocation != null) {
                mListener.onLocationChanged(lastLocation)
            }
            mLocationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, mListener)
            Log.d("BoundLocationMgr", "Listener added")


        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        fun removeLocationListener() {
            if (mLocationManager == null) {
                return
            }
            mLocationManager!!.removeUpdates(mListener)
            mLocationManager = null
            Log.d("BoundLocationMgr", "Listener removed")
        }
    }
}
