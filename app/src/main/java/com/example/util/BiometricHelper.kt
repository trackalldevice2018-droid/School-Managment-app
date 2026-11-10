package com.example.util

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

enum class BiometricAvailability(val messageBn: String, val isUsable: Boolean) {
    AVAILABLE("ডিভাইসে ফিঙ্গারপ্রিন্ট সেন্সর সক্রিয় ও প্রস্তুত আছে", true),
    NONE_ENROLLED("ফোনের সেটিংসে কোনো ফিঙ্গারপ্রিন্ট বা ফেস রেজিস্টার করা নেই", false),
    NO_HARDWARE("এই ডিভাইসে ফিঙ্গারপ্রিন্ট সেন্সর নেই (পিন কোড ব্যবহার করুন)", false),
    HARDWARE_UNAVAILABLE("ফিঙ্গারপ্রিন্ট সেন্সর সাময়িকভাবে ব্যস্ত বা অনুপলব্ধ", false),
    SECURITY_UPDATE_REQUIRED("বায়োমেট্রিক সিকিউরিটি আপডেট প্রয়োজন", false),
    UNKNOWN("বায়োমেট্রিক স্ট্যাটাস জানা যায়নি", false)
}

object BiometricHelper {

    fun checkBiometricAvailability(context: Context): BiometricAvailability {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NONE_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAvailability.HARDWARE_UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricAvailability.SECURITY_UPDATE_REQUIRED
            else -> BiometricAvailability.UNKNOWN
        }
    }

    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String = "স্কুল ম্যানেজমেন্ট বায়োমেট্রিক আনলক",
        subtitle: String = "আপনার ফিঙ্গারপ্রিন্ট বা ফেস দিয়ে অ্যাপ্লিকেশন আনলক করুন",
        negativeButtonText: String = "পিন কোড ব্যবহার করুন",
        onAuthSuccess: () -> Unit,
        onAuthError: (errorCode: Int, errString: String) -> Unit,
        onAuthFailed: () -> Unit
    ): BiometricPrompt? {
        val availability = checkBiometricAvailability(activity)
        if (!availability.isUsable) {
            onAuthError(-1, availability.messageBn)
            return null
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()

        val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onAuthSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onAuthError(errorCode, errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onAuthFailed()
            }
        })

        biometricPrompt.authenticate(promptInfo)
        return biometricPrompt
    }
}
