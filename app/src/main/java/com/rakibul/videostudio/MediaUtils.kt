package com.rakibul.videostudio

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import android.media.MediaMetadataRetriever
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

object MediaUtils {
    fun durationOf(resolver: ContentResolver, uri: Uri): Long = try { resolver.openAssetFileDescriptor(uri,"r")?.use { afd -> MediaMetadataRetriever().let { r -> r.setDataSource(afd.fileDescriptor); val d=r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()?:0L; r.release(); d } } ?: 0L } catch(_:Exception){0L}
    fun displayName(resolver: ContentResolver, uri: Uri): String = try { resolver.query(uri,arrayOf(OpenableColumns.DISPLAY_NAME),null,null,null)?.use { c -> if(c.moveToFirst()) c.getString(0) else uri.lastPathSegment ?: "Media" } ?: (uri.lastPathSegment ?: "Media") } catch(_:Exception){uri.lastPathSegment ?: "Media"}
    fun formatMs(ms: Long): String { val s=(ms/1000).coerceAtLeast(0); return String.format(java.util.Locale.US,"%02d:%02d",s/60,s%60) }
    fun makeThumbnails(context: Context, uri: Uri, duration: Long): List<ImageBitmap> { val r=MediaMetadataRetriever(); return try { r.setDataSource(context,uri); (0 until 8).mapNotNull { i -> val t=if(duration<=0)0L else duration*i/7L; r.getFrameAtTime(t*1000L,MediaMetadataRetriever.OPTION_CLOSEST_SYNC)?.asImageBitmap() } } catch(_:Exception){emptyList()} finally {r.release()} }
}
