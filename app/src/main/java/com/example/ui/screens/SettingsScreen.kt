package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BrandingWatermark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.AuthMode
import com.example.util.BiometricHelper
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SchoolSettings
import com.example.ui.SchoolViewModel
import com.example.ui.components.AsyncPhotoAvatar
import com.example.ui.components.PhotoPickerControl

data class ThemeColorOption(val name: String, val hex: Long)

val ThemeColorOptions = listOf(
    ThemeColorOption("Academy Blue", 0xFF1E3A8A),
    ThemeColorOption("Emerald Green", 0xFF0D9488),
    ThemeColorOption("Royal Purple", 0xFF7C3AED),
    ThemeColorOption("Deep Crimson", 0xFFBE123C),
    ThemeColorOption("Amber Gold", 0xFFD97706),
    ThemeColorOption("Slate Navy", 0xFF334155)
)

@Composable
fun SettingsScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val currentSettings by viewModel.settings.collectAsStateWithLifecycle()

    var schoolName by remember(currentSettings) { mutableStateOf(currentSettings.schoolName) }
    var schoolAddress by remember(currentSettings) { mutableStateOf(currentSettings.schoolAddress) }
    var eiinNumber by remember(currentSettings) { mutableStateOf(currentSettings.eiinNumber) }
    var contactPhone by remember(currentSettings) { mutableStateOf(currentSettings.contactPhone) }
    var contactEmail by remember(currentSettings) { mutableStateOf(currentSettings.contactEmail) }
    var principalName by remember(currentSettings) { mutableStateOf(currentSettings.principalName) }
    var principalTitle by remember(currentSettings) { mutableStateOf(currentSettings.principalTitle) }
    var customLogoUri by remember(currentSettings) { mutableStateOf(currentSettings.customLogoUri) }
    var selectedThemeHex by remember(currentSettings) { mutableLongStateOf(currentSettings.themeColorHex) }
    var showWatermark by remember(currentSettings) { mutableStateOf(currentSettings.showWatermark) }
    var watermarkOpacity by remember(currentSettings) { mutableFloatStateOf(currentSettings.watermarkOpacity) }
    var routineInstructions by remember(currentSettings) { mutableStateOf(currentSettings.routineInstructions) }
    var isSecurityLockEnabled by remember(currentSettings) { mutableStateOf(currentSettings.isSecurityLockEnabled) }
    var authMode by remember(currentSettings) { mutableStateOf(currentSettings.authMode) }
    var isGoogleAuthEnabled by remember(currentSettings) { mutableStateOf(currentSettings.isGoogleAuthEnabled) }
    var googleAccountEmail by remember(currentSettings) { mutableStateOf(currentSettings.googleAccountEmail) }
    var autoLockOnBackground by remember(currentSettings) { mutableStateOf(currentSettings.autoLockOnBackground) }
    var securityQuestion by remember(currentSettings) { mutableStateOf(currentSettings.securityQuestion) }
    var securityAnswer by remember(currentSettings) { mutableStateOf(currentSettings.securityAnswer) }
    var showChangePinDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val biometricAvailability = remember(context) { BiometricHelper.checkBiometricAvailability(context) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(6.dp))

        // Header Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "কেন্দ্রীয় সফটওয়্যার সেটিংস (Central Settings)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "লোগো, প্রতিষ্ঠানের নাম, থিম কালার, হেডিং, সিগনেচার ও জলছাপ এক জায়গায় পরিবর্তন করুন।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Section 1: Logo & Branding
        SettingsSectionCard(
            title = "১. লোগো ও ব্র্যান্ডিং (Logo & Branding)",
            icon = Icons.Default.School
        ) {
            PhotoPickerControl(
                selectedUri = customLogoUri,
                onPhotoSelected = { customLogoUri = it },
                label = "প্রতিষ্ঠানের অফিসিয়াল লোগো আপলোড"
            )

            if (customLogoUri != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "ডিফল্ট লোগোতে ফিরে যান",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { customLogoUri = null }
                            .padding(4.dp)
                    )
                }
            }
        }

        // Section 2: School Name, Address & Headings
        SettingsSectionCard(
            title = "২. প্রতিষ্ঠানের পরিচিতি ও হেডিং (Institution & Headings)",
            icon = Icons.Default.Description
        ) {
            OutlinedTextField(
                value = schoolName,
                onValueChange = { schoolName = it },
                label = { Text("প্রতিষ্ঠানের পূর্ণ নাম / সফটওয়্যার নাম *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("setting_school_name")
            )

            OutlinedTextField(
                value = schoolAddress,
                onValueChange = { schoolAddress = it },
                label = { Text("প্রতিষ্ঠানের ঠিকানা (Address) *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("setting_school_address")
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = eiinNumber,
                    onValueChange = { eiinNumber = it },
                    label = { Text("EIIN নম্বর") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = contactPhone,
                    onValueChange = { contactPhone = it },
                    label = { Text("হেল্পলাইন ফোন") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = contactEmail,
                onValueChange = { contactEmail = it },
                label = { Text("অফিসিয়াল ইমেইল") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Section 3: Theme & Color
        SettingsSectionCard(
            title = "৩. অ্যাপ ও সিস্টেমের থিম কালার (Theme & Color)",
            icon = Icons.Default.ColorLens
        ) {
            Text(
                text = "পছন্দের মূল প্রাইমারি থিম রঙ নির্বাচন করুন:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ThemeColorOptions.forEach { opt ->
                    val isSelected = selectedThemeHex == opt.hex
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { selectedThemeHex = opt.hex }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(opt.hex))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = opt.name.split(" ").first(),
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Section 4: Signatures & Authority
        SettingsSectionCard(
            title = "৪. ডিজিটাল স্বাক্ষর ও কর্তৃপক্ষ (Authority & Signature)",
            icon = Icons.Default.Draw
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = principalName,
                    onValueChange = { principalName = it },
                    label = { Text("প্রধান শিক্ষকের নাম *") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = principalTitle,
                    onValueChange = { principalTitle = it },
                    label = { Text("পদবী (Designation) *") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Section 5: Watermark & Layout Options
        SettingsSectionCard(
            title = "৫. লেআউট ও জলছাপ (Layout & Watermark)",
            icon = Icons.Default.BrandingWatermark
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "মার্কশীট ও ট্রান্সক্রিপ্টে জলছাপ দেখান (Watermark)",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "অফিসিয়াল ট্রান্সক্রিপ্টের ব্যাকগ্রাউন্ডে প্রতিষ্ঠানের জলছাপ প্রদর্শন",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = showWatermark,
                    onCheckedChange = { showWatermark = it }
                )
            }

            if (showWatermark) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "জলছাপের দৃশ্যমানতা (Opacity): ${(watermarkOpacity * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall
                )
                Slider(
                    value = watermarkOpacity,
                    onValueChange = { watermarkOpacity = it },
                    valueRange = 0.04f..0.25f,
                    steps = 10
                )
            }
        }

        // Section 6: Exam Routine Instructions
        SettingsSectionCard(
            title = "৬. পরীক্ষার রুটিনের দিকনির্দেশনা (Exam Instructions)",
            icon = Icons.Default.Description
        ) {
            Text(
                text = "রুটিন ও প্রবেশপত্রের নিচে মুদ্রিত নির্দেশনাসমূহ:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = routineInstructions,
                onValueChange = { routineInstructions = it },
                minLines = 3,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Section 7: Security & Master PIN
        SettingsSectionCard(
            title = "৭. অ্যাপ সিকিউরিটি ও বায়োমেট্রিক/পিন লক (Biometric & PIN Security)",
            icon = Icons.Default.Lock
        ) {
            // Master Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "অ্যাপ খোলার সময় সিকিউরিটি লক সক্রিয় রাখুন",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "চালু থাকলে আপনি ছাড়া অন্য কেউ এই অ্যাপে প্রবেশ করতে পারবে না",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isSecurityLockEnabled,
                    onCheckedChange = { isSecurityLockEnabled = it }
                )
            }

            if (isSecurityLockEnabled) {
                Spacer(modifier = Modifier.height(4.dp))

                // Toggle Authentication Method (Biometric vs PIN)
                Text(
                    text = "সিকিউরিটি আনলক পদ্ধতি নির্বাচন করুন:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AuthMode.entries.forEach { mode ->
                        FilterChip(
                            selected = authMode == mode,
                            onClick = { authMode = mode },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (mode) {
                                            AuthMode.PIN_ONLY -> Icons.Default.VpnKey
                                            AuthMode.GOOGLE_ACCOUNT -> Icons.Default.AccountCircle
                                            else -> Icons.Default.Fingerprint
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(mode.labelBn)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Device Biometric Hardware Status Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = when {
                        biometricAvailability.isUsable -> Color(0xFFD1FAE5)
                        biometricAvailability == com.example.util.BiometricAvailability.NONE_ENROLLED -> Color(0xFFFEF3C7)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = if (biometricAvailability.isUsable) Color(0xFF047857) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "ডিভাইস বায়োমেট্রিক স্ট্যাটাস:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (biometricAvailability.isUsable) Color(0xFF047857) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = biometricAvailability.messageBn,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Auto-lock when not in use Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "অ্যাপ ব্যাকগ্রাউন্ডে গেলে স্বয়ংক্রিয় লক (Auto-Lock)",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "অ্যাপ মিনিমাইজ করলে বা ফোন স্ক্রিন বন্ধ হলে পুনরায় আনলক করতে হবে",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = autoLockOnBackground,
                        onCheckedChange = { autoLockOnBackground = it }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // PIN Management
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("মাস্টার পিন কোড:", style = MaterialTheme.typography.labelSmall)
                        Text("সক্রিয় (••••)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    Button(
                        onClick = { showChangePinDialog = true },
                        modifier = Modifier.testTag("change_pin_button")
                    ) {
                        Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("পিন পরিবর্তন")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("সিকিউরিটি রিকভারি প্রশ্ন ও উত্তর (পিন ভুলে গেলে রিসেট করার জন্য):", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelSmall)

                OutlinedTextField(
                    value = securityQuestion,
                    onValueChange = { securityQuestion = it },
                    label = { Text("সিকিউরিটি প্রশ্ন *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = securityAnswer,
                    onValueChange = { securityAnswer = it },
                    label = { Text("সিকিউরিটি উত্তর *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(10.dp))

                // Google Account API Management
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Google Account API দিয়ে এক্সেস (Google Login)",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "অনুমোদিত গুগল অ্যাকাউন্ট দিয়ে সরাসরি অ্যাপ আনলক ও এক্সেস নিয়ন্ত্রণ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isGoogleAuthEnabled,
                        onCheckedChange = { isGoogleAuthEnabled = it }
                    )
                }

                if (isGoogleAuthEnabled) {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = googleAccountEmail,
                        onValueChange = { googleAccountEmail = it },
                        label = { Text("অনুমোদিত Google অ্যাকাউন্ট ইমেইল *") },
                        placeholder = { Text("যেমন: trackalldevice2018@gmail.com") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = currentSettings.googleAccountName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (currentSettings.isGoogleUserSignedIn) "লগইন স্ট্যাটাস: সংযুক্ত (Signed In)" else "লগইন স্ট্যাটাস: অনুমোদিত (Ready)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.signInGoogleAccount(googleAccountEmail.trim(), "মাদ্রাসা এডমিন")
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("যাচাই করুন", fontSize = 11.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { viewModel.lockApp() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("এখনই অ্যাপ লক করুন (Lock Test)")
                }
            }
        }

        // Section 8: আমাদের সম্পর্কে ও সফটওয়্যার নির্মাতা পরিচিতি
        SettingsSectionCard(
            title = "৮. আমাদের সম্পর্কে ও নির্মাতা পরিচিতি (About Us)",
            icon = Icons.Default.Info
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.developer_alam),
                    contentDescription = "Developer Md. Alam",
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "মোঃ আলম",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "বিএসএস অনার্স & ওয়েব এক্সপার্ট, ডাটা রিসার্চার",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "ফোন: 01846505302 • ইমেইল: alamkhan.ak716@gmail.com",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Button(
                    onClick = { viewModel.openAboutUsDialog() },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("বিস্তারিত")
                }
            }
        }

        // Save Button
        Button(
            onClick = {
                val updated = currentSettings.copy(
                    schoolName = schoolName.trim(),
                    schoolAddress = schoolAddress.trim(),
                    eiinNumber = eiinNumber.trim(),
                    contactPhone = contactPhone.trim(),
                    contactEmail = contactEmail.trim(),
                    principalName = principalName.trim(),
                    principalTitle = principalTitle.trim(),
                    customLogoUri = customLogoUri,
                    themeColorHex = selectedThemeHex,
                    showWatermark = showWatermark,
                    watermarkOpacity = watermarkOpacity,
                    routineInstructions = routineInstructions.trim(),
                    isSecurityLockEnabled = isSecurityLockEnabled,
                    authMode = authMode,
                    isGoogleAuthEnabled = isGoogleAuthEnabled,
                    googleAccountEmail = googleAccountEmail.trim(),
                    autoLockOnBackground = autoLockOnBackground,
                    securityQuestion = securityQuestion.trim(),
                    securityAnswer = securityAnswer.trim()
                )
                viewModel.updateSettings(updated)
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("save_settings_button")
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("সব সেটিংস সংরক্ষণ করুন (Save All Settings)", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(50.dp))
    }

    if (showChangePinDialog) {
        var currentPinInput by remember { mutableStateOf("") }
        var newPinInput by remember { mutableStateOf("") }
        var confirmPinInput by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            title = { Text("মাস্টার পিন পরিবর্তন করুন", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "অ্যাপের সুরক্ষায় একটি নতুন ৪-ডিজিটের মাস্টার পিন সেট করুন:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = currentPinInput,
                        onValueChange = { if (it.length <= 4) currentPinInput = it },
                        label = { Text("বর্তমান পিন *") },
                        placeholder = { Text("ডিফল্ট: 1234") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = { if (it.length <= 4) newPinInput = it },
                        label = { Text("নতুন ৪-ডিজিট পিন *") },
                        placeholder = { Text("যেমন: 5678") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = confirmPinInput,
                        onValueChange = { if (it.length <= 4) confirmPinInput = it },
                        label = { Text("নতুন পিন পুনরায় লিখুন *") },
                        placeholder = { Text("যেমন: 5678") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorMsg != null) {
                        Text(
                            text = errorMsg!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPinInput.length != 4) {
                            errorMsg = "নতুন পিন অবশ্যই ৪ ডিজিটের হতে হবে!"
                        } else if (newPinInput != confirmPinInput) {
                            errorMsg = "উভয় নতুন পিন একই হতে হবে!"
                        } else {
                            val success = viewModel.changeSecurityPin(currentPinInput.trim(), newPinInput.trim())
                            if (success) {
                                showChangePinDialog = false
                            } else {
                                errorMsg = "বর্তমান পিন সঠিক নয়! (ডিফল্ট: 1234)"
                            }
                        }
                    },
                    enabled = currentPinInput.isNotBlank() && newPinInput.isNotBlank() && confirmPinInput.isNotBlank()
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showChangePinDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
fun SettingsSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            content()
        }
    }
}
