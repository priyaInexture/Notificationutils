package com.location.fingerprint

import android.app.KeyguardManager
import android.content.SharedPreferences
import android.databinding.DataBindingUtil
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.support.annotation.Nullable
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.location.locationutills.R
import com.location.locationutills.databinding.ActivityFingerPrintBinding
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.*

class FingerPrintActivity : AppCompatActivity() {

    lateinit var mBinding: ActivityFingerPrintBinding
    private val TAG = FingerPrintActivity::class.java.simpleName

    private val DIALOG_FRAGMENT_TAG = "myFragment"
    private val SECRET_MESSAGE = "Very secret message"
    private val KEY_NAME_NOT_INVALIDATED = "key_not_invalidated"
    internal val DEFAULT_KEY_NAME = "default_key"

    private var mKeyStore: KeyStore? = null
    private var mKeyGenerator: KeyGenerator? = null
    private var mSharedPreferences: SharedPreferences? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this@FingerPrintActivity, R.layout.activity_finger_print)
        setSupportActionBar(mBinding.toolbar)

        try {
            mKeyStore = KeyStore.getInstance("AndroidKeyStore")
        } catch (e: KeyStoreException) {
            throw RuntimeException("Failed to get an instance of KeyStore", e)
        }

        try {
            mKeyGenerator = KeyGenerator
                    .getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get an instance of KeyGenerator", e)
        } catch (e: NoSuchProviderException) {
            throw RuntimeException("Failed to get an instance of KeyGenerator", e)
        }

        val defaultCipher: Cipher
        val cipherNotInvalidated: Cipher
        try {
            defaultCipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7)
            cipherNotInvalidated = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get an instance of Cipher", e)
        } catch (e: NoSuchPaddingException) {
            throw RuntimeException("Failed to get an instance of Cipher", e)
        }

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        val keyguardManager = getSystemService(KeyguardManager::class.java)
        val fingerprintManager = getSystemService(FingerprintManager::class.java)
        val purchaseButton = mBinding.purchaseButton
        val purchaseButtonNotInvalidated = mBinding.purchaseButtonNotInvalidated

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            purchaseButtonNotInvalidated.isEnabled = true
            purchaseButtonNotInvalidated.setOnClickListener(
                    PurchaseButtonClickListener(cipherNotInvalidated,
                            KEY_NAME_NOT_INVALIDATED))
        } else {
            // Hide the purchase button which uses a non-invalidated key
            // if the app doesn't work on Android N preview
            purchaseButtonNotInvalidated.visibility = View.GONE
            mBinding.purchaseButtonNotInvalidatedDescription.visibility = View.GONE
        }

        if (!keyguardManager!!.isKeyguardSecure) {
            // Show a message that the user hasn't set up a fingerprint or lock screen.
            Toast.makeText(this,
                    "Secure lock screen hasn't set up.\n" + "Go to 'Settings -> Security -> Fingerprint' to set up a fingerprint",
                    Toast.LENGTH_LONG).show()
            purchaseButton.isEnabled = false
            purchaseButtonNotInvalidated.isEnabled = false
            return
        }

        // Now the protection level of USE_FINGERPRINT permission is normal instead of dangerous.
        // See http://developer.android.com/reference/android/Manifest.permission.html#USE_FINGERPRINT
        // The line below prevents the false positive inspection from Android Studio

        if (!fingerprintManager.hasEnrolledFingerprints()) {
            purchaseButton.isEnabled = false
            // This happens when no fingerprints are registered.
            Toast.makeText(this,
                    "Go to 'Settings -> Security -> Fingerprint' and register at least one" + " fingerprint",
                    Toast.LENGTH_LONG).show()
            return
        }
        createKey(DEFAULT_KEY_NAME, true)
        createKey(KEY_NAME_NOT_INVALIDATED, false)
        purchaseButton.isEnabled = true
        purchaseButton.setOnClickListener(
                PurchaseButtonClickListener(defaultCipher, DEFAULT_KEY_NAME))
    }

    /**
     * Initialize the [Cipher] instance with the created key in the
     * [.createKey] method.
     *
     * @param keyName the key name to init the cipher
     * @return `true` if initialization is successful, `false` if the lock screen has
     * been disabled or reset after the key was generated, or if a fingerprint got enrolled after
     * the key was generated.
     */
    private fun initCipher(cipher: Cipher, keyName: String): Boolean {
        try {
            mKeyStore?.load(null)
            val key = mKeyStore?.getKey(keyName, null) as SecretKey
            cipher.init(Cipher.ENCRYPT_MODE, key)
            return true
        } catch (e: KeyPermanentlyInvalidatedException) {
            return false
        } catch (e: KeyStoreException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: CertificateException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: UnrecoverableKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: IOException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: InvalidKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        }

    }

    /**
     * Proceed the purchase operation
     *
     * @param withFingerprint `true` if the purchase was made by using a fingerprint
     * @param cryptoObject the Crypto object
     */
    fun onPurchased(withFingerprint: Boolean,
                    cryptoObject: FingerprintManager.CryptoObject?) {
        if (withFingerprint) {
            // If the user has authenticated with fingerprint, verify that using cryptography and
            // then show the confirmation message.
            assert(cryptoObject != null)
            tryEncrypt(cryptoObject!!.cipher)
        } else {
            // Authentication happened with backup password. Just show the confirmation message.
            showConfirmation(null)
        }
    }

    // Show confirmation, if fingerprint was used show crypto information.
    private fun showConfirmation(encrypted: ByteArray?) {
        mBinding.confirmationMessage.visibility = View.VISIBLE
        if (encrypted != null) {
            val v = mBinding.confirmationMessage
            v.visibility = View.VISIBLE
            v.text = Base64.encodeToString(encrypted, 0 /* flags */)
        }
    }

    /**
     * Tries to encrypt some data with the generated key in [.createKey] which is
     * only works if the user has just authenticated via fingerprint.
     */
    private fun tryEncrypt(cipher: Cipher) {
        try {
            val encrypted = cipher.doFinal(SECRET_MESSAGE.toByteArray())
            showConfirmation(encrypted)
        } catch (e: BadPaddingException) {
            Toast.makeText(this, "Failed to encrypt the data with the generated key. " + "Retry the purchase", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Failed to encrypt the data with the generated key." + e.message)
        } catch (e: IllegalBlockSizeException) {
            Toast.makeText(this, "Failed to encrypt the data with the generated key. " + "Retry the purchase", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Failed to encrypt the data with the generated key." + e.message)
        }

    }

    /**
     * Creates a symmetric key in the Android Key Store which can only be used after the user has
     * authenticated with fingerprint.
     *
     * @param keyName the name of the key to be created
     * @param invalidatedByBiometricEnrollment if `false` is passed, the created key will not
     * be invalidated even if a new fingerprint is enrolled.
     * The default value is `true`, so passing
     * `true` doesn't change the behavior
     * (the key will be invalidated if a new fingerprint is
     * enrolled.). Note that this parameter is only valid if
     * the app works on Android N developer preview.
     */
    fun createKey(keyName: String, invalidatedByBiometricEnrollment: Boolean) {
        // The enrolling flow for fingerprint. This is where you ask the user to set up fingerprint
        // for your flow. Use of keys is necessary if you need to know if the set of
        // enrolled fingerprints has changed.
        try {
            mKeyStore?.load(null)
            // Set the alias of the entry in Android KeyStore where the key will appear
            // and the constrains (purposes) in the constructor of the Builder

            val builder = KeyGenParameterSpec.Builder(keyName,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    // Require the user to authenticate with a fingerprint to authorize every use
                    // of the key
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)

            // This is a workaround to avoid crashes on devices whose API level is < 24
            // because KeyGenParameterSpec.Builder#setInvalidatedByBiometricEnrollment is only
            // visible on API level +24.
            // Ideally there should be a compat library for KeyGenParameterSpec.Builder but
            // which isn't available yet.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment)
            }
            mKeyGenerator?.init(builder.build())
            mKeyGenerator?.generateKey()
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        } catch (e: InvalidAlgorithmParameterException) {
            throw RuntimeException(e)
        } catch (e: CertificateException) {
            throw RuntimeException(e)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }


    private inner class PurchaseButtonClickListener internal constructor(internal var mCipher: Cipher, internal var mKeyName: String) : View.OnClickListener {

        override fun onClick(view: View) {

            mBinding.confirmationMessage.visibility = View.GONE
            mBinding.encryptedMessage.visibility = View.GONE
            // Set up the crypto object for later. The object will be authenticated by use
            // of the fingerprint.
            if (initCipher(mCipher, mKeyName)) {

                // Show the fingerprint dialog. The user has the option to use the fingerprint with
                // crypto, or you can fall back to using a server-side verified password.
                val fragment = FingerprintAuthenticationDialogFragment()
                fragment.setCryptoObject(FingerprintManager.CryptoObject(mCipher))
                val useFingerprintPreference = mSharedPreferences!!.getBoolean(getString(R.string.use_fingerprint_to_authenticate_key), true)
                if (useFingerprintPreference) {
                    fragment.setStage(
                            FingerprintAuthenticationDialogFragment.Stage.FINGERPRINT)
                } else {
                    fragment.setStage(
                            FingerprintAuthenticationDialogFragment.Stage.PASSWORD)
                }
                fragment.show(fragmentManager, DIALOG_FRAGMENT_TAG)
            } else {
                // This happens if the lock screen has been disabled or or a fingerprint got
                // enrolled. Thus show the dialog to authenticate with their password first
                // and ask the user if they want to authenticate with fingerprints in the
                // future
                val fragment = FingerprintAuthenticationDialogFragment()
                fragment.setCryptoObject(FingerprintManager.CryptoObject(mCipher))
                fragment.setStage(
                        FingerprintAuthenticationDialogFragment.Stage.NEW_FINGERPRINT_ENROLLED)
                fragment.show(fragmentManager, DIALOG_FRAGMENT_TAG)
            }
        }
    }
}
