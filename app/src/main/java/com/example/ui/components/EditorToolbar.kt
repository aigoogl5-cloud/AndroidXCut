package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderDark
import com.example.ui.theme.GoldenYellow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.StudioError
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.TrackType

@Composable
fun EditorToolbar(
    selectedClipIndex: Int?,
    selectedTrackType: TrackType,
    onSplitClip: () -> Unit,
    onOpenSpeed: () -> Unit,
    onOpenFilters: () -> Unit,
    onOpenTransitions: () -> Unit,
    onRotateClip: () -> Unit,
    onDuplicateClip: () -> Unit,
    onDeleteClip: () -> Unit,
    onDeselectClip: () -> Unit,
    onOpenAudioPicker: () -> Unit,
    onOpenTextEditor: () -> Unit,
    onOpenStickerPicker: () -> Unit,
    onOpenCanvasRatio: () -> Unit,
    onOpenAdjustments: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioSurface)
            .border(1.dp, BorderDark),
        color = StudioSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedClipIndex != null) {
                // CLIP SELECTED MODE: CapCut Pro Clip Operations
                ToolbarActionItem(
                    icon = Icons.Default.ContentCut,
                    label = "Split",
                    tint = NeonCyan,
                    testTag = "toolbar_split",
                    onClick = onSplitClip
                )
                ToolbarActionItem(
                    icon = Icons.Default.Speed,
                    label = "Speed",
                    tint = GoldenYellow,
                    testTag = "toolbar_speed",
                    onClick = onOpenSpeed
                )
                ToolbarActionItem(
                    icon = Icons.Default.Filter,
                    label = "Filters",
                    tint = NeonPink,
                    testTag = "toolbar_filter",
                    onClick = onOpenFilters
                )
                ToolbarActionItem(
                    icon = Icons.Default.Transform,
                    label = "Transition",
                    tint = Color(0xFF00BBF9),
                    testTag = "toolbar_transition",
                    onClick = onOpenTransitions
                )
                ToolbarActionItem(
                    icon = Icons.Default.RotateRight,
                    label = "Rotate",
                    tint = Color.White,
                    testTag = "toolbar_rotate",
                    onClick = onRotateClip
                )
                ToolbarActionItem(
                    icon = Icons.Default.ContentCopy,
                    label = "Duplicate",
                    tint = Color.White,
                    testTag = "toolbar_duplicate",
                    onClick = onDuplicateClip
                )
                ToolbarActionItem(
                    icon = Icons.Default.Delete,
                    label = "Delete",
                    tint = StudioError,
                    testTag = "toolbar_delete",
                    onClick = onDeleteClip
                )
                ToolbarActionItem(
                    icon = Icons.Default.Check,
                    label = "Done",
                    tint = NeonCyan,
                    testTag = "toolbar_done",
                    onClick = onDeselectClip
                )
            } else {
                // GLOBAL MODE: Project Layers & Creators
                ToolbarActionItem(
                    icon = Icons.Default.Movie,
                    label = "Clips",
                    tint = NeonCyan,
                    testTag = "toolbar_global_clips",
                    onClick = {}
                )
                ToolbarActionItem(
                    icon = Icons.Default.Audiotrack,
                    label = "Audio",
                    tint = Color(0xFF06D6A0),
                    testTag = "toolbar_global_audio",
                    onClick = onOpenAudioPicker
                )
                ToolbarActionItem(
                    icon = Icons.Default.TextFields,
                    label = "Text",
                    tint = NeonPink,
                    testTag = "toolbar_global_text",
                    onClick = onOpenTextEditor
                )
                ToolbarActionItem(
                    icon = Icons.Default.Star,
                    label = "Stickers",
                    tint = GoldenYellow,
                    testTag = "toolbar_global_stickers",
                    onClick = onOpenStickerPicker
                )
                ToolbarActionItem(
                    icon = Icons.Default.Filter,
                    label = "Filters",
                    tint = Color(0xFF9D4EDD),
                    testTag = "toolbar_global_filters",
                    onClick = onOpenFilters
                )
                ToolbarActionItem(
                    icon = Icons.Default.AspectRatio,
                    label = "Ratio",
                    tint = Color(0xFF00BBF9),
                    testTag = "toolbar_global_ratio",
                    onClick = onOpenCanvasRatio
                )
                ToolbarActionItem(
                    icon = Icons.Default.Tune,
                    label = "Adjust",
                    tint = Color.White,
                    testTag = "toolbar_global_adjust",
                    onClick = onOpenAdjustments
                )
            }
        }
    }
}

@Composable
private fun ToolbarActionItem(
    icon: ImageVector,
    label: String,
    tint: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(StudioSurfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
