package com.rakibul.videostudio

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Transformer
import java.io.File

class ExportEngine(private val context: Context) {
    @OptIn(UnstableApi::class)
    fun exportVideo(inputUri: Uri, onComplete: (File) -> Unit, onError: (Exception) -> Unit) {
        val outputFile = File(context.getExternalFilesDir(null), "RakibulExport_${System.currentTimeMillis()}.mp4")
        
        val mediaItem = MediaItem.fromUri(inputUri)
        val editedMediaItem = EditedMediaItem.Builder(mediaItem).build()
        val composition = Composition.Builder(androidx.media3.transformer.EditedMediaItemSequence(editedMediaItem)).build()

        val transformer = Transformer.Builder(context)
            .setListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: androidx.media3.transformer.ExportResult) {
                    onComplete(outputFile)
                }
                override fun onError(composition: Composition, exportResult: androidx.media3.transformer.ExportResult, exception: androidx.media3.transformer.ExportException) {
                    onError(exception)
                }
            })
            .build()

        try {
            transformer.start(composition, outputFile.absolutePath)
        } catch (e: Exception) {
            onError(e)
        }
    }
}
