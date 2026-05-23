package com.example.translationplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ============================================================
//  DARK THEME COLORS
// ============================================================
val BrandBg = Color(0xFF121212)        // Near-black background
val BrandCard = Color(0xFF1E1E1E)      // Dark card
val BrandAccent = Color(0xFFCCCCCC)    // Light gray accent
val DividerColor = Color(0xFF333333)   // Dark divider
val TextSecondary = Color(0xFF8E8E93)  // Gray secondary text
val TextPrimary = Color(0xFFF5F5F5)    // Near-white text

// ============================================================
//  DATA STRUCTURES
// ============================================================
data class DialogueLine(val start: Int, val en: String, val bn: String)
data class SavedLoop(val name: String, val start: Int, val end: Int, val repeats: Int)
data class SavedNote(val time: Int, val text: String)
data class SongItem(val title: String, val artist: String, val duration: Int)

enum class ActiveView {
    CLEAN, DIALOGUE, LOOP, NOTES
}

enum class Screen { HOME, PLAYER, PROFILE }

// ============================================================
//  MOCK DATASET
// ============================================================
val dialogueDataset = listOf(
    DialogueLine(0, "Take a step into my cabin", "আমার ঘরে একটি পদক্ষেপ নাও"),
    DialogueLine(6, "Take a deep breath, ease your mind", "দীর্ঘ শ্বাস নাও, মনকে শান্ত করো"),
    DialogueLine(12, "Baby, why don't you just meet me in the middle?", "প্রিয়, তুমি কেন আমার সাথে মাঝামাঝি জায়গায় এসে দেখা করছ না?"),
    DialogueLine(18, "I'm losing my mind just a little", "আমি আমার মানসিক নিয়ন্ত্রণ কিছুটা হারিয়ে ফেলছি"),
    DialogueLine(24, "So baby, pull me closer", "তাই প্রিয়, আমাকে তোমার আরও কাছে টেনে নাও")
)

val musicLibrary = listOf(
    SongItem("The Middle", "Maren Morris", 30),
    SongItem("Blinding Lights", "The Weeknd", 45),
    SongItem("Shape of You", "Ed Sheeran", 38),
    SongItem("Bohemian Rhapsody", "Queen", 55),
    SongItem("Hotel California", "Eagles", 42),
    SongItem("Imagine", "John Lennon", 35)
)

// ============================================================
//  PARENT — SCREEN NAVIGATION
// ============================================================
@Composable
fun HorizonApp() {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var previousScreen by remember { mutableStateOf(Screen.HOME) }
    var activeSongTitle by remember { mutableStateOf("The Middle") }

    when (currentScreen) {
        Screen.HOME -> HomeScreen(
            onSongClick = { song ->
                activeSongTitle = song.title
                currentScreen = Screen.PLAYER
            },
            onProfileClick = {
                previousScreen = Screen.HOME
                currentScreen = Screen.PROFILE
            }
        )
        Screen.PLAYER -> TranslationPlayerScreen(
            songTitle = activeSongTitle,
            onHomeClick = { currentScreen = Screen.HOME },
            onProfileClick = {
                previousScreen = Screen.PLAYER
                currentScreen = Screen.PROFILE
            }
        )
        Screen.PROFILE -> ProfileScreen(
            onBack = { currentScreen = previousScreen }
        )
    }
}

// ============================================================
//  HOME SCREEN — Music Library
// ============================================================
@Composable
fun HomeScreen(
    onSongClick: (SongItem) -> Unit,
    onProfileClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- HOME HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Profile icon + Name column
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(BrandAccent.copy(alpha = 0.1f))
                            .border(1.dp, DividerColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = BrandAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Alex Mercer",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Horizon Loop",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "|",
                                color = DividerColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Pro v1.2.0",
                                color = BrandAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // --- MUSIC LIST ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Your Library",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                    )
                }
                itemsIndexed(musicLibrary) { idx, song ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(BrandCard)
                            .border(1.dp, DividerColor, RoundedCornerShape(14.dp))
                            .clickable { onSongClick(song) }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DividerColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song.title,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = song.artist,
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = String.format("%d:%02d", song.duration / 60, song.duration % 60),
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = BrandAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
//  PROFILE SCREEN — Full Page
// ============================================================
@Composable
fun ProfileScreen(
    onBack: () -> Unit
) {
    var groqApiKey by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(BrandAccent.copy(alpha = 0.1f))
                        .border(1.dp, DividerColor, CircleShape)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = BrandAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "Settings",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // --- SETTINGS CONTENT ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Profile info card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(BrandCard)
                        .border(1.dp, DividerColor, RoundedCornerShape(14.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(BrandAccent.copy(alpha = 0.1f))
                            .border(1.dp, DividerColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = BrandAccent,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Alex Mercer",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Student",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Groq API Key
                Column {
                    Text(
                        text = "Groq Platform API Key",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = groqApiKey,
                        onValueChange = { groqApiKey = it },
                        placeholder = { Text("Enter your Groq API key...", fontSize = 12.sp, color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandAccent,
                            unfocusedBorderColor = DividerColor,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = BrandAccent
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Voice Engine
                Column {
                    Text(
                        text = "Voice Engine",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(BrandCard)
                            .border(1.dp, BrandAccent.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Whisper Large v3",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "OpenAI Whisper — highest accuracy",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = BrandAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
//  PLAYER SCREEN — Translation Player
// ============================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TranslationPlayerScreen(
    songTitle: String = "The Middle",
    onHomeClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var activeView by remember { mutableStateOf(ActiveView.CLEAN) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(0) }
    val duration = 30
    var speedIndex by remember { mutableStateOf(0) }
    val speeds = listOf(1.0f, 1.25f, 1.5f, 2.0f, 0.5f, 0.75f)
    val currentSpeed = speeds[speedIndex]

    var isLoopFormOpen by remember { mutableStateOf(false) }
    var isNoteFormOpen by remember { mutableStateOf(false) }
    val selectedDialogueIndices = remember { mutableStateListOf<Int>() }
    val savedLoops = remember { mutableStateListOf<SavedLoop>() }
    val savedNotes = remember { mutableStateListOf<SavedNote>() }

    var loopNameInput by remember { mutableStateOf("") }
    var loopStartInput by remember { mutableStateOf("0") }
    var loopEndInput by remember { mutableStateOf("0") }
    var loopRepeatsSelection by remember { mutableStateOf(1) }
    var noteTextInput by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val dialogueListState = rememberLazyListState()

    val activeDialogueIndex = remember(currentTime) {
        var index = 0
        for (i in dialogueDataset.indices) {
            if (currentTime >= dialogueDataset[i].start) {
                index = i
            }
        }
        index
    }

    val statusLine = remember(currentSpeed, activeView, savedLoops.size) {
        val viewName = activeView.name.lowercase()
        val loopWord = when (savedLoops.size) {
            0 -> "zero"
            1 -> "one"
            2 -> "two"
            3 -> "three"
            else -> savedLoops.size.toString()
        }
        "Mode:$viewName | Loop: $loopWord | Speed:${currentSpeed}X"
    }

    LaunchedEffect(isPlaying, currentSpeed) {
        if (isPlaying) {
            while (currentTime < duration) {
                delay((1000f / currentSpeed).toLong())
                currentTime = (currentTime + 1).coerceAtMost(duration)
            }
            isPlaying = false
        }
    }

    LaunchedEffect(activeDialogueIndex) {
        if (activeView == ActiveView.DIALOGUE) {
            coroutineScope.launch {
                dialogueListState.animateScrollToItem(activeDialogueIndex)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 190.dp)
        ) {
            // ================================================================
            //  HEADER: Profile info + Left-aligned icons (no background)
            // ================================================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                // --- Top: Back + Profile ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(BrandAccent.copy(alpha = 0.1f))
                                .border(1.dp, DividerColor, CircleShape)
                                .clickable { onHomeClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Home",
                                tint = BrandAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(BrandAccent.copy(alpha = 0.1f))
                                .border(1.dp, DividerColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = BrandAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Alex Mercer",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Horizon Loop",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "|",
                                    color = DividerColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Pro v1.2.0",
                                    color = BrandAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    // Right side: Send + Profile icons
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = { /* Navigate to translation */ },
                            modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Send, contentDescription = "Send",
                                tint = BrandAccent, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = { onProfileClick() },
                            modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Person, contentDescription = "Profile",
                                tint = BrandAccent, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Icon Row: Speed, Mode, Loop, Notes — LEFT-ALIGNED, NO BACKGROUND ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Speed
                    Text(
                        text = "${currentSpeed}x",
                        color = BrandAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(BrandAccent.copy(alpha = 0.1f))
                            .clickable { speedIndex = (speedIndex + 1) % speeds.size }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    )

                    // Mode
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                activeView = if (activeView == ActiveView.CLEAN) ActiveView.DIALOGUE else ActiveView.CLEAN
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (activeView == ActiveView.DIALOGUE) Icons.Default.Subtitles else Icons.Default.Visibility,
                            contentDescription = "Mode",
                            tint = if (activeView == ActiveView.DIALOGUE || activeView == ActiveView.CLEAN) BrandAccent else TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Loop
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .combinedClickable(
                                onClick = { activeView = ActiveView.LOOP },
                                onLongClick = {
                                    activeView = ActiveView.LOOP
                                    isLoopFormOpen = !isLoopFormOpen
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = "Loop",
                            tint = if (activeView == ActiveView.LOOP) BrandAccent else TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Notes
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .combinedClickable(
                                onClick = { activeView = ActiveView.NOTES },
                                onLongClick = {
                                    activeView = ActiveView.NOTES
                                    isNoteFormOpen = !isNoteFormOpen
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Notes",
                            tint = if (activeView == ActiveView.NOTES) BrandAccent else TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // --- VIEW SWITCHING ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 10.dp)
            ) {
                when (activeView) {
                    ActiveView.CLEAN -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "\"${dialogueDataset[activeDialogueIndex].en}\"",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "\"${dialogueDataset[activeDialogueIndex].bn}\"",
                                color = BrandAccent,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    ActiveView.DIALOGUE -> {
                        LazyColumn(
                            state = dialogueListState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(dialogueDataset) { idx, item ->
                                val isSelected = selectedDialogueIndices.contains(idx)
                                val isCurrentPlaying = (idx == activeDialogueIndex)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            when {
                                                isSelected -> BrandAccent.copy(alpha = 0.08f)
                                                isCurrentPlaying -> Color.White.copy(alpha = 0.04f)
                                                else -> BrandCard
                                            }
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = when {
                                                isSelected -> BrandAccent.copy(alpha = 0.3f)
                                                isCurrentPlaying -> Color.White.copy(alpha = 0.1f)
                                                else -> DividerColor
                                            },
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .combinedClickable(
                                            onClick = {
                                                if (selectedDialogueIndices.contains(idx)) {
                                                    selectedDialogueIndices.remove(idx)
                                                } else {
                                                    selectedDialogueIndices.add(idx)
                                                }
                                            }
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = String.format("%02d:%02d", item.start / 60, item.start % 60),
                                                color = if (isCurrentPlaying) BrandAccent else TextSecondary,
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier
                                                    .background(
                                                        if (isCurrentPlaying) BrandAccent.copy(alpha = 0.15f)
                                                        else Color.White.copy(alpha = 0.06f),
                                                        RoundedCornerShape(6.dp)
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                            if (isCurrentPlaying) {
                                                Box(
                                                    modifier = Modifier
                                                        .padding(top = 6.dp)
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(BrandAccent)
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "\"${item.en}\"",
                                                    color = when {
                                                        isSelected || isCurrentPlaying -> TextPrimary
                                                        else -> TextPrimary.copy(alpha = 0.7f)
                                                    },
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected || isCurrentPlaying) FontWeight.Bold else FontWeight.Medium,
                                                    lineHeight = 18.sp
                                                )
                                                Text(
                                                    text = "\"${item.bn}\"",
                                                    color = when {
                                                        isSelected || isCurrentPlaying -> BrandAccent.copy(alpha = 0.9f)
                                                        else -> TextSecondary
                                                    },
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected || isCurrentPlaying) FontWeight.Bold else FontWeight.Normal,
                                                    modifier = Modifier.padding(top = 4.dp),
                                                    lineHeight = 16.sp
                                                )
                                            }
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) BrandAccent
                                                    else Color.White.copy(alpha = 0.06f)
                                                )
                                                .border(
                                                    1.5.dp,
                                                    if (isSelected) BrandAccent else DividerColor,
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Add,
                                                contentDescription = "Select",
                                                tint = if (isSelected) BrandBg else TextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    ActiveView.LOOP -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // --- Saved Loops ---
                            Text(
                                text = "Saved Loops",
                                color = TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            LazyColumn(modifier = Modifier.weight(1f)) {
                                itemsIndexed(savedLoops) { index, loop ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(BrandCard)
                                            .border(1.dp, DividerColor, RoundedCornerShape(14.dp))
                                            .padding(14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            // Left: Play button
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(BrandAccent.copy(alpha = 0.12f))
                                                    .border(1.dp, BrandAccent.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                                    .clickable {
                                                        currentTime = loop.start
                                                        isPlaying = true
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = "Play loop",
                                                    tint = BrandAccent,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            // Center: Loop details
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = loop.name,
                                                    color = TextPrimary,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // Start badge
                                                    Text(
                                                        text = "A:${loop.start}s",
                                                        color = BrandAccent,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = FontFamily.Monospace,
                                                        modifier = Modifier
                                                            .background(BrandAccent.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                    // End badge
                                                    Text(
                                                        text = "B:${loop.end}s",
                                                        color = BrandAccent,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = FontFamily.Monospace,
                                                        modifier = Modifier
                                                            .background(BrandAccent.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                    // Repeats badge
                                                    Text(
                                                        text = if (loop.repeats == 999) "∞" else "${loop.repeats}x",
                                                        color = TextSecondary,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }
                                            }
                                            // Right: Delete
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White.copy(alpha = 0.05f))
                                                    .border(1.dp, DividerColor, CircleShape)
                                                    .clickable { savedLoops.removeAt(index) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = TextSecondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }

                            // Loop Add Form
                            AnimatedVisibility(
                                visible = isLoopFormOpen,
                                enter = slideInVertically { it } + fadeIn(),
                                exit = slideOutVertically { it } + fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(BrandCard, RoundedCornerShape(14.dp))
                                        .border(1.dp, DividerColor, RoundedCornerShape(14.dp))
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("New Loop", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = loopNameInput,
                                            onValueChange = { loopNameInput = it },
                                            label = { Text("Name", fontSize = 10.sp, color = TextSecondary) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = BrandAccent,
                                                unfocusedBorderColor = DividerColor,
                                                focusedTextColor = TextPrimary,
                                                unfocusedTextColor = TextPrimary
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = loopRepeatsSelection.toString(),
                                            onValueChange = { loopRepeatsSelection = it.toIntOrNull() ?: 1 },
                                            label = { Text("Repeats", fontSize = 10.sp, color = TextSecondary) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = BrandAccent,
                                                unfocusedBorderColor = DividerColor,
                                                focusedTextColor = TextPrimary,
                                                unfocusedTextColor = TextPrimary
                                            ),
                                            modifier = Modifier.width(80.dp)
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = loopStartInput,
                                            onValueChange = { loopStartInput = it },
                                            label = { Text("Start (sec)", fontSize = 10.sp, color = TextSecondary) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = BrandAccent,
                                                unfocusedBorderColor = DividerColor,
                                                focusedTextColor = TextPrimary,
                                                unfocusedTextColor = TextPrimary
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = loopEndInput,
                                            onValueChange = { loopEndInput = it },
                                            label = { Text("End (sec)", fontSize = 10.sp, color = TextSecondary) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = BrandAccent,
                                                unfocusedBorderColor = DividerColor,
                                                focusedTextColor = TextPrimary,
                                                unfocusedTextColor = TextPrimary
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                currentTime = loopStartInput.toIntOrNull() ?: 0
                                                isPlaying = true
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = BrandAccent.copy(alpha = 0.1f),
                                                contentColor = BrandAccent
                                            ),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Preview", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = {
                                                val startS = loopStartInput.toIntOrNull() ?: 0
                                                val endS = loopEndInput.toIntOrNull() ?: 0
                                                if (endS > startS) {
                                                    savedLoops.add(
                                                        SavedLoop(
                                                            name = loopNameInput.ifBlank { "Loop ${startS}-${endS}s" },
                                                            start = startS,
                                                            end = endS,
                                                            repeats = loopRepeatsSelection
                                                        )
                                                    )
                                                    isLoopFormOpen = false
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = BrandAccent,
                                                contentColor = BrandBg
                                            ),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Save", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    ActiveView.NOTES -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // --- Saved Notes ---
                            Text(
                                text = "Saved Notes",
                                color = TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            LazyColumn(modifier = Modifier.weight(1f)) {
                                itemsIndexed(savedNotes) { index, note ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(BrandCard)
                                            .border(1.dp, DividerColor, RoundedCornerShape(14.dp))
                                            .padding(14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            // Left: Note icon
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(BrandAccent.copy(alpha = 0.1f))
                                                    .border(1.dp, BrandAccent.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.EditNote,
                                                    contentDescription = null,
                                                    tint = BrandAccent,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                            // Center: Note text + timestamp
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = note.text,
                                                    color = TextPrimary,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "@ ${String.format("%d:%02d", note.time / 60, note.time % 60)}",
                                                    color = TextSecondary,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace,
                                                    modifier = Modifier
                                                        .background(BrandAccent.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            // Right: Delete
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White.copy(alpha = 0.05f))
                                                    .border(1.dp, DividerColor, CircleShape)
                                                    .clickable { savedNotes.removeAt(index) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = TextSecondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }

                            // Note Add Form
                            AnimatedVisibility(
                                visible = isNoteFormOpen,
                                enter = slideInVertically { it } + fadeIn(),
                                exit = slideOutVertically { it } + fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(BrandCard, RoundedCornerShape(14.dp))
                                        .border(1.dp, DividerColor, RoundedCornerShape(14.dp))
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("New Note", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    OutlinedTextField(
                                        value = noteTextInput,
                                        onValueChange = { noteTextInput = it },
                                        placeholder = { Text("Write a study note...", fontSize = 12.sp, color = TextSecondary) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = BrandAccent,
                                            unfocusedBorderColor = DividerColor,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(
                                        text = "Current time: ${String.format("%d:%02d", currentTime / 60, currentTime % 60)}",
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Button(
                                        onClick = {
                                            if (noteTextInput.isNotBlank()) {
                                                savedNotes.add(
                                                    SavedNote(time = currentTime, text = noteTextInput)
                                                )
                                                noteTextInput = ""
                                                isNoteFormOpen = false
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = BrandAccent,
                                            contentColor = BrandBg
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Add Note", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        HorizonMusicPlayer(
            songTitle = songTitle,
            isPlaying = isPlaying,
            currentTime = currentTime,
            duration = duration,
            currentSpeed = currentSpeed,
            statusLine = statusLine,
            onPlayPause = { isPlaying = !isPlaying },
            onRewind = { currentTime = (currentTime - 5).coerceAtLeast(0) },
            onForward = { currentTime = (currentTime + 5).coerceAtMost(duration - 1) },
            onSeek = { fraction -> currentTime = (fraction * duration).toInt().coerceIn(0, duration) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
