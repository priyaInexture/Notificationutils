package com.location.locationutills

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationManager.NETWORK_PROVIDER
import android.os.Bundle
import android.util.Log
import com.google.android.gms.location.*

@SuppressLint("StaticFieldLeak")
/**
 * Created by Android on 2/16/2018.
 */

object LocationLifecycleTracker : LocationListener{

    private lateinit var mContext: Context
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    // The minimum distance to change Updates in meters
    private val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 10 // 10 meters

    // The minimum time between updates in milliseconds
    private val MIN_TIME_BW_UPDATES = (1000 * 60 * 1).toLong() // 1 minute

    // flag for GPS status
    private var isGPSEnabled = false
    private lateinit var listener: CustomLocationListener
    //    private var locationCallback: LocationCallback? = null
    private val UPDATE_INTERVAL = 1000 * 5
    private val FASTEST_INTERVAL = 1000 * 5
    private var locationRequest: LocationRequest? = LocationRequest()
    private var mStoredCallbackList: ArrayList<LocationCallback> = arrayListOf()
    private var mCusromStoredCallbackList: ArrayList<CustomLocationListener> = arrayListOf()

    // flag for network status
    internal var isNetworkEnabled = false
    // flag for GPS status
    private var canGetLocation = false
    private var location: Location? = null // location
    private var latitude: Double = 0.toDouble() // latitude
    private var longitude: Double = 0.toDouble() // longitude

    // Declaring a Location Manager
    lateinit var locationManager: LocationManager


    fun initTracker(context: Context) {

        this.mContext = context
        Log.d(ContentValues.TAG, "initTracker: init tracker....")
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext)
        getLocation()
    }


    @SuppressLint("MissingPermission")
    private fun getLocation(): Location? {
        try {
            locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true
                if (isNetworkEnabled) {

                    Log.d("Network", "Network")
                    if (true) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                        requestLocationUpdates(
                                NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this)

                        if (location != null) {

                            val mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext!!)
                            @android.annotation.SuppressLint("MissingPermission") val lastLocation = mFusedLocationClient.lastLocation
                            lastLocation.addOnSuccessListener { location ->
                                if (location != null) {
                                    latitude = location.latitude
                                    longitude = location.longitude
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return location
    }

    /**
     *It provides continuous updates of location using location listener
     * @return location
     * */

    @SuppressLint("MissingPermission")
    fun addLocationListener( listener: CustomLocationListener) {
        try {
            if (location != null) {

                locationRequest?.interval = UPDATE_INTERVAL.toLong()
                locationRequest?.fastestInterval = FASTEST_INTERVAL.toLong()
                locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        Log.d(ContentValues.TAG, "onLocationResult: .................." + location!!.longitude + "....." + location!!.latitude + "........" + location!!.time)
                        for (location in locationResult!!.locations) {
                            listener.onLocationChage(location)
                        }
                    }
                }

                mStoredCallbackList.add(locationCallback)
                mCusromStoredCallbackList.add(listener)
                Log.d(ContentValues.TAG, "onLocationResult: Temp$mStoredCallbackList..........$mCusromStoredCallbackList")
                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            }
        } catch (e: Exception) {
            Log.d(ContentValues.TAG, "addListener: Error" + e)

        }
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     */
    fun removeLocationListener( removeAllListener: CustomLocationListener) {
        locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            /* here we created two arraylist ,1st one is for custom location listener callback and 2nd one is for manual callback
              listener ,bcz we get listener from custom location and we have to remove from manual listener so both
              stored in a array and find the index of each ,using index we can remove listener...*/
            val mListenerIndex = mCusromStoredCallbackList.indexOf(removeAllListener)

            if (locationManager != null) {
                mFusedLocationClient.removeLocationUpdates(mStoredCallbackList[mListenerIndex])
            }
        } catch (e: Exception) {
            Log.d(ContentValues.TAG, "removeListener: Error" + e)
        }
    }

    override fun onLocationChanged(location: Location) {
        val data = listener.onLocationChage(location)
        Log.d(ContentValues.TAG, "onLocationChanged: Time interval" + data)
    }

    override fun onProviderDisabled(provider: String) {}

    override fun onProviderEnabled(provider: String) {}

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

}

private fun requestLocationUpdates(networK_PROVIDER: String, miN_TIME_BW_UPDATES: Long, miN_DISTANCE_CHANGE_FOR_UPDATES: Long, locationLifecycleTracker: LocationLifecycleTracker) {}
