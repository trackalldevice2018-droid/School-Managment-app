package com.example.util

import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Build

data class GoogleUserInfo(
    val email: String,
    val displayName: String,
    val photoUrl: String? = null
)

object GoogleAccountHelper {

    /**
     * Creates an Intent to prompt the user to choose an existing Google Account on the device.
     */
    fun createChooseAccountIntent(selectedAccountEmail: String? = null): Intent {
        val selectedAccount = if (!selectedAccountEmail.isNullOrBlank()) {
            android.accounts.Account(selectedAccountEmail, "com.google")
        } else null

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AccountManager.newChooseAccountIntent(
                selectedAccount,
                null,
                arrayOf("com.google"),
                null,
                null,
                null,
                null
            )
        } else {
            @Suppress("DEPRECATION")
            AccountManager.newChooseAccountIntent(
                selectedAccount,
                null,
                arrayOf("com.google"),
                false,
                null,
                null,
                null,
                null
            )
        }
    }

    /**
     * Extracts selected Google Account email from the Activity result Intent.
     */
    fun extractAccountFromIntent(data: Intent?): String? {
        if (data == null) return null
        return data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
    }

    fun extractAccountEmail(data: Intent?): String? = extractAccountFromIntent(data)

    /**
     * Extracts a clean user display name from email (e.g. trackalldevice2018 -> Trackalldevice).
     */
    fun getDisplayNameFromEmail(email: String): String {
        val username = email.substringBefore("@")
        return username.replace(".", " ")
            .replace("_", " ")
            .split(" ")
            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
    }
}
