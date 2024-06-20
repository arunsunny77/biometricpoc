package com.biometricpoc

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.facebook.react.bridge.*
import java.security.KeyStore
import java.util.Enumeration
import java.util.concurrent.Executor

class Biometry (private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    companion object {
        private const val KEY_ALIAS = "secure_item"
    }

    override fun getName(): String {
        return "Biometry"
    }

    @ReactMethod
    fun openSettings() {
        try {
            val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
            currentActivity?.startActivity(intent)
        } catch (e: Exception) {
            // Handle error, if any
        }
    }

    @ReactMethod
    fun availabilityOption(promise: Promise) {
        val biometricManager = BiometricManager.from(reactContext)
        val status = WritableNativeMap()

        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                status.putString("status", "YES")
                status.putString("type", "")
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                status.putString("status", "NO")
                status.putString("type", "BIOMETRIC_NOT_AVAILABLE")
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL)
                enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BiometricManager.Authenticators.BIOMETRIC_STRONG)
                status.putString("status", "NO")
                status.putString("type", "BIOMETRIC_NOT_ENROLLED")
            }
        }
        promise.resolve(status)
    }

    @ReactMethod
    fun setBiometric(token: String, promise: Promise) {
        val activity = currentActivity as? FragmentActivity
        val status = WritableNativeMap()

        activity?.runOnUiThread {
            val executor = ContextCompat.getMainExecutor(activity)

            val biometricPrompt = BiometricPrompt(activity, executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {

                            super.onAuthenticationSucceeded(result!!)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                saveBiometric(token, promise)
                            }
                        }

                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                                status.putString("status", "failed")
                                status.putString("error", "ACCESS_DENIED")
                                promise.resolve(status)
                            }
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                        }
                    })

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Authenticate with Biometric")
                    .setSubtitle("Scan your fingerprint")
                    .setNegativeButtonText("Cancel")
                    .build()

            biometricPrompt.authenticate(promptInfo)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun saveBiometric(token: String, promise: Promise) {
        val status = BiometricSupport.save(token, reactContext)
        promise.resolve(status)
    }

    @ReactMethod
    fun decrypt(promise: Promise) {
        val status = BiometricSupport.decrypt(reactContext)
        promise.resolve(status)
    }

    @ReactMethod
    fun verify(promise: Promise) {
        val activity = currentActivity as? FragmentActivity

        activity?.run {
            val cipher = BiometricSupport.getCipherForBiometrics(reactContext)
            cipher?.let {
                val executor = ContextCompat.getMainExecutor(activity)

                val biometricPrompt = BiometricPrompt(activity, executor,
                        object : BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                super.onAuthenticationSucceeded(result)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    decrypt(promise)
                                }
                            }

                            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                                    val status = WritableNativeMap()
                                    status.putString("error", "FINGERPRINT_NOT_RECOGNIZED")
                                    status.putString("status", "failed")
                                    promise.resolve(status)
                                }
                            }

                            override fun onAuthenticationFailed() {
                                super.onAuthenticationFailed()
                            }
                        })

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Authenticate with Biometric")
                        .setSubtitle("Scan your fingerprint")
                        .setNegativeButtonText("Cancel")
                        .build()

                biometricPrompt.authenticate(promptInfo)
            }
        }
    }

    @ReactMethod
    fun resetBiometric() {
        clearKeyStore()
    }

    private fun clearKeyStore() {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            val aliases: Enumeration<String> = keyStore.aliases()
            while (aliases.hasMoreElements()) {
                val alias = aliases.nextElement()
                keyStore.deleteEntry(alias)
            }
        } catch (e: Exception) {
            // Handle exception
        }
    }
}
