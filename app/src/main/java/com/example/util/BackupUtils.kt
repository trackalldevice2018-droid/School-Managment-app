package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object BackupUtils {

    /**
     * Writes CSV content to cache and launches an ACTION_SEND chooser targeting
     * Google Drive, Google Sheets, Gmail, Files, WhatsApp, etc.
     */
    fun shareCsvToDriveOrSheets(context: Context, fileName: String, csvContent: String, title: String = "Share CSV") {
        try {
            val cacheDir = File(context.cacheDir, "exports")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            val cleanFilename = if (fileName.endsWith(".csv")) fileName else "$fileName.csv"
            val file = File(cacheDir, cleanFilename)
            FileOutputStream(file).use { fos ->
                fos.write(csvContent.toByteArray(Charsets.UTF_8))
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, cleanFilename)
                putExtra(Intent.EXTRA_TEXT, "মাদ্রাসার ব্যাকআপ ডাটা ফাইল: $cleanFilename\nGoogle Drive / Google Sheets এ সরাসরি সংরক্ষণ করতে পারেন।")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "শেয়ার বা ব্যাকআপ করতে ব্যর্থ: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
