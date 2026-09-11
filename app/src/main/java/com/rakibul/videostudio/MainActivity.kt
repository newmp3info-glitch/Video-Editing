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
                CapCutClassicEditorScreen()
            }
        }
    }
}

@Composable
fun CapCutClassicEditorScreen() {
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var currentTool by remember { mutableStateOf("Timeline - Select a tool below") }
    var isPlaying by remember { mutableStateOf(true) }
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
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            alignment = Alignment.CenterVertically
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
                onClick = { /* Export Trigger */ }
            ) {
                Text(text = "Export", color = Color.White)
            }
        }

        // Video Preview Area
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
                    Text(text = "Select a video from gallery to start editing", color = Color.Gray)
                }
            }
        }

        // Timeline Track Bar (Active only when video is loaded)
        if (selectedVideoUri != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .background(Color(0xFF181818))
                    .padding(8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF2C3E50))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    alignment = Alignment.CenterVertically
                ) {
                    Row(alignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Movie, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Main Video Track", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    }
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.clickable { isPlaying = !isPlaying }
                    )
                }
            }
        }

        // Active Tool Feedback Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF222222))
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = currentTool, color = Color(0xFF00B0FF), style = MaterialTheme.typography.bodySmall)
        }

        // CapCut Classic Bottom Toolbar (No AI, Pure Editing Features with Correct Icons)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1E1E))
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ToolItem(icon = Icons.Default.ContentCut, label = "Split") { currentTool = "Split Tool Selected" }
            ToolItem(icon = Icons.Default.Speed, label = "Speed") { currentTool = "Speed Control Selected" }
            ToolItem(icon = Icons.Default.VolumeUp, label = "Volume") { currentTool = "Audio Volume Selected" }
            ToolItem(icon = Icons.Default.AudioFile, label = "Audio") { currentTool = "Background Music Selected" }
            ToolItem(icon = Icons.Default.TextFields, label = "Text") { currentTool = "Text & Subtitles Selected" }
            ToolItem(icon = Icons.Default.Star, label = "Stickers") { currentTool = "Stickers & Emojis Selected" }
            ToolItem(icon = Icons.Default.Grain, label = "Effects") { currentTool = "Video Effects Selected" }
            ToolItem(icon = Icons.Default.Filter, label = "Filters") { currentTool = "Color Filters Selected" }
            ToolItem(icon = Icons.Default.AspectRatio, label = "Ratio") { currentTool = "Aspect Ratio Selected" }
            ToolItem(icon = Icons.Default.Delete, label = "Delete") { 
                currentTool = "Project Reset"
                selectedVideoUri = null
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
            imageVector = icon, // Fixed: Passes the exact tool icon correctly
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, color = Color.LightGray, style = MaterialTheme.typography.labelSmall)
    }
}
