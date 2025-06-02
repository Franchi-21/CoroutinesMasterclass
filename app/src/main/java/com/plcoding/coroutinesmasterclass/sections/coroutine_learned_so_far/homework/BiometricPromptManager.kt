package com.plcoding.coroutinesmasterclass.sections.coroutine_learned_so_far.homework

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class BiometricPromptManager(
    private val activity: AppCompatActivity
) {
    suspend fun showBiometricPrompt(
        title: String,
        description: String
    ): BiometricResult {
        return suspendCancellableCoroutine { continuation ->
            val manager = BiometricManager.from(activity)
            val authenticators = if (Build.VERSION.SDK_INT >= 30) {
                BIOMETRIC_STRONG or DEVICE_CREDENTIAL
            } else BIOMETRIC_STRONG

            val promptInfo = PromptInfo.Builder()
                .setTitle(title)
                .setDescription(description)
                .setAllowedAuthenticators(authenticators)

            if (Build.VERSION.SDK_INT < 30) {
                promptInfo.setNegativeButtonText("Cancel")
            }

            when (manager.canAuthenticate(authenticators)) {
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                    continuation.resume(BiometricResult.HardwareUnavailable)
                    return@suspendCancellableCoroutine
                }

                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                    continuation.resume(BiometricResult.FeatureUnavailable)
                    return@suspendCancellableCoroutine
                }

                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    continuation.resume(BiometricResult.AuthenticationNotSet)
                    return@suspendCancellableCoroutine
                }

                BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                    continuation.resume(BiometricResult.SecurityUpdateRequired)
                    return@suspendCancellableCoroutine
                }

                BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                    continuation.resume(BiometricResult.FeatureUnavailable)
                    return@suspendCancellableCoroutine
                }

                BiometricManager.BIOMETRIC_STATUS_UNKNOWN, BiometricManager.BIOMETRIC_SUCCESS -> {
                }
            }

            val prompt = BiometricPrompt(
                activity,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        if (continuation.isActive) {
                            continuation.resume(BiometricResult.AuthenticationError(errString.toString()))
                        }
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        if (continuation.isActive) {
                            continuation.resume(BiometricResult.AuthenticationSuccess)
                        }
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        if (continuation.isActive) {
                            continuation.resume(BiometricResult.AuthenticationFailed)
                        }
                    }
                }
            )

            continuation.invokeOnCancellation {
                prompt.cancelAuthentication()
            }

            prompt.authenticate(promptInfo.build())
        }
    }
}
