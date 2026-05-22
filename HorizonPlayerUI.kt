package com.example.translationplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- NEW COLOR EXTENSIONS ---
val PlayerCardBg = Color(0xFF0A0E17)
val PlayerBorder = Color(0xFF1A1F2E)
val PlayerAccentGlow = Color(0xFF10B981)
val PlayerTextPrimary = Color(0xFFF1F1F6)
val PlayerTextSecondary = Color(0xFF6B7280)
val PlayerControlBg = Color(0xFF1A1F2E)
val PlayerProgressTrack = Color(0xFF1F2937)
val PlayerProgressFilled = Color(0xFF10B981)
val PlayerHeartActive = Color(0xFFEF4444)
val PlayerShuffleActive = Color(0xFF10B981)
val PlayerRepeatActive = Color(0xFF10B981)

// --- MOCK ALBUM ART GRADIENT (placeholder for actual image) ---
val AlbumArtGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF10B981),
        Color(0xFF059669),
        Color(0xFF047857),
        Color(0xFF065F46)
    )
)

// --- MOCK ALBUM ART PLACEHOLDER ---
@Composable
fun AlbumArtPlaceholder(size: Int) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AlbumArtGradient),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = "Album Art",
            tint = Color.White.copy(alpha = 0.3f),
            modifier = Modifier.size((size * 0.45).dp)
        )
        Box(
            modifier = Modifier
                .size((size * 0.75).dp)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.1f),
                    shape = CircleShape
                )
        )
    }
}

// --- WAVEFORM VISUALIZER (animated with playback progress) ---
@Composable
fun MiniWaveform(
    modifier: Modifier = Modifier,
    barCount: Int = 24,
    currentTime: Int = 0,
    duration: Int = 30,
    accentColor: Color = PlayerProgressFilled,
    inactiveColor: Color = PlayerProgressTrack
) {
    val heights = remember {
        List(barCount) {
            listOf(
                0.15f, 0.35f, 0.20f, 0.55f, 0.70f, 0.30f, 0.25f, 0.45f,
                0.80f, 0.55f, 0.40f, 0.65f, 0.90f, 0.50f, 0.28f, 0.48f,
                0.72f, 0.85f, 0.60f, 0.38f, 0.50f, 0.75f, 0.45f, 0.30f
            )[it % 24]
        }
    }

    val completedThreshold = if (duration > 0) (currentTime * barCount / duration) else 0

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        heights.forEachIndexed { index, height ->
            val isCompleted = index <= completedThreshold
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(height)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isCompleted) accentColor.copy(alpha = 0.7f)
                        else inactiveColor
                    )
            )
        }
    }
}

// ============================================================
//  THE MAIN NEW PLAYER COMPOSABLE — Clean, Organized, Modern
// ============================================================
@Composable
fun HorizonMusicPlayer(
    isPlaying: Boolean,
    currentTime: Int,
    duration: Int,
    currentSpeed: Float,
    onPlayPause: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // --- Internal Player State ---
    var isLiked by remember { mutableStateOf(false) }
    var isShuffled by remember { mutableStateOf(false) }
    var repeatMode by remember { mutableStateOf(0) } // 0 = none, 1 = all, 2 = one

    val progressFraction = if (duration > 0) currentTime.toFloat() / duration else 0f

    // Track the progress bar's pixel width for click-to-seek
    var progressBarSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(PlayerCardBg)
            .border(
                width = 1.dp,
                color = PlayerBorder,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            )
    ) {
        // Subtle top glow line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            PlayerAccentGlow.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
                .align(Alignment.TopCenter)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ───────────── ROW 1: Album Art + Track Info + Like ─────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Album Art
                AlbumArtPlaceholder(size = 52)

                // Track Info (takes remaining space)
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "The Middle",
                        color = PlayerTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Zedd, Maren Morris, Grey",
                        color = PlayerTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isPlaying) "Now Playing" else "Paused",
                        color = PlayerAccentGlow.copy(alpha = 0.8f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Like Button
                IconButton(
                    onClick = { isLiked = !isLiked },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) PlayerHeartActive else PlayerTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // ───────────── ROW 2: Seekable Progress Bar ─────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Seekable progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(PlayerProgressTrack)
                        .onSizeChanged { progressBarSize = it }
                        .pointerInput(duration) {
                            detectTapGestures { tapOffset ->
                                if (progressBarSize.width > 0 && duration > 0) {
                                    val fraction = (tapOffset.x / progressBarSize.width)
                                        .coerceIn(0f, 1f)
                                    val seekTime = (fraction * duration).toInt()
                                    onSeek(seekTime)
                                }
                            }
                        }
                ) {
                    // Filled portion
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressFraction.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        PlayerProgressFilled,
                                        PlayerProgressFilled.copy(alpha = 0.7f)
                                    )
                                )
                            )
                    )
                    // Playhead knob positioned at the right edge of the filled portion
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressFraction.coerceIn(0f, 1f))
                            .fillMaxHeight(),
                        contentAlignment = Alignment.CenterEnd
                    ) {                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                .background(Color.White)
                                .border(2.dp, PlayerAccentGlow, CircleShape)
                        )
                    }
                }

                // Time labels + speed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = String.format("%d:%02d", currentTime / 60, currentTime % 60),
                        color = PlayerTextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${currentSpeed}x",
                        color = PlayerAccentGlow,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = String.format("%d:%02d", duration / 60, duration % 60),
                        color = PlayerTextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // ───────────── ROW 3: Mini Waveform (animated with progress) ─────────────
            MiniWaveform(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .padding(horizontal = 4.dp),
                barCount = 24,
                currentTime = currentTime,
                duration = duration,
                accentColor = PlayerAccentGlow
            )

            // ───────────── ROW 4: Main Controls + Secondary Controls ─────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Secondary controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle
                    IconButton(
                        onClick = { isShuffled = !isShuffled },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (isShuffled) PlayerShuffleActive else PlayerTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    // Repeat (cycles: off → all → one)
                    IconButton(
                        onClick = { repeatMode = (repeatMode + 1) % 3 },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = when (repeatMode) {
                                2 -> Icons.Default.RepeatOne
                                else -> Icons.Default.Repeat
                            },
                            contentDescription = "Repeat",
                            tint = if (repeatMode > 0) PlayerRepeatActive else PlayerTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Center: Primary controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Previous track
                    IconButton(
                        onClick = { onPrevious() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = PlayerTextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Rewind -5s
                    IconButton(
                        onClick = { onRewind() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastRewind,
                            contentDescription = "Rewind 5s",
                            tint = PlayerTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Play/Pause (main button)
                    IconButton(
                        onClick = { onPlayPause() },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        PlayerAccentGlow,
                                        PlayerAccentGlow.copy(alpha = 0.8f)
                                    )
                                )
                            )
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Forward +5s
                    IconButton(
                        onClick = { onForward() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Forward 5s",
                            tint = PlayerTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Next track
                    IconButton(
                        onClick = { onNext() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = PlayerTextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Right: Queue button
                IconButton(
                    onClick = { /* Queue */ },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = "Queue",
                        tint = PlayerTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // ───────────── ROW 5: Status indicator chip ─────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PlayerControlBg)
                        .border(1.dp, PlayerBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Speed ${currentSpeed}x  •  No Loop  •  ${if (isPlaying) "Playing" else "Paused"}",
                        color = PlayerTextSecondary,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
