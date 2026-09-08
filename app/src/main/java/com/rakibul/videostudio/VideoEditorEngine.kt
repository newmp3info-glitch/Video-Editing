package com.rakibul.videostudio

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class VideoEditorEngine(context: Context) {
    val player: ExoPlayer = ExoPlayer.Builder(context).build().apply { repeatMode = Player.REPEAT_MODE_OFF }
    fun load(clips: List<Clip>, selectedIndex: Int, play: Boolean) { val items=clips.map { c -> MediaItem.Builder().setUri(c.uri).setClippingConfiguration(MediaItem.ClippingConfiguration.Builder().setStartPositionMs(c.trimStartMs).setEndPositionMs(c.trimEndMs).build()).build() }; if(items.isEmpty()){player.clearMediaItems();return}; val i=selectedIndex.coerceIn(0,items.lastIndex); player.setMediaItems(items,i,0L); player.prepare(); player.playWhenReady=play; player.setPlaybackParameters(PlaybackParameters(clips[i].speed)); player.volume=clips[i].volume }
    fun seekGlobal(clips: List<Clip>, globalMs: Long) { var left=globalMs.coerceAtLeast(0); for((i,c) in clips.withIndex()){val d=c.editDurationMs;if(left<=d||i==clips.lastIndex){player.seekTo(i,(c.trimStartMs+(left*c.speed).toLong()).coerceIn(c.trimStartMs,c.trimEndMs));return};left-=d} }
    fun release(){player.release()}
}
