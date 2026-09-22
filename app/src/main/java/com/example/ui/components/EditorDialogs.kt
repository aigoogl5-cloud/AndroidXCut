package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AspectRatio
import com.example.model.AudioCategory
import com.example.model.AudioSoundPreset
import com.example.model.CanvasBackground
import com.example.model.ExportConfig
import com.example.model.ExportFps
import com.example.model.ExportResolution
import com.example.model.MediaClip
import com.example.model.OverlayTextStyle
import com.example.model.TransitionType
import com.example.model.VideoFilter
import com.example.render.ExportProgress
import com.example.ui.theme.BorderDark
import com.example.ui.theme.GoldenYellow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceLight
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedDialog(
    clip: MediaClip,
    onSpeedChanged: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    var speed by remember { mutableFloatStateOf(clip.speed) }
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Clip Speed (${String.format(Locale.US, "%.2fx", speed)})",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Presets row
            val presets = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f, 3.0f)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { p ->
                    val isSelected = speed == p
                    Surface(
                        onClick = {
                            speed = p
                            onSpeedChanged(p)
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) NeonCyan else StudioSurfaceVariant,
                        modifier = Modifier.testTag("speed_preset_$p")
                    ) {
                        Text(
                            text = "${p}x",
                            color = if (isSelected) Color.Black else TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Continuous Slider
            Slider(
                value = speed,
                onValueChange = {
                    speed = it
                    onSpeedChanged(it)
                },
                valueRange = 0.25f..3.0f,
                steps = 11,
                colors = SliderDefaults.colors(
                    thumbColor = NeonCyan,
                    activeTrackColor = NeonCyan,
                    inactiveTrackColor = StudioSurfaceLight
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    currentFilter: VideoFilter,
    brightness: Float,
    contrast: Float,
    saturation: Float,
    vignette: Float,
    onFilterSelected: (VideoFilter) -> Unit,
    onAdjustmentsChanged: (brightness: Float, contrast: Float, saturation: Float, vignette: Float) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf(currentFilter) }
    var curBrightness by remember { mutableFloatStateOf(brightness) }
    var curContrast by remember { mutableFloatStateOf(contrast) }
    var curSaturation by remember { mutableFloatStateOf(saturation) }
    var curVignette by remember { mutableFloatStateOf(vignette) }
    var activeTab by remember { mutableIntStateOf(0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = StudioSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Color & Grading",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            TabRow(
                selectedTabIndex = activeTab,
                containerColor = StudioSurface,
                contentColor = NeonCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = NeonCyan
                    )
                }
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Filters", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Fine Adjust", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (activeTab == 0) {
                // Filters Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.height(280.dp)
                ) {
                    items(VideoFilter.values()) { f ->
                        val isSelected = selectedFilter == f
                        Surface(
                            onClick = {
                                selectedFilter = f
                                onFilterSelected(f)
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) NeonCyan.copy(alpha = 0.2f) else StudioSurfaceVariant,
                            modifier = Modifier
                                .height(72.dp)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) NeonCyan else BorderDark,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .testTag("filter_option_${f.name}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = f.label,
                                    color = if (isSelected) NeonCyan else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = f.tag,
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            } else {
                // Adjustments sliders
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    AdjustmentSliderRow("Brightness", curBrightness, -0.6f..0.6f) {
                        curBrightness = it
                        onAdjustmentsChanged(curBrightness, curContrast, curSaturation, curVignette)
                    }
                    AdjustmentSliderRow("Contrast", curContrast, 0.5f..1.8f) {
                        curContrast = it
                        onAdjustmentsChanged(curBrightness, curContrast, curSaturation, curVignette)
                    }
                    AdjustmentSliderRow("Saturation", curSaturation, 0f..2f) {
                        curSaturation = it
                        onAdjustmentsChanged(curBrightness, curContrast, curSaturation, curVignette)
                    }
                    AdjustmentSliderRow("Vignette", curVignette, 0f..1f) {
                        curVignette = it
                        onAdjustmentsChanged(curBrightness, curContrast, curSaturation, curVignette)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AdjustmentSliderRow(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(String.format(Locale.US, "%.2f", value), color = NeonCyan, fontSize = 12.sp)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = NeonCyan,
                activeTrackColor = NeonCyan,
                inactiveTrackColor = StudioSurfaceLight
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransitionDialog(
    clip: MediaClip,
    onTransitionSelected: (TransitionType, Long) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTransition by remember { mutableStateOf(clip.transition) }
    var durationMs by remember { mutableLongStateOf(clip.transitionDurationMs) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = StudioSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Clip Transition",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Duration Slider: 200ms to 1200ms
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Transition Duration", color = TextSecondary, fontSize = 12.sp)
                Text("${durationMs}ms", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = durationMs.toFloat(),
                onValueChange = {
                    durationMs = it.toLong()
                    onTransitionSelected(selectedTransition, durationMs)
                },
                valueRange = 200f..1200f,
                colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.height(240.dp)
            ) {
                items(TransitionType.values()) { t ->
                    val isSelected = selectedTransition == t
                    Surface(
                        onClick = {
                            selectedTransition = t
                            onTransitionSelected(t, durationMs)
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) NeonPink.copy(alpha = 0.2f) else StudioSurfaceVariant,
                        modifier = Modifier
                            .height(64.dp)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) NeonPink else BorderDark,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .testTag("transition_${t.name}")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(6.dp)
                        ) {
                            Text(
                                text = t.label,
                                color = if (isSelected) NeonPink else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioDialog(
    onAddAudio: (preset: AudioSoundPreset, category: AudioCategory) -> Unit,
    onPreviewSound: (AudioSoundPreset) -> Unit,
    onDismiss: () -> Unit
) {
    var activeTab by remember { mutableIntStateOf(0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = StudioSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Offline Audio Studio",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            TabRow(
                selectedTabIndex = activeTab,
                containerColor = StudioSurface,
                contentColor = NeonCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = NeonCyan
                    )
                }
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Music (BGM)", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Sound FX (SFX)", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            val category = if (activeTab == 0) AudioCategory.BGM else AudioCategory.SFX
            val items = AudioSoundPreset.values().filter { it.category == category }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { preset ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StudioSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(preset.iconEmoji, fontSize = 20.sp)
                                Column {
                                    Text(
                                        text = preset.title,
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${preset.defaultDurationMs / 1000f}s • 100% Offline Synthesizer",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconButton(
                                    onClick = { onPreviewSound(preset) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = "Preview",
                                        tint = NeonCyan
                                    )
                                }
                                Button(
                                    onClick = {
                                        onAddAudio(preset, category)
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp).testTag("add_audio_${preset.name}")
                                ) {
                                    Text("Add", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextEditorDialog(
    initialText: String = "",
    onSaveText: (text: String, style: OverlayTextStyle, colorHex: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialText) }
    var selectedStyle by remember { mutableStateOf(OverlayTextStyle.SANS) }
    var selectedColor by remember { mutableLongStateOf(0xFFFFFFFF) }

    val colorPalette = listOf(
        0xFFFFFFFF, 0xFF00F5D4, 0xFFF72585, 0xFFFFD166, 0xFF06D6A0, 0xFF00BBF9, 0xFF7928CA
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = StudioSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Add Text / Subtitle",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Enter title or caption...", color = TextSecondary) },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = StudioSurfaceVariant,
                    unfocusedContainerColor = StudioSurfaceVariant,
                    focusedIndicatorColor = NeonCyan,
                    unfocusedIndicatorColor = BorderDark
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("text_input_field")
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text("Typography Style", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OverlayTextStyle.values().forEach { style ->
                    val isSelected = selectedStyle == style
                    Surface(
                        onClick = { selectedStyle = style },
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) NeonPink else StudioSurfaceVariant,
                        modifier = Modifier.testTag("font_style_${style.name}")
                    ) {
                        Text(
                            text = style.label,
                            color = if (isSelected) Color.White else TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text("Color", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                colorPalette.forEach { cHex ->
                    val isSelected = selectedColor == cHex
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(cHex))
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) NeonCyan else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedColor = cHex }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        onSaveText(text, selectedStyle, selectedColor)
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_text_button")
            ) {
                Text("Add to Timeline", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StickerDialog(
    onSelectSticker: (emoji: String, tag: String) -> Unit,
    onDismiss: () -> Unit
) {
    val stickers = listOf(
        "🔥" to "Fire", "⚡" to "Lightning", "🎬" to "Movie", "💥" to "Boom",
        "❤️" to "Love", "🌟" to "Star", "☕" to "Coffee", "🚀" to "Rocket",
        "🎧" to "Beats", "👾" to "Cyber", "✨" to "Sparkle", "🏆" to "VIP",
        "🎯" to "Target", "💎" to "Diamond", "👀" to "Eyes", "💯" to "100"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = StudioSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Graphic Stickers & Emojis",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.height(260.dp)
            ) {
                items(stickers) { (emoji, tag) ->
                    Surface(
                        onClick = {
                            onSelectSticker(emoji, tag)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = StudioSurfaceVariant,
                        modifier = Modifier
                            .height(64.dp)
                            .border(1.dp, BorderDark, RoundedCornerShape(10.dp))
                            .testTag("sticker_$tag")
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(emoji, fontSize = 26.sp)
                            Text(tag, color = TextSecondary, fontSize = 9.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanvasRatioDialog(
    currentRatio: AspectRatio,
    currentBg: CanvasBackground,
    onRatioChanged: (AspectRatio) -> Unit,
    onBgChanged: (CanvasBackground) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = StudioSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Canvas Aspect Ratio",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AspectRatio.values().forEach { r ->
                    val isSelected = currentRatio == r
                    Surface(
                        onClick = { onRatioChanged(r) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) NeonCyan else StudioSurfaceVariant,
                        modifier = Modifier.testTag("ratio_option_${r.name}")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = r.label,
                                color = if (isSelected) Color.Black else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = r.description,
                                color = if (isSelected) Color.Black.copy(alpha = 0.8f) else TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Canvas Background", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CanvasBackground.values().forEach { bg ->
                    val isSelected = currentBg == bg
                    Surface(
                        onClick = { onBgChanged(bg) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) NeonPink else StudioSurfaceVariant,
                        modifier = Modifier.testTag("canvas_bg_${bg.name}")
                    ) {
                        Text(
                            text = bg.label,
                            color = if (isSelected) Color.White else TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportModalDialog(
    exportProgress: ExportProgress?,
    onStartExport: (ExportConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedRes by remember { mutableStateOf(ExportResolution.FHD_1080P) }
    var selectedFps by remember { mutableStateOf(ExportFps.FPS_30) }

    ModalBottomSheet(
        onDismissRequest = {
            if (exportProgress == null || exportProgress.isComplete) {
                onDismiss()
            }
        },
        containerColor = StudioSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            if (exportProgress == null) {
                // CONFIGURATION SCREEN
                Text(
                    text = "Export Offline Video",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Resolution", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExportResolution.values().forEach { res ->
                        val isSelected = selectedRes == res
                        Surface(
                            onClick = { selectedRes = res },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) NeonCyan else StudioSurfaceVariant,
                            modifier = Modifier.weight(1f).testTag("export_res_${res.name}")
                        ) {
                            Text(
                                text = res.label,
                                color = if (isSelected) Color.Black else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Frame Rate", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExportFps.values().forEach { fps ->
                        val isSelected = selectedFps == fps
                        Surface(
                            onClick = { selectedFps = fps },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) NeonPink else StudioSurfaceVariant,
                            modifier = Modifier.weight(1f).testTag("export_fps_${fps.name}")
                        ) {
                            Text(
                                text = fps.label,
                                color = if (isSelected) Color.White else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        onStartExport(ExportConfig(resolution = selectedRes, fps = selectedFps))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("start_export_button")
                ) {
                    Icon(Icons.Default.Download, contentDescription = "Export", tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Offline Export", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            } else if (!exportProgress.isComplete) {
                // RENDERING PROGRESS SCREEN
                Text(
                    text = "Exporting Video...",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = exportProgress.currentStage,
                    color = NeonCyan,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { exportProgress.progress },
                    color = NeonCyan,
                    trackColor = StudioSurfaceLight,
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Frame ${exportProgress.currentFrame} / ${exportProgress.totalFrames}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${(exportProgress.progress * 100).toInt()}%",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            } else {
                // COMPLETED EXPORT SCREEN
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(NeonCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Done", tint = Color.Black, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Export Ready!", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Saved locally in app gallery (${exportProgress.fileSizeBytes / (1024 * 1024)} MB)",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
