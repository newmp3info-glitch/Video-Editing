package com.rakibul.videostudio

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

class VideoEditorEngine(context: Context) {
    val player: ExoPlayer = ExoPlayer.Builder(context).build()

    fun load(clip: EditorClipModel, autoPlay: Boolean = false) {
        val item = MediaItem.Builder()
            .setUri(clip.uri)
            .setClippingConfiguration(
                MediaItem.ClippingConfiguration.Builder()
                    .setStartPositionMs(clip.trimStartMs)
                    .setEndPositionMs(clip.trimEndMs)
                    .build()
            ).build()
        player.setMediaItem(item)
        player.prepare()
        player.volume = clip.volume
        player.setPlaybackSpeed(clip.speed)
        player.playWhenReady = autoPlay
    }

    fun seekClipLocal(positionMs: Long) = player.seekTo(positionMs.coerceAtLeast(0L))
    fun play() { player.playWhenReady = true }
    fun pause() { player.playWhenReady = false }
    fun release() = player.release()
}
