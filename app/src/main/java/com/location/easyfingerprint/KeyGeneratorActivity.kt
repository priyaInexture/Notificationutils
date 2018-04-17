package com.location.easyfingerprint

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.support.v4.app.ActivityCompat
import android.widget.TextView
import com.location.locationutills.R
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey

class KeyGeneratorActivity : AppCompatActivity() {

    private val KEY_NAME = "yourKey"
    private var cipher: Cipher? = null
    private var keyStore: KeyStore? = null
    private var keyGenerator: KeyGenerator? = null
    private var textView: TextView? = null
    private var cryptoObject: FingerprintManager.CryptoObject? = null
    private var fingerprintManager: FingerprintManager? = null
    private var keyguardManager: KeyguardManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_key_generator)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {


            keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            fingerprintManager = getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager


            if (!fingerprintManager!!.isHardwareDetected) {

                textView!!.text = "Your device doesn't support fingerprint authentication"

            }



            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                textView!!.text = "Please enable the fingerprint permission"

            }

            if (!fingerprintManager!!.hasEnrolledFingerprints()) {
                textView!!.text = "No fingerprint configured. Please register at least one fingerprint in your device's Settings"

            }

            if (!keyguardManager!!.isKeyguardSecure) {
                textView!!.text = "Please enable lockscreen security in your device's Settings"
            } else {
                try {

                    generateKey()
                } catch (e: FingerprintException) {
                    e.printStackTrace()
                }

                if (initCipher()) {
                    cryptoObject = FingerprintManager.CryptoObject(cipher!!)
                    val helper = FingerPrintDetector(this)
                    helper.startAuth(fingerprintManager!!, cryptoObject!!)
                }
            }

        }
    }


    @Throws(FingerprintException::class)
    private fun generateKey() {
        try {

            keyStore = KeyStore.getInstance("AndroidKeyStore")


            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")

            keyStore!!.load(null)
            keyGenerator!!.init(KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build())

            keyGenerator!!.generateKey()

        } catch (exc: KeyStoreException) {
            exc.printStackTrace()
            throw FingerprintException(exc)
        } catch (exc: NoSuchAlgorithmException) {
            exc.printStackTrace()
            throw FingerprintException(exc)
        } catch (exc: NoSuchProviderException) {
            exc.printStackTrace()
            throw FingerprintException(exc)
        } catch (exc: InvalidAlgorithmParameterException) {
            exc.printStackTrace()
            throw FingerprintException(exc)
        } catch (exc: CertificateException) {
            exc.printStackTrace()
            throw FingerprintException(exc)
        } catch (exc: IOException) {
            exc.printStackTrace()
            throw FingerprintException(exc)
        }


    }


    fun initCipher(): Boolean {
        try {
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get Cipher", e)
        } catch (e: NoSuchPaddingException) {
            throw RuntimeException("Failed to get Cipher", e)
        }

        try {
            keyStore!!.load(
                    null)
            val key = keyStore!!.getKey(KEY_NAME, null) as SecretKey
            cipher!!.init(Cipher.ENCRYPT_MODE, key)
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


    private inner class FingerprintException(e: Exception) : Exception(e)
}
