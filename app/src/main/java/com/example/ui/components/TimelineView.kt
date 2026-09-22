package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioCategory
import com.example.model.MediaClip
import com.example.model.Project
import com.example.model.TransitionType
import com.example.ui.theme.AudioTrackColor
import com.example.ui.theme.BorderDark
import com.example.ui.theme.GoldenYellow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceLight
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTrackColor
import com.example.ui.theme.VideoTrackActiveColor
import com.example.ui.theme.VideoTrackColor
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun TimelineView(
    project: Project,
    timelinePositionMs: Long,
    selectedClipIndex: Int?,
    onSelectClip: (Int?) -> Unit,
    onSeekTo: (Long) -> Unit,
    onOpenTransitionPicker: () -> Unit,
    onOpenAudioPicker: () -> Unit,
    onOpenTextEditor: () -> Unit,
    onAddClip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    // Timeline scaling factor: 1 ms = 0.05 dp => 1 second = 50 dp
    val dpPerMs = 0.055f
    val totalTimelineWidthDp = (project.totalDurationMs * dpPerMs).coerceAtLeast(360f)

    // Auto-scroll timeline to keep playhead in view when playing
    LaunchedEffect(timelinePositionMs) {
        val playheadDp = timelinePositionMs * dpPerMs
        val scrollTarget = (playheadDp - 150).coerceAtLeast(0f)
        if (scrollTarget.toInt() != scrollState.value && !scrollState.isScrollInProgress) {
            scrollState.scrollTo(scrollTarget.toInt())
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioSurface)
            .border(1.dp, BorderDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(start = 16.dp, end = 80.dp, top = 8.dp, bottom = 8.dp)
        ) {
            // 1. Time Ruler
            TimeRuler(
                totalDurationMs = project.totalDurationMs,
                dpPerMs = dpPerMs,
                onSeekTo = onSeekTo,
                modifier = Modifier
                    .width(totalTimelineWidthDp.dp)
                    .height(24.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 2. Main Video Track
            VideoClipsTrack(
                clips = project.clips,
                selectedClipIndex = selectedClipIndex,
                dpPerMs = dpPerMs,
                onSelectClip = onSelectClip,
                onOpenTransitionPicker = onOpenTransitionPicker,
                onAddClip = onAddClip
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 3. Audio Tracks (BGM & SFX)
            AudioTracksLayer(
                project = project,
                dpPerMs = dpPerMs,
                onOpenAudioPicker = onOpenAudioPicker
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 4. Overlays Track (Text & Stickers)
            OverlaysTrackLayer(
                project = project,
                dpPerMs = dpPerMs,
                onOpenTextEditor = onOpenTextEditor
            )
        }

        // 5. Playhead Line (Stationary relative to scroll viewport or absolute)
        val playheadOffsetDp = 16f + (timelinePositionMs * dpPerMs) - scrollState.value
        if (playheadOffsetDp >= 0) {
            PlayheadIndicator(
                offsetDp = playheadOffsetDp,
                currentTimeMs = timelinePositionMs,
                modifier = Modifier.fillMaxHeight()
            )
        }
    }
}

@Composable
private fun TimeRuler(
    totalDurationMs: Long,
    dpPerMs: Float,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .pointerInput(totalDurationMs) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val clickedMs = (offset.x / dpPerMs).toLong().coerceIn(0L, totalDurationMs)
                        onSeekTo(clickedMs)
                    },
                    onDrag = { change, _ ->
                        val dragMs = (change.position.x / dpPerMs).toLong().coerceIn(0L, totalDurationMs)
                        onSeekTo(dragMs)
                    }
                )
            }
    ) {
        val width = size.width
        val height = size.height

        // Bottom border of ruler
        drawLine(
            color = BorderDark,
            start = Offset(0f, height),
            end = Offset(width, height),
            strokeWidth = 1f
        )

        val secondIntervalMs = 1000L
        val totalSeconds = (totalDurationMs / 1000L) + 2

        for (s in 0..totalSeconds) {
            val x = (s * secondIntervalMs * dpPerMs) * density
            val isMajor = s % 2 == 0L

            drawLine(
                color = if (isMajor) NeonCyan.copy(alpha = 0.8f) else TextMuted,
                start = Offset(x, if (isMajor) height * 0.4f else height * 0.7f),
                end = Offset(x, height),
                strokeWidth = if (isMajor) 1.5f else 1f
            )
        }
    }
}

@Composable
private fun VideoClipsTrack(
    clips: List<MediaClip>,
    selectedClipIndex: Int?,
    dpPerMs: Float,
    onSelectClip: (Int?) -> Unit,
    onOpenTransitionPicker: () -> Unit,
    onAddClip: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        clips.forEachIndexed { index, clip ->
            val isSelected = selectedClipIndex == index
            val clipWidthDp = (clip.effectiveDurationMs * dpPerMs).coerceAtLeast(64f)

            Box(
                modifier = Modifier
                    .width(clipWidthDp.dp)
                    .height(64.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) VideoTrackActiveColor else VideoTrackColor)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) GoldenYellow else BorderDark,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable {
                        onSelectClip(if (isSelected) null else index)
                    }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .testTag("clip_item_$index")
            ) {
                // Trim Handles if selected (CapCut style yellow ends)
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .width(6.dp)
                            .fillMaxHeight()
                            .background(GoldenYellow, RoundedCornerShape(2.dp))
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .width(6.dp)
                            .fillMaxHeight()
                            .background(GoldenYellow, RoundedCornerShape(2.dp))
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = if (isSelected) 8.dp else 2.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = clip.title,
                            color = if (isSelected) GoldenYellow else TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (clip.speed != 1.0f) {
                            Text(
                                text = "${clip.speed}x",
                                color = NeonCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Duration badge & filter badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.1fs", clip.effectiveDurationMs / 1000f),
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                        if (clip.filter != com.example.model.VideoFilter.NONE) {
                            Text(
                                text = clip.filter.label,
                                color = NeonPink,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Transition node button between clips
            if (index < clips.lastIndex) {
                val hasTransition = clip.transition != TransitionType.NONE
                Surface(
                    onClick = {
                        onSelectClip(index)
                        onOpenTransitionPicker()
                    },
                    shape = RoundedCornerShape(4.dp),
                    color = if (hasTransition) NeonPink else StudioSurfaceLight,
                    modifier = Modifier
                        .size(24.dp)
                        .offset(x = 0.dp)
                        .testTag("transition_button_$index")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Transform,
                            contentDescription = "Transition",
                            tint = if (hasTransition) Color.White else TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Add Clip Button at end of video track
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            onClick = onAddClip,
            shape = RoundedCornerShape(6.dp),
            color = StudioSurfaceVariant,
            modifier = Modifier
                .width(60.dp)
                .height(64.dp)
                .border(1.dp, BorderDark, RoundedCornerShape(6.dp))
                .clickable { onAddClip() }
                .testTag("add_clip_track_button")
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Clip",
                    tint = NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Add",
                    color = NeonCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun AudioTracksLayer(
    project: Project,
    dpPerMs: Float,
    onOpenAudioPicker: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (project.audioTracks.isEmpty()) {
            Surface(
                onClick = onOpenAudioPicker,
                shape = RoundedCornerShape(4.dp),
                color = StudioSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .width(180.dp)
                    .height(28.dp)
                    .border(1.dp, BorderDark, RoundedCornerShape(4.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Audiotrack,
                        contentDescription = "Add Audio",
                        tint = NeonCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "+ Add Music & Sound FX",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            project.audioTracks.forEach { track ->
                val trackWidth = (track.durationMs * dpPerMs).coerceAtLeast(80f)
                Box(
                    modifier = Modifier
                        .width(trackWidth.dp)
                        .height(28.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(AudioTrackColor)
                        .border(1.dp, BorderDark, RoundedCornerShape(4.dp))
                        .clickable { onOpenAudioPicker() }
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (track.category == AudioCategory.BGM) Icons.Default.Audiotrack else Icons.Default.GraphicEq,
                            contentDescription = "Audio",
                            tint = NeonCyan,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${track.preset.iconEmoji} ${track.title}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OverlaysTrackLayer(
    project: Project,
    dpPerMs: Float,
    onOpenTextEditor: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (project.overlays.isEmpty()) {
            Surface(
                onClick = onOpenTextEditor,
                shape = RoundedCornerShape(4.dp),
                color = StudioSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .width(180.dp)
                    .height(26.dp)
                    .border(1.dp, BorderDark, RoundedCornerShape(4.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TextFields,
                        contentDescription = "Add Text",
                        tint = NeonPink,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "+ Add Text & Stickers",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            project.overlays.forEach { overlay ->
                val duration = (overlay.endMs - overlay.startMs).coerceAtLeast(500L)
                val widthDp = (duration * dpPerMs).coerceAtLeast(60f)
                Box(
                    modifier = Modifier
                        .width(widthDp.dp)
                        .height(26.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(TextTrackColor)
                        .border(1.dp, BorderDark, RoundedCornerShape(4.dp))
                        .clickable { onOpenTextEditor() }
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = if (overlay.stickerEmoji.isNotEmpty()) "${overlay.stickerEmoji} Sticker" else overlay.text,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayheadIndicator(
    offsetDp: Float,
    currentTimeMs: Long,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .offset { IntOffset(offsetDp.dp.roundToPx(), 0) }
            .width(2.dp)
            .background(NeonCyan)
            .testTag("playhead_indicator")
    ) {
        // Glowing cyan scrubber top knob
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-4).dp)
                .size(10.dp)
                .background(NeonCyan, CircleShape)
        )
    }
}
