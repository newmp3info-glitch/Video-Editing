package com.rakibul.videostudio

object TimelineEngine {
    fun totalDuration(clips: List<Clip>): Long = clips.sumOf { it.editDurationMs }
    fun durationBefore(clips: List<Clip>, index: Int): Long = clips.take(index.coerceAtLeast(0)).sumOf { it.editDurationMs }
    fun clipAt(clips: List<Clip>, positionMs: Long): Int { var left=positionMs.coerceAtLeast(0); for(i in clips.indices){ if(left <= clips[i].editDurationMs || i==clips.lastIndex) return i; left-=clips[i].editDurationMs }; return 0 }
    fun globalToLocal(clips: List<Clip>, positionMs: Long): Pair<Int,Long> { val i=clipAt(clips,positionMs); return i to (positionMs-durationBefore(clips,i)).coerceAtLeast(0) }
}
