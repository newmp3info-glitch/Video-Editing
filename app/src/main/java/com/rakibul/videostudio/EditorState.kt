package com.rakibul.videostudio

class EditorProjectState {
    private val undoStack = ArrayDeque<List<Clip>>()
    private val redoStack = ArrayDeque<List<Clip>>()
    fun pushUndo(clips: List<Clip>) { if (clips.isNotEmpty()) { undoStack.addLast(clips); while (undoStack.size > 30) undoStack.removeFirst() }; redoStack.clear() }
    fun undo(current: List<Clip>): List<Clip>? { if (undoStack.isEmpty()) return null; redoStack.addLast(current); return undoStack.removeLast() }
    fun redo(current: List<Clip>): List<Clip>? { if (redoStack.isEmpty()) return null; undoStack.addLast(current); return redoStack.removeLast() }
}
