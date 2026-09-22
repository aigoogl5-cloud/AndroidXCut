package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.OfflineAudioEngine
import com.example.data.AndroidxcutDatabase
import com.example.data.ProjectRepository
import com.example.model.AspectRatio
import com.example.model.AudioCategory
import com.example.model.AudioSoundPreset
import com.example.model.AudioTrack
import com.example.model.CanvasBackground
import com.example.model.DemoClipPreset
import com.example.model.ExportConfig
import com.example.model.MediaClip
import com.example.model.OverlayItem
import com.example.model.OverlayTextStyle
import com.example.model.OverlayType
import com.example.model.Project
import com.example.model.TransitionType
import com.example.model.VideoFilter
import com.example.render.ExportProgress
import com.example.render.OfflineExportManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

enum class TrackType {
    CLIPS, AUDIO, TEXT, STICKERS, FILTERS, CANVAS
}

sealed class EditorDialog {
    object SpeedAdjust : EditorDialog()
    object FilterPicker : EditorDialog()
    object TransitionPicker : EditorDialog()
    object AudioPicker : EditorDialog()
    data class TextEditor(val editingOverlayId: String? = null) : EditorDialog()
    object StickerPicker : EditorDialog()
    object CanvasRatio : EditorDialog()
    object ExportModal : EditorDialog()
    object NewProjectOptions : EditorDialog()
    data class RenameProject(val projectId: String, val currentTitle: String) : EditorDialog()
}

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProjectRepository
    private val audioEngine = OfflineAudioEngine(viewModelScope)

    val allProjects: StateFlow<List<Project>>

    private val _activeProject = MutableStateFlow<Project?>(null)
    val activeProject: StateFlow<Project?> = _activeProject.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _timelinePositionMs = MutableStateFlow(0L)
    val timelinePositionMs: StateFlow<Long> = _timelinePositionMs.asStateFlow()

    private val _selectedClipIndex = MutableStateFlow<Int?>(null)
    val selectedClipIndex: StateFlow<Int?> = _selectedClipIndex.asStateFlow()

    private val _selectedTrackType = MutableStateFlow(TrackType.CLIPS)
    val selectedTrackType: StateFlow<TrackType> = _selectedTrackType.asStateFlow()

    private val _selectedOverlayId = MutableStateFlow<String?>(null)
    val selectedOverlayId: StateFlow<String?> = _selectedOverlayId.asStateFlow()

    private val _activeDialog = MutableStateFlow<EditorDialog?>(null)
    val activeDialog: StateFlow<EditorDialog?> = _activeDialog.asStateFlow()

    private val _exportProgress = MutableStateFlow<ExportProgress?>(null)
    val exportProgress: StateFlow<ExportProgress?> = _exportProgress.asStateFlow()

    // Undo / Redo stacks
    private val undoStack = mutableListOf<Project>()
    private val redoStack = mutableListOf<Project>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private var playbackJob: Job? = null

    init {
        val db = AndroidxcutDatabase.getDatabase(application)
        repository = ProjectRepository(db.projectDao())
        allProjects = repository.allProjects.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            repository.allProjects.collect { projects ->
                if (projects.isEmpty()) {
                    repository.seedSampleProjects()
                }
            }
        }
    }

    fun openProject(project: Project) {
        pause()
        undoStack.clear()
        redoStack.clear()
        updateUndoRedoStates()
        _activeProject.value = project
        _timelinePositionMs.value = 0L
        _selectedClipIndex.value = if (project.clips.isNotEmpty()) 0 else null
        _selectedTrackType.value = TrackType.CLIPS
    }

    fun closeProject() {
        pause()
        _activeProject.value?.let { p ->
            viewModelScope.launch { repository.saveProject(p) }
        }
        _activeProject.value = null
        _selectedClipIndex.value = null
        _activeDialog.value = null
    }

    fun openDialog(dialog: EditorDialog) {
        _activeDialog.value = dialog
    }

    fun closeDialog() {
        _activeDialog.value = null
    }

    fun selectTrackType(trackType: TrackType) {
        _selectedTrackType.value = trackType
        if (trackType != TrackType.CLIPS) {
            _selectedClipIndex.value = null
        }
    }

    fun selectClip(index: Int?) {
        _selectedClipIndex.value = index
        if (index != null) {
            _selectedTrackType.value = TrackType.CLIPS
            _selectedOverlayId.value = null
            // Seek playhead to start of this clip
            _activeProject.value?.let { project ->
                var startMs = 0L
                for (i in 0 until index.coerceAtMost(project.clips.size)) {
                    startMs += project.clips[i].effectiveDurationMs
                }
                seekTo(startMs)
            }
        }
    }

    fun selectOverlay(id: String?) {
        _selectedOverlayId.value = id
        if (id != null) {
            _selectedClipIndex.value = null
        }
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        val project = _activeProject.value ?: return
        if (project.clips.isEmpty()) return

        if (_timelinePositionMs.value >= project.totalDurationMs) {
            _timelinePositionMs.value = 0L
        }

        _isPlaying.value = true

        // Play BGM if project has audio track
        project.audioTracks.firstOrNull { it.category == AudioCategory.BGM && !it.isMuted }?.let { bgm ->
            audioEngine.startBgm(bgm.preset, bgm.volume)
        }

        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            var lastTick = System.currentTimeMillis()
            while (isActive && _isPlaying.value) {
                delay(33) // ~30 fps ticker
                val now = System.currentTimeMillis()
                val delta = (now - lastTick).coerceAtMost(100L)
                lastTick = now

                val nextPos = _timelinePositionMs.value + delta
                val total = project.totalDurationMs

                if (nextPos >= total) {
                    _timelinePositionMs.value = 0L
                    // Loop playback or pause
                } else {
                    _timelinePositionMs.value = nextPos
                }
            }
        }
    }

    fun pause() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
        audioEngine.stopAll()
    }

    fun seekTo(positionMs: Long) {
        val total = _activeProject.value?.totalDurationMs ?: 5000L
        _timelinePositionMs.value = positionMs.coerceIn(0L, total)
    }

    fun stepFrame(forward: Boolean) {
        pause()
        val step = 100L // 0.1 sec step
        val total = _activeProject.value?.totalDurationMs ?: 5000L
        val next = if (forward) _timelinePositionMs.value + step else _timelinePositionMs.value - step
        _timelinePositionMs.value = next.coerceIn(0L, total)
    }

    // Split clip at playhead
    fun splitClipAtPlayhead() {
        val project = _activeProject.value ?: return
        if (project.clips.isEmpty()) return

        val playhead = _timelinePositionMs.value
        var accumulated = 0L
        var targetIndex = -1
        var offsetIntoClip = 0L

        for (i in project.clips.indices) {
            val clip = project.clips[i]
            val clipEnd = accumulated + clip.effectiveDurationMs
            if (playhead > accumulated && playhead < clipEnd) {
                targetIndex = i
                offsetIntoClip = playhead - accumulated
                break
            }
            accumulated = clipEnd
        }

        if (targetIndex != -1) {
            pushUndo()
            val original = project.clips[targetIndex]
            val splitPointSourceMs = original.trimStartMs + (offsetIntoClip * original.speed).toLong()

            // Clip 1: start to splitPoint
            val part1 = original.copy(
                id = UUID.randomUUID().toString(),
                title = "${original.title} (Part 1)",
                trimEndMs = splitPointSourceMs
            )

            // Clip 2: splitPoint to end
            val part2 = original.copy(
                id = UUID.randomUUID().toString(),
                title = "${original.title} (Part 2)",
                trimStartMs = splitPointSourceMs
            )

            val newClips = project.clips.toMutableList()
            newClips.removeAt(targetIndex)
            newClips.add(targetIndex, part2)
            newClips.add(targetIndex, part1)

            applyProjectUpdate(project.copy(clips = newClips))
            _selectedClipIndex.value = targetIndex
            audioEngine.playSfx(AudioSoundPreset.CAMERA_CLICK, 0.7f)
        }
    }

    fun trimActiveClip(newStartMs: Long, newEndMs: Long) {
        val project = _activeProject.value ?: return
        val index = _selectedClipIndex.value ?: return
        if (index !in project.clips.indices) return

        pushUndo()
        val clip = project.clips[index]
        val validStart = newStartMs.coerceIn(0L, clip.sourceDurationMs - 300L)
        val validEnd = newEndMs.coerceIn(validStart + 300L, clip.sourceDurationMs)

        val updatedClip = clip.copy(trimStartMs = validStart, trimEndMs = validEnd)
        val newClips = project.clips.toMutableList()
        newClips[index] = updatedClip
        applyProjectUpdate(project.copy(clips = newClips))
    }

    fun setActiveClipSpeed(speed: Float) {
        val project = _activeProject.value ?: return
        val index = _selectedClipIndex.value ?: return
        if (index !in project.clips.indices) return

        pushUndo()
        val updatedClip = project.clips[index].copy(speed = speed)
        val newClips = project.clips.toMutableList()
        newClips[index] = updatedClip
        applyProjectUpdate(project.copy(clips = newClips))
    }

    fun setActiveClipFilter(filter: VideoFilter) {
        val project = _activeProject.value ?: return
        val index = _selectedClipIndex.value ?: return
        if (index !in project.clips.indices) return

        pushUndo()
        val updatedClip = project.clips[index].copy(filter = filter)
        val newClips = project.clips.toMutableList()
        newClips[index] = updatedClip
        applyProjectUpdate(project.copy(clips = newClips))
    }

    fun setActiveClipAdjustments(brightness: Float, contrast: Float, saturation: Float, vignette: Float) {
        val project = _activeProject.value ?: return
        val index = _selectedClipIndex.value ?: return
        if (index !in project.clips.indices) return

        pushUndo()
        val updatedClip = project.clips[index].copy(
            brightness = brightness,
            contrast = contrast,
            saturation = saturation,
            vignette = vignette
        )
        val newClips = project.clips.toMutableList()
        newClips[index] = updatedClip
        applyProjectUpdate(project.copy(clips = newClips))
    }

    fun setActiveClipTransition(transition: TransitionType, durationMs: Long = 500L) {
        val project = _activeProject.value ?: return
        val index = _selectedClipIndex.value ?: return
        if (index !in project.clips.indices) return

        pushUndo()
        val updatedClip = project.clips[index].copy(
            transition = transition,
            transitionDurationMs = durationMs
        )
        val newClips = project.clips.toMutableList()
        newClips[index] = updatedClip
        applyProjectUpdate(project.copy(clips = newClips))
    }

    fun rotateActiveClip() {
        val project = _activeProject.value ?: return
        val index = _selectedClipIndex.value ?: return
        if (index !in project.clips.indices) return

        pushUndo()
        val current = project.clips[index].rotation
        val nextRotation = (current + 90) % 360
        val updatedClip = project.clips[index].copy(rotation = nextRotation)
        val newClips = project.clips.toMutableList()
        newClips[index] = updatedClip
        applyProjectUpdate(project.copy(clips = newClips))
    }

    fun duplicateActiveClip() {
        val project = _activeProject.value ?: return
        val index = _selectedClipIndex.value ?: return
        if (index !in project.clips.indices) return

        pushUndo()
        val clip = project.clips[index]
        val duplicate = clip.copy(
            id = UUID.randomUUID().toString(),
            title = "${clip.title} (Copy)"
        )
        val newClips = project.clips.toMutableList()
        newClips.add(index + 1, duplicate)
        applyProjectUpdate(project.copy(clips = newClips))
        _selectedClipIndex.value = index + 1
    }

    fun deleteActiveClip() {
        val project = _activeProject.value ?: return
        val index = _selectedClipIndex.value ?: return
        if (index !in project.clips.indices) return

        pushUndo()
        val newClips = project.clips.toMutableList()
        newClips.removeAt(index)
        applyProjectUpdate(project.copy(clips = newClips))

        if (newClips.isEmpty()) {
            _selectedClipIndex.value = null
        } else {
            _selectedClipIndex.value = (index - 1).coerceAtLeast(0)
        }
    }

    fun moveClip(fromIndex: Int, toIndex: Int) {
        val project = _activeProject.value ?: return
        if (fromIndex !in project.clips.indices || toIndex !in project.clips.indices) return

        pushUndo()
        val newClips = project.clips.toMutableList()
        val item = newClips.removeAt(fromIndex)
        newClips.add(toIndex, item)
        applyProjectUpdate(project.copy(clips = newClips))
        _selectedClipIndex.value = toIndex
    }

    fun addClip(clip: MediaClip) {
        val project = _activeProject.value ?: return
        pushUndo()
        val newClips = project.clips.toMutableList()
        newClips.add(clip)
        applyProjectUpdate(project.copy(clips = newClips))
        _selectedClipIndex.value = newClips.lastIndex
    }

    fun addAudioTrack(preset: AudioSoundPreset, category: AudioCategory) {
        val project = _activeProject.value ?: return
        pushUndo()
        val newTrack = AudioTrack(
            title = preset.title,
            category = category,
            preset = preset,
            startTimelineMs = _timelinePositionMs.value,
            durationMs = preset.defaultDurationMs,
            volume = 0.9f
        )
        val newTracks = project.audioTracks.toMutableList()
        newTracks.add(newTrack)
        applyProjectUpdate(project.copy(audioTracks = newTracks))
        audioEngine.playSfx(preset, 0.8f)
    }

    fun removeAudioTrack(id: String) {
        val project = _activeProject.value ?: return
        pushUndo()
        val newTracks = project.audioTracks.filterNot { it.id == id }
        applyProjectUpdate(project.copy(audioTracks = newTracks))
    }

    fun addTextOverlay(text: String, style: OverlayTextStyle, colorHex: Long) {
        val project = _activeProject.value ?: return
        pushUndo()
        val start = _timelinePositionMs.value
        val end = (start + 3000L).coerceAtMost(project.totalDurationMs)
        val overlay = OverlayItem(
            type = OverlayType.TEXT,
            text = text,
            textStyle = style,
            textColorHex = colorHex,
            startMs = start,
            endMs = end
        )
        val newOverlays = project.overlays.toMutableList()
        newOverlays.add(overlay)
        applyProjectUpdate(project.copy(overlays = newOverlays))
        _selectedOverlayId.value = overlay.id
    }

    fun addSticker(emoji: String, tag: String) {
        val project = _activeProject.value ?: return
        pushUndo()
        val start = _timelinePositionMs.value
        val end = (start + 3000L).coerceAtMost(project.totalDurationMs)
        val overlay = OverlayItem(
            type = OverlayType.STICKER,
            stickerEmoji = emoji,
            stickerTag = tag,
            scale = 1.3f,
            startMs = start,
            endMs = end
        )
        val newOverlays = project.overlays.toMutableList()
        newOverlays.add(overlay)
        applyProjectUpdate(project.copy(overlays = newOverlays))
        _selectedOverlayId.value = overlay.id
    }

    fun updateOverlay(updated: OverlayItem) {
        val project = _activeProject.value ?: return
        pushUndo()
        val newOverlays = project.overlays.map { if (it.id == updated.id) updated else it }
        applyProjectUpdate(project.copy(overlays = newOverlays))
    }

    fun removeOverlay(id: String) {
        val project = _activeProject.value ?: return
        pushUndo()
        val newOverlays = project.overlays.filterNot { it.id == id }
        applyProjectUpdate(project.copy(overlays = newOverlays))
        if (_selectedOverlayId.value == id) {
            _selectedOverlayId.value = null
        }
    }

    fun setAspectRatio(aspectRatio: AspectRatio) {
        val project = _activeProject.value ?: return
        pushUndo()
        applyProjectUpdate(project.copy(aspectRatio = aspectRatio))
    }

    fun setCanvasBackground(bg: CanvasBackground) {
        val project = _activeProject.value ?: return
        pushUndo()
        applyProjectUpdate(project.copy(canvasBg = bg))
    }

    fun startOfflineExport(context: Context, config: ExportConfig) {
        val project = _activeProject.value ?: return
        pause()
        viewModelScope.launch {
            OfflineExportManager.startOfflineExport(context, project, config).collect { progress ->
                _exportProgress.value = progress
            }
        }
    }

    fun clearExportProgress() {
        _exportProgress.value = null
    }

    fun playSfxPreview(preset: AudioSoundPreset) {
        audioEngine.playSfx(preset, 0.9f)
    }

    fun createNewProject(title: String, ratio: AspectRatio) {
        val newProject = Project(
            id = UUID.randomUUID().toString(),
            title = if (title.isBlank()) "New Edit ${System.currentTimeMillis() % 1000}" else title,
            aspectRatio = ratio,
            canvasBg = CanvasBackground.BLUR,
            clips = listOf(
                MediaClip(
                    title = "Cyber City",
                    demoPreset = DemoClipPreset.CYBER_CITY,
                    sourceDurationMs = 5000L,
                    trimStartMs = 0L,
                    trimEndMs = 5000L
                )
            )
        )
        viewModelScope.launch {
            repository.saveProject(newProject)
            openProject(newProject)
        }
    }

    fun duplicateProject(project: Project) {
        viewModelScope.launch {
            val copy = project.copy(
                id = UUID.randomUUID().toString(),
                title = "${project.title} (Copy)",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.saveProject(copy)
        }
    }

    fun renameProject(id: String, newTitle: String) {
        viewModelScope.launch {
            val project = repository.getProject(id) ?: return@launch
            val updated = project.copy(title = newTitle, updatedAt = System.currentTimeMillis())
            repository.saveProject(updated)
            if (_activeProject.value?.id == id) {
                _activeProject.value = updated
            }
        }
    }

    fun deleteProject(id: String) {
        viewModelScope.launch {
            repository.deleteProject(id)
            if (_activeProject.value?.id == id) {
                closeProject()
            }
        }
    }

    private fun pushUndo() {
        _activeProject.value?.let { current ->
            undoStack.add(current)
            if (undoStack.size > 25) {
                undoStack.removeAt(0)
            }
            redoStack.clear()
            updateUndoRedoStates()
        }
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val current = _activeProject.value ?: return
            redoStack.add(current)
            val previous = undoStack.removeAt(undoStack.lastIndex)
            applyProjectUpdate(previous, saveUndo = false)
            updateUndoRedoStates()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val current = _activeProject.value ?: return
            undoStack.add(current)
            val next = redoStack.removeAt(redoStack.lastIndex)
            applyProjectUpdate(next, saveUndo = false)
            updateUndoRedoStates()
        }
    }

    private fun updateUndoRedoStates() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }

    private fun applyProjectUpdate(updated: Project, saveUndo: Boolean = true) {
        _activeProject.value = updated
        viewModelScope.launch {
            repository.saveProject(updated)
        }
        if (saveUndo) {
            updateUndoRedoStates()
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.stopAll()
    }
}
