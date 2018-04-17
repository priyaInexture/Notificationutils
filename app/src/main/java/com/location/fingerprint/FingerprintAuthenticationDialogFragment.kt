package com.location.fingerprint

import android.app.DialogFragment
import android.content.Context
import android.content.SharedPreferences
import android.hardware.fingerprint.FingerprintManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.location.locationutills.R

class FingerprintAuthenticationDialogFragment : DialogFragment(), TextView.OnEditorActionListener, FingerprintUiHelper.Callback {

    private var mCancelButton: Button? = null
    private var mSecondDialogButton: Button? = null
    private var mFingerprintContent: View? = null
    private var mBackupContent: View? = null
    private var mPassword: EditText? = null
    private var mUseFingerprintFutureCheckBox: CheckBox? = null
    private var mPasswordDescriptionTextView: TextView? = null
    private var mNewFingerprintEnrolledTextView: TextView? = null

    private var mStage = Stage.FINGERPRINT

    private var mCryptoObject: FingerprintManager.CryptoObject? = null
    private var mFingerprintUiHelper: FingerprintUiHelper? = null
    private var mActivity: FingerPrintActivity? = null

    private var mInputMethodManager: InputMethodManager? = null
    private var mSharedPreferences: SharedPreferences? = null

    private val mShowKeyboardRunnable = Runnable { mInputMethodManager?.showSoftInput(mPassword, 0) }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // Do not create a new Fragment when the Activity is re-created such as orientation changes.
        retainInstance = true
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog)

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        dialog.setTitle(getString(R.string.sign_in))
        val v = inflater?.inflate(R.layout.fingerprint_dialog_container, container, false)
        mCancelButton = v?.findViewById(R.id.cancel_button)
        mCancelButton?.setOnClickListener { dismiss() }

        mSecondDialogButton = v?.findViewById(R.id.second_dialog_button)
        mSecondDialogButton?.setOnClickListener {
            if (mStage == Stage.FINGERPRINT) {
                goToBackup()
            } else {
                verifyPassword()
            }
        }
        mFingerprintContent = v?.findViewById(R.id.fingerprint_container)
        mBackupContent = v?.findViewById(R.id.backup_container)
        mPassword = v?.findViewById(R.id.password)
        mPassword?.setOnEditorActionListener(this)
        mPasswordDescriptionTextView = v?.findViewById(R.id.password_description)
        mUseFingerprintFutureCheckBox = v?.findViewById(R.id.use_fingerprint_in_future_check)
        mNewFingerprintEnrolledTextView = v?.findViewById(R.id.new_fingerprint_enrolled_description)
        mFingerprintUiHelper = FingerprintUiHelper(
                mActivity!!.getSystemService(FingerprintManager::class.java),
                v?.findViewById(R.id.fingerprint_icon) as ImageView,
                v.findViewById(R.id.fingerprint_status) as TextView, this)
        updateStage()

        // If fingerprint authentication is not available, switch immediately to the backup
        // (password) screen.
        if (!mFingerprintUiHelper!!.isFingerprintAuthAvailable) {
            goToBackup()
        }
        return v
    }

    override fun onResume() {
        super.onResume()
        if (mStage == Stage.FINGERPRINT) {
            mCryptoObject?.let { mFingerprintUiHelper?.startListening(it) }
        }
    }

    fun setStage(stage: Stage) {
        mStage = stage
    }

    override fun onPause() {
        super.onPause()
        mFingerprintUiHelper?.stopListening()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = activity as FingerPrintActivity
        mInputMethodManager = context.getSystemService(InputMethodManager::class.java)
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    /**
     * Sets the crypto object to be passed in when authenticating with fingerprint.
     */
    fun setCryptoObject(cryptoObject: FingerprintManager.CryptoObject) {
        mCryptoObject = cryptoObject
    }

    /**
     * Switches to backup (password) screen. This either can happen when fingerprint is not
     * available or the user chooses to use the password authentication method by pressing the
     * button. This can also happen when the user had too many fingerprint attempts.
     */
    private fun goToBackup() {
        mStage = Stage.PASSWORD
        updateStage()
        mPassword?.requestFocus()

        // Show the keyboard.
        mPassword?.postDelayed(mShowKeyboardRunnable, 500)

        // Fingerprint is not used anymore. Stop listening for it.
        mFingerprintUiHelper?.stopListening()
    }

    /**
     * Checks whether the current entered password is correct, and dismisses the the dialog and
     * let's the activity know about the result.
     */
    private fun verifyPassword() {
        if (!checkPassword(mPassword?.text.toString())) {
            return
        }
        if (mStage == Stage.NEW_FINGERPRINT_ENROLLED) {
            val editor = mSharedPreferences?.edit()
            editor?.putBoolean(getString(R.string.use_fingerprint_to_authenticate_key),
                    mUseFingerprintFutureCheckBox!!.isChecked)
            editor?.apply()

            if (mUseFingerprintFutureCheckBox!!.isChecked) {
                // Re-create the key so that fingerprints including new ones are validated.
                mActivity?.createKey((activity as FingerPrintActivity).DEFAULT_KEY_NAME, true)
                mStage = Stage.FINGERPRINT
            }
        }
        mPassword?.setText("")
        mActivity?.onPurchased(false /* without Fingerprint */, null)
        dismiss()
    }

    /**
     * @return true if `password` is correct, false otherwise
     */
    private fun checkPassword(password: String): Boolean {
        // Assume the password is always correct.
        // In the real world situation, the password needs to be verified in the server side.
        return password.isNotEmpty()
    }

    private fun updateStage() {
        when (mStage) {
            FingerprintAuthenticationDialogFragment.Stage.FINGERPRINT -> {
                mCancelButton!!.setText(R.string.cancel)
                mSecondDialogButton!!.setText(R.string.use_password)
                mFingerprintContent!!.visibility = View.VISIBLE
                mBackupContent!!.visibility = View.GONE
            }
            FingerprintAuthenticationDialogFragment.Stage.NEW_FINGERPRINT_ENROLLED,
                // Intentional fall through
            FingerprintAuthenticationDialogFragment.Stage.PASSWORD -> {
                mCancelButton?.setText(R.string.cancel)
                mSecondDialogButton?.setText(R.string.ok)
                mFingerprintContent?.visibility = View.GONE
                mBackupContent?.visibility = View.VISIBLE
                if (mStage == Stage.NEW_FINGERPRINT_ENROLLED) {
                    mPasswordDescriptionTextView?.visibility = View.GONE
                    mNewFingerprintEnrolledTextView?.visibility = View.VISIBLE
                    mUseFingerprintFutureCheckBox?.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean {
        if (actionId == EditorInfo.IME_ACTION_GO) {
            verifyPassword()
            return true
        }
        return false
    }

    override fun onAuthenticated() {
        // Callback from FingerprintUiHelper. Let the activity know that authentication was
        // successful.
        mActivity!!.onPurchased(true /* withFingerprint */, mCryptoObject)
        dismiss()
    }

    override fun onError() {
        goToBackup()
    }

    /**
     * Enumeration to indicate which authentication method the user is trying to authenticate with.
     */
    enum class Stage {
        FINGERPRINT,
        NEW_FINGERPRINT_ENROLLED,
        PASSWORD
    }
}