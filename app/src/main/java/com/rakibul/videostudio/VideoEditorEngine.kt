package com.rakibul.videostudio

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

class VideoEditorEngine(private val context: Context) {
    val player: ExoPlayer = ExoPlayer.Builder(context).build()

    fun loadVideo(uriString: String) {
        val mediaItem = MediaItem.fromUri(uriString)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
    }

    fun release() {
        player.release()
    }
}
