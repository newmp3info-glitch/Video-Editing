package com.rakibul.videostudio

import android.content.ContentResolver
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.audio.ChannelMixingAudioProcessor
import androidx.media3.common.audio.ChannelMixingMatrix
import androidx.media3.common.audio.SpeedProvider
import androidx.media3.effect.Brightness
import androidx.media3.effect.CanvasOverlay
import androidx.media3.effect.Contrast
import androidx.media3.effect.Crop
import androidx.media3.transformer.Effects
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.Presentation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.Transformer
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private const val BRAND = "Rakibul Video Studio"

@OptIn(UnstableApi::class, ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { RakibulEditor() }
    }
}

data class Clip(
    val id: Long,
    val uri: Uri,
    val name: String,
    val durationMs: Long,
    val trimStartMs: Long = 0,
    val trimEndMs: Long = durationMs,
    val speed: Float = 1f,
    val volume: Float = 1f,
    val effect: String = "None",
    val effectIntensity: Float = 0.7f,
    val crop: String = "9:16"
) {
    val editDurationMs: Long get() = max(1L, ((trimEndMs - trimStartMs) / speed).toLong())
}

data class AudioTrack(
    val id: Long,
    val uri: Uri,
    val name: String,
    val durationMs: Long,
    val volume: Float = 1f,
    val startMs: Long = 0L
)

data class Sticker(
    val id: Long,
    val emoji: String,
    val label: String,
    val atMs: Long,
    val durationMs: Long = 1800L,
    val x: Float = 0.5f,
    val y: Float = 0.78f,
    val scale: Float = 1f,
    val animation: String = "Pop"
)

enum class Tool(val label: String, val icon: @Composable () -> Unit) {
    EDIT("Edit", { Icon(Icons.Default.ContentCut, null) }),
    AUDIO("Audio", { Icon(Icons.Default.AudioFile, null) }),
    TEXT("Text", { Icon(Icons.Default.TextFields, null) }),
    STICKERS("Stickers", { Text("✨", fontSize = 20.sp) }),
    EFFECTS("Effects", { Icon(Icons.Default.AutoFixHigh, null) }),
    OVERLAY("Overlay", { Icon(Icons.Default.Layers, null) }),
    CAPTIONS("Captions", { Icon(Icons.Default.TextFields, null) }),
    FILTERS("Filters", { Icon(Icons.Default.FilterVintage, null) }),
    ADJUST("Adjust", { Icon(Icons.Default.GraphicEq, null) })
}

private val stickerCatalog = listOf(
    "❤️" to "Like", "👍" to "Like Now", "🔔" to "Subscribe", "❤️‍🔥" to "Follow", "👇" to "Click Here",
    "🔥" to "Fire", "😂" to "LOL", "😍" to "Love", "👏" to "Clap", "🎉" to "Party", "💯" to "100",
    "⭐" to "Star", "✨" to "Sparkle", "💥" to "Boom", "⚡" to "Lightning", "🚀" to "Go", "💬" to "Comment",
    "📢" to "Share", "🙏" to "Thanks", "😱" to "Wow", "🥳" to "Celebrate", "😎" to "Cool", "🎯" to "Goal"
)

@OptIn(UnstableApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RakibulEditor() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var clips by remember { mutableStateOf<List<Clip>>(emptyList()) }
    var selectedIndex by remember { mutableIntStateOf(0) }
    var audioTracks by remember { mutableStateOf<List<AudioTrack>>(emptyList()) }
    var captions by remember { mutableStateOf<List<Sticker>>(emptyList()) }
    var stickers by remember { mutableStateOf<List<Sticker>>(emptyList()) }
    var crop by remember { mutableStateOf("9:16") }
    var tool by remember { mutableStateOf(Tool.EDIT) }
    var sheetOpen by remember { mutableStateOf(false) }
    var exportMessage by remember { mutableStateOf("") }
    var showExport by remember { mutableStateOf(false) }
    var showTextDialog by remember { mutableStateOf(false) }
    var undoStack by remember { mutableStateOf<List<List<Clip>>>(emptyList()) }
    var redoStack by remember { mutableStateOf<List<List<Clip>>>(emptyList()) }
    var globalPosition by remember { mutableLongStateOf(0L) }
    var isPlaying by remember { mutableStateOf(false) }
    var thumbnails by remember { mutableStateOf<Map<Long, List<ImageBitmap>>>(emptyMap()) }
    var panelEffect by remember { mutableStateOf("None") }
    var effectIntensity by remember { mutableFloatStateOf(0.7f) }
    var brightness by remember { mutableFloatStateOf(0f) }
    var contrast by remember { mutableFloatStateOf(1f) }
    var speed by remember { mutableFloatStateOf(1f) }
    var volume by remember { mutableFloatStateOf(1f) }

    fun pushUndo() {
        if (clips.isNotEmpty()) undoStack = (undoStack + listOf(clips)).takeLast(30)
        redoStack = emptyList()
    }

    val player = remember { ExoPlayer.Builder(context).build().apply { repeatMode = Player.REPEAT_MODE_OFF } }
    val audioPlayers = remember { mutableStateMapOf<Long, ExoPlayer>() }

    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                if (clips.isNotEmpty()) {
                    selectedIndex = player.currentMediaItemIndex.coerceIn(0, clips.lastIndex)
                    val c = clips[selectedIndex]
                    player.setPlaybackParameters(PlaybackParameters(c.speed))
                    player.volume = c.volume
                }
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
            audioPlayers.values.forEach { it.release() }
            audioPlayers.clear()
        }
    }

    LaunchedEffect(clips, selectedIndex) {
        if (clips.isEmpty()) {
            player.clearMediaItems()
            return@LaunchedEffect
        }
        val wasPlaying = isPlaying
        val items = clips.map { c ->
            MediaItem.Builder().setUri(c.uri).setClippingConfiguration(
                MediaItem.ClippingConfiguration.Builder().setStartPositionMs(c.trimStartMs).setEndPositionMs(c.trimEndMs).build()
            ).build()
        }
        val index = selectedIndex.coerceIn(0, clips.lastIndex)
        player.setMediaItems(items, index, 0L)
        player.prepare()
        player.playWhenReady = wasPlaying
        player.setPlaybackParameters(PlaybackParameters(clips[index].speed))
        player.volume = clips[index].volume
    }

    LaunchedEffect(player) {
        while (true) {
            if (player.playbackState != Player.STATE_IDLE && clips.isNotEmpty()) {
                val base = clips.take(player.currentMediaItemIndex.coerceAtLeast(0)).sumOf { it.editDurationMs }
                globalPosition = base + player.currentPosition
            }
            delay(60)
        }
    }

    LaunchedEffect(clips) {
        if (clips.isEmpty()) return@LaunchedEffect
        thumbnails = withContext(Dispatchers.IO) {
            val out = thumbnails.toMutableMap()
            clips.forEach { if (!out.containsKey(it.id)) out[it.id] = makeThumbnails(context, it.uri, it.durationMs) }
            out
        }
    }

    LaunchedEffect(audioTracks, isPlaying) {
        val activeIds = audioTracks.map { it.id }.toSet()
        audioPlayers.keys.toList().filterNot { it in activeIds }.forEach { id -> audioPlayers.remove(id)?.release() }
        audioTracks.forEach { track ->
            val p = audioPlayers.getOrPut(track.id) { ExoPlayer.Builder(context).build() }
            p.volume = track.volume
            if (p.mediaItemCount == 0) {
                p.setMediaItem(MediaItem.fromUri(track.uri))
                p.repeatMode = Player.REPEAT_MODE_ONE
                p.prepare()
            }
            if (isPlaying) p.play() else p.pause()
        }
    }

    fun updateClip(index: Int, newClip: Clip) {
        if (index !in clips.indices) return
        pushUndo()
        clips = clips.toMutableList().also { it[index] = newClip }
    }

    fun splitAtPlayhead() {
        if (clips.isEmpty()) return
        val idx = selectedIndex.coerceIn(0, clips.lastIndex)
        val before = clips.take(idx).sumOf { it.editDurationMs }
        val local = (globalPosition - before).coerceAtLeast(0L)
        val c = clips[idx]
        val sourceCut = c.trimStartMs + (local * c.speed).toLong()
        if (sourceCut <= c.trimStartMs + 250 || sourceCut >= c.trimEndMs - 250) return
        pushUndo()
        val a = c.copy(id = System.nanoTime(), trimEndMs = sourceCut)
        val b = c.copy(id = System.nanoTime() + 1, trimStartMs = sourceCut)
        clips = clips.toMutableList().also { it.removeAt(idx); it.add(idx, a); it.add(idx + 1, b) }
        selectedIndex = idx
    }

    fun deleteSelected() {
        if (clips.isEmpty()) return
        pushUndo()
        clips = clips.toMutableList().also { it.removeAt(selectedIndex.coerceIn(0, it.lastIndex)) }
        selectedIndex = selectedIndex.coerceAtMost((clips.lastIndex).coerceAtLeast(0))
    }

    fun moveClip(from: Int, to: Int) {
        if (from !in clips.indices || to !in clips.indices || from == to) return
        pushUndo()
        clips = clips.toMutableList().also { val item = it.removeAt(from); it.add(to, item) }
        selectedIndex = to
    }

    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) scope.launch {
            val wasEmpty = clips.isEmpty()
            val added = uris.mapIndexed { i, uri ->
                runCatching { context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                Clip(System.nanoTime() + i, uri, displayName(context.contentResolver, uri), durationOf(context.contentResolver, uri))
            }
            pushUndo(); clips = clips + added
            if (wasEmpty) { selectedIndex = 0; isPlaying = true }
        }
    }

    val audioLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) {
            audioTracks = audioTracks + uris.mapIndexed { i, uri ->
                runCatching { context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                AudioTrack(System.nanoTime() + i, uri, displayName(context.contentResolver, uri), durationOf(context.contentResolver, uri))
            }
        }
    }

    fun addSticker(emoji: String, label: String) {
        stickers = stickers + Sticker(System.nanoTime(), emoji, label, globalPosition)
    }

    Surface(color = Color(0xFF070707), modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopBar(
                onExport = { showExport = true },
                onUndo = { if (undoStack.isNotEmpty()) { redoStack = redoStack + listOf(clips); clips = undoStack.last(); undoStack = undoStack.dropLast(1) } },
                onRedo = { if (redoStack.isNotEmpty()) { undoStack = undoStack + listOf(clips); clips = redoStack.last(); redoStack = redoStack.dropLast(1) } }
            )
            PreviewArea(
                player = player,
                lightning = panelEffect == "Lightning",
                filter = panelEffect,
                intensity = effectIntensity,
                stickers = stickers,
                captions = captions,
                globalPosition = globalPosition,
                onPlay = { if (clips.isEmpty()) videoLauncher.launch(arrayOf("video/*")) else if (isPlaying) player.pause() else player.play() }
            )
            Timeline(
                clips = clips, audio = audioTracks, thumbnails = thumbnails, selectedIndex = selectedIndex, globalPosition = globalPosition,
                stickers = stickers, onSelect = { selectedIndex = it },
                onTrim = { idx, start, end -> if (idx in clips.indices) { val c = clips[idx]; clips = clips.toMutableList().also { it[idx] = c.copy(trimStartMs = start, trimEndMs = end) } } },
                onMove = ::moveClip, onAdd = { videoLauncher.launch(arrayOf("video/*")) }, onAddAudio = { audioLauncher.launch(arrayOf("audio/*")) },
                onDeleteAudio = { id -> audioTracks = audioTracks.filterNot { it.id == id } },
                onDeleteSticker = { id -> stickers = stickers.filterNot { it.id == id } }
            )
            Spacer(Modifier.weight(1f))
            BottomTools(selected = tool) { tool = it; sheetOpen = true }
        }
    }

    if (sheetOpen) {
        ModalBottomSheet(onDismissRequest = { sheetOpen = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = Color(0xFF171717)) {
            ToolPanel(
                tool = tool, crop = crop, onCrop = { crop = it }, effect = panelEffect, intensity = effectIntensity,
                onEffect = { panelEffect = it; if (clips.isNotEmpty()) updateClip(selectedIndex, clips[selectedIndex].copy(effect = it, effectIntensity = effectIntensity)) },
                onIntensity = { effectIntensity = it }, brightness = brightness, contrast = contrast,
                onBrightness = { brightness = it }, onContrast = { contrast = it }, speed = speed, onSpeed = { v -> speed = v; if (clips.isNotEmpty()) updateClip(selectedIndex, clips[selectedIndex].copy(speed = v)); player.setPlaybackParameters(PlaybackParameters(v)) },
                volume = volume, onVolume = { v -> volume = v; if (clips.isNotEmpty()) updateClip(selectedIndex, clips[selectedIndex].copy(volume = v)); player.volume = v },
                onSplit = { splitAtPlayhead(); sheetOpen = false }, onDelete = { deleteSelected(); sheetOpen = false }, onAddAudio = { audioLauncher.launch(arrayOf("audio/*")); sheetOpen = false },
                onAddSticker = ::addSticker, onAddText = { showTextDialog = true }, captions = captions,
                onAddCaption = { captions = captions + Sticker(System.nanoTime(), "💬", "Caption", globalPosition, 2200L, .5f, .68f, 0.9f, "Type") },
                onClose = { sheetOpen = false }, audioCount = audioTracks.size
            )
        }
    }

    if (showTextDialog) {
        var text by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest = { showTextDialog = false }, title = { Text("Add animated text") }, text = { androidx.compose.material3.OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Text") }) }, confirmButton = {
            TextButton(onClick = { if (text.isNotBlank()) { captions = captions + Sticker(System.nanoTime(), "", text, globalPosition, 2200L, .5f, .62f, 0.9f, "Type"); showTextDialog = false } }) { Text("Add") }
        })
    }

    if (showExport) {
        ExportDialog(onDismiss = { showExport = false }, onExport = { quality ->
            showExport = false; exportMessage = "Exporting $quality…"
            exportProject(context, clips, audioTracks, stickers + captions, crop, brightness, contrast, quality) { _, msg -> exportMessage = msg; scope.launch { delay(4500); exportMessage = "" } }
        })
    }
}

@Composable
private fun TopBar(onExport: () -> Unit, onUndo: () -> Unit, onRedo: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(BRAND, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
        IconButton(onClick = onUndo) { Icon(Icons.Default.Undo, null, tint = Color.LightGray) }
        IconButton(onClick = onRedo) { Icon(Icons.Default.Redo, null, tint = Color.LightGray) }
        Button(onClick = onExport, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBFF5FF), contentColor = Color.Black), shape = RoundedCornerShape(7.dp)) { Icon(Icons.Default.SaveAlt, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(5.dp)); Text("Export") }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun PreviewArea(player: ExoPlayer, lightning: Boolean, filter: String, intensity: Float, stickers: List<Sticker>, captions: List<Sticker>, globalPosition: Long, onPlay: () -> Unit) {
    val context = LocalContext.current
    var pos by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) { while (true) { pos = player.currentPosition; delay(50) } }
    val activeStickers = (stickers + captions).filter { globalPosition in it.atMs..(it.atMs + it.durationMs) }
    val lightningHit = lightning && pos % 2300L < 110L
    Box(Modifier.fillMaxWidth().padding(horizontal = 10.dp).aspectRatio(0.60f).clip(RoundedCornerShape(12.dp)).background(Color.Black)) {
        AndroidView(factory = { PlayerView(context).apply { useController = false; resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT; this.player = player; layoutParams = ViewGroup.LayoutParams(-1, -1) } }, modifier = Modifier.fillMaxSize())
        if (filter == "Vivid") Box(Modifier.fillMaxSize().background(Color(0x1838FFB8)))
        if (filter == "Warm") Box(Modifier.fillMaxSize().background(Color(0x18FF9A4D)))
        if (filter == "Cool") Box(Modifier.fillMaxSize().background(Color(0x183D8DFF)))
        if (filter == "Mono") Box(Modifier.fillMaxSize().background(Color(0x44505050)))
        activeStickers.forEach { AnimatedSticker(it, globalPosition) }
        if (lightningHit) LightningOverlay(intensity)
        IconButton(onClick = onPlay, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp).background(Color(0x99000000), RoundedCornerShape(50))) { if (player.isPlaying) Icon(Icons.Default.Pause, null, tint = Color.White) else Icon(Icons.Default.PlayArrow, null, tint = Color.White) }
    }
}

@Composable
private fun AnimatedSticker(sticker: Sticker, position: Long) {
    val transition = rememberInfiniteTransition(label = "sticker")
    val pulse by transition.animateFloat(0.92f, 1.08f, infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse")
    val local = ((position - sticker.atMs).coerceAtLeast(0L)).toFloat() / sticker.durationMs.coerceAtLeast(1L)
    val entrance = (local / 0.18f).coerceIn(0f, 1f)
    val alpha = if (local > .86f) ((1f - local) / .14f).coerceIn(0f, 1f) else entrance
    val scale = sticker.scale * (if (sticker.animation == "Bounce") pulse else (0.72f + 0.28f * entrance))
    Box(Modifier.fillMaxSize().graphicsLayer { translationX = (sticker.x - .5f) * 280f; translationY = (sticker.y - .5f) * 480f; scaleX = scale; scaleY = scale; rotationZ = if (sticker.animation == "Wiggle") kotlin.math.sin(local * 18.0).toFloat() * 5f else 0f; this.alpha = alpha }, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (sticker.emoji.isNotEmpty()) Text(sticker.emoji, fontSize = 48.sp)
            if (sticker.label.isNotBlank()) Text(sticker.label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(Color(0xAA000000), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 3.dp))
        }
    }
}

@Composable
private fun LightningOverlay(intensity: Float) {
    androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
        drawRect(Color.White.copy(alpha = 0.25f + .55f * intensity))
        val w = size.width; val h = size.height
        val path = Path().apply { moveTo(.60f*w, 0f); lineTo(.48f*w, .18f*h); lineTo(.64f*w, .31f*h); lineTo(.43f*w, .53f*h); lineTo(.55f*w, .69f*h); lineTo(.35f*w, h) }
        drawPath(path, Color.White, style = Stroke(width = 5.dp.toPx()))
        drawPath(path, Color(0xFFB9F6FF), style = Stroke(width = 2.dp.toPx()))
    }
}

@Composable
private fun Timeline(
    clips: List<Clip>, audio: List<AudioTrack>, thumbnails: Map<Long, List<ImageBitmap>>, selectedIndex: Int, globalPosition: Long, stickers: List<Sticker>,
    onSelect: (Int) -> Unit, onTrim: (Int, Long, Long) -> Unit, onMove: (Int, Int) -> Unit, onAdd: () -> Unit, onAddAudio: () -> Unit,
    onDeleteAudio: (Long) -> Unit, onDeleteSticker: (Long) -> Unit
) {
    val total = clips.sumOf { it.editDurationMs }.coerceAtLeast(1L)
    val scroll = rememberScrollState()
    Column(Modifier.fillMaxWidth().background(Color(0xFF111111)).padding(vertical = 7.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) { Text(formatMs(globalPosition), color = Color.LightGray, fontSize = 11.sp); Spacer(Modifier.weight(1f)); Text(formatMs(total), color = Color.Gray, fontSize = 11.sp) }
        Box(Modifier.fillMaxWidth().horizontalScroll(scroll).padding(horizontal = 8.dp)) {
            Row(Modifier.height(90.dp), verticalAlignment = Alignment.CenterVertically) {
                clips.forEachIndexed { idx, clip ->
                    val width = (clip.editDurationMs / 115L).coerceIn(80L, 330L).toInt().dp
                    ClipBlock(clip, width, idx == selectedIndex, thumbnails[clip.id].orEmpty(), { onSelect(idx) }, { s,e -> onTrim(idx,s,e) })
                }
                Box(Modifier.width(66.dp).fillMaxHeight().padding(4.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF242424)).clickable(onClick = onAdd), contentAlignment = Alignment.Center) { Icon(Icons.Default.Add, null, tint = Color.White) }
            }
        }
        if (audio.isNotEmpty()) {
            audio.forEach { track ->
                Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 2.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFF181818)).padding(5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AudioFile, null, tint = Color(0xFF2DE1C2), modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text(track.name, color = Color.LightGray, fontSize = 10.sp, maxLines = 1, modifier = Modifier.weight(1f)); Text("AUDIO", color = Color.Gray, fontSize = 8.sp); IconButton(onClick = { onDeleteAudio(track.id) }) { Icon(Icons.Default.Delete, null, tint = Color.Gray, modifier = Modifier.size(16.dp)) }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 3.dp).clip(RoundedCornerShape(6.dp)).clickable(onClick = onAddAudio).background(Color(0xFF1A1A1A)).padding(8.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.AudioFile, null, tint = Color.Gray, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("+ Add music / sound effect", color = Color.Gray, fontSize = 11.sp) }
        if (stickers.isNotEmpty()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) { Text("STICKERS", color = Color.Gray, fontSize = 8.sp); Spacer(Modifier.width(8.dp)); stickers.takeLast(5).forEach { s -> Text(if (s.emoji.isNotEmpty()) s.emoji else "T", fontSize = 17.sp, modifier = Modifier.padding(horizontal = 3.dp).clickable { onDeleteSticker(s.id) }) } }
        }
        Box(Modifier.fillMaxWidth().height(3.dp)) { val frac = (globalPosition.toFloat()/total.toFloat()).coerceIn(0f,1f); Box(Modifier.fillMaxHeight().fillMaxWidth(frac).background(Color(0xFF2DE1C2))) }
    }
}

@Composable
private fun ClipBlock(clip: Clip, width: androidx.compose.ui.unit.Dp, selected: Boolean, thumbs: List<ImageBitmap>, onClick: () -> Unit, onTrim: (Long, Long) -> Unit) {
    var leftDrag by remember { mutableFloatStateOf(0f) }
    var rightDrag by remember { mutableFloatStateOf(0f) }
    val totalSource = clip.durationMs.coerceAtLeast(1L)
    Row(Modifier.width(width).fillMaxHeight().padding(3.dp).clip(RoundedCornerShape(6.dp)).border(if (selected) 2.dp else 1.dp, if (selected) Color(0xFF2DE1C2) else Color(0xFF363636), RoundedCornerShape(6.dp))) {
        Box(Modifier.width(13.dp).fillMaxHeight().background(if (selected) Color(0xFF2DE1C2) else Color(0xFF333333)).pointerInput(clip) { detectDragGestures { change, drag -> change.consume(); leftDrag += drag.x; val delta=(leftDrag/width.value*totalSource).toLong(); onTrim((clip.trimStartMs+delta).coerceIn(0L,clip.trimEndMs-250L),clip.trimEndMs); leftDrag=0f } })
        Box(Modifier.weight(1f).fillMaxHeight().clickable(onClick = onClick)) {
            if (thumbs.isNotEmpty()) Row(Modifier.fillMaxSize()) { thumbs.forEach { Image(it, null, Modifier.weight(1f).fillMaxHeight(), contentScale = ContentScale.Crop) } } else Box(Modifier.fillMaxSize().background(Color(0xFF272727)), contentAlignment = Alignment.Center) { Text(clip.name.take(14), color = Color.White, fontSize = 9.sp) }
            Column(Modifier.align(Alignment.BottomStart).padding(3.dp)) { Text(clip.name.take(18), color = Color.White, fontSize = 8.sp, maxLines = 1); Text("${clip.speed}x • ${clip.effect}", color = Color.White.copy(alpha=.75f), fontSize = 7.sp) }
        }
        Box(Modifier.width(13.dp).fillMaxHeight().background(if (selected) Color(0xFF2DE1C2) else Color(0xFF333333)).pointerInput(clip) { detectDragGestures { change, drag -> change.consume(); rightDrag += drag.x; val delta=(rightDrag/width.value*totalSource).toLong(); onTrim(clip.trimStartMs,(clip.trimEndMs+delta).coerceIn(clip.trimStartMs+250L,totalSource)); rightDrag=0f } })
    }
}

@Composable
private fun BottomTools(selected: Tool, onSelect: (Tool) -> Unit) {
    Row(Modifier.fillMaxWidth().background(Color(0xFF171717)).padding(top=6.dp,bottom=8.dp).horizontalScroll(rememberScrollState()), horizontalArrangement=Arrangement.spacedBy(4.dp)) {
        Tool.values().forEach { tool -> val active=tool==selected; Column(Modifier.width(68.dp).clickable { onSelect(tool) }.padding(vertical=3.dp), horizontalAlignment=Alignment.CenterHorizontally) { Box(Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(if(active) Color(0xFF253A39) else Color.Transparent),contentAlignment=Alignment.Center) { tool.icon() }; Text(tool.label,color=if(active) Color(0xFF2DE1C2) else Color.LightGray,fontSize=8.sp) } }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun ToolPanel(
    tool: Tool, crop: String, onCrop: (String)->Unit, effect: String, intensity: Float, onEffect:(String)->Unit, onIntensity:(Float)->Unit,
    brightness:Float, contrast:Float, onBrightness:(Float)->Unit, onContrast:(Float)->Unit, speed:Float, onSpeed:(Float)->Unit, volume:Float, onVolume:(Float)->Unit,
    onSplit:()->Unit, onDelete:()->Unit, onAddAudio:()->Unit, onAddSticker:(String,String)->Unit, onAddText:()->Unit, captions:List<Sticker>, onAddCaption:()->Unit, onClose:()->Unit, audioCount:Int
) {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically){ Text(tool.label,color=Color.White,fontWeight=FontWeight.Bold,fontSize=20.sp,modifier=Modifier.weight(1f)); IconButton(onClick=onClose){Icon(Icons.Default.Close,null,tint=Color.White)} }
        Spacer(Modifier.height(8.dp))
        when(tool){
            Tool.EDIT->{ ActionRow("Split at playhead",Icons.Default.ContentCut,onSplit); ActionRow("Delete selected clip",Icons.Default.Delete,onDelete); SliderLine("Speed",speed,.25f,4f,onSpeed,"x"); SliderLine("Clip volume",volume,0f,1.5f,onVolume); Text("Drag both trim handles directly on the timeline.",color=Color.Gray,fontSize=12.sp) }
            Tool.AUDIO->{ ActionRow("Add music / SFX",Icons.Default.AudioFile,onAddAudio); Text("Audio tracks: $audioCount",color=Color.Gray,fontSize=12.sp); SliderLine("Selected clip volume",volume,0f,1.5f,onVolume); Text("Multiple audio tracks are mixed during export.",color=Color.Gray,fontSize=12.sp) }
            Tool.TEXT->{ ActionRow("Add animated text",Icons.Default.TextFields,onAddText); Text("Text appears as an animated timeline overlay.",color=Color.Gray,fontSize=12.sp) }
            Tool.STICKERS->{ Text("Animated emoji / CTA stickers",color=Color.White,fontWeight=FontWeight.SemiBold); Spacer(Modifier.height(6.dp)); LazyRow(horizontalArrangement=Arrangement.spacedBy(8.dp)){items(stickerCatalog){(emoji,label)->Column(Modifier.width(76.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF242424)).clickable{onAddSticker(emoji,label)}.padding(7.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(emoji,fontSize=30.sp);Text(label,color=Color.White,fontSize=9.sp,textAlign=TextAlign.Center)}}}; Spacer(Modifier.height(8.dp)); Text("Tap any sticker to add it at the playhead. Stickers pop, bounce or wiggle while playing and are included in export.",color=Color.Gray,fontSize=12.sp) }
            Tool.EFFECTS->{ EffectGrid(effect,onEffect); SliderLine("Effect intensity",intensity,0f,1f,onIntensity); Text("Lightning is an instant electric flash, not a long overlay.",color=Color.Gray,fontSize=12.sp) }
            Tool.OVERLAY->{ ActionRow("Add text / emoji overlay",Icons.Default.Layers,onAddText); Text("Photo editing is intentionally excluded. Video overlays only.",color=Color.Gray,fontSize=12.sp) }
            Tool.CAPTIONS->{ ActionRow("Add animated caption",Icons.Default.TextFields,onAddCaption); captions.takeLast(6).forEach{Text("${formatMs(it.atMs)}  ${if(it.emoji.isBlank())it.label else it.emoji+" "+it.label}",color=Color.LightGray,fontSize=12.sp,modifier=Modifier.padding(5.dp))} }
            Tool.FILTERS->{ EffectGrid(effect,onEffect,true); SliderLine("Filter strength",intensity,0f,1f,onIntensity) }
            Tool.ADJUST->{ SliderLine("Brightness",brightness,-1f,1f,onBrightness); SliderLine("Contrast",contrast,.5f,1.8f,onContrast) }
        }
        Spacer(Modifier.height(14.dp)); Text("Crop",color=Color.White,fontWeight=FontWeight.SemiBold); Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(7.dp)){listOf("16:9","9:16","1:1","4:5","4:3").forEach{p->FilterChip(p,p==crop){onCrop(p)}}}
        Spacer(Modifier.height(20.dp))
    }
}

@Composable private fun ActionRow(label:String,icon:androidx.compose.ui.graphics.vector.ImageVector,onClick:()->Unit){Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable(onClick=onClick).background(Color(0xFF242424)).padding(12.dp),verticalAlignment=Alignment.CenterVertically){Icon(icon,null,tint=Color(0xFF2DE1C2));Spacer(Modifier.width(10.dp));Text(label,color=Color.White,fontSize=14.sp)};Spacer(Modifier.height(8.dp))}
@Composable private fun SliderLine(label:String,value:Float,min:Float,max:Float,onValue:(Float)->Unit,suffix:String=""){Column(Modifier.fillMaxWidth().padding(vertical=5.dp)){Row{Text(label,color=Color.LightGray,fontSize=12.sp);Spacer(Modifier.weight(1f));Text("${"%.2f".format(Locale.US,value)}$suffix",color=Color(0xFF2DE1C2),fontSize=12.sp)};Slider(value=value,onValueChange=onValue,valueRange=min..max,colors=SliderDefaults.colors(thumbColor=Color(0xFF2DE1C2),activeTrackColor=Color(0xFF2DE1C2)))}}
@Composable private fun FilterChip(label:String,active:Boolean,onClick:()->Unit){Box(Modifier.clip(RoundedCornerShape(9.dp)).border(1.dp,if(active)Color(0xFF2DE1C2)else Color(0xFF3A3A3A),RoundedCornerShape(9.dp)).clickable(onClick=onClick).padding(horizontal=13.dp,vertical=8.dp)){Text(label,color=if(active)Color(0xFF2DE1C2)else Color.LightGray,fontSize=11.sp)}}
@Composable private fun EffectGrid(selected:String,onSelect:(String)->Unit,filtersOnly:Boolean=false){val effects=if(filtersOnly)listOf("None","Vivid","Warm","Cool","Mono","Vintage")else listOf("None","Lightning","Flash Shake","Blur","Vivid","Warm","Cool","Mono","Vintage");LazyRow(horizontalArrangement=Arrangement.spacedBy(8.dp)){items(effects){e->Column(Modifier.width(82.dp).clip(RoundedCornerShape(10.dp)).border(1.dp,if(e==selected)Color(0xFF2DE1C2)else Color(0xFF343434),RoundedCornerShape(10.dp)).clickable{onSelect(e)}.padding(7.dp),horizontalAlignment=Alignment.CenterHorizontally){Box(Modifier.fillMaxWidth().height(58.dp).clip(RoundedCornerShape(8.dp)).background(effectColor(e)),contentAlignment=Alignment.Center){if(e=="Lightning")Icon(Icons.Default.FlashOn,null,tint=Color.White,modifier=Modifier.size(32.dp))else Text(e.take(3),color=Color.White,fontWeight=FontWeight.Bold)};Text(e,color=Color.White,fontSize=9.sp,textAlign=TextAlign.Center,modifier=Modifier.padding(top=5.dp))}}}}
private fun effectColor(name:String):Color=when(name){"Lightning"->Color(0xFF263C4A);"Flash Shake"->Color(0xFF7A7A7A);"Blur"->Color(0xFF4E4A55);"Vivid"->Color(0xFF2D6A58);"Warm"->Color(0xFF7A4930);"Cool"->Color(0xFF315A77);"Mono"->Color(0xFF565656);"Vintage"->Color(0xFF65523B);else->Color(0xFF303030)}

@Composable private fun ExportDialog(onDismiss:()->Unit,onExport:(String)->Unit){AlertDialog(onDismissRequest=onDismiss,title={Text("Export video")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){Text("Video + multiple audio tracks + emoji/text overlays are rendered into MP4.");listOf("720p","1080p","4K").forEach{q->OutlinedButton(onClick={onExport(q)},modifier=Modifier.fillMaxWidth()){Text(q)}}}},confirmButton={TextButton(onClick=onDismiss){Text("Cancel")}})}

private fun seekGlobal(player:ExoPlayer,clips:List<Clip>,globalMs:Long){var left=globalMs.coerceAtLeast(0L);for((i,c)in clips.withIndex()){val d=c.editDurationMs;if(left<=d||i==clips.lastIndex){player.seekTo(i,(left*c.speed).toLong().coerceIn(0L,c.trimEndMs-c.trimStartMs)+c.trimStartMs);return};left-=d}}
private fun makeThumbnails(context:Context,uri:Uri,duration:Long):List<ImageBitmap>{val r=android.media.MediaMetadataRetriever();return try{r.setDataSource(context,uri);(0 until 8).mapNotNull{i->val t=if(duration<=0)0L else duration*i/7L;r.getFrameAtTime(t*1000L,android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC)?.asImageBitmap()}}catch(_:Exception){emptyList()}finally{r.release()}}
private fun durationOf(resolver:ContentResolver,uri:Uri):Long=try{resolver.openAssetFileDescriptor(uri,"r")?.use{afd->val r=android.media.MediaMetadataRetriever();r.setDataSource(afd.fileDescriptor);val d=r.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()?:0L;r.release();d}?:0L}catch(_:Exception){0L}
private fun displayName(resolver:ContentResolver,uri:Uri):String=try{resolver.query(uri,arrayOf(OpenableColumns.DISPLAY_NAME),null,null,null)?.use{c->if(c.moveToFirst())c.getString(0)else uri.lastPathSegment?:"Media"}?:uri.lastPathSegment?:"Media"}catch(_:Exception){uri.lastPathSegment?:"Media"}
private fun formatMs(ms:Long):String{val s=(ms/1000).coerceAtLeast(0L);return String.format(Locale.US,"%02d:%02d",s/60,s%60)}

@UnstableApi
private class AnimatedStickerOverlay(private val sticker:Sticker):CanvasOverlay(true){
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply{typeface=Typeface.DEFAULT_BOLD;textAlign=Paint.Align.CENTER}
    override fun onDraw(canvas:Canvas,presentationTimeUs:Long){val ms=presentationTimeUs/1000L;val local=ms-sticker.atMs;if(local<0||local>sticker.durationMs)return;val f=local.toFloat()/sticker.durationMs.coerceAtLeast(1L);val entrance=(f/.18f).coerceIn(0f,1f);val exit=if(f>.86f)((1f-f)/.14f).coerceIn(0f,1f)else 1f;val alpha=(255f*min(entrance,exit)).toInt();val animScale=if(sticker.animation=="Bounce"){1f+.10f*kotlin.math.sin(f*18.0)}else .72+.28*entrance;val frameWidth=canvas.width.toFloat();val frameHeight=canvas.height.toFloat();val size=(frameHeight*.075f*sticker.scale*animScale).toFloat();paint.textSize=size;paint.alpha=alpha;val x=frameWidth*sticker.x;val y=frameHeight*sticker.y;canvas.save();canvas.rotate(if(sticker.animation=="Wiggle")kotlin.math.sin(f*18.0).toFloat()*5f else 0f,x,y);if(sticker.emoji.isNotBlank()){canvas.drawText(sticker.emoji,x,y,paint);};if(sticker.label.isNotBlank()){paint.textSize=frameHeight*.022f;paint.color=android.graphics.Color.WHITE;canvas.drawText(sticker.label,x,y+size*.55f,paint)};canvas.restore()}
}

@UnstableApi
private fun buildStickerEffects(stickers:List<Sticker>):androidx.media3.common.Effect?=if(stickers.isEmpty())null else OverlayEffect(stickers.map{AnimatedStickerOverlay(it)})

@OptIn(UnstableApi::class)
private fun exportProject(context:Context,clips:List<Clip>,audio:List<AudioTrack>,stickers:List<Sticker>,crop:String,brightness:Float,contrast:Float,quality:String,callback:(Boolean,String)->Unit){
    if(clips.isEmpty()){callback(false,"Import a video first.");return}
    val outDir=File(context.getExternalFilesDir(null),"exports").apply{mkdirs()};val stamp=SimpleDateFormat("yyyyMMdd_HHmmss",Locale.US).format(Date());val out=File(outDir,"Rakibul_$stamp.mp4")
    val edited=clips.map{clip->
        val media=MediaItem.Builder().setUri(clip.uri).setClippingConfiguration(MediaItem.ClippingConfiguration.Builder().setStartPositionMs(clip.trimStartMs).setEndPositionMs(clip.trimEndMs).build()).build()
        val effects=mutableListOf<androidx.media3.common.Effect>()
        if(abs(brightness)>.01f)effects+=Brightness(brightness.coerceIn(-1f,1f))
        if(abs(contrast-1f)>.01f)effects+=Contrast(contrast.coerceIn(.5f,2f))
        cropEffect(crop)?.let{effects+=it};presentationEffect(quality,crop)?.let{effects+=it}
        val builder=EditedMediaItem.Builder(media).setEffects(Effects(emptyList(),effects))
        if(clip.speed!=1f)builder.setSpeed(object:SpeedProvider{override fun getSpeed(presentationTimeUs:Long)=clip.speed;override fun getNextSpeedChangeTimeUs(timeUs:Long)=androidx.media3.common.C.TIME_UNSET})
        builder.build()
    }
    val videoSeq=EditedMediaItemSequence.withAudioAndVideoFrom(edited)
    val sequences=mutableListOf(videoSeq)
    audio.forEach{track->
        val processors=mutableListOf<androidx.media3.common.audio.AudioProcessor>()
        if(track.volume!=1f){val p=ChannelMixingAudioProcessor();p.putChannelMixingMatrix(ChannelMixingMatrix(1,1,floatArrayOf(track.volume.coerceIn(0f,2f))));p.putChannelMixingMatrix(ChannelMixingMatrix(2,2,floatArrayOf(track.volume.coerceIn(0f,2f),0f,0f,track.volume.coerceIn(0f,2f))));processors+=p}
        val a=EditedMediaItem.Builder(MediaItem.fromUri(track.uri)).setRemoveVideo(true).setEffects(Effects(processors,emptyList())).build()
        val seq=EditedMediaItemSequence.Builder(setOf(androidx.media3.common.C.TRACK_TYPE_AUDIO)).addItem(a).setIsLooping(true).build();sequences+=seq
    }
    val compositionBuilder = Composition.Builder(sequences)
    buildStickerEffects(stickers)?.let { compositionBuilder.setEffects(Effects(emptyList(), listOf(it))) }
    val composition = compositionBuilder.build()
    val transformer=Transformer.Builder(context).setVideoMimeType(androidx.media3.common.MimeTypes.VIDEO_H264).setAudioMimeType(androidx.media3.common.MimeTypes.AUDIO_AAC).addListener(object:Transformer.Listener{
        override fun onCompleted(composition:Composition,exportResult:androidx.media3.transformer.ExportResult){callback(true,"Exported: ${out.absolutePath}")}
        override fun onError(composition:Composition,exportResult:androidx.media3.transformer.ExportResult,exportException:ExportException){callback(false,"Export failed: ${exportException.message?:"unknown error"}")}
    }).build()
    runCatching{transformer.start(composition,out.absolutePath)}.onFailure{callback(false,"Export failed: ${it.message?:"unknown error"}")}
}

@OptIn(UnstableApi::class)
private fun presentationEffect(quality:String,crop:String):Presentation{val base=when(quality){"720p"->720;"1080p"->1080;else->2160};val(w,h)=when(crop){"9:16"->base to base*16/9;"1:1"->base to base;"4:5"->base to base*5/4;"4:3"->base to base*3/4;else->base*16/9 to base};return Presentation.createForWidthAndHeight(w.toInt(),h.toInt(),Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP)}
@OptIn(UnstableApi::class)
private fun cropEffect(crop:String):Crop?=when(crop){"16:9"->Crop(-1f,1f,-.7778f,.7778f);"9:16"->Crop(-.5625f,.5625f,-1f,1f);"1:1"->Crop(-1f,1f,-1f,1f);"4:5"->Crop(-.8f,.8f,-1f,1f);"4:3"->Crop(-1f,1f,-.75f,.75f);else->null}
