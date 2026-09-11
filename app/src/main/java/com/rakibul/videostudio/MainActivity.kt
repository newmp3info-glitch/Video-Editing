package com.rakibul.videostudio

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF121212)
            ) {
                CapCutExactTimelineScreen()
            }
        }
    }
}

@Composable
fun CapCutExactTimelineScreen() {
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var currentTool by remember { mutableStateOf("Timeline Ready - Select any tool below") }
    var isPlaying by remember { mutableStateOf(true) }
    var audioAdded by remember { mutableStateOf(false) }
    var textAdded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedVideoUri = uri
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar (CapCut Style)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1E1E))
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.clickable { selectedVideoUri = null }
            )
            Text(
                text = "Rakibul Video Studio",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF)),
                onClick = { /* Export action */ },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(text = "Export", color = Color.White)
            }
        }

        // Video Preview / Player Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (selectedVideoUri != null) {
                val exoPlayer = remember {
                    ExoPlayer.Builder(context).build().apply {
                        setMediaItem(MediaItem.fromUri(selectedVideoUri!!))
                        prepare()
                        playWhenReady = true
                    }
                }

                DisposableEffect(selectedVideoUri) {
                    onDispose {
                        exoPlayer.release()
                    }
                }

                AndroidView(
                    factory = {
                        PlayerView(context).apply {
                            player = exoPlayer
                            useController = true
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Initial Import Screen
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        onClick = { videoPickerLauncher.launch("video/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(160.dp, 50.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "New Project", color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Select a video to load multi-track timeline", color = Color.Gray)
                }
            }
        }

        // --- CAPCUT EXACT MULTI-TRACK TIMELINE SECTION ---
        if (selectedVideoUri != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF181818))
                    .padding(8.dp)
            ) {
                // Playhead & Controls Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "00:00 / 01:00", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { isPlaying = !isPlaying }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 1. Video Track (Multiple Thumbnail Blocks like CapCut)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .background(Color(0xFF222222))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(6) { index ->
                        Box(
                            modifier = Modifier
                                .size(55.dp, 45.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF2C3E50)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Movie, contentDescription = null, tint = Color(0xFF3498DB), modifier = Modifier.size(20.dp))
                            Text(text = "0${index+1}", color = Color.White, modifier = Modifier.align(Alignment.BottomEnd).padding(2.dp), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    // Add more block button (+)
                    Box(
                        modifier = Modifier
                            .size(40.dp, 45.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF333333))
                            .clickable { videoPickerLauncher.launch("video/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add clip", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 2. Audio Track Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(35.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1E272C))
                        .clickable { 
                            audioAdded = true
                            currentTool = "Audio track added successfully!"
                        }
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AudioFile, contentDescription = null, tint = Color(0xFF2ECC71), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (audioAdded) "🎵 Background_Audio_Track.mp3 (Active)" else "+ Add audio",
                        color = if (audioAdded) Color(0xFF2ECC71) else Color.Gray,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 3. Text Track Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(35.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF2C1E27))
                        .clickable { 
                            textAdded = true
                            currentTool = "Text overlay added successfully!"
                        }
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.TextFields, contentDescription = null, tint = Color(0xFFE74C3C), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (textAdded) "💬 Subtitle_Text_Block (Active)" else "+ Add text",
                        color = if (textAdded) Color(0xFFE74C3C) else Color.Gray,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        // Active Tool Status Feedback
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A1A))
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = currentTool, color = Color(0xFF00B0FF), style = MaterialTheme.typography.labelSmall)
        }

        // CapCut Bottom Editing Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1E1E))
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            ToolItem(icon = Icons.Default.ContentCut, label = "Split") { currentTool = "Split Tool Applied" }
            ToolItem(icon = Icons.Default.Speed, label = "Speed") { currentTool = "Speed Control Applied" }
            ToolItem(icon = Icons.Default.VolumeUp, label = "Volume") { currentTool = "Volume Adjusted" }
            ToolItem(icon = Icons.Default.AudioFile, label = "Audio") { 
                audioAdded = true
                currentTool = "Audio Track Selected & Added" 
            }
            ToolItem(icon = Icons.Default.TextFields, label = "Text") { 
                textAdded = true
                currentTool = "Text Tool Selected & Added" 
            }
            ToolItem(icon = Icons.Default.Star, label = "Stickers") { currentTool = "Stickers Applied" }
            ToolItem(icon = Icons.Default.Grain, label = "Effects") { currentTool = "Video Effects Applied" }
            ToolItem(icon = Icons.Default.Filter, label = "Filters") { currentTool = "Color Filters Applied" }
            ToolItem(icon = Icons.Default.AspectRatio, label = "Ratio") { currentTool = "Aspect Ratio Changed" }
            ToolItem(icon = Icons.Default.Delete, label = "Delete") { 
                currentTool = "Project Reset"
                selectedVideoUri = null
                audioAdded = false
                textAdded = false
            }
        }
    }
}

@Composable
fun ToolItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, color = Color.LightGray, style = MaterialTheme.typography.labelSmall)
    }
}
