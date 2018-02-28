package com.location.locationutills

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.MediaController
import android.widget.Toast
import com.livinglifetechway.k4kotlin.logD
import com.location.locationutills.databinding.ActivityPickerBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class PickerActivity : AppCompatActivity() {

    companion object {
        const val SELECT_IMAGE = 60
        val TAG = PickerActivity::class.java.simpleName
        private const val SELECT_VIDEO = 1
        const val SELECT_FILE = 89
        const val CAMERA_REQUEST = 98
        const val VIDEO_CAPTURE = 445
    }

    var selectedVideoPath: String? = null
    lateinit var uri: Uri
    lateinit var fileuri: Uri
    var mCurrentPhotoPath: String? = null
    lateinit var videoUri: Uri
    lateinit var mBinding: ActivityPickerBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_picker)
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        mBinding.btnFile.setOnClickListener { filepicker() }
        mBinding.btnImg.setOnClickListener { imgpicker() }
        mBinding.btnVideo.setOnClickListener { videopicker() }
        mBinding.btnCaptureImg.setOnClickListener { imgcapturepicker() }
        mBinding.btnCaptureVideo.setOnClickListener { videocapturepicker() }
    }

    fun videocapturepicker() {

        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            val mediaFile = File(
                    Environment.getExternalStorageDirectory().absolutePath + "/myvideo.mp4")
            videoUri = Uri.fromFile(mediaFile)
            Log.d(TAG, "recorded video uri........................." + videoUri)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
            startActivityForResult(intent, VIDEO_CAPTURE)

        } else {
            Toast.makeText(this, "No camera on device", Toast.LENGTH_LONG).show()
        }
    }

    fun imgcapturepicker() {

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
                Log.i(TAG, "IOException")
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile))
                startActivityForResult(cameraIntent, CAMERA_REQUEST)
            }
        }
    }

    fun imgpicker() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_PICK
        startActivityForResult(Intent.createChooser(intent, "select image"), SELECT_IMAGE)
    }

    fun videopicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "video/*"
        startActivityForResult(Intent.createChooser(intent, "select video"), SELECT_VIDEO)
    }

    fun filepicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "*/*"
        startActivityForResult(intent, SELECT_FILE)
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
                imageFileName, // prefix
                ".jpg", // suffix
                storageDir      // directory
        )

        // Save a file: path for use with ACTION_VIEW intents
        this.mCurrentPhotoPath = "file:" + image.absolutePath
        return image
    }


    /*store video in external storage */
    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createVideoFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "MP4_" + timeStamp + "_"
        val storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES)
        val video = File.createTempFile(
                imageFileName, // prefix
                ".mp4", // suffix
                storageDir      // directory
        )

        // Save a file: path for use with ACTION_VIEW intents
        var pathVideo = videoUri.toString()
        pathVideo = "file:" + video.absolutePath
        return video
    }


    private fun getImgPathFromURIPath(contentURI: Uri, activity: Activity): String? {
        val cursor = activity.contentResolver.query(contentURI, null, null, null, null)
        if (cursor == null) {
            return contentURI.path
        } else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            val string = cursor.getString(idx)
            cursor.close()
            return string
        }
    }

    fun getFilePathFromURIPath(uri: Uri, activity: Activity): String? {
        val projection = arrayOf(MediaStore.Files.FileColumns.DATA)
        val cursor = managedQuery(uri, projection, null, null, null)
        if (cursor != null) {
            val column_index = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            cursor.moveToFirst()
            return cursor.getString(column_index)
        } else
            return null
    }

    fun getVideoPathFromURIPath(uri: Uri, activity: Activity): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = managedQuery(uri, projection, null, null, null)
        if (cursor != null) {
            val column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(column_index)
        } else
            return null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == SELECT_IMAGE && resultCode == Activity.RESULT_OK) {
            uri = data!!.getData()

            val filePath = getImgPathFromURIPath(uri, this@PickerActivity)
            val mImgPath = File(filePath)
            Log.d(TAG, "Filename " + filePath)
            mBinding.ivTest.setImageURI(Uri.parse(filePath))
//            val mFile = RequestBody.create(MediaType.parse("image/*"), file)
//            fileToUpload = MultipartBody.Part.createFormData("file", file.name, mFile)
        } else if (requestCode == SELECT_VIDEO && resultCode == Activity.RESULT_OK) {
            selectedVideoPath = getVideoPathFromURIPath(data!!.data, this@PickerActivity)
            try {
                if (selectedVideoPath == null) {
                    finish()
                } else {

                    /*send this video path to multipart*/
                    val mVideoPath = File(selectedVideoPath).logD("VIDEO PATH............")
                    val mediaController = MediaController(this)
                    mediaController.setAnchorView(mBinding.vvVideo)
                    Log.d("video", "video......" + selectedVideoPath)
                    mBinding.vvVideo.setMediaController(mediaController)
                    mBinding.vvVideo.setVideoPath(selectedVideoPath)
                    mBinding.vvVideo.requestFocus()
                    mBinding.vvVideo.start()

                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else if (requestCode == SELECT_FILE && resultCode == Activity.RESULT_OK) {
            fileuri = data!!.data

            val docPath = getFilePathFromURIPath(fileuri, this@PickerActivity)
            val mdocPath = File(docPath)
            Log.d(TAG, "FILES .......... " + docPath)

        } else if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            try {
                val mImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, Uri.parse(mCurrentPhotoPath))
                Log.d(TAG, "Camera Image Path ..." + mCurrentPhotoPath)
                mBinding.ivTest.setImageBitmap(mImageBitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        } else if (requestCode == VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Toast.makeText(this, "Video has been saved to:\n" + data?.data, Toast.LENGTH_LONG).show();
            mBinding.vvVideo.setVideoURI(videoUri)
            mBinding.vvVideo.setMediaController(MediaController(this))
            mBinding.vvVideo.requestFocus()
            mBinding.vvVideo.start()

        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}
