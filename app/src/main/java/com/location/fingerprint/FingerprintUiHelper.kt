package com.location.fingerprint

import android.hardware.fingerprint.FingerprintManager
import android.os.CancellationSignal
import android.widget.ImageView
import android.widget.TextView
import com.location.locationutills.R

class FingerprintUiHelper
/**
 * Constructor for [FingerprintUiHelper].
 */
internal constructor(private val mFingerprintManager: FingerprintManager,
                     private val mIcon: ImageView, private val mErrorTextView: TextView, private val mCallback: Callback) : FingerprintManager.AuthenticationCallback() {
    private var mCancellationSignal: CancellationSignal? = null

    private var mSelfCancelled: Boolean = false

    // The line below prevents the false positive inspection from Android Studio
    val isFingerprintAuthAvailable: Boolean
        get() = mFingerprintManager.isHardwareDetected && mFingerprintManager.hasEnrolledFingerprints()

    private val mResetErrorTextRunnable = Runnable {
        mErrorTextView.setTextColor(
                mErrorTextView.resources.getColor(R.color.colorAccent, null))
        mErrorTextView.text = mErrorTextView.resources.getString(R.string.fingerprint_hint)
        mIcon.setImageResource(R.drawable.ic_fp_40px)
    }

    fun startListening(cryptoObject: FingerprintManager.CryptoObject) {
        if (!isFingerprintAuthAvailable) {
            return
        }
        mCancellationSignal = CancellationSignal()
        mSelfCancelled = false
        // The line below prevents the false positive inspection from Android Studio

        mFingerprintManager
                .authenticate(cryptoObject, mCancellationSignal, 0 /* flags */, this, null)
        mIcon.setImageResource(R.drawable.ic_fp_40px)
    }

    fun stopListening() {
        if (mCancellationSignal != null) {
            mSelfCancelled = true
            mCancellationSignal!!.cancel()
            mCancellationSignal = null
        }
    }

    override fun onAuthenticationError(errMsgId: Int, errString: CharSequence) {
        if (!mSelfCancelled) {
            showError(errString)
            mIcon.postDelayed({ mCallback.onError() }, ERROR_TIMEOUT_MILLIS)
        }
    }

    override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence) {
        showError(helpString)
    }

    override fun onAuthenticationFailed() {
        showError(mIcon.resources.getString(
                R.string.fingerprint_not_recognized))
    }

    override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
        mErrorTextView.removeCallbacks(mResetErrorTextRunnable)
        mIcon.setImageResource(R.drawable.ic_fingerprint_success)
        mErrorTextView.setTextColor(
                mErrorTextView.resources.getColor(R.color.colorGreen, null))
        mErrorTextView.text = mErrorTextView.resources.getString(R.string.fingerprint_success)
        mIcon.postDelayed({ mCallback.onAuthenticated() }, SUCCESS_DELAY_MILLIS)
    }

    private fun showError(error: CharSequence) {
        mIcon.setImageResource(R.drawable.ic_fingerprint_error)
        mErrorTextView.text = error
        mErrorTextView.setTextColor(
                mErrorTextView.resources.getColor(R.color.colorRed, null))
        mErrorTextView.removeCallbacks(mResetErrorTextRunnable)
        mErrorTextView.postDelayed(mResetErrorTextRunnable, ERROR_TIMEOUT_MILLIS)
    }

    interface Callback {

        fun onAuthenticated()

        fun onError()
    }

    companion object {

        private val ERROR_TIMEOUT_MILLIS: Long = 1600
        private val SUCCESS_DELAY_MILLIS: Long = 1300
    }
}
