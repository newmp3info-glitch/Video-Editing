package com.rakibul.videostudio

object TimelineEngine {
    fun totalDuration(clips: List<EditorClipModel>): Long = clips.sumOf { it.durationMs }

    fun clipStart(clips: List<EditorClipModel>, index: Int): Long =
        clips.take(index.coerceAtLeast(0)).sumOf { it.durationMs }

    fun clipAt(clips: List<EditorClipModel>, globalMs: Long): Int {
        if (clips.isEmpty()) return -1
        var cursor = 0L
        clips.forEachIndexed { i, clip ->
            val end = cursor + clip.durationMs
            if (globalMs in cursor..end) return i
            cursor = end
        }
        return clips.lastIndex
    }

    fun globalToLocal(clips: List<EditorClipModel>, globalMs: Long): Pair<Int, Long> {
        val index = clipAt(clips, globalMs)
        if (index < 0) return -1 to 0L
        return index to (globalMs - clipStart(clips, index)).coerceIn(0L, clips[index].durationMs)
    }
}
