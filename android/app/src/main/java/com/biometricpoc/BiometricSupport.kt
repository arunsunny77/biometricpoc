package com.biometricpoc

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableNativeMap
import java.io.IOException
import java.security.*
import javax.crypto.*
import javax.crypto.spec.GCMParameterSpec
import javax.security.cert.CertificateException

object BiometricSupport {

    private const val AndroidKeyStore = "AndroidKeyStore"
    private const val AES_MODE = "AES/GCM/NoPadding"
    private const val KEY_ALIAS = "KEY_ALIAS"

    lateinit var iv: ByteArray
    lateinit var encryptedData: ByteArray
    lateinit var _promise: Promise

    fun decrypt(context: ReactApplicationContext): WritableNativeMap? {
        try {
            val sharedPreferences = getPreference(context)
            val base64EncryptedToken = sharedPreferences.getString("encryptToken", null)
            val base64EncryptedIV = sharedPreferences.getString("iv", null)
            if (base64EncryptedToken != null) {
                val encryptedToken = Base64.decode(base64EncryptedToken, Base64.DEFAULT)
                val iv = Base64.decode(base64EncryptedIV, Base64.DEFAULT)
                val keyStore = KeyStore.getInstance(AndroidKeyStore)
                keyStore.load(null)
                val secretKeyEntry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
                val secretKey = secretKeyEntry.secretKey
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                val spec = GCMParameterSpec(128, iv)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
                val decodedData = cipher.doFinal(encryptedToken)
                val unencryptedString = String(decodedData, Charsets.UTF_8)

                val status = WritableNativeMap()
                status.putString("password", unencryptedString)
                return status
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun save(token: String, context: Context): WritableNativeMap? {
        try {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, AndroidKeyStore)
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    // .setUserAuthenticationRequired(true)
                    .setInvalidatedByBiometricEnrollment(true)
                    .build()
            keyGenerator.init(keyGenParameterSpec)
            val secretKey = keyGenerator.generateKey()

            val cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_GCM + "/"
                    + KeyProperties.ENCRYPTION_PADDING_NONE)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            iv = cipher.iv
            encryptedData = cipher.doFinal(token.toByteArray(Charsets.UTF_8))
            val base64EncryptedToken = Base64.encodeToString(encryptedData, Base64.DEFAULT)
            val base64EncryptedIV = Base64.encodeToString(iv, Base64.DEFAULT)
            val sharedPreferences = getPreference(context)
            val editor = sharedPreferences.edit()
            editor.putString("encryptToken", base64EncryptedToken)
            editor.putString("iv", base64EncryptedIV)
            editor.apply()

            val status = WritableNativeMap()
            status.putString("status", "success")
            return status

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getCipherForBiometrics(reactContext: ReactApplicationContext): Cipher? {
        val sharedPreferences = getPreference(reactContext)
        val base64EncryptedIV = sharedPreferences.getString("iv", null)
        try {
            val cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_GCM + "/"
                            + KeyProperties.ENCRYPTION_PADDING_NONE)
            val key: SecretKey
            val keyStore = KeyStore.getInstance(AndroidKeyStore)
            val iv = Base64.decode(base64EncryptedIV, Base64.DEFAULT)
            val spec = GCMParameterSpec(128, iv)
            keyStore.load(null)
            key = keyStore.getKey(KEY_ALIAS, null) as SecretKey
            cipher.init(Cipher.DECRYPT_MODE, key, spec)
            return cipher
        } catch (e: KeyPermanentlyInvalidatedException) {
            return null
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
        } catch (e: NoSuchPaddingException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: InvalidAlgorithmParameterException) {
            throw RuntimeException(e)
        }
    }

    private fun getPreference(context: Context): SharedPreferences {
        try {
            val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

            return EncryptedSharedPreferences.create(
                    context,
                    "my_secure_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

//            val masterKey = MasterKey.Builder(context)
//                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
//                    .build()
//            return EncryptedSharedPreferences.create(
//                    context.toString(),
//                    "secret_shared_prefs",
//                    masterKey,
//                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
//                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
        } catch (e: GeneralSecurityException) {
            throw RuntimeException(e)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}
