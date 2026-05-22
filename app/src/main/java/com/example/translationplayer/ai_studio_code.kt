package com.example.translationplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// --- GRAY + WHITE COLOR THEME ---
val BrandBg = Color(0xFF121212)
val BrandCard = Color(0xFF1A1A1A)
val BrandAccent = Color(0xFFE0E0E0) // Light gray accent
val DividerColor = Color(0xFF333333)

// --- DATA STRUCTURES ---
data class DialogueLine(val start: Int, val en: String, val bn: String)
data class SavedLoop(val name: String, val start: Int, val end: Int, val repeats: Int)
data class SavedNote(val time: Int, val text: String)

enum class ActiveView {
    CLEAN, DIALOGUE, LOOP, NOTES
}

// Mock Dataset
val dialogueDataset = listOf(
    DialogueLine(0, "Take a step into my cabin", "আমার ঘরে একটি পদক্ষেপ নাও"),
    DialogueLine(6, "Take a deep breath, ease your mind", "দীর্ঘ শ্বাস নাও, মনকে শান্ত করো"),
    DialogueLine(12, "Baby, why don't you just meet me in the middle?", "প্রিয়, তুমি কেন আমার সাথে মাঝামাঝি জায়গায় এসে দেখা করছ না?"),
    DialogueLine(18, "I'm losing my mind just a little", "আমি আমার মানসিক নিয়ন্ত্রণ কিছুটা হারিয়ে ফেলছি"),
    DialogueLine(24, "So baby, pull me closer", "তাই প্রিয়, আমাকে তোমার আরও কাছে টেনে নাও")
)


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TranslationPlayerScreen() {
    // --- STATE MANAGEMENT ---
    var activeView by remember { mutableStateOf(ActiveView.CLEAN) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(0) }
    val duration = 30
    var speedIndex by remember { mutableStateOf(0) }
    val speeds = listOf(1.0f, 1.25f, 1.5f, 2.0f, 0.5f, 0.75f)
    val currentSpeed = speeds[speedIndex]

    // Form Toggle States
    var isLoopFormOpen by remember { mutableStateOf(false) }
    var isNoteFormOpen by remember { mutableStateOf(false) }

    // Selection States
    val selectedDialogueIndices = remember { mutableStateListOf<Int>() }

    // Dynamic Lists
    val savedLoops = remember { mutableStateListOf<SavedLoop>() }
    val savedNotes = remember { mutableStateListOf<SavedNote>() }

    // Form inputs
    var loopNameInput by remember { mutableStateOf("") }
    var loopStartInput by remember { mutableStateOf("0") }
    var loopEndInput by remember { mutableStateOf("0") }
    var loopRepeatsSelection by remember { mutableStateOf(1) } // 1x, 3x, 5x, 999 (Infinite)
    var noteTextInput by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val dialogueListState = rememberLazyListState()

    // Active Dialogue Selection logic based on time
    val activeDialogueIndex = remember(currentTime) {
        var index = 0
        for (i in dialogueDataset.indices) {
            if (currentTime >= dialogueDataset[i].start) {
                index = i
            }
        }
        index
    }

    // Status line for the player card: mode:clean | loop:0 | speed:1X
    val statusLine = remember(currentSpeed, activeView, savedLoops.size) {
        val viewName = activeView.name.lowercase()
        val loopCount = savedLoops.size
        "mode:$viewName | loop:$loopCount | speed:${currentSpeed}X"
    }

    // Auto Scroll active dialogue line
    LaunchedEffect(activeDialogueIndex) {
        if (activeView == ActiveView.DIALOGUE) {
            coroutineScope.launch {
                dialogueListState.animateScrollToItem(activeDialogueIndex)
            }
        }
    }

    // --- MAIN SCREEN LAYOUT ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 200.dp) // Leave space for the minimal player card
        ) {
            // --- TOP HEADER ACTION ROW ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left profile details group
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = { /* Back Action */ },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color.LightGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Alex Mercer",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Student",
                            color = Color.Gray,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Consolidated Right Control Hub
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.3f))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Speed Selector
                    Text(
                        text = "${currentSpeed}x",
                        color = Color.LightGray,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .combinedClickable(
                                onClick = { speedIndex = (speedIndex + 1) % speeds.size }
                            )
                            .padding(8.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))
                    Box(modifier = Modifier.width(1.dp).height(12.dp).background(DividerColor))
                    Spacer(modifier = Modifier.width(4.dp))

                    // Mode Toggle (Clean vs Dialogue)
                    IconButton(
                        onClick = {
                            activeView = if (activeView == ActiveView.CLEAN) ActiveView.DIALOGUE else ActiveView.CLEAN
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (activeView == ActiveView.DIALOGUE) Icons.Default.Subtitles else Icons.Default.Visibility,
                            contentDescription = "Toggle Mode",
                            tint = if (activeView == ActiveView.DIALOGUE || activeView == ActiveView.CLEAN) BrandAccent else Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))
                    Box(modifier = Modifier.width(1.dp).height(12.dp).background(DividerColor))
                    Spacer(modifier = Modifier.width(4.dp))

                    // Loop view controller (Long press for Add interface)
                    IconButton(
                        onClick = { activeView = ActiveView.LOOP },
                        modifier = Modifier
                            .size(28.dp)
                            .combinedClickable(
                                onClick = { activeView = ActiveView.LOOP },
                                onLongClick = {
                                    activeView = ActiveView.LOOP
                                    isLoopFormOpen = !isLoopFormOpen
                                }
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = "Loop view",
                            tint = if (activeView == ActiveView.LOOP) BrandAccent else Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))
                    Box(modifier = Modifier.width(1.dp).height(12.dp).background(DividerColor))
                    Spacer(modifier = Modifier.width(4.dp))

                    // Notes view controller (Long press for Add interface)
                    IconButton(
                        onClick = { activeView = ActiveView.NOTES },
                        modifier = Modifier
                            .size(28.dp)
                            .combinedClickable(
                                onClick = { activeView = ActiveView.NOTES },
                                onLongClick = {
                                    activeView = ActiveView.NOTES
                                    isNoteFormOpen = !isNoteFormOpen
                                }
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Notes Mode",
                            tint = if (activeView == ActiveView.NOTES) BrandAccent else Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // --- VIEW CONDITIONAL SWITCHING ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp)
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
                                color = Color.White,
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
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(dialogueDataset) { idx, item ->
                                val isSelected = selectedDialogueIndices.contains(idx)
                                val isCurrentPlaying = (idx == activeDialogueIndex)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = {
                                                if (selectedDialogueIndices.contains(idx)) {
                                                    selectedDialogueIndices.remove(idx)
                                                } else {
                                                    selectedDialogueIndices.add(idx)
                                                }
                                            }
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) BrandAccent.copy(alpha = 0.3f) else Color.Transparent
                                        )
                                        .background(if (isSelected) BrandAccent.copy(alpha = 0.02f) else Color.Transparent)
                                        .padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = String.format("%02d:%02d", item.start / 60, item.start % 60),
                                            color = Color.LightGray,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .background(Color.White.copy(alpha = 0.05f))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                        )
                                        Column {
                                            Text(
                                                text = "\"${item.en}\"",
                                                color = if (isSelected || isCurrentPlaying) Color.White else Color.Gray,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected || isCurrentPlaying) FontWeight.Bold else FontWeight.Medium
                                            )
                                            Text(
                                                text = "\"${item.bn}\"",
                                                color = if (isSelected || isCurrentPlaying) BrandAccent else Color.Gray,
                                                fontSize = 10.sp,
                                                fontWeight = if (isSelected || isCurrentPlaying) FontWeight.Bold else FontWeight.Medium,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) BrandAccent.copy(alpha = 0.15f) else Color.Transparent)
                                            .border(1.dp, if (isSelected) BrandAccent else Color.Gray, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) Icons.Default.RepeatOne else Icons.Default.PlayArrow,
                                            contentDescription = "Indicator",
                                            tint = if (isSelected) BrandAccent else Color.Gray,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    ActiveView.LOOP -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Saved Loop Title Label
                            Text(
                                text = "Saved Loops",
                                color = Color.Gray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Saved Loops List Shelf
                            LazyColumn(modifier = Modifier.weight(1f)) {
                                itemsIndexed(savedLoops) { index, loop ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                            .background(BrandCard)
                                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = loop.name,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "A:${loop.start}s │ B:${loop.end}s │ repeats: ${if (loop.repeats == 999) "∞" else "${loop.repeats}x"}",
                                                color = BrandAccent,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            IconButton(
                                                onClick = {
                                                    currentTime = loop.start
                                                    isPlaying = true
                                                },
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .background(BrandAccent.copy(alpha = 0.15f), CircleShape)
                                                    .border(1.dp, BrandAccent.copy(alpha = 0.3f), CircleShape)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = "Load loop",
                                                    tint = BrandAccent,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = { savedLoops.removeAt(index) },
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
                                                    .border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete loop",
                                                    tint = Color.Gray,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }

                            // Loop Adding Interface Form (Stationary at Bottom of view)
                            AnimatedVisibility(
                                visible = isLoopFormOpen,
                                enter = slideInVertically { it } + fadeIn(),
                                exit = slideOutVertically { it } + fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(BrandCard, RoundedCornerShape(12.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = loopNameInput,
                                            onValueChange = { loopNameInput = it },
                                            label = { Text("Loop Name", fontSize = 8.sp) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = BrandAccent,
                                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = loopRepeatsSelection.toString(),
                                            onValueChange = {
                                                loopRepeatsSelection = it.toIntOrNull() ?: 1
                                            },
                                            label = { Text("Repeats", fontSize = 8.sp) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = BrandAccent,
                                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
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
                                            label = { Text("Start (sec)", fontSize = 8.sp) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = BrandAccent,
                                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = loopEndInput,
                                            onValueChange = { loopEndInput = it },
                                            label = { Text("End (sec)", fontSize = 8.sp) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = BrandAccent,
                                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
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
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2A2A)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = "Try")
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Preview", fontSize = 10.sp)
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
                                                contentColor = Color(0xFF1A1A1A)
                                            ),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Save Loop", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    ActiveView.NOTES -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "Saved Notes",
                                color = Color.Gray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Saved Notes list Vertical Layout
                            LazyColumn(modifier = Modifier.weight(1f)) {
                                itemsIndexed(savedNotes) { index, note ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                            .background(BrandCard)
                                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = note.text,
                                            color = Color.LightGray,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = { savedNotes.removeAt(index) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }

                            // Note Adding Interface Form (Stationary at Bottom of view)
                            AnimatedVisibility(
                                visible = isNoteFormOpen,
                                enter = slideInVertically { it } + fadeIn(),
                                exit = slideOutVertically { it } + fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(BrandCard, RoundedCornerShape(12.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "Study Notebook",
                                        color = Color.Gray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    OutlinedTextField(
                                        value = noteTextInput,
                                        onValueChange = { noteTextInput = it },
                                        placeholder = { Text("Write a study note...", fontSize = 12.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = BrandAccent,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Button(
                                        onClick = {
                                            if (noteTextInput.isNotBlank()) {
                                                savedNotes.add(
                                                    SavedNote(
                                                        time = currentTime,
                                                        text = noteTextInput
                                                    )
                                                )
                                                noteTextInput = ""
                                                isNoteFormOpen = false
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = BrandAccent,
                                            contentColor = Color(0xFF1A1A1A)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Add Note to Notebook", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- NEW HORIZON MUSIC PLAYER (Fixed at bottom) ---
        HorizonMusicPlayer(
            isPlaying = isPlaying,
            currentTime = currentTime,
            duration = duration,
            currentSpeed = currentSpeed,
            statusLine = statusLine,
            onPlayPause = { isPlaying = !isPlaying },
            onRewind = { currentTime = (currentTime - 5).coerceAtLeast(0) },
            onForward = { currentTime = (currentTime + 5).coerceAtMost(duration - 1) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}