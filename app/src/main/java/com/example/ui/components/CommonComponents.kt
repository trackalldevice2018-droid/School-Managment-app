package com.example.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AsyncPhotoAvatar(
    photoUri: String?,
    name: String,
    colorHex: Long,
    size: Int = 48,
    shapeCorner: Int = 12,
    modifier: Modifier = Modifier
) {
    if (!photoUri.isNullOrBlank()) {
        val imageModel = if (photoUri.startsWith("/")) java.io.File(photoUri) else photoUri
        coil.compose.SubcomposeAsyncImage(
            model = imageModel,
            contentDescription = "Photo of $name",
            contentScale = ContentScale.Crop,
            loading = {
                AvatarInitials(name = name, colorHex = colorHex, size = size)
            },
            error = {
                AvatarInitials(name = name, colorHex = colorHex, size = size)
            },
            modifier = modifier
                .size(size.dp)
                .clip(RoundedCornerShape(shapeCorner.dp))
                .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(shapeCorner.dp))
        )
    } else {
        AvatarInitials(
            name = name,
            colorHex = colorHex,
            size = size,
            modifier = modifier
        )
    }
}

@Composable
fun PhotoPickerControl(
    selectedUri: String?,
    onPhotoSelected: (String?) -> Unit,
    label: String = "পাসপোর্ট সাইজ ছবি আপলোড করুন",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = com.example.util.ImageStorageHelper.saveImageToInternalStorage(context, uri) ?: uri.toString()
            onPhotoSelected(savedPath)
        }
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncPhotoAvatar(
                photoUri = selectedUri,
                name = "Student",
                colorHex = 0xFF1E3A8A,
                size = 54,
                shapeCorner = 10
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (selectedUri != null) "ছবি সংযুক্ত করা হয়েছে (Photo attached)" else "গ্যালারি থেকে ছবি নির্বাচন করুন",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selectedUri != null) Color(0xFF059669) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (selectedUri != null) {
                    OutlinedButton(
                        onClick = { onPhotoSelected(null) },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text("মুছুন", style = MaterialTheme.typography.labelSmall)
                    }
                }

                OutlinedButton(
                    onClick = {
                        launcher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (selectedUri != null) "পরিবর্তন" else "বাছাই করুন", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun AvatarInitials(
    name: String,
    colorHex: Long,
    size: Int = 44,
    modifier: Modifier = Modifier
) {
    val initials = name.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color(colorHex)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials.ifBlank { "ST" },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = (size * 0.38).sp
        )
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SchoolLogoView(
    logoUri: String?,
    modifier: Modifier = Modifier,
    size: Int = 44,
    shapeCorner: Int = 10
) {
    if (!logoUri.isNullOrBlank()) {
        val imageModel = if (logoUri.startsWith("/")) java.io.File(logoUri) else logoUri
        coil.compose.SubcomposeAsyncImage(
            model = imageModel,
            contentDescription = "School Logo",
            contentScale = ContentScale.Crop,
            loading = {
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_school_logo),
                    contentDescription = "Default School Logo",
                    modifier = Modifier.size(size.dp)
                )
            },
            error = {
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_school_logo),
                    contentDescription = "Default School Logo",
                    modifier = Modifier.size(size.dp)
                )
            },
            modifier = modifier
                .size(size.dp)
                .clip(RoundedCornerShape(shapeCorner.dp))
        )
    } else {
        Box(
            modifier = modifier
                .size(size.dp)
                .clip(RoundedCornerShape(shapeCorner.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_school_logo),
                contentDescription = "Default School Logo",
                modifier = Modifier.size((size * 0.85).dp)
            )
        }
    }
}

@Composable
fun WatermarkContainer(
    settings: com.example.data.model.SchoolSettings,
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit
) {
    Box(modifier = modifier) {
        if (settings.showWatermark) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!settings.customLogoUri.isNullOrBlank()) {
                    val imageModel = if (settings.customLogoUri.startsWith("/")) java.io.File(settings.customLogoUri) else settings.customLogoUri
                    coil.compose.SubcomposeAsyncImage(
                        model = imageModel,
                        contentDescription = "Watermark",
                        contentScale = ContentScale.Fit,
                        alpha = settings.watermarkOpacity.coerceIn(0.04f, 0.25f),
                        modifier = Modifier.size(160.dp)
                    )
                } else {
                    Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_school_logo),
                        contentDescription = "Watermark",
                        alpha = settings.watermarkOpacity.coerceIn(0.04f, 0.25f),
                        modifier = Modifier.size(160.dp)
                    )
                }
            }
        }
        content()
    }
}

@Composable
fun AboutUsDialog(
    settings: com.example.data.model.SchoolSettings,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "আমাদের সম্পর্কে ও নির্মাতা পরিচিতি",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Developer Profile Card
                androidx.compose.material3.Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val ctx = LocalContext.current

                        Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.developer_alam),
                            contentDescription = "Developer Md. Alam",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(width = 130.dp, height = 155.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(2.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "মোঃ আলম",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "বিএসএস অনার্স & ওয়েব এক্সপার্ট, ডাটা রিসার্চার",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "Software Creator & Solution Architect",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Developer Contact Details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val intent = android.content.Intent(
                                        android.content.Intent.ACTION_DIAL,
                                        android.net.Uri.parse("tel:01846505302")
                                    )
                                    ctx.startActivity(intent)
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("01846505302", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            OutlinedButton(
                                onClick = {
                                    val intent = android.content.Intent(
                                        android.content.Intent.ACTION_SENDTO,
                                        android.net.Uri.parse("mailto:alamkhan.ak716@gmail.com")
                                    )
                                    ctx.startActivity(intent)
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Email", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Text(
                            text = "ইমেইল: alamkhan.ak716@gmail.com",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // What Can Be Done Section (কী কী করা যাবে)
                androidx.compose.material3.Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "সফটওয়্যারের প্রধান সুবিধাসমূহ (কী কী করা যাবে):",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        val features = listOf(
                            "১. শ্রেণি ও বিষয় ব্যবস্থাপনা: নার্সারি থেকে দশম শ্রেণি পর্যন্ত আবশ্যিক ও ঐচ্ছিক বিষয় এবং ৫০/১০০ মার্কিং স্কেল পরিচালনা।",
                            "২. শিক্ষার্থী ডাটাবেজ: ছবি সহ শিক্ষার্থী নিবন্ধন, সিঙ্গেল ও একাধিক (বাল্ক) ডিলিট, ডাটা এডিট ও স্মার্ট সার্চ।",
                            "৩. এক্সেল ইমপোর্ট ও এক্সপোর্ট: এক ক্লিকে এক্সেল (CSV) থেকে শিক্ষার্থী তথ্য আপলোড এবং শ্রেণিভিত্তিক ডাউনলোড।",
                            "৪. স্বয়ংক্রিয় পরীক্ষার রুটিন: ড্রপডাউন বিষয় নির্বাচন, এডিটেবল তারিখ-বার এবং A4 সাইজে ৪টি অথবা ৬টি স্লিপ প্রিন্ট/PDF।",
                            "৫. স্বয়ংক্রিয় বাল্ক প্রবেশপত্র (Admit Card): শ্রেণিভিত্তিক শিক্ষার্থীদের নাম, রোল ও স্বাক্ষর স্থান সহ প্রবেশপত্র জেনারেট এবং A4 সাইজে ৪টি অথবা ৬টি প্রিন্ট।",
                            "৬. ডুপ্লিকেটমুক্ত নম্বর এন্ট্রি: একই বিষয়ে ডুপ্লিকেট এন্ট্রি প্রতিরোধ ও আবশ্যিক বিষয়ভিত্তিক স্বয়ংক্রিয় জিপিএ এবং মেধা স্থান গণনা।",
                            "৭. একাডেমিক ট্রান্সক্রিপ্ট ও মেধাতালিকা: প্রফেশনাল মার্কশীট ও শ্রেণিভিত্তিক মেধা তালিকা তৈরি ও প্রিন্ট।",
                            "৮. লোগো ও ওয়াটারমার্ক: নিজস্ব মাদ্রাসার/প্রতিষ্ঠানের লোগো আপলোড, থিম রঙ এবং ব্যাকগ্রাউন্ড জলছাপ (Watermark)।",
                            "৯. বায়োমেট্রিক ও পিন সিকিউরিটি: ফিঙ্গারপ্রিন্ট বা ফেস দিয়ে আনলক, ৪-ডিজিট মাস্টার পিন ও ব্যাকগ্রাউন্ড অটো-লক।"
                        )

                        features.forEach { feat ->
                            Text(
                                text = feat,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Institution info footer
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = settings.schoolName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${settings.schoolAddress} • ${settings.eiinNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.Button(onClick = onDismiss) {
                Text("ঠিক আছে")
            }
        }
    )
}
