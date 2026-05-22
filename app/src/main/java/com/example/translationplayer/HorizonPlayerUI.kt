package com.example.translationplayer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- GRAY + WHITE COLOR SCHEME ---
val CardSurface = Color(0xFF1A1A1A)
val CardBorder = Color(0xFF2A2A2A)
val TextPrimary = Color(0xFFF5F5F5)
val TextSecondary = Color(0xFFB0B0B0)
val TextTertiary = Color(0xFF909090)
val ProgressTrack = Color(0xFF333333)
val ProgressFilled = Color(0xFFE0E0E0)
val ControlButtonBg = Color(0xFF2A2A2A)
val PlayPauseBg = Color(0xFFE0E0E0)
val PlayPauseIcon = Color(0xFF1A1A1A)

// ============================================================
//  AUDIO VISUALIZATION — Smooth Waveform Line
//  A continuous curvy waveform drawn on Canvas that undulates
//  to feel alive during playback.
// ============================================================
@Composable
fun WaveformLine(
    progressFraction: Float,
    modifier: Modifier = Modifier
) {
    val pointCount = 36
    val yOffsets = remember {
        mutableStateListOf<Float>().apply {
            repeat(pointCount) { add((0..40).random() / 100f) }
        }
    }

    // Animate the waveform curve every 120ms for a live feel
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(120)
            for (i in yOffsets.indices) {
                yOffsets[i] = (yOffsets[i] + ((0..30).random() - 15) / 200f)
                    .coerceIn(0.1f, 0.9f)
            }
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
    ) {
        val w = size.width
        val h = size.height
        val stepX = w / (pointCount - 1).toFloat()
        val midY = h / 2f
        val amp = h * 0.4f

        // Build all waveform points
        val pts = List(pointCount) { i ->
            Offset(i * stepX, midY + (yOffsets[i] - 0.5f) * amp)
        }

        val splitIdx = (progressFraction * pointCount).toInt().coerceIn(1, pointCount - 1)

        // ── Played portion (light gray fill) ──
        if (progressFraction > 0f) {
            val p = Path()
            p.moveTo(pts[0].x, midY)
            p.lineTo(pts[0].x, pts[0].y)
            for (i in 0 until splitIdx) {
                val cx = (pts[i].x + pts[i + 1].x) / 2f
                p.cubicTo(cx, pts[i].y, cx, pts[i + 1].y, pts[i + 1].x, pts[i + 1].y)
            }
            p.lineTo(pts[splitIdx].x, midY)
            p.close()
            drawPath(p, color = ProgressFilled)
        }

        // ── Unplayed portion (dark gray fill) ──
        if (progressFraction < 1f && splitIdx < pointCount - 1) {
            val p = Path()
            p.moveTo(pts[splitIdx].x, midY)
            p.lineTo(pts[splitIdx].x, pts[splitIdx].y)
            for (i in splitIdx until pointCount - 1) {
                val cx = (pts[i].x + pts[i + 1].x) / 2f
                p.cubicTo(cx, pts[i].y, cx, pts[i + 1].y, pts[i + 1].x, pts[i + 1].y)
            }
            p.lineTo(pts.last().x, midY)
            p.close()
            drawPath(p, color = ProgressTrack)
        }

        // ── Playhead line ──
        val px = progressFraction * w
        drawLine(
            color = Color.White.copy(alpha = 0.18f),
            start = Offset(px, 0f),
            end = Offset(px, h),
            strokeWidth = 1.dp.toPx()
        )
    }
}

// ============================================================
//  MINIMAL MUSIC PLAYER CARD — Gray + White Theme
//  Layout:
//   1. Song Title
//   2. Status line: loop:X | quote | speed:Yx
//   3. Audio Visualization (Waveform)
//   4. Time labels
//   5. Backward ◀️  Play/Pause ▶️  Forward only
// ============================================================
@Composable
fun HorizonMusicPlayer(
    isPlaying: Boolean,
    currentTime: Int,
    duration: Int,
    currentSpeed: Float,
    statusLine: String = "",
    onPlayPause: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progressFraction = if (duration > 0) currentTime.toFloat() / duration else 0f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(CardSurface)
            .border(
                width = 0.5.dp,
                color = CardBorder,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
    ) {
        // Subtle top border glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
                .align(Alignment.TopCenter)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ───────────── 1. SONG TITLE ─────────────
            Text(
                text = "The Middle",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // ───────────── 2. STATUS LINE ─────────────
            // Format: loop:1 | "quote text" | speed:2x
            Text(
                text = statusLine.ifEmpty { "${currentSpeed}x" },
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                letterSpacing = 0.3.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ───────────── 3. AUDIO VISUALIZATION ─────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                WaveformLine(
                    progressFraction = progressFraction,
                    modifier = Modifier.fillMaxWidth()
                )

                // Time labels below waveform
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = String.format("%d:%02d", currentTime / 60, currentTime % 60),
                        color = TextTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = String.format("%d:%02d", duration / 60, duration % 60),
                        color = TextTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ───────────── 4. CONTROLS ─────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Backward
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ControlButtonBg)
                        .border(0.5.dp, CardBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { onRewind() },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastRewind,
                            contentDescription = "Rewind 5s",
                            tint = TextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(28.dp))

                // Play/Pause
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(PlayPauseBg),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { onPlayPause() },
                        modifier = Modifier.size(60.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = PlayPauseIcon,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(28.dp))

                // Forward
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ControlButtonBg)
                        .border(0.5.dp, CardBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { onForward() },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Forward 5s",
                            tint = TextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
