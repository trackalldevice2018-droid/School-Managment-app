package com.example.ui.screens

import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.AccountCircle
import com.example.util.GoogleAccountHelper
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.AuthMode
import com.example.ui.SchoolViewModel
import com.example.util.BiometricHelper

@Composable
fun SecurityLockScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val biometricAvailability = remember(context) { BiometricHelper.checkBiometricAvailability(context) }

    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showForgotPinDialog by remember { mutableStateOf(false) }
    var activeInputMode by remember(settings.authMode) {
        mutableStateOf(if (settings.authMode == AuthMode.BIOMETRIC_ONLY && biometricAvailability.isUsable) "BIOMETRIC" else "PIN")
    }

    var showGoogleDialog by remember { mutableStateOf(false) }
    var enteredGoogleEmail by remember { mutableStateOf(settings.googleAccountEmail) }

    val googleAccountLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val email = GoogleAccountHelper.extractAccountEmail(result.data)
        if (!email.isNullOrBlank()) {
            val success = viewModel.unlockWithGoogleAccount(email)
            if (!success) {
                isError = true
                errorMessage = "অননুমোদিত অ্যাকাউন্ট ($email)! অনুমোদিত অ্যাডমিন ইমেইল দিয়ে প্রবেশ করুন।"
            }
        }
    }

    fun triggerGoogleAccountAuth() {
        try {
            val intent = GoogleAccountHelper.createChooseAccountIntent(settings.googleAccountEmail)
            googleAccountLauncher.launch(intent)
        } catch (e: Exception) {
            showGoogleDialog = true
        }
    }

    fun triggerBiometricAuth() {
        if (activity == null) return
        BiometricHelper.showBiometricPrompt(
            activity = activity,
            title = "${settings.schoolName} - বায়োমেট্রিক সিকিউরিটি",
            subtitle = "ফিঙ্গারপ্রিন্ট বা ফেস দিয়ে অ্যাপ্লিকেশন আনলক করুন",
            negativeButtonText = "পিন কোড ব্যবহার করুন",
            onAuthSuccess = {
                viewModel.unlockWithBiometric()
            },
            onAuthError = { errorCode, errString ->
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON || errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                    activeInputMode = "PIN"
                } else {
                    isError = true
                    errorMessage = errString
                }
            },
            onAuthFailed = {
                isError = true
                errorMessage = "বায়োমেট্রিক মেলেনি! পুনরায় চেষ্টা করুন।"
            }
        )
    }

    var hasAutoPromptedBiometric by remember { mutableStateOf(false) }
    LaunchedEffect(settings.authMode, biometricAvailability) {
        if (!hasAutoPromptedBiometric &&
            (settings.authMode == AuthMode.BIOMETRIC_OR_PIN || settings.authMode == AuthMode.BIOMETRIC_ONLY) &&
            biometricAvailability.isUsable &&
            activity != null
        ) {
            hasAutoPromptedBiometric = true
            triggerBiometricAuth()
        }
    }

    fun onDigitPress(digit: String) {
        if (enteredPin.length < 4) {
            val newPin = enteredPin + digit
            enteredPin = newPin
            isError = false
            if (newPin.length == 4) {
                val success = viewModel.unlockApp(newPin)
                if (!success) {
                    isError = true
                    errorMessage = "ভুল পিন কোড! পুনরায় সঠিক পিন দিন।"
                    enteredPin = ""
                }
            }
        }
    }

    fun onBackspace() {
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            isError = false
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Institution Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_school_logo),
                    contentDescription = "School Logo",
                    modifier = Modifier.size(60.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = settings.schoolName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "${settings.eiinNumber} • সুরক্ষিত স্কুল ম্যানেজমেন্ট",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Central Authentication Section
            if (activeInputMode == "BIOMETRIC" && settings.authMode == AuthMode.BIOMETRIC_ONLY) {
                // Pure Biometric View
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .clickable { triggerBiometricAuth() }
                            .testTag("biometric_fingerprint_touch")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "Fingerprint Sensor",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "ফিঙ্গারপ্রিন্ট দিয়ে আনলক করুন",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "ডিভাইসের ফিঙ্গারপ্রিন্ট সেন্সরে আঙুল স্পর্শ করুন",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { triggerBiometricAuth() },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("পুনরায় স্ক্যান করুন")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(onClick = { activeInputMode = "PIN" }) {
                        Icon(Icons.Default.Password, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("অথবা পিন কোড দিয়ে আনলক করুন")
                    }
                }
            } else {
                // PIN View (with quick Biometric button if supported)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isError) Icons.Default.Lock else Icons.Default.Security,
                            contentDescription = null,
                            tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "মাস্টার সিকিউরিটি পিন দিন",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "অ্যাপ্লিকেশনের ডেটা সুরক্ষায় ৪-ডিজিট পিন কোড দিন",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 4 PIN Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(4) { index ->
                            val isFilled = index < enteredPin.length
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isError -> MaterialTheme.colorScheme.error
                                            isFilled -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    )
                                    .border(
                                        width = 1.5.dp,
                                        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Error Message
                    AnimatedVisibility(
                        visible = isError,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Keypad & Bottom Action
            if (activeInputMode == "PIN" || settings.authMode != AuthMode.BIOMETRIC_ONLY) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NumericKeypad(
                        onDigitClick = { onDigitPress(it) },
                        onBackspaceClick = { onBackspace() },
                        showBiometricKey = biometricAvailability.isUsable && settings.authMode != AuthMode.PIN_ONLY,
                        onBiometricClick = { triggerBiometricAuth() }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (biometricAvailability.isUsable && settings.authMode != AuthMode.PIN_ONLY) {
                            TextButton(
                                onClick = { triggerBiometricAuth() },
                                modifier = Modifier.testTag("biometric_quick_auth_button")
                            ) {
                                Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ফিঙ্গারপ্রিন্ট", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        TextButton(
                            onClick = { showForgotPinDialog = true },
                            modifier = Modifier.testTag("forgot_pin_button")
                        ) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "পিন ভুলে গেছেন?",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Google Account API Sign-In Button
                    OutlinedButton(
                        onClick = { triggerGoogleAccountAuth() },
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .height(44.dp)
                            .testTag("google_account_unlock_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Google অ্যাকাউন্ট দিয়ে প্রবেশ করুন",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                }
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    // Google Account API Login / Confirm Dialog
    if (showGoogleDialog) {
        AlertDialog(
            onDismissRequest = { showGoogleDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Google Account API প্রমাণীকরণ",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "অনুমোদিত অ্যাডমিন Google অ্যাকাউন্ট দিয়ে সফটওয়্যারে এক্সেস নিশ্চিত করুন:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = enteredGoogleEmail,
                        onValueChange = { enteredGoogleEmail = it },
                        label = { Text("Google অ্যাকাউন্ট ইমেইল *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "ডিফল্ট অনুমোদিত ইমেইল: ${settings.googleAccountEmail}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = viewModel.unlockWithGoogleAccount(enteredGoogleEmail)
                        showGoogleDialog = false
                        if (!success) {
                            isError = true
                            errorMessage = "অননুমোদিত অ্যাকাউন্ট! অনুমোদিত ইমেইল দিয়ে প্রবেশ করুন।"
                        }
                    },
                    enabled = enteredGoogleEmail.isNotBlank()
                ) {
                    Text("লগইন ও আনলক করুন")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showGoogleDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Forgot PIN / Recovery Dialog
    if (showForgotPinDialog) {
        var answerInput by remember { mutableStateOf("") }
        var newPinInput by remember { mutableStateOf("") }
        var recoveryError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showForgotPinDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LockOpen, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("মাস্টার পিন রিকভারি", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "নিচের সিকিউরিটি প্রশ্নের সঠিক উত্তর দিন এবং নতুন ৪-ডিজিট পিন সেট করুন:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "প্রশ্ন: ${settings.securityQuestion}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    OutlinedTextField(
                        value = answerInput,
                        onValueChange = {
                            answerInput = it
                            recoveryError = false
                        },
                        label = { Text("আপনার উত্তর লিখুন *") },
                        placeholder = { Text("যেমন: Mathematics") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = { if (it.length <= 4) newPinInput = it },
                        label = { Text("নতুন ৪-ডিজিট পিন *") },
                        placeholder = { Text("যেমন: 1234") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "জরুরি নোট: ডিফল্ট ইমার্জেন্সি মাস্টার কোড: 9988",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )

                    if (recoveryError) {
                        Text(
                            text = "⚠️ সিকিউরিটি উত্তর সঠিক নয়!",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (answerInput.isNotBlank() && newPinInput.length == 4) {
                            val success = viewModel.recoverAndResetPin(answerInput.trim(), newPinInput.trim())
                            if (success) {
                                showForgotPinDialog = false
                            } else {
                                recoveryError = true
                            }
                        }
                    },
                    enabled = answerInput.isNotBlank() && newPinInput.length == 4
                ) {
                    Text("রিসেট ও আনলক করুন")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showForgotPinDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
fun NumericKeypad(
    onDigitClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    showBiometricKey: Boolean = false,
    onBiometricClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(if (showBiometricKey) "BIO" else "", "0", "DEL")
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        keys.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { key ->
                    when (key) {
                        "" -> {
                            Spacer(modifier = Modifier.size(64.dp))
                        }
                        "BIO" -> {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .clickable { onBiometricClick() }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = "Use Biometric",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        }
                        "DEL" -> {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .clickable { onBackspaceClick() }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                                        contentDescription = "Backspace",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        else -> {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 1.5.dp,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .clickable { onDigitClick(key) }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = key,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 22.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
