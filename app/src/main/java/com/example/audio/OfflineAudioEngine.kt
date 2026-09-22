package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.model.AudioSoundPreset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * High-performance 100% offline PCM audio synthesizer for BGM and SFX playback
 * in Androidxcut without requiring external cloud audio or large asset downloads.
 */
class OfflineAudioEngine(private val scope: CoroutineScope) {

    private val sampleRate = 44100
    private var activeBgmTrack: AudioTrack? = null
    private var activeSfxTrack: AudioTrack? = null
    private var bgmJob: Job? = null
    private var sfxJob: Job? = null

    fun playSfx(preset: AudioSoundPreset, volume: Float = 1.0f) {
        sfxJob?.cancel()
        sfxJob = scope.launch(Dispatchers.Default) {
            try {
                activeSfxTrack?.release()
                val samples = generateSfxSamples(preset)
                val bufferSize = samples.size * 2
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize.coerceAtLeast(1024))
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.setVolume(volume.coerceIn(0f, 1f))
                track.write(samples, 0, samples.size)
                track.play()
                activeSfxTrack = track
            } catch (_: Exception) {}
        }
    }

    fun startBgm(preset: AudioSoundPreset, volume: Float = 0.8f) {
        bgmJob?.cancel()
        bgmJob = scope.launch(Dispatchers.Default) {
            try {
                activeBgmTrack?.release()
                val minBuffer = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBuffer * 4)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                track.setVolume(volume.coerceIn(0f, 1f))
                track.play()
                activeBgmTrack = track

                val chunkDuration = 0.1 // 100ms per chunk
                val chunkSize = (sampleRate * chunkDuration).toInt()
                val buffer = ShortArray(chunkSize)
                var globalSample = 0L

                val bpm = when (preset) {
                    AudioSoundPreset.CYBER_BEAT -> 128
                    AudioSoundPreset.LOFI_SUNSET -> 85
                    AudioSoundPreset.CHILLHOP_GROOVE -> 92
                    AudioSoundPreset.EPIC_RISE -> 120
                    else -> 100
                }
                val beatInterval = (sampleRate * 60.0 / bpm).toInt()

                while (isActive) {
                    for (i in 0 until chunkSize) {
                        val t = (globalSample + i).toDouble() / sampleRate
                        val beatPos = (globalSample + i) % beatInterval
                        val beatFrac = beatPos.toDouble() / beatInterval

                        // Procedural beat synthesis (Kick, HiHat, Synth Lead, Bass)
                        val sampleVal = when (preset) {
                            AudioSoundPreset.CYBER_BEAT -> {
                                val kick = if (beatFrac < 0.2) {
                                    val kickFreq = 140.0 * exp(-beatFrac * 18.0)
                                    sin(2.0 * PI * kickFreq * t) * (1.0 - beatFrac / 0.2)
                                } else 0.0
                                val bassFreq = 55.0 * (if (((globalSample + i) / (beatInterval * 4)) % 2 == 0L) 1.0 else 1.25)
                                val bass = 0.5 * sin(2.0 * PI * bassFreq * t)
                                val leadFreq = 440.0 * (1.0 + 0.25 * (((globalSample + i) / (beatInterval / 2)) % 4))
                                val lead = 0.2 * sin(2.0 * PI * leadFreq * t)
                                (kick * 0.5 + bass * 0.35 + lead * 0.15)
                            }
                            AudioSoundPreset.LOFI_SUNSET -> {
                                val chordFreq = 220.0 * (if (((globalSample + i) / (beatInterval * 2)) % 2 == 0L) 1.0 else 1.189)
                                val pad = 0.4 * sin(2.0 * PI * chordFreq * t) + 0.2 * sin(2.0 * PI * chordFreq * 1.5 * t)
                                val softKick = if (beatFrac < 0.25) {
                                    sin(2.0 * PI * 80.0 * exp(-beatFrac * 10.0) * t) * (1.0 - beatFrac / 0.25)
                                } else 0.0
                                (pad * 0.6 + softKick * 0.4)
                            }
                            AudioSoundPreset.CHILLHOP_GROOVE -> {
                                val bass = 0.4 * sin(2.0 * PI * 65.0 * t)
                                val pianoFreq = 330.0 * (1.0 + (((globalSample + i) / (beatInterval)) % 3) * 0.2)
                                val keys = 0.25 * sin(2.0 * PI * pianoFreq * t)
                                (bass * 0.5 + keys * 0.5)
                            }
                            else -> {
                                0.3 * sin(2.0 * PI * 110.0 * t)
                            }
                        }

                        buffer[i] = (sampleVal.coerceIn(-1.0, 1.0) * 32000).toInt().toShort()
                    }
                    track.write(buffer, 0, buffer.size)
                    globalSample += chunkSize
                }
            } catch (_: Exception) {}
        }
    }

    fun stopBgm() {
        bgmJob?.cancel()
        bgmJob = null
        try {
            activeBgmTrack?.stop()
            activeBgmTrack?.release()
            activeBgmTrack = null
        } catch (_: Exception) {}
    }

    fun stopAll() {
        stopBgm()
        sfxJob?.cancel()
        sfxJob = null
        try {
            activeSfxTrack?.stop()
            activeSfxTrack?.release()
            activeSfxTrack = null
        } catch (_: Exception) {}
    }

    private fun generateSfxSamples(preset: AudioSoundPreset): ShortArray {
        val durationSec = preset.defaultDurationMs / 1000.0
        val count = (sampleRate * durationSec).toInt()
        val data = ShortArray(count)

        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / count
            val envelope = (1.0 - progress).coerceIn(0.0, 1.0)

            val v = when (preset) {
                AudioSoundPreset.WHOOSH_FAST -> {
                    // White noise swept bandpass
                    val noise = (Math.random() * 2.0 - 1.0)
                    val sweep = sin(PI * progress)
                    noise * sweep * 0.7
                }
                AudioSoundPreset.CAMERA_CLICK -> {
                    if (progress < 0.15) {
                        (Math.random() * 2.0 - 1.0) * (1.0 - progress / 0.15)
                    } else if (progress in 0.3..0.45) {
                        (Math.random() * 2.0 - 1.0) * (1.0 - (progress - 0.3) / 0.15)
                    } else 0.0
                }
                AudioSoundPreset.GLITCH_ZAP -> {
                    val buzz = sin(2.0 * PI * (220.0 + (i % 80) * 40.0) * t)
                    val crackle = (Math.random() * 2.0 - 1.0) * 0.4
                    (buzz * 0.6 + crackle) * envelope
                }
                AudioSoundPreset.IMPACT_BOOM -> {
                    val pitch = 90.0 * exp(-progress * 6.0)
                    val rumble = sin(2.0 * PI * pitch * t)
                    val noise = (Math.random() * 2.0 - 1.0) * exp(-progress * 12.0)
                    (rumble * 0.7 + noise * 0.3) * envelope
                }
                AudioSoundPreset.BELL_CHIME -> {
                    val f = 880.0
                    (0.6 * sin(2.0 * PI * f * t) + 0.3 * sin(2.0 * PI * f * 2.0 * t) + 0.1 * sin(2.0 * PI * f * 3.0 * t)) *
                            exp(-progress * 4.0)
                }
                else -> 0.0
            }
            data[i] = (v.coerceIn(-1.0, 1.0) * 32000).toInt().toShort()
        }
        return data
    }
}
