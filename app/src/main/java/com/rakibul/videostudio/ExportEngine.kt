package com.rakibul.videostudio

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.effect.Presentation
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.Transformer
import java.io.File

object ExportEngine {
    fun outputFile(context: Context, quality: String): File {
        val dir = File(context.getExternalFilesDir(null), "exports").apply { mkdirs() }
        return File(dir, "Rakibul_${quality}_${System.currentTimeMillis()}.mp4")
    }

    fun presentation(quality: String, crop: String): Presentation {
        val base = when (quality) { "720p" -> 720; "1080p" -> 1080; else -> 2160 }
        val size = when (crop) {
            "9:16" -> base to (base * 16 / 9)
            "1:1" -> base to base
            "4:5" -> base to (base * 5 / 4)
            "4:3" -> base to (base * 3 / 4)
            else -> (base * 16 / 9) to base
        }
        return Presentation.createForWidthAndHeight(
            size.first.toInt(), size.second.toInt(), Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP
        )
    }

    fun exportSingleClip(
        context: Context,
        clip: EditorClipModel,
        quality: String,
        crop: String,
        onResult: (Boolean, Uri?) -> Unit
    ) {
        val file = outputFile(context, quality)
        val edited = EditedMediaItem.Builder(MediaItem.fromUri(clip.uri))
            .setEffects(Effects(emptyList(), listOf(presentation(quality, crop))))
            .build()
        Transformer.Builder(context).addListener(object : Transformer.Listener {
            override fun onCompleted(composition: androidx.media3.transformer.Composition, exportResult: androidx.media3.transformer.ExportResult) {
                onResult(true, Uri.fromFile(file))
            }
            override fun onError(composition: androidx.media3.transformer.Composition, exportResult: androidx.media3.transformer.ExportResult, exportException: androidx.media3.transformer.ExportException) {
                onResult(false, null)
            }
        }).build().start(edited, file.absolutePath)
    }
}
