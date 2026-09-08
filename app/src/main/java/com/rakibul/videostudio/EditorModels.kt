package com.rakibul.videostudio

import android.net.Uri

data class Clip(
    val id: Long,
    val uri: Uri,
    val name: String,
    val durationMs: Long,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = durationMs,
    val speed: Float = 1f,
    val volume: Float = 1f,
    val effect: String = "None",
    val effectIntensity: Float = 0.7f,
    val crop: String = "9:16"
) { val editDurationMs: Long get() = ((trimEndMs - trimStartMs) / speed).toLong().coerceAtLeast(1L) }

data class AudioTrack(
    val id: Long, val uri: Uri, val name: String, val durationMs: Long,
    val volume: Float = 1f, val startMs: Long = 0L
)

data class Sticker(
    val id: Long, val emoji: String, val label: String, val atMs: Long,
    val durationMs: Long = 1800L, val x: Float = .5f, val y: Float = .78f,
    val scale: Float = 1f, val animation: String = "Pop"
)
