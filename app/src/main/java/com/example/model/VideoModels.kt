package com.example.model

import java.util.UUID

enum class AspectRatio(
    val label: String,
    val ratio: Float,
    val description: String
) {
    RATIO_9_16("9:16", 9f / 16f, "TikTok, Shorts, Reels"),
    RATIO_16_9("16:9", 16f / 9f, "YouTube & Landscape"),
    RATIO_1_1("1:1", 1f, "Instagram Feed"),
    RATIO_4_5("4:5", 4f / 5f, "Portrait Post"),
    RATIO_21_9("21:9", 21f / 9f, "Cinematic Ultra-Wide")
}

enum class CanvasBackground(val label: String) {
    BLUR("Blur Background"),
    BLACK("Studio Black"),
    GRADIENT_CYBER("Cyber Neon"),
    GRADIENT_SUNSET("Sunset Horizon"),
    DARK_SLATE("Dark Slate")
}

enum class VideoFilter(val label: String, val tag: String) {
    NONE("Normal", "clean"),
    CYBERPUNK("Cyberpunk", "neon"),
    VINTAGE("Vintage 90s", "film"),
    EMERALD("Emerald", "nature"),
    SEPIA("Sepia", "retro"),
    NOIR("Noir B&W", "dramatic"),
    SUNSET("Sunset Glow", "warm"),
    GLITCH("VHS Glitch", "retro"),
    WARM("Amber Warm", "cozy"),
    COOL("Ice Cool", "cold"),
    TEAL_ORANGE("Teal & Orange", "cinema")
}

enum class TransitionType(val label: String) {
    NONE("Cut"),
    FADE("Fade to Black"),
    DISSOLVE("Cross Dissolve"),
    SLIDE_LEFT("Slide Left"),
    SLIDE_RIGHT("Slide Right"),
    ZOOM_IN("Zoom In"),
    FLASH_WHITE("White Flash"),
    GLITCH("VHS Glitch"),
    WIPE("Wipe Left")
}

enum class DemoClipPreset(
    val title: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val defaultDurationMs: Long
) {
    CYBER_CITY("Cyber City Drift", 0xFF00F5D4, 0xFF7928CA, 5000L),
    SUNSET_HORIZON("Sunset Cyber Horizon", 0xFFFF7B00, 0xFFFF007F, 4500L),
    NEON_TUNNEL("Neon Tunnel Hyperspeed", 0xFF00BBF9, 0xFFF72585, 4000L),
    LOFI_CHILL("Lo-Fi Coffee Vibes", 0xFF9D4EDD, 0xFFFFD166, 6000L),
    NATURE_AURORA("Emerald Aurora Flow", 0xFF06D6A0, 0xFF118AB2, 5000L)
}

data class MediaClip(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val uri: String? = null,
    val demoPreset: DemoClipPreset? = null,
    val sourceDurationMs: Long = 5000L,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = 5000L,
    val speed: Float = 1.0f,
    val volume: Float = 1.0f,
    val rotation: Int = 0,
    val isMuted: Boolean = false,
    val filter: VideoFilter = VideoFilter.NONE,
    val brightness: Float = 0f, // -1f to 1f
    val contrast: Float = 1.0f, // 0.5f to 2.0f
    val saturation: Float = 1.0f, // 0f to 2.0f
    val vignette: Float = 0f, // 0f to 1.0f
    val transition: TransitionType = TransitionType.NONE,
    val transitionDurationMs: Long = 500L
) {
    val effectiveDurationMs: Long
        get() = ((trimEndMs - trimStartMs).coerceAtLeast(200L) / speed).toLong()
}

enum class AudioCategory(val label: String) {
    BGM("Music"),
    SFX("Sound Effects")
}

enum class AudioSoundPreset(
    val title: String,
    val category: AudioCategory,
    val defaultDurationMs: Long,
    val iconEmoji: String
) {
    CYBER_BEAT("Cyberpunk Synthwave", AudioCategory.BGM, 16000L, "⚡"),
    LOFI_SUNSET("Lo-Fi Sunset Chill", AudioCategory.BGM, 18000L, "☕"),
    CHILLHOP_GROOVE("Chillhop Beat Loop", AudioCategory.BGM, 15000L, "🎧"),
    EPIC_RISE("Cinematic Rise & Drop", AudioCategory.BGM, 14000L, "🎬"),
    WHOOSH_FAST("Fast Cinematic Whoosh", AudioCategory.SFX, 450L, "💨"),
    CAMERA_CLICK("Vintage Shutter Click", AudioCategory.SFX, 350L, "📸"),
    GLITCH_ZAP("Digital Glitch Buzz", AudioCategory.SFX, 500L, "💥"),
    IMPACT_BOOM("Heavy Cinematic Boom", AudioCategory.SFX, 1200L, "💣"),
    BELL_CHIME("Crystal Bell Ding", AudioCategory.SFX, 800L, "🔔")
}

data class AudioTrack(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: AudioCategory,
    val preset: AudioSoundPreset,
    val startTimelineMs: Long = 0L,
    val durationMs: Long = 10000L,
    val volume: Float = 1.0f,
    val isMuted: Boolean = false
)

enum class OverlayType { TEXT, STICKER }

enum class OverlayTextStyle(val label: String) {
    SANS("Sans"),
    SERIF("Serif"),
    BOLD_HEADING("Impact"),
    MONO("Tech Mono"),
    NEON("Neon Glow")
}

data class OverlayItem(
    val id: String = UUID.randomUUID().toString(),
    val type: OverlayType,
    val text: String = "",
    val textStyle: OverlayTextStyle = OverlayTextStyle.SANS,
    val textColorHex: Long = 0xFFFFFFFF,
    val bgColorHex: Long = 0xAA000000,
    val stickerEmoji: String = "",
    val stickerTag: String = "",
    val xPercent: Float = 0f, // -0.4f to 0.4f
    val yPercent: Float = 0f, // -0.4f to 0.4f
    val scale: Float = 1.0f,
    val startMs: Long = 0L,
    val endMs: Long = 3000L
)

enum class ExportResolution(val label: String, val width: Int, val height: Int) {
    HD_720P("720p HD", 720, 1280),
    FHD_1080P("1080p Full HD", 1080, 1920),
    UHD_4K("4K Ultra HD", 2160, 3840)
}

enum class ExportFps(val label: String, val fps: Int) {
    FPS_24("24 FPS (Cinematic)", 24),
    FPS_30("30 FPS (Standard)", 30),
    FPS_60("60 FPS (Ultra Smooth)", 60)
}

data class ExportConfig(
    val resolution: ExportResolution = ExportResolution.FHD_1080P,
    val fps: ExportFps = ExportFps.FPS_30
)

data class Project(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val aspectRatio: AspectRatio = AspectRatio.RATIO_9_16,
    val canvasBg: CanvasBackground = CanvasBackground.BLUR,
    val clips: List<MediaClip> = emptyList(),
    val audioTracks: List<AudioTrack> = emptyList(),
    val overlays: List<OverlayItem> = emptyList()
) {
    val totalDurationMs: Long
        get() = clips.sumOf { it.effectiveDurationMs }.coerceAtLeast(1000L)
}
