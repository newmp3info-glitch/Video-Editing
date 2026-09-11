package com.rakibul.videostudio

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { RakibulStudioApp() }
    }
}

@Composable
fun RakibulStudioApp() {
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var currentTab by remember { mutableStateOf("Edit") }

    if (selectedUri == null) {
        HomeScreen(
            onVideoSelected = { selectedUri = it },
            currentTab = currentTab,
            onTabSelect = { currentTab = it }
        )
    } else {
        RakibulEditorScreen(
            videoUri = selectedUri!!,
            onBack = { selectedUri = null }
        )
    }
}

@Composable
fun HomeScreen(onVideoSelected: (Uri) -> Unit, currentTab: String, onTabSelect: (String) -> Unit) {
    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onVideoSelected(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F3A78), Color(0xFF0B132B), Color(0xFF070707))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Import", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = {}) { Icon(Icons.Default.Search, null, tint = Color.White) }
            }

            Spacer(Modifier.height(12.dp))
            Text("Video create", color = Color.Gray, fontSize = 12.sp)
            Text("Get started >", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                        .clickable { videoLauncher.launch("video/*") },
                    colors = CardDefaults.cardColors(containerColor = Color(0xCC1E293B))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("New video", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                        .clickable { },
                    colors = CardDefaults.cardColors(containerColor = Color(0xCC1E293B))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Image, null, tint = Color.White, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Edit photo", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            val tools = listOf(
                "AutoCut" to Icons.Default.AutoAwesome,
                "Retouch" to Icons.Default.Face,
                "Photo tools" to Icons.Default.Photo,
                "Shoot and record" to Icons.Default.Videocam,
                "Auto enhance" to Icons.Default.HighQuality,
                "Auto captions" to Icons.Default.ClosedCaption,
                "Remove background" to Icons.Default.PersonRemove,
                "Desktop editor" to Icons.Default.Computer,
                "All tools (16)" to Icons.Default.Apps
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(tools) { (label, icon) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { videoLauncher.launch("video/*") }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0x22FFFFFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(label, color = Color.LightGray, fontSize = 11.sp, maxLines = 1)
                    }
                }
            }

            // Bottom Nav
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("Edit" to Icons.Default.ContentCut, "Templates" to Icons.Default.Dashboard, "AI Lab" to Icons.Default.AutoFixHigh, "Projects" to Icons.Default.Folder, "Inbox" to Icons.Default.Inbox, "Me" to Icons.Default.Person).forEach { (tab, icon) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onTabSelect(tab) }
                    ) {
                        Icon(icon, null, tint = if (currentTab == tab) Color(0xFF2DE1C2) else Color.Gray, modifier = Modifier.size(20.dp))
                        Text(tab, color = if (currentTab == tab) Color(0xFF2DE1C2) else Color.Gray, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun RakibulEditorScreen(videoUri: Uri, onBack: () -> Unit) {
    val context = LocalContext.current
    val editorEngine = remember { VideoEditorEngine(context) }
    var showExportDialog by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }

    DisposableEffect(videoUri) {
        editorEngine.loadVideo(videoUri.toString())
        onDispose { editorEngine.release() }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) { Icon(Icons.Default.Close, null, tint = Color.White) }
                Text("AI UHD v", color = Color.White, fontSize = 14.sp)
                Button(
                    onClick = { showExportDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2DE1C2))
                ) {
                    Text("Export", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            // Video Preview Player
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = editorEngine.player
                            useController = true
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Timeline Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color(0xFF121212))
                    .padding(8.dp)
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("00:00 / 01:02", color = Color.Gray, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Icon(Icons.Default.VolumeUp, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Icon(Icons.Default.Undo, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Icon(Icons.Default.Redo, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .background(Color(0xFF1E293B), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Main Video Track (Timeline)", color = Color.White, fontSize = 12.sp)
                    }
                }
            }

            // Bottom Tools Menu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1A1A))
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("Edit" to Icons.Default.ContentCut, "Audio" to Icons.Default.Audiotrack, "Text" to Icons.Default.TextFields, "Effects" to Icons.Default.AutoFixHigh, "Ratio" to Icons.Default.AspectRatio).forEach { (label, icon) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.height(4.dp))
                        Text(label, color = Color.White, fontSize = 10.sp)
                    }
                }
            }
        }

        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                title = { Text("Export Settings") },
                text = {
                    Column {
                        Text("Resolution: 1080p / 4K")
                        Text("Frame Rate: 30fps / 60fps")
                        Spacer(Modifier.height(8.dp))
                        if (isExporting) {
                            Text("Exporting video using Media3 Transformer...", color = Color(0xFF2DE1C2))
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        isExporting = true
                        val exportEngine = ExportEngine(context)
                        exportEngine.exportVideo(
                            inputUri = videoUri,
                            onComplete = {
                                isExporting = false
                                showExportDialog = false
                            },
                            onError = {
                                isExporting = false
                                showExportDialog = false
                            }
                        )
                    }) {
                        Text("Start Export")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExportDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
