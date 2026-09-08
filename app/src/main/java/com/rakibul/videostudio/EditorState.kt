package com.rakibul.videostudio

class EditorProjectState {
    private val undo = ArrayDeque<List<EditorClipModel>>()
    private val redo = ArrayDeque<List<EditorClipModel>>()
    var clips: List<EditorClipModel> = emptyList()
        private set

    fun replace(next: List<EditorClipModel>) {
        undo.addLast(clips)
        while (undo.size > 30) undo.removeFirst()
        clips = next
        redo.clear()
    }

    fun undo(): Boolean {
        if (undo.isEmpty()) return false
        redo.addLast(clips)
        clips = undo.removeLast()
        return true
    }

    fun redo(): Boolean {
        if (redo.isEmpty()) return false
        undo.addLast(clips)
        clips = redo.removeLast()
        return true
    }

    fun add(clip: EditorClipModel) = replace(clips + clip)
    fun delete(index: Int) = if (index in clips.indices) replace(clips.toMutableList().also { it.removeAt(index) }) else Unit

    fun reorder(from: Int, to: Int) {
        if (from !in clips.indices || to !in clips.indices || from == to) return
        replace(clips.toMutableList().also { list -> val item = list.removeAt(from); list.add(to, item) })
    }

    fun trim(index: Int, startMs: Long, endMs: Long) {
        if (index !in clips.indices) return
        val c = clips[index]
        val s = startMs.coerceIn(0L, c.sourceDurationMs - 1)
        val e = endMs.coerceIn(s + 1L, c.sourceDurationMs)
        replace(clips.toMutableList().also { it[index] = c.copy(trimStartMs = s, trimEndMs = e) })
    }
}
