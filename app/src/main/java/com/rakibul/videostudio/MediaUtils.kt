package com.rakibul.videostudio

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.util.Locale

object MediaUtils {
    fun durationMs(context: Context, uri: Uri): Long {
        val r = android.media.MediaMetadataRetriever()
        return try {
            r.setDataSource(context, uri)
            r.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        } catch (_: Exception) { 0L } finally { r.release() }
    }

    fun displayName(resolver: ContentResolver, uri: Uri): String {
        return try {
            resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
                if (it.moveToFirst()) it.getString(0) else uri.lastPathSegment ?: "Media"
            } ?: (uri.lastPathSegment ?: "Media")
        } catch (_: Exception) { uri.lastPathSegment ?: "Media" }
    }

    fun formatTime(ms: Long): String {
        val seconds = (ms / 1000L).coerceAtLeast(0L)
        return String.format(Locale.US, "%02d:%02d", seconds / 60L, seconds % 60L)
    }
}
