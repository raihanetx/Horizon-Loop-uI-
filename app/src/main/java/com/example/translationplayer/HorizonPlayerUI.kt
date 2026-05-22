package com.example.translationplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Gray/White accent colors for the player card
private val PlayerCardBg = Color(0xFF1A1A1A)
private val PlayerAccent = Color(0xFFCCCCCC)
private val PlayerWaveActive = Color(0xFFBBBBBB)
private val PlayerWaveInactive = Color(0xFF444444)

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
    // Waveform Column Sizing Percentages
    val waveformHeights = listOf(
        25, 45, 15, 60, 75, 40, 30, 50, 85, 60, 45, 70, 95, 55, 30, 50, 75, 90, 65, 40, 55, 80, 50, 35, 40, 60, 30, 45, 20, 35
    )

    // Parse status line into segments separated by ` | `
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
            .background(PlayerCardBg)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .padding(vertical = 16.dp, horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // --- ZONE 1: Title & Status Section ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = songTitle,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Dynamic status line with | separators
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
                                    color = Color.Gray.copy(alpha = 0.3f),
                                    fontSize = 11.sp,
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
                                        color = Color.Gray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.4.sp
                                    )
                                    Text(
                                        text = ":",
                                        color = Color.Gray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = value,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.4.sp
                                    )
                                }
                            } else {
                                Text(
                                    text = segment,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.4.sp
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "${currentSpeed}x",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.4.sp
                    )
                }
            }

            // --- ZONE 2: Waveform Progress Section ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = String.format("%d:%02d", currentTime / 60, currentTime % 60),
                    color = Color.Gray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
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
                                .background(if (isCompleted) PlayerWaveActive else PlayerWaveInactive)
                        )
                    }
                }
                Text(
                    text = String.format("%d:%02d", duration / 60, duration % 60),
                    color = Color.Gray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // --- ZONE 3: Media Control Buttons ---
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
                        tint = Color.LightGray,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(20.dp))
                // Circular Play/Pause Button — gray instead of green
                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(PlayerAccent)
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
                        tint = Color.LightGray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
