package com.example.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ImageStorageHelper {

    fun saveImageToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(sourceUri) ?: return null
            val photoDir = File(context.filesDir, "student_photos").apply {
                if (!exists()) mkdirs()
            }
            val fileName = "student_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            val destFile = File(photoDir, fileName)
            FileOutputStream(destFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
