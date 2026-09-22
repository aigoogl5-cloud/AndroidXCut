package com.example.data

import com.example.model.AspectRatio
import com.example.model.AudioCategory
import com.example.model.AudioSoundPreset
import com.example.model.AudioTrack
import com.example.model.CanvasBackground
import com.example.model.DemoClipPreset
import com.example.model.MediaClip
import com.example.model.OverlayItem
import com.example.model.OverlayTextStyle
import com.example.model.OverlayType
import com.example.model.Project
import com.example.model.TransitionType
import com.example.model.VideoFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProjectRepository(private val projectDao: ProjectDao) {

    val allProjects: Flow<List<Project>> = projectDao.getAllProjects().map { list ->
        list.map { ProjectConverters.entityToProject(it) }
    }

    suspend fun getProject(id: String): Project? {
        val entity = projectDao.getProjectById(id) ?: return null
        return ProjectConverters.entityToProject(entity)
    }

    suspend fun saveProject(project: Project) {
        val updated = project.copy(updatedAt = System.currentTimeMillis())
        projectDao.insertProject(ProjectConverters.projectToEntity(updated))
    }

    suspend fun deleteProject(id: String) {
        projectDao.deleteProjectById(id)
    }

    suspend fun seedSampleProjects() {
        val sample1 = Project(
            id = "template_cyberpunk_reel",
            title = "Cyberpunk Reel",
            createdAt = System.currentTimeMillis() - 86400000L,
            updatedAt = System.currentTimeMillis() - 3600000L,
            aspectRatio = AspectRatio.RATIO_9_16,
            canvasBg = CanvasBackground.GRADIENT_CYBER,
            clips = listOf(
                MediaClip(
                    id = "clip_cyber_1",
                    title = "City Drift",
                    demoPreset = DemoClipPreset.CYBER_CITY,
                    sourceDurationMs = 5000L,
                    trimStartMs = 0L,
                    trimEndMs = 4000L,
                    speed = 1.0f,
                    filter = VideoFilter.CYBERPUNK,
                    transition = TransitionType.DISSOLVE,
                    transitionDurationMs = 500L
                ),
                MediaClip(
                    id = "clip_cyber_2",
                    title = "Hyperspeed",
                    demoPreset = DemoClipPreset.NEON_TUNNEL,
                    sourceDurationMs = 4000L,
                    trimStartMs = 500L,
                    trimEndMs = 3500L,
                    speed = 1.25f,
                    filter = VideoFilter.GLITCH,
                    transition = TransitionType.FLASH_WHITE,
                    transitionDurationMs = 400L
                ),
                MediaClip(
                    id = "clip_cyber_3",
                    title = "Horizon Glow",
                    demoPreset = DemoClipPreset.SUNSET_HORIZON,
                    sourceDurationMs = 4500L,
                    trimStartMs = 0L,
                    trimEndMs = 4500L,
                    speed = 1.0f,
                    filter = VideoFilter.TEAL_ORANGE,
                    transition = TransitionType.NONE
                )
            ),
            audioTracks = listOf(
                AudioTrack(
                    id = "audio_cyber_bgm",
                    title = "Cyberpunk Synthwave",
                    category = AudioCategory.BGM,
                    preset = AudioSoundPreset.CYBER_BEAT,
                    startTimelineMs = 0L,
                    durationMs = 12000L,
                    volume = 0.9f
                ),
                AudioTrack(
                    id = "audio_cyber_whoosh",
                    title = "Fast Whoosh",
                    category = AudioCategory.SFX,
                    preset = AudioSoundPreset.WHOOSH_FAST,
                    startTimelineMs = 3800L,
                    durationMs = 500L,
                    volume = 1.0f
                )
            ),
            overlays = listOf(
                OverlayItem(
                    id = "overlay_title_1",
                    type = OverlayType.TEXT,
                    text = "NEON DREAMS",
                    textStyle = OverlayTextStyle.NEON,
                    textColorHex = 0xFF00F5D4,
                    bgColorHex = 0xAA0D1117,
                    xPercent = 0f,
                    yPercent = -0.25f,
                    scale = 1.3f,
                    startMs = 200L,
                    endMs = 3500L
                ),
                OverlayItem(
                    id = "overlay_sticker_1",
                    type = OverlayType.STICKER,
                    stickerEmoji = "⚡",
                    stickerTag = "Lightning",
                    xPercent = 0.25f,
                    yPercent = -0.26f,
                    scale = 1.2f,
                    startMs = 200L,
                    endMs = 3500L
                )
            )
        )

        val sample2 = Project(
            id = "template_sunset_vlog",
            title = "Sunset Travel Vlog",
            createdAt = System.currentTimeMillis() - 172800000L,
            updatedAt = System.currentTimeMillis() - 7200000L,
            aspectRatio = AspectRatio.RATIO_16_9,
            canvasBg = CanvasBackground.GRADIENT_SUNSET,
            clips = listOf(
                MediaClip(
                    id = "clip_vlog_1",
                    title = "Golden Hour",
                    demoPreset = DemoClipPreset.SUNSET_HORIZON,
                    sourceDurationMs = 5000L,
                    trimStartMs = 0L,
                    trimEndMs = 5000L,
                    filter = VideoFilter.SUNSET,
                    transition = TransitionType.ZOOM_IN,
                    transitionDurationMs = 600L
                ),
                MediaClip(
                    id = "clip_vlog_2",
                    title = "Emerald Aurora",
                    demoPreset = DemoClipPreset.NATURE_AURORA,
                    sourceDurationMs = 5000L,
                    trimStartMs = 0L,
                    trimEndMs = 4500L,
                    filter = VideoFilter.EMERALD,
                    transition = TransitionType.FADE
                )
            ),
            audioTracks = listOf(
                AudioTrack(
                    id = "audio_vlog_bgm",
                    title = "Epic Rise & Drop",
                    category = AudioCategory.BGM,
                    preset = AudioSoundPreset.EPIC_RISE,
                    startTimelineMs = 0L,
                    durationMs = 9500L,
                    volume = 0.85f
                )
            ),
            overlays = listOf(
                OverlayItem(
                    id = "overlay_vlog_title",
                    type = OverlayType.TEXT,
                    text = "TRAVEL DIARIES",
                    textStyle = OverlayTextStyle.BOLD_HEADING,
                    textColorHex = 0xFFFFD166,
                    bgColorHex = 0x88000000,
                    xPercent = 0f,
                    yPercent = 0.28f,
                    scale = 1.1f,
                    startMs = 500L,
                    endMs = 4000L
                )
            )
        )

        val sample3 = Project(
            id = "template_lofi_beats",
            title = "Lo-Fi Coffee Session",
            createdAt = System.currentTimeMillis() - 259200000L,
            updatedAt = System.currentTimeMillis() - 14400000L,
            aspectRatio = AspectRatio.RATIO_1_1,
            canvasBg = CanvasBackground.DARK_SLATE,
            clips = listOf(
                MediaClip(
                    id = "clip_lofi_1",
                    title = "Cozy Chill",
                    demoPreset = DemoClipPreset.LOFI_CHILL,
                    sourceDurationMs = 6000L,
                    trimStartMs = 0L,
                    trimEndMs = 6000L,
                    filter = VideoFilter.VINTAGE,
                    transition = TransitionType.DISSOLVE
                )
            ),
            audioTracks = listOf(
                AudioTrack(
                    id = "audio_lofi_bgm",
                    title = "Lo-Fi Sunset Chill",
                    category = AudioCategory.BGM,
                    preset = AudioSoundPreset.LOFI_SUNSET,
                    startTimelineMs = 0L,
                    durationMs = 6000L,
                    volume = 0.8f
                )
            ),
            overlays = listOf(
                OverlayItem(
                    id = "overlay_lofi_text",
                    type = OverlayType.TEXT,
                    text = "CHILL STUDY VIBES",
                    textStyle = OverlayTextStyle.MONO,
                    textColorHex = 0xFFFFFFFF,
                    xPercent = 0f,
                    yPercent = 0.22f,
                    scale = 1.0f,
                    startMs = 0L,
                    endMs = 6000L
                ),
                OverlayItem(
                    id = "overlay_lofi_sticker",
                    type = OverlayType.STICKER,
                    stickerEmoji = "☕",
                    stickerTag = "Coffee",
                    xPercent = 0f,
                    yPercent = -0.1f,
                    scale = 1.5f,
                    startMs = 0L,
                    endMs = 6000L
                )
            )
        )

        saveProject(sample1)
        saveProject(sample2)
        saveProject(sample3)
    }
}
