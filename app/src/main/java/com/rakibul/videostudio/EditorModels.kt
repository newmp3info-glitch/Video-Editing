package com.rakibul.videostudio

import android.net.Uri

data class EditorClipModel(
    val id: Long,
    val uri: Uri,
    val name: String,
    val sourceDurationMs: Long,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = sourceDurationMs,
    val speed: Float = 1f,
    val volume: Float = 1f,
    val effect: String = "None",
    val effectIntensity: Float = 0.7f,
    val filter: String = "Original",
    val crop: String = "9:16"
) {
    val durationMs: Long get() = (trimEndMs - trimStartMs).coerceAtLeast(1L)
}

data class EditorAudioModel(
    val id: Long,
    val uri: Uri,
    val name: String,
    val durationMs: Long,
    val startMs: Long = 0L,
    val volume: Float = 1f
)

data class EditorOverlayModel(
    val id: Long,
    val text: String,
    val startMs: Long,
    val durationMs: Long = 1800L,
    val x: Float = .5f,
    val y: Float = .75f,
    val scale: Float = 1f,
    val animation: String = "Pop"
)

enum class CropPreset(val value: String) {
    ORIGINAL("Original"), RATIO_16_9("16:9"), RATIO_9_16("9:16"),
    SQUARE("1:1"), RATIO_4_5("4:5"), RATIO_4_3("4:3")
}

enum class StickerAnimation(val value: String) { POP("Pop"), BOUNCE("Bounce"), WIGGLE("Wiggle") }
