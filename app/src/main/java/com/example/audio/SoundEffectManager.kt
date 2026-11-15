package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Procedural 16-bit PCM arcade sound synthesizer using Android AudioTrack.
 * Produces crisp, zero-dependency tactile sound effects for shooting, bouncing,
 * popping, combo praise ("AWESOME!", "EXCELLENT!"), explosions, lightning, and rewards.
 */
class SoundEffectManager {

    @Volatile
    var isEnabled: Boolean = true

    private val executor = Executors.newSingleThreadExecutor()
    private val sampleRate = 22050

    fun playShoot() {
        if (!isEnabled) return
        playAsync {
            val samples = generateSweep(
                startFreq = 260.0,
                endFreq = 680.0,
                durationMs = 95,
                volume = 0.28f
            )
            playPcm(samples)
        }
    }

    fun playBounce() {
        if (!isEnabled) return
        playAsync {
            val samples = generateSweep(
                startFreq = 520.0,
                endFreq = 390.0,
                durationMs = 45,
                volume = 0.20f
            )
            playPcm(samples)
        }
    }

    fun playPop(clusterSize: Int, combo: Int) {
        if (!isEnabled) return
        playAsync {
            val baseFreq = (460.0 + (combo.coerceAtMost(6) * 55.0)).coerceAtMost(920.0)
            val count = clusterSize.coerceIn(1, 5)
            val totalSamples = mutableListOf<Short>()
            for (i in 0 until count) {
                val freq = baseFreq + i * 75.0
                val pop = generateSweep(
                    startFreq = freq * 0.75,
                    endFreq = freq * 1.45,
                    durationMs = 55,
                    volume = 0.30f
                )
                totalSamples.addAll(pop.toList())
            }
            playPcm(totalSamples.toShortArray())
        }
    }

    fun playPraise(tier: Int) {
        if (!isEnabled) return
        playAsync {
            // Musical celebratory arpeggio scaling with praise tier (Nice/Awesome/Excellent/Fantastic)
            val notes = when {
                tier >= 4 -> listOf(523.25, 659.25, 783.99, 1046.50, 1318.51) // C5-E5-G5-C6-E6
                tier == 3 -> listOf(523.25, 659.25, 783.99, 1046.50) // C5-E5-G5-C6
                tier == 2 -> listOf(440.00, 554.37, 659.25) // A4-C#5-E5
                else -> listOf(523.25, 659.25) // C5-E5
            }
            val combined = mutableListOf<Short>()
            notes.forEachIndexed { idx, freq ->
                val dur = if (idx == notes.lastIndex) 170 else 68
                combined.addAll(generateTone(freq, dur, volume = 0.26f, harmonic = true).toList())
            }
            playPcm(combined.toShortArray())
        }
    }

    fun playDropCascade(dropCount: Int) {
        if (!isEnabled) return
        playAsync {
            val steps = dropCount.coerceIn(2, 6)
            val combined = mutableListOf<Short>()
            for (i in 0 until steps) {
                val freq = 600.0 + i * 90.0
                combined.addAll(generateTone(freq, 50, volume = 0.22f, harmonic = true).toList())
            }
            playPcm(combined.toShortArray())
        }
    }

    fun playExplosion() {
        if (!isEnabled) return
        playAsync {
            val numSamples = (sampleRate * 240) / 1000
            val out = ShortArray(numSamples)
            val rand = Random(System.nanoTime())
            var lowPass = 0.0
            for (i in 0 until numSamples) {
                val t = i.toDouble() / numSamples
                val env = exp(-t * 5.0)
                val white = rand.nextDouble(-1.0, 1.0)
                lowPass = lowPass * 0.85 + white * 0.15
                val sub = sin(2.0 * PI * (120.0 - t * 80.0) * (i.toDouble() / sampleRate))
                val sample = (lowPass * 0.7 + sub * 0.5) * env * 0.55
                out[i] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
            }
            playPcm(out)
        }
    }

    fun playLightning() {
        if (!isEnabled) return
        playAsync {
            val zap1 = generateSweep(1500.0, 420.0, 90, 0.28f)
            val zap2 = generateSweep(1800.0, 620.0, 110, 0.28f)
            playPcm(zap1 + zap2)
        }
    }

    fun playLevelWin() {
        if (!isEnabled) return
        playAsync {
            val melody = listOf(
                523.25 to 90,
                659.25 to 90,
                783.99 to 90,
                1046.50 to 160,
                783.99 to 80,
                1046.50 to 260
            )
            val combined = mutableListOf<Short>()
            melody.forEach { (freq, dur) ->
                combined.addAll(generateTone(freq, dur, 0.28f, harmonic = true).toList())
            }
            playPcm(combined.toShortArray())
        }
    }

    fun playLevelFail() {
        if (!isEnabled) return
        playAsync {
            val notes = listOf(392.0 to 120, 349.23 to 120, 311.13 to 140, 261.63 to 240)
            val combined = mutableListOf<Short>()
            notes.forEach { (freq, dur) ->
                combined.addAll(generateTone(freq, dur, 0.24f, harmonic = false).toList())
            }
            playPcm(combined.toShortArray())
        }
    }

    fun playCoinPurchase() {
        if (!isEnabled) return
        playAsync {
            val c1 = generateTone(987.77, 65, 0.24f, harmonic = true) // B5
            val c2 = generateTone(1318.51, 160, 0.26f, harmonic = true) // E6
            playPcm(c1 + c2)
        }
    }

    fun playUnlock() {
        if (!isEnabled) return
        playAsync {
            val notes = listOf(587.33, 739.99, 880.0, 1174.66) // D major sparkle
            val combined = mutableListOf<Short>()
            notes.forEachIndexed { i, freq ->
                combined.addAll(generateTone(freq, if (i == 3) 220 else 75, 0.26f, true).toList())
            }
            playPcm(combined.toShortArray())
        }
    }

    fun playButtonClick() {
        if (!isEnabled) return
        playAsync {
            val click = generateSweep(600.0, 760.0, 32, 0.16f)
            playPcm(click)
        }
    }

    private fun playAsync(block: () -> Unit) {
        try {
            executor.execute {
                try {
                    block()
                } catch (e: Throwable) {
                    Log.w("SoundEffectManager", "Audio playback skipped: ${e.message}")
                }
            }
        } catch (_: Throwable) {
        }
    }

    private fun generateSweep(
        startFreq: Double,
        endFreq: Double,
        durationMs: Int,
        volume: Float
    ): ShortArray {
        val count = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(count)
        var phase = 0.0
        for (i in 0 until count) {
            val progress = i.toDouble() / count
            val freq = startFreq + (endFreq - startFreq) * progress
            phase += 2.0 * PI * freq / sampleRate
            val env = sin(PI * progress) // smooth bell envelope
            val sample = sin(phase) * env * volume
            buffer[i] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        return buffer
    }

    private fun generateTone(
        freq: Double,
        durationMs: Int,
        volume: Float,
        harmonic: Boolean
    ): ShortArray {
        val count = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / count
            val attack = (progress * 12.0).coerceAtMost(1.0)
            val decay = exp(-progress * 3.2)
            val env = attack * decay
            var wave = sin(2.0 * PI * freq * t)
            if (harmonic) {
                wave = wave * 0.72 + sin(2.0 * PI * freq * 2.0 * t) * 0.28
            }
            val sample = wave * env * volume
            buffer[i] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        return buffer
    }

    private fun playPcm(samples: ShortArray) {
        if (samples.isEmpty()) return
        val minBuf = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(samples.size * 2)

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
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
            .setBufferSizeInBytes(minBuf)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        try {
            track.write(samples, 0, samples.size)
            track.play()
            val sleepMs = ((samples.size.toLong() * 1000L) / sampleRate) + 25L
            Thread.sleep(sleepMs)
        } finally {
            try {
                track.stop()
                track.release()
            } catch (_: Throwable) {
            }
        }
    }

    fun release() {
        executor.shutdownNow()
    }
}
