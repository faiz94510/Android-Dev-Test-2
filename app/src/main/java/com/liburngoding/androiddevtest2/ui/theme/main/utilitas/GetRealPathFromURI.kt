package com.liburngoding.androiddevtest2.ui.theme.main.utilitas

import android.content.Context
import android.net.Uri
import android.provider.MediaStore

object GetRealPathFromURI {
    fun getRealPath(context : Context, uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                return it.getString(columnIndex)
            }
        }
        // Jika tidak ada yang ditemukan, kembalikan null
        return null
    }
}