package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DemoClipPreset
import com.example.model.ExportConfig
import com.example.model.MediaClip
import com.example.ui.components.AudioDialog
import com.example.ui.components.CanvasRatioDialog
import com.example.ui.components.EditorToolbar
import com.example.ui.components.ExportModalDialog
import com.example.ui.components.FilterDialog
import com.example.ui.components.SpeedDialog
import com.example.ui.components.StickerDialog
import com.example.ui.components.TextEditorDialog
import com.example.ui.components.TimelineView
import com.example.ui.components.TransitionDialog
import com.example.ui.components.VideoPlayerView
import com.example.ui.theme.BorderDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.EditorDialog
import com.example.viewmodel.EditorViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val project by viewModel.activeProject.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val timelinePositionMs by viewModel.timelinePositionMs.collectAsState()
    val selectedClipIndex by viewModel.selectedClipIndex.collectAsState()
    val selectedTrackType by viewModel.selectedTrackType.collectAsState()
    val selectedOverlayId by viewModel.selectedOverlayId.collectAsState()
    val activeDialog by viewModel.activeDialog.collectAsState()
    val exportProgress by viewModel.exportProgress.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()

    BackHandler {
        viewModel.closeProject()
        onBack()
    }

    // Android Photo/Video Picker launcher for importing media into timeline
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val clip = MediaClip(
                id = UUID.randomUUID().toString(),
                title = "Clip ${(project?.clips?.size ?: 0) + 1}",
                uri = uri.toString(),
                sourceDurationMs = 5000L,
                trimStartMs = 0L,
                trimEndMs = 5000L
            )
            viewModel.addClip(clip)
        }
    }

    val currentProject = project ?: return

    Scaffold(
        containerColor = StudioBackground,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = currentProject.title,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Surface(
                            onClick = { viewModel.openDialog(EditorDialog.CanvasRatio) },
                            color = StudioSurfaceVariant,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = currentProject.aspectRatio.label,
                                color = NeonCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.closeProject()
                            onBack()
                        },
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = canUndo,
                        modifier = Modifier.testTag("undo_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = if (canUndo) Color.White else TextMuted
                        )
                    }

                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = canRedo,
                        modifier = Modifier.testTag("redo_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            tint = if (canRedo) Color.White else TextMuted
                        )
                    }

                    Button(
                        onClick = { viewModel.openDialog(EditorDialog.ExportModal) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .height(34.dp)
                            .testTag("export_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Export",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Export",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StudioBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. VIDEO PREVIEW MONITOR
            VideoPlayerView(
                project = currentProject,
                timelinePositionMs = timelinePositionMs,
                isPlaying = isPlaying,
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onStepFrame = { forward -> viewModel.stepFrame(forward) },
                selectedOverlayId = selectedOverlayId,
                onSelectOverlay = { id -> viewModel.selectOverlay(id) },
                onUpdateOverlay = { updated -> viewModel.updateOverlay(updated) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            // 2. TIMELINE MULTI-TRACK AREA
            TimelineView(
                project = currentProject,
                timelinePositionMs = timelinePositionMs,
                selectedClipIndex = selectedClipIndex,
                onSelectClip = { index -> viewModel.selectClip(index) },
                onSeekTo = { ms -> viewModel.seekTo(ms) },
                onOpenTransitionPicker = { viewModel.openDialog(EditorDialog.TransitionPicker) },
                onOpenAudioPicker = { viewModel.openDialog(EditorDialog.AudioPicker) },
                onOpenTextEditor = { viewModel.openDialog(EditorDialog.TextEditor()) },
                onAddClip = {
                    // Quick add demo clip or pick visual media
                    mediaPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
            )

            // 3. EDITING TOOLBAR (CapCut contextual bar)
            EditorToolbar(
                selectedClipIndex = selectedClipIndex,
                selectedTrackType = selectedTrackType,
                onSplitClip = { viewModel.splitClipAtPlayhead() },
                onOpenSpeed = { viewModel.openDialog(EditorDialog.SpeedAdjust) },
                onOpenFilters = { viewModel.openDialog(EditorDialog.FilterPicker) },
                onOpenTransitions = { viewModel.openDialog(EditorDialog.TransitionPicker) },
                onRotateClip = { viewModel.rotateActiveClip() },
                onDuplicateClip = { viewModel.duplicateActiveClip() },
                onDeleteClip = { viewModel.deleteActiveClip() },
                onDeselectClip = { viewModel.selectClip(null) },
                onOpenAudioPicker = { viewModel.openDialog(EditorDialog.AudioPicker) },
                onOpenTextEditor = { viewModel.openDialog(EditorDialog.TextEditor()) },
                onOpenStickerPicker = { viewModel.openDialog(EditorDialog.StickerPicker) },
                onOpenCanvasRatio = { viewModel.openDialog(EditorDialog.CanvasRatio) },
                onOpenAdjustments = { viewModel.openDialog(EditorDialog.FilterPicker) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // DIALOGS & BOTTOM SHEETS
    when (val dialog = activeDialog) {
        is EditorDialog.SpeedAdjust -> {
            val clip = selectedClipIndex?.let { currentProject.clips.getOrNull(it) }
                ?: currentProject.clips.firstOrNull()
            clip?.let {
                SpeedDialog(
                    clip = it,
                    onSpeedChanged = { speed -> viewModel.setActiveClipSpeed(speed) },
                    onDismiss = { viewModel.closeDialog() }
                )
            }
        }
        is EditorDialog.FilterPicker -> {
            val clip = selectedClipIndex?.let { currentProject.clips.getOrNull(it) }
                ?: currentProject.clips.firstOrNull()
            clip?.let {
                FilterDialog(
                    currentFilter = it.filter,
                    brightness = it.brightness,
                    contrast = it.contrast,
                    saturation = it.saturation,
                    vignette = it.vignette,
                    onFilterSelected = { f -> viewModel.setActiveClipFilter(f) },
                    onAdjustmentsChanged = { b, c, s, v -> viewModel.setActiveClipAdjustments(b, c, s, v) },
                    onDismiss = { viewModel.closeDialog() }
                )
            }
        }
        is EditorDialog.TransitionPicker -> {
            val clip = selectedClipIndex?.let { currentProject.clips.getOrNull(it) }
                ?: currentProject.clips.firstOrNull()
            clip?.let {
                TransitionDialog(
                    clip = it,
                    onTransitionSelected = { t, d -> viewModel.setActiveClipTransition(t, d) },
                    onDismiss = { viewModel.closeDialog() }
                )
            }
        }
        is EditorDialog.AudioPicker -> {
            AudioDialog(
                onAddAudio = { preset, category -> viewModel.addAudioTrack(preset, category) },
                onPreviewSound = { preset -> viewModel.playSfxPreview(preset) },
                onDismiss = { viewModel.closeDialog() }
            )
        }
        is EditorDialog.TextEditor -> {
            TextEditorDialog(
                onSaveText = { text, style, colorHex -> viewModel.addTextOverlay(text, style, colorHex) },
                onDismiss = { viewModel.closeDialog() }
            )
        }
        is EditorDialog.StickerPicker -> {
            StickerDialog(
                onSelectSticker = { emoji, tag -> viewModel.addSticker(emoji, tag) },
                onDismiss = { viewModel.closeDialog() }
            )
        }
        is EditorDialog.CanvasRatio -> {
            CanvasRatioDialog(
                currentRatio = currentProject.aspectRatio,
                currentBg = currentProject.canvasBg,
                onRatioChanged = { r -> viewModel.setAspectRatio(r) },
                onBgChanged = { bg -> viewModel.setCanvasBackground(bg) },
                onDismiss = { viewModel.closeDialog() }
            )
        }
        is EditorDialog.ExportModal -> {
            ExportModalDialog(
                exportProgress = exportProgress,
                onStartExport = { config -> viewModel.startOfflineExport(context, config) },
                onDismiss = {
                    viewModel.clearExportProgress()
                    viewModel.closeDialog()
                }
            )
        }
        else -> {}
    }
}
