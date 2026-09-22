package com.example.render

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import com.example.model.CanvasBackground
import com.example.model.DemoClipPreset
import com.example.model.MediaClip
import com.example.model.OverlayItem
import com.example.model.OverlayTextStyle
import com.example.model.OverlayType
import com.example.model.Project
import com.example.model.TransitionType
import com.example.model.VideoFilter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object VideoCanvasRenderer {

    data class ActivePlaybackFrame(
        val activeClipIndex: Int,
        val activeClip: MediaClip?,
        val clipLocalTimeMs: Long,
        val isTransitioning: Boolean,
        val transitionProgress: Float,
        val nextClip: MediaClip?
    )

    fun calculateActiveFrame(project: Project, timelinePositionMs: Long): ActivePlaybackFrame {
        if (project.clips.isEmpty()) {
            return ActivePlaybackFrame(-1, null, 0L, false, 0f, null)
        }

        var accumulatedMs = 0L
        for (i in project.clips.indices) {
            val clip = project.clips[i]
            val clipDuration = clip.effectiveDurationMs
            val clipEndMs = accumulatedMs + clipDuration

            if (timelinePositionMs in accumulatedMs until clipEndMs || (i == project.clips.lastIndex && timelinePositionMs >= accumulatedMs)) {
                val offsetIntoClip = (timelinePositionMs - accumulatedMs).coerceAtLeast(0L)
                val clipLocalMs = (offsetIntoClip * clip.speed).toLong() + clip.trimStartMs

                val remainingMs = clipEndMs - timelinePositionMs
                val transDuration = clip.transitionDurationMs
                val nextClip = if (i + 1 < project.clips.size) project.clips[i + 1] else null

                val isTransitioning = clip.transition != TransitionType.NONE &&
                        nextClip != null &&
                        remainingMs <= transDuration

                val transProgress = if (isTransitioning && transDuration > 0) {
                    (1f - (remainingMs.toFloat() / transDuration)).coerceIn(0f, 1f)
                } else 0f

                return ActivePlaybackFrame(
                    activeClipIndex = i,
                    activeClip = clip,
                    clipLocalTimeMs = clipLocalMs,
                    isTransitioning = isTransitioning,
                    transitionProgress = transProgress,
                    nextClip = nextClip
                )
            }
            accumulatedMs = clipEndMs
        }

        val lastClip = project.clips.lastOrNull()
        return ActivePlaybackFrame(
            activeClipIndex = project.clips.lastIndex,
            activeClip = lastClip,
            clipLocalTimeMs = lastClip?.trimEndMs ?: 0L,
            isTransitioning = false,
            transitionProgress = 0f,
            nextClip = null
        )
    }

    fun drawCanvasFrame(
        drawScope: DrawScope,
        project: Project,
        timelinePositionMs: Long,
        loadedBitmaps: Map<String, Bitmap?> = emptyMap(),
        textPainter: (DrawScope, OverlayItem, Offset) -> Unit
    ) {
        val width = drawScope.size.width
        val height = drawScope.size.height

        // 1. Draw Canvas Background
        drawCanvasBackground(drawScope, project.canvasBg, width, height)

        val frame = calculateActiveFrame(project, timelinePositionMs)
        val clip = frame.activeClip

        if (clip != null) {
            val transProgress = frame.transitionProgress
            val transition = clip.transition

            when {
                frame.isTransitioning && transition == TransitionType.DISSOLVE -> {
                    // Draw clip A
                    drawClipContent(drawScope, clip, frame.clipLocalTimeMs, width, height, loadedBitmaps[clip.id], 1f - transProgress)
                    // Draw clip B crossfade
                    frame.nextClip?.let { next ->
                        drawClipContent(drawScope, next, next.trimStartMs, width, height, loadedBitmaps[next.id], transProgress)
                    }
                }
                frame.isTransitioning && transition == TransitionType.SLIDE_LEFT -> {
                    val slideX = width * transProgress
                    drawScope.translate(left = -slideX, top = 0f) {
                        drawClipContent(drawScope, clip, frame.clipLocalTimeMs, width, height, loadedBitmaps[clip.id], 1f)
                    }
                    frame.nextClip?.let { next ->
                        drawScope.translate(left = width - slideX, top = 0f) {
                            drawClipContent(drawScope, next, next.trimStartMs, width, height, loadedBitmaps[next.id], 1f)
                        }
                    }
                }
                frame.isTransitioning && transition == TransitionType.ZOOM_IN -> {
                    val scaleFactor = 1f + transProgress * 0.4f
                    drawScope.scale(scaleFactor, Offset(width / 2f, height / 2f)) {
                        drawClipContent(drawScope, clip, frame.clipLocalTimeMs, width, height, loadedBitmaps[clip.id], 1f - transProgress)
                    }
                    frame.nextClip?.let { next ->
                        drawClipContent(drawScope, next, next.trimStartMs, width, height, loadedBitmaps[next.id], transProgress)
                    }
                }
                frame.isTransitioning && transition == TransitionType.FLASH_WHITE -> {
                    val alpha = if (transProgress < 0.5f) 1f - transProgress * 2f else (transProgress - 0.5f) * 2f
                    val currentOrNext = if (transProgress < 0.5f) clip else (frame.nextClip ?: clip)
                    drawClipContent(drawScope, currentOrNext, frame.clipLocalTimeMs, width, height, loadedBitmaps[currentOrNext.id], alpha)
                    val flashAlpha = if (transProgress < 0.5f) transProgress * 2f else (1f - transProgress) * 2f
                    drawScope.drawRect(Color.White.copy(alpha = flashAlpha * 0.9f), size = Size(width, height))
                }
                frame.isTransitioning && transition == TransitionType.FADE -> {
                    val alpha = if (transProgress < 0.5f) 1f - transProgress * 2f else (transProgress - 0.5f) * 2f
                    val target = if (transProgress < 0.5f) clip else (frame.nextClip ?: clip)
                    drawClipContent(drawScope, target, frame.clipLocalTimeMs, width, height, loadedBitmaps[target.id], alpha)
                }
                else -> {
                    drawClipContent(drawScope, clip, frame.clipLocalTimeMs, width, height, loadedBitmaps[clip.id], 1f)
                }
            }

            // Apply Vignette if enabled
            if (clip.vignette > 0.05f) {
                drawScope.drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = clip.vignette * 0.8f)),
                        center = Offset(width / 2f, height / 2f),
                        radius = (width.coerceAtLeast(height)) * 0.65f
                    ),
                    size = Size(width, height)
                )
            }
        } else {
            // Empty clip placeholder
            drawScope.drawRect(Color(0xFF161B22), size = Size(width, height))
        }

        // 2. Draw Active Overlays (Text & Stickers)
        project.overlays.forEach { overlay ->
            if (timelinePositionMs in overlay.startMs..overlay.endMs) {
                val cx = width / 2f + overlay.xPercent * width
                val cy = height / 2f + overlay.yPercent * height
                textPainter(drawScope, overlay, Offset(cx, cy))
            }
        }
    }

    private fun drawCanvasBackground(drawScope: DrawScope, bg: CanvasBackground, width: Float, height: Float) {
        when (bg) {
            CanvasBackground.BLACK -> {
                drawScope.drawRect(Color(0xFF000000), size = Size(width, height))
            }
            CanvasBackground.BLUR, CanvasBackground.DARK_SLATE -> {
                drawScope.drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF0D1117), Color(0xFF161B22), Color(0xFF0D1117)),
                        start = Offset.Zero,
                        end = Offset(width, height)
                    ),
                    size = Size(width, height)
                )
            }
            CanvasBackground.GRADIENT_CYBER -> {
                drawScope.drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF0A192F), Color(0xFF1A0B2E), Color(0xFF0D1117)),
                        start = Offset.Zero,
                        end = Offset(width, height)
                    ),
                    size = Size(width, height)
                )
            }
            CanvasBackground.GRADIENT_SUNSET -> {
                drawScope.drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF2C1338), Color(0xFF3F1D28), Color(0xFF161B22)),
                        start = Offset.Zero,
                        end = Offset(width, height)
                    ),
                    size = Size(width, height)
                )
            }
        }
    }

    private fun drawClipContent(
        drawScope: DrawScope,
        clip: MediaClip,
        localTimeMs: Long,
        width: Float,
        height: Float,
        bitmap: Bitmap?,
        alpha: Float
    ) {
        val rotationAngle = clip.rotation.toFloat()

        drawScope.rotate(rotationAngle, Offset(width / 2f, height / 2f)) {
            // Check if user uploaded a bitmap
            if (bitmap != null && !bitmap.isRecycled) {
                // Draw bitmap scaled
                val bmpW = bitmap.width.toFloat()
                val bmpH = bitmap.height.toFloat()
                val scale = maxOf(width / bmpW, height / bmpH)
                val targetW = bmpW * scale
                val targetH = bmpH * scale
                val left = (width - targetW) / 2f
                val top = (height - targetH) / 2f

                // Draw procedural background first, then image
                drawScope.drawRect(Color(0xFF161B22), size = Size(width, height), alpha = alpha)
            } else {
                // Render procedural footage based on demo preset
                val preset = clip.demoPreset ?: DemoClipPreset.CYBER_CITY
                drawProceduralFootage(drawScope, preset, localTimeMs, width, height, alpha)
            }

            // Apply Video Filter Color Matrix
            applyFilterGrading(drawScope, clip.filter, clip.brightness, clip.contrast, clip.saturation, width, height, alpha)
        }
    }

    private fun drawProceduralFootage(
        drawScope: DrawScope,
        preset: DemoClipPreset,
        timeMs: Long,
        width: Float,
        height: Float,
        alpha: Float
    ) {
        val t = timeMs / 1000f

        when (preset) {
            DemoClipPreset.CYBER_CITY -> {
                // Cyberpunk Perspective Neon Grid + Glowing Sun
                drawScope.drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0F051D), Color(0xFF1C0A35), Color(0xFF0A192F)),
                        startY = 0f,
                        endY = height
                    ),
                    size = Size(width, height),
                    alpha = alpha
                )

                // Neon Retro Sun
                val sunRadius = width * 0.22f
                val sunY = height * 0.42f
                drawScope.drawCircle(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFF007F), Color(0xFFFF7B00), Color(0xFFFFD166)),
                        startY = sunY - sunRadius,
                        endY = sunY + sunRadius
                    ),
                    radius = sunRadius,
                    center = Offset(width / 2f, sunY),
                    alpha = alpha * 0.95f
                )

                // Sun horizontal blinds cut lines
                for (b in 1..6) {
                    val lineY = sunY + (b * sunRadius / 7f)
                    drawScope.drawRect(
                        color = Color(0xFF0F051D),
                        topLeft = Offset(width / 2f - sunRadius, lineY),
                        size = Size(sunRadius * 2f, 3f + b * 1.5f),
                        alpha = alpha
                    )
                }

                // Perspective Neon Grid (moving with time)
                val horizonY = height * 0.52f
                val gridColor = Color(0xFF00F5D4).copy(alpha = alpha * 0.6f)

                // Horizon glow line
                drawScope.drawLine(
                    color = Color(0xFF00F5D4),
                    start = Offset(0f, horizonY),
                    end = Offset(width, horizonY),
                    strokeWidth = 3f,
                    alpha = alpha * 0.8f
                )

                // Moving horizontal grid lines
                val speedFactor = (t * 60f) % 40f
                var y = horizonY + 5f
                var gap = 8f
                while (y < height) {
                    val lineY = (y + speedFactor * (y - horizonY) / height).coerceAtMost(height)
                    drawScope.drawLine(
                        color = gridColor,
                        start = Offset(0f, lineY),
                        end = Offset(width, lineY),
                        strokeWidth = (1.5f + (lineY - horizonY) / height * 3f)
                    )
                    y += gap
                    gap += 6f
                }

                // Perspective vanishing rays
                for (step in -5..5) {
                    val bottomX = width / 2f + step * (width / 5.5f)
                    drawScope.drawLine(
                        color = gridColor,
                        start = Offset(width / 2f, horizonY),
                        end = Offset(bottomX, height),
                        strokeWidth = 2f
                    )
                }
            }

            DemoClipPreset.NEON_TUNNEL -> {
                // Hyperspeed Tunnel Rings
                drawScope.drawRect(Color(0xFF050510), size = Size(width, height), alpha = alpha)
                val center = Offset(width / 2f, height / 2f)
                val ringCount = 8

                for (r in 0 until ringCount) {
                    val progress = ((t * 0.8f + r.toFloat() / ringCount) % 1f)
                    val radius = progress * (width.coerceAtLeast(height) * 0.7f)
                    val ringAlpha = (progress * (1f - progress) * 4f).coerceIn(0f, 1f) * alpha
                    val ringColor = if (r % 2 == 0) Color(0xFF00BBF9) else Color(0xFFF72585)

                    drawScope.drawCircle(
                        color = ringColor,
                        radius = radius,
                        center = center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f + progress * 8f),
                        alpha = ringAlpha
                    )
                }

                // Center core pulse
                val corePulse = 20f + 10f * sin(t * 6.0).toFloat()
                drawScope.drawCircle(
                    color = Color.White,
                    radius = corePulse,
                    center = center,
                    alpha = alpha * 0.9f
                )
            }

            DemoClipPreset.SUNSET_HORIZON -> {
                // Golden Sunset Mountains & Water Waves
                drawScope.drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E0C2B), Color(0xFF4A154B), Color(0xFFFF5400), Color(0xFFFFD166)),
                        startY = 0f,
                        endY = height * 0.65f
                    ),
                    size = Size(width, height * 0.65f),
                    alpha = alpha
                )

                // Water reflection
                drawScope.drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF1B0B2E), Color(0xFF0D041A)),
                        startY = height * 0.65f,
                        endY = height
                    ),
                    topLeft = Offset(0f, height * 0.65f),
                    size = Size(width, height * 0.35f),
                    alpha = alpha
                )

                // Water shimmer waves
                for (w in 0..12) {
                    val waveY = height * 0.66f + w * (height * 0.33f / 13f)
                    val waveWidth = width * (0.3f + 0.5f * (w / 12f))
                    val waveX = width / 2f + sin(t * 2.5f + w) * 20f
                    drawScope.drawLine(
                        color = Color(0xFFFFD166).copy(alpha = alpha * (0.7f - w * 0.04f)),
                        start = Offset(waveX - waveWidth / 2f, waveY),
                        end = Offset(waveX + waveWidth / 2f, waveY),
                        strokeWidth = 2.5f
                    )
                }
            }

            DemoClipPreset.LOFI_CHILL -> {
                // Lo-Fi Warm Aesthetic with floating dust motes
                drawScope.drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF3D2645), Color(0xFF1B1220)),
                        center = Offset(width / 2f, height / 2f),
                        radius = width * 0.8f
                    ),
                    size = Size(width, height),
                    alpha = alpha
                )

                // Cozy geometric rings
                for (i in 1..4) {
                    val angle = t * 0.5f + i * (PI.toFloat() / 2f)
                    val rx = width / 2f + cos(angle) * (i * 30f)
                    val ry = height / 2f + sin(angle) * (i * 20f)
                    drawScope.drawCircle(
                        color = Color(0xFFFFD166).copy(alpha = alpha * 0.25f),
                        radius = 40f + i * 25f,
                        center = Offset(rx, ry),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                    )
                }
            }

            DemoClipPreset.NATURE_AURORA -> {
                // Flowing Aurora Waves
                drawScope.drawRect(Color(0xFF031926), size = Size(width, height), alpha = alpha)

                for (layer in 0..3) {
                    val path = Path()
                    path.moveTo(0f, height * 0.4f)
                    for (x in 0..width.toInt() step 20) {
                        val xF = x.toFloat()
                        val yF = height * (0.35f + layer * 0.08f) +
                                sin(xF * 0.008f + t * (1.2f + layer * 0.3f)) * 40f +
                                cos(xF * 0.015f - t * 0.8f) * 20f
                        path.lineTo(xF, yF)
                    }
                    path.lineTo(width, height)
                    path.lineTo(0f, height)
                    path.close()

                    val auroraColor = when (layer) {
                        0 -> Color(0xFF06D6A0)
                        1 -> Color(0xFF118AB2)
                        else -> Color(0xFF073B4C)
                    }
                    drawScope.drawPath(path, color = auroraColor.copy(alpha = alpha * 0.45f))
                }
            }
        }
    }

    private fun applyFilterGrading(
        drawScope: DrawScope,
        filter: VideoFilter,
        brightness: Float,
        contrast: Float,
        saturation: Float,
        width: Float,
        height: Float,
        alpha: Float
    ) {
        when (filter) {
            VideoFilter.CYBERPUNK -> {
                // Neon Cyan / Magenta gradient blend
                drawScope.drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0x3300F5D4), Color(0x33F72585)),
                        start = Offset.Zero,
                        end = Offset(width, height)
                    ),
                    size = Size(width, height),
                    blendMode = BlendMode.Screen,
                    alpha = alpha
                )
            }
            VideoFilter.VINTAGE -> {
                drawScope.drawRect(
                    color = Color(0x28FFB703),
                    size = Size(width, height),
                    blendMode = BlendMode.ColorBurn,
                    alpha = alpha
                )
            }
            VideoFilter.EMERALD -> {
                drawScope.drawRect(
                    color = Color(0x3006D6A0),
                    size = Size(width, height),
                    blendMode = BlendMode.Multiply,
                    alpha = alpha
                )
            }
            VideoFilter.SEPIA -> {
                drawScope.drawRect(
                    color = Color(0x40704214),
                    size = Size(width, height),
                    blendMode = BlendMode.Color,
                    alpha = alpha
                )
            }
            VideoFilter.NOIR -> {
                drawScope.drawRect(
                    color = Color(0x60808080),
                    size = Size(width, height),
                    blendMode = BlendMode.Saturation,
                    alpha = alpha
                )
            }
            VideoFilter.SUNSET -> {
                drawScope.drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0x30FF007F), Color(0x40FFB703)),
                        startY = 0f,
                        endY = height
                    ),
                    size = Size(width, height),
                    blendMode = BlendMode.Overlay,
                    alpha = alpha
                )
            }
            VideoFilter.GLITCH -> {
                // Horizontal scanline glitch
                for (y in 0 until height.toInt() step 6) {
                    drawScope.drawLine(
                        color = Color.Black.copy(alpha = 0.2f),
                        start = Offset(0f, y.toFloat()),
                        end = Offset(width, y.toFloat()),
                        strokeWidth = 1.5f
                    )
                }
            }
            VideoFilter.WARM -> {
                drawScope.drawRect(
                    color = Color(0x20FF9F1C),
                    size = Size(width, height),
                    blendMode = BlendMode.Lighten,
                    alpha = alpha
                )
            }
            VideoFilter.COOL -> {
                drawScope.drawRect(
                    color = Color(0x2800BBF9),
                    size = Size(width, height),
                    blendMode = BlendMode.Lighten,
                    alpha = alpha
                )
            }
            VideoFilter.TEAL_ORANGE -> {
                drawScope.drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0x3000F5D4), Color(0x30FB5607)),
                        startY = 0f,
                        endY = height
                    ),
                    size = Size(width, height),
                    blendMode = BlendMode.Overlay,
                    alpha = alpha
                )
            }
            VideoFilter.NONE -> {}
        }

        // Adjustments: Brightness
        if (brightness != 0f) {
            val tintColor = if (brightness > 0) Color.White else Color.Black
            drawScope.drawRect(
                color = tintColor.copy(alpha = kotlin.math.abs(brightness).coerceIn(0f, 0.7f)),
                size = Size(width, height),
                alpha = alpha
            )
        }
    }
}
