package com.example.nfcmuseumguide.data

import android.content.Context
import android.net.Uri
import java.io.File

class ImageStorage(private val context: Context) {
    fun copyImageToAppStorage(sourceUri: Uri): String? {
        return runCatching {
            val input = context.contentResolver.openInputStream(sourceUri) ?: return null
            val dir = File(context.filesDir, "exhibit_photos")
            if (!dir.exists()) dir.mkdirs()

            val file = File(dir, "exhibit_${System.currentTimeMillis()}.jpg")
            input.use { inputStream ->
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            Uri.fromFile(file).toString()
        }.getOrNull()
    }
}