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
import org.json.JSONArray
import org.json.JSONObject

object ProjectConverters {

    fun projectToEntity(project: Project): ProjectEntity {
        val clipsArray = JSONArray()
        project.clips.forEach { clip ->
            val obj = JSONObject()
            obj.put("id", clip.id)
            obj.put("title", clip.title)
            obj.put("uri", clip.uri ?: "")
            obj.put("demoPreset", clip.demoPreset?.name ?: "")
            obj.put("sourceDurationMs", clip.sourceDurationMs)
            obj.put("trimStartMs", clip.trimStartMs)
            obj.put("trimEndMs", clip.trimEndMs)
            obj.put("speed", clip.speed.toDouble())
            obj.put("volume", clip.volume.toDouble())
            obj.put("rotation", clip.rotation)
            obj.put("isMuted", clip.isMuted)
            obj.put("filter", clip.filter.name)
            obj.put("brightness", clip.brightness.toDouble())
            obj.put("contrast", clip.contrast.toDouble())
            obj.put("saturation", clip.saturation.toDouble())
            obj.put("vignette", clip.vignette.toDouble())
            obj.put("transition", clip.transition.name)
            obj.put("transitionDurationMs", clip.transitionDurationMs)
            clipsArray.put(obj)
        }

        val audioArray = JSONArray()
        project.audioTracks.forEach { audio ->
            val obj = JSONObject()
            obj.put("id", audio.id)
            obj.put("title", audio.title)
            obj.put("category", audio.category.name)
            obj.put("preset", audio.preset.name)
            obj.put("startTimelineMs", audio.startTimelineMs)
            obj.put("durationMs", audio.durationMs)
            obj.put("volume", audio.volume.toDouble())
            obj.put("isMuted", audio.isMuted)
            audioArray.put(obj)
        }

        val overlaysArray = JSONArray()
        project.overlays.forEach { overlay ->
            val obj = JSONObject()
            obj.put("id", overlay.id)
            obj.put("type", overlay.type.name)
            obj.put("text", overlay.text)
            obj.put("textStyle", overlay.textStyle.name)
            obj.put("textColorHex", overlay.textColorHex)
            obj.put("bgColorHex", overlay.bgColorHex)
            obj.put("stickerEmoji", overlay.stickerEmoji)
            obj.put("stickerTag", overlay.stickerTag)
            obj.put("xPercent", overlay.xPercent.toDouble())
            obj.put("yPercent", overlay.yPercent.toDouble())
            obj.put("scale", overlay.scale.toDouble())
            obj.put("startMs", overlay.startMs)
            obj.put("endMs", overlay.endMs)
            overlaysArray.put(obj)
        }

        return ProjectEntity(
            id = project.id,
            title = project.title,
            createdAt = project.createdAt,
            updatedAt = project.updatedAt,
            aspectRatio = project.aspectRatio.name,
            canvasBg = project.canvasBg.name,
            clipsJson = clipsArray.toString(),
            audioTracksJson = audioArray.toString(),
            overlaysJson = overlaysArray.toString(),
            thumbnailUri = project.clips.firstOrNull()?.uri
        )
    }

    fun entityToProject(entity: ProjectEntity): Project {
        val aspectRatio = try {
            AspectRatio.valueOf(entity.aspectRatio)
        } catch (_: Exception) {
            AspectRatio.RATIO_9_16
        }

        val canvasBg = try {
            CanvasBackground.valueOf(entity.canvasBg)
        } catch (_: Exception) {
            CanvasBackground.BLUR
        }

        val clips = mutableListOf<MediaClip>()
        try {
            val clipsArray = JSONArray(entity.clipsJson)
            for (i in 0 until clipsArray.length()) {
                val obj = clipsArray.getJSONObject(i)
                val uriStr = obj.optString("uri")
                val presetStr = obj.optString("demoPreset")
                val demoPreset = if (presetStr.isNotEmpty()) {
                    try { DemoClipPreset.valueOf(presetStr) } catch (_: Exception) { null }
                } else null

                val filterStr = obj.optString("filter", "NONE")
                val filter = try { VideoFilter.valueOf(filterStr) } catch (_: Exception) { VideoFilter.NONE }

                val transStr = obj.optString("transition", "NONE")
                val transition = try { TransitionType.valueOf(transStr) } catch (_: Exception) { TransitionType.NONE }

                clips.add(
                    MediaClip(
                        id = obj.optString("id"),
                        title = obj.optString("title", "Clip ${i + 1}"),
                        uri = if (uriStr.isNotEmpty()) uriStr else null,
                        demoPreset = demoPreset,
                        sourceDurationMs = obj.optLong("sourceDurationMs", 5000L),
                        trimStartMs = obj.optLong("trimStartMs", 0L),
                        trimEndMs = obj.optLong("trimEndMs", 5000L),
                        speed = obj.optDouble("speed", 1.0).toFloat(),
                        volume = obj.optDouble("volume", 1.0).toFloat(),
                        rotation = obj.optInt("rotation", 0),
                        isMuted = obj.optBoolean("isMuted", false),
                        filter = filter,
                        brightness = obj.optDouble("brightness", 0.0).toFloat(),
                        contrast = obj.optDouble("contrast", 1.0).toFloat(),
                        saturation = obj.optDouble("saturation", 1.0).toFloat(),
                        vignette = obj.optDouble("vignette", 0.0).toFloat(),
                        transition = transition,
                        transitionDurationMs = obj.optLong("transitionDurationMs", 500L)
                    )
                )
            }
        } catch (_: Exception) {}

        val audioTracks = mutableListOf<AudioTrack>()
        try {
            val audioArray = JSONArray(entity.audioTracksJson)
            for (i in 0 until audioArray.length()) {
                val obj = audioArray.getJSONObject(i)
                val catStr = obj.optString("category", "BGM")
                val cat = try { AudioCategory.valueOf(catStr) } catch (_: Exception) { AudioCategory.BGM }
                val presetStr = obj.optString("preset", "CYBER_BEAT")
                val preset = try { AudioSoundPreset.valueOf(presetStr) } catch (_: Exception) { AudioSoundPreset.CYBER_BEAT }

                audioTracks.add(
                    AudioTrack(
                        id = obj.optString("id"),
                        title = obj.optString("title", preset.title),
                        category = cat,
                        preset = preset,
                        startTimelineMs = obj.optLong("startTimelineMs", 0L),
                        durationMs = obj.optLong("durationMs", preset.defaultDurationMs),
                        volume = obj.optDouble("volume", 1.0).toFloat(),
                        isMuted = obj.optBoolean("isMuted", false)
                    )
                )
            }
        } catch (_: Exception) {}

        val overlays = mutableListOf<OverlayItem>()
        try {
            val overlaysArray = JSONArray(entity.overlaysJson)
            for (i in 0 until overlaysArray.length()) {
                val obj = overlaysArray.getJSONObject(i)
                val typeStr = obj.optString("type", "TEXT")
                val type = try { OverlayType.valueOf(typeStr) } catch (_: Exception) { OverlayType.TEXT }
                val styleStr = obj.optString("textStyle", "SANS")
                val style = try { OverlayTextStyle.valueOf(styleStr) } catch (_: Exception) { OverlayTextStyle.SANS }

                overlays.add(
                    OverlayItem(
                        id = obj.optString("id"),
                        type = type,
                        text = obj.optString("text", ""),
                        textStyle = style,
                        textColorHex = obj.optLong("textColorHex", 0xFFFFFFFF),
                        bgColorHex = obj.optLong("bgColorHex", 0xAA000000),
                        stickerEmoji = obj.optString("stickerEmoji", ""),
                        stickerTag = obj.optString("stickerTag", ""),
                        xPercent = obj.optDouble("xPercent", 0.0).toFloat(),
                        yPercent = obj.optDouble("yPercent", 0.0).toFloat(),
                        scale = obj.optDouble("scale", 1.0).toFloat(),
                        startMs = obj.optLong("startMs", 0L),
                        endMs = obj.optLong("endMs", 3000L)
                    )
                )
            }
        } catch (_: Exception) {}

        return Project(
            id = entity.id,
            title = entity.title,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            aspectRatio = aspectRatio,
            canvasBg = canvasBg,
            clips = clips,
            audioTracks = audioTracks,
            overlays = overlays
        )
    }
}
