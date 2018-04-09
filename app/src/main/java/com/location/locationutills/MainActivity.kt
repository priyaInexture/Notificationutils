package com.location.locationutills

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.content.Intent
import android.databinding.DataBindingUtil
import android.location.Location
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.livinglifetechway.quickpermissions.annotations.OnPermissionsPermanentlyDenied
import com.livinglifetechway.quickpermissions.annotations.WithPermissions
import com.livinglifetechway.quickpermissions.util.QuickPermissionsRequest
import com.location.locationutills.databinding.ActivityMainBinding
import com.location.notification.NotificationActivity


class MainActivity : AppCompatActivity() {

    lateinit var mBinding: ActivityMainBinding
    lateinit var listener: CustomLocationListener

    @SuppressLint("SetTextI18n")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        LocationTracker.initTracker(this)
        methodWithPermissions()
        mBinding.btnNotification.setOnClickListener {
            startActivity(Intent(this@MainActivity, NotificationActivity::class.java))
        }
        mBinding.btnPicker.setOnClickListener {
            startActivity(Intent(this@MainActivity, PickerActivity::class.java))

        }
    }


    @WithPermissions(
            permissions = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA]

    )
    fun methodWithPermissions() {
        Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();

        // show location button click event
        mBinding.btn.setOnClickListener {

            // check if GPS enabled
            if (LocationTracker.getLastKnownLocation()) {
                val latitude = LocationTracker.getLatitude()
                AppPreference.lat = latitude.toString()
                val longitude = LocationTracker.getLongitude()
                AppPreference.long = longitude.toString()

                mBinding.tvResult.text = "lat :" + latitude.toString() + "long :" + longitude.toString()
                // \n is for new line
                Toast.makeText(applicationContext, "Your Location is - \nLat: $latitude\nLong: $longitude", Toast.LENGTH_LONG).show()
            } else {
                LocationTracker.showSettingsAlert()
            }
        }
        mBinding.btnStop.setOnClickListener {
            //            LocationTracker.stopUsingGPS(listener)
            LocationTracker.removeLocationListener(listener)
            Log.d(TAG, "onCreate: Stop GPS")
        }
        mBinding.btnLastLocation.setOnClickListener {
            listener = object : CustomLocationListener {
                override fun onLocationChage(mLocation: Location?) {
                    Log.d(TAG, "onLocationChage: .............$mLocation")
                    Toast.makeText(applicationContext, "location" + mLocation, Toast.LENGTH_LONG).show()

                }
            }
            LocationTracker.addLocationListener(listener)
        }
        mBinding.btnNext.setOnClickListener {
            val intent = Intent(this@MainActivity, Main2Activity::class.java)
            startActivity(intent)

        }
        mBinding.btnMap.setOnClickListener {
            val intent = Intent(this@MainActivity, MapActivity::class.java)
            startActivity(intent)
        }

    }

    @OnPermissionsPermanentlyDenied
    fun whenPermissionsArePermanentlyDenied(arg: QuickPermissionsRequest) {
        AlertDialog.Builder(this)
                .setTitle("Permissions Permanently Denied")
                .setMessage("These permissions are required to proceed futher. Please allow from app settings")
                .setPositiveButton("Settings", DialogInterface.OnClickListener { dialogInterface, i ->
                    // this will open app settings for allowing permissions and wait for the results
                    arg.openAppSettings()
                })
                .setNegativeButton("Close", DialogInterface.OnClickListener { dialogInterface, i ->
                    arg.cancel() // this will cancel flow and the method won't be called
                })
                .show()

    }


    companion object {
        val MY_PERMISSIONS_REQUEST_CODE = 50

    }


}


