package com.example.ui.components

import android.graphics.Paint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OverlayItem
import com.example.model.OverlayTextStyle
import com.example.model.OverlayType
import com.example.model.Project
import com.example.render.VideoCanvasRenderer
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.StudioBackground
import java.util.Locale

@Composable
fun VideoPlayerView(
    project: Project,
    timelinePositionMs: Long,
    isPlaying: Boolean,
    onTogglePlayPause: () -> Unit,
    onStepFrame: (Boolean) -> Unit,
    selectedOverlayId: String? = null,
    onSelectOverlay: (String?) -> Unit = {},
    onUpdateOverlay: (OverlayItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showControls by remember { mutableStateOf(true) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioBackground),
        contentAlignment = Alignment.Center
    ) {
        val containerWidth = maxWidth
        val containerHeight = maxHeight
        val targetRatio = project.aspectRatio.ratio

        // Maintain aspect ratio within container constraints
        Box(
            modifier = Modifier
                .aspectRatio(targetRatio, matchHeightConstraintsFirst = (containerWidth / containerHeight > targetRatio))
                .fillMaxSize(0.96f)
                .clip(RoundedCornerShape(8.dp))
                .shadow(8.dp, RoundedCornerShape(8.dp))
                .background(Color.Black)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    showControls = !showControls
                },
            contentAlignment = Alignment.Center
        ) {
            // Real-time Canvas Rendering
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("video_canvas_preview")
            ) {
                VideoCanvasRenderer.drawCanvasFrame(
                    drawScope = this,
                    project = project,
                    timelinePositionMs = timelinePositionMs,
                    loadedBitmaps = emptyMap()
                ) { scope, overlay, center ->
                    // Native canvas text painter
                    scope.drawContext.canvas.nativeCanvas.apply {
                        val paint = Paint().apply {
                            isAntiAlias = true
                            textAlign = Paint.Align.CENTER
                            textSize = 34f * overlay.scale
                            color = overlay.textColorHex.toInt()
                            when (overlay.textStyle) {
                                OverlayTextStyle.BOLD_HEADING -> {
                                    isFakeBoldText = true
                                    letterSpacing = 0.1f
                                }
                                OverlayTextStyle.SERIF -> {
                                    typeface = android.graphics.Typeface.SERIF
                                }
                                OverlayTextStyle.MONO -> {
                                    typeface = android.graphics.Typeface.MONOSPACE
                                }
                                OverlayTextStyle.NEON -> {
                                    isFakeBoldText = true
                                    setShadowLayer(16f, 0f, 0f, overlay.textColorHex.toInt())
                                }
                                OverlayTextStyle.SANS -> {
                                    typeface = android.graphics.Typeface.DEFAULT
                                }
                            }
                        }

                        if (overlay.type == OverlayType.STICKER) {
                            val emojiPaint = Paint().apply {
                                isAntiAlias = true
                                textAlign = Paint.Align.CENTER
                                textSize = 64f * overlay.scale
                            }
                            drawText(overlay.stickerEmoji, center.x, center.y + 20f, emojiPaint)
                        } else {
                            // Text background banner
                            val text = overlay.text
                            val textWidth = paint.measureText(text)
                            val bgPaint = Paint().apply {
                                color = overlay.bgColorHex.toInt()
                                style = Paint.Style.FILL
                            }
                            val padX = 18f * overlay.scale
                            val padY = 12f * overlay.scale
                            drawRoundRect(
                                center.x - textWidth / 2f - padX,
                                center.y - 28f * overlay.scale - padY,
                                center.x + textWidth / 2f + padX,
                                center.y + 12f * overlay.scale + padY,
                                12f,
                                12f,
                                bgPaint
                            )

                            drawText(text, center.x, center.y, paint)
                        }
                    }
                }
            }

            // Top Badges (Aspect Ratio & Resolution)
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = project.aspectRatio.label,
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "OFFLINE 1080P",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Bottom Floating Controls Overlay
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Timecode display: 00:04.2 / 00:15.0
                    val currentSec = timelinePositionMs / 1000f
                    val totalSec = project.totalDurationMs / 1000f
                    Text(
                        text = String.format(Locale.US, "%02d:%04.1f / %02d:%04.1f",
                            (currentSec / 60).toInt(), currentSec % 60,
                            (totalSec / 60).toInt(), totalSec % 60
                        ),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.testTag("timecode_indicator")
                    )

                    // Transport controls
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onStepFrame(false) },
                            modifier = Modifier.size(36.dp).testTag("step_backward_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FastRewind,
                                contentDescription = "Step -0.1s",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Surface(
                            onClick = onTogglePlayPause,
                            shape = CircleShape,
                            color = NeonCyan,
                            modifier = Modifier.size(38.dp).testTag("play_pause_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = { onStepFrame(true) },
                            modifier = Modifier.size(36.dp).testTag("step_forward_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FastForward,
                                contentDescription = "Step +0.1s",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
