package com.example.translationplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ============================================================
//  DARK THEME PLAYER CARD COLORS
// ============================================================
private val PCardBg = Color(0xFF1A1A1A)
private val PAccent = Color(0xFFCCCCCC)
private val PWaveActive = Color(0xFFCCCCCC)
private val PWaveInactive = Color(0xFF333333)
private val PText = Color(0xFFE8E8E8)

@Composable
fun HorizonMusicPlayer(
    songTitle: String = "The Middle",
    currentTime: Int,
    duration: Int,
    isPlaying: Boolean,
    currentSpeed: Float,
    statusLine: String = "",
    onPlayPause: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onSeek: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val waveformHeights = listOf(
        25, 45, 15, 60, 75, 40, 30, 50, 85, 60, 45, 70, 95, 55, 30, 50, 75, 90, 65, 40, 55, 80, 50, 35, 40, 60, 30, 45, 20, 35
    )

    val statusSegments = remember(statusLine) {
        if (statusLine.isNotBlank()) {
            statusLine.split(" | ").map { it.trim() }
        } else {
            emptyList()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(190.dp)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(PCardBg)
            .border(
                width = 1.dp,
                color = Color(0xFF333333),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .padding(vertical = 16.dp, horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // --- ZONE 1: Title & Status ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = songTitle,
                    color = PText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (statusSegments.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        statusSegments.forEachIndexed { idx, segment ->
                            if (idx > 0) {
                                Text(
                                    text = " | ",
                                    color = PText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            val colonIdx = segment.indexOf(':')
                            if (colonIdx >= 0) {
                                val label = segment.substring(0, colonIdx)
                                val value = segment.substring(colonIdx + 1)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = label,
                                        color = PText,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = ":",
                                        color = PText,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = value,
                                        color = PText,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Text(
                                    text = segment,
                                    color = PText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "${currentSpeed}x",
                        color = PText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // --- ZONE 2: Interactive Waveform ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = String.format("%d:%02d", currentTime / 60, currentTime % 60),
                    color = PText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                                onSeek(fraction)
                            }
                        }
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { change, _ ->
                                change.consume()
                                val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                                onSeek(fraction)
                            }
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    waveformHeights.forEachIndexed { i, height ->
                        val isCompleted = (i <= (currentTime * waveformHeights.size / duration))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(height / 100f)
                                .padding(horizontal = 1.dp)
                                .clip(RoundedCornerShape(99.dp))
                                .background(if (isCompleted) PWaveActive else PWaveInactive)
                        )
                    }
                }
                Text(
                    text = String.format("%d:%02d", duration / 60, duration % 60),
                    color = PText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // --- ZONE 3: Media Controls ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onRewind,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "Rewind",
                        tint = PText,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(20.dp))
                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(PAccent)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "PlayPause",
                        tint = Color(0xFF1A1A1A),
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(20.dp))
                IconButton(
                    onClick = onForward,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Forward",
                        tint = PText,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
