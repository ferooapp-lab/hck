package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.UserRole
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.DeepBackground
import com.example.ui.theme.DeepSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.NeonCoral
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    currentRole: UserRole,
    messages: List<ChatMessage>,
    onSendMessage: (String, Boolean) -> Unit,
    onSendLocation: () -> Unit,
    onSendVoiceNote: (Int) -> Unit,
    onAddReaction: (String, String) -> Unit,
    onInspectMessageCrypto: (ChatMessage) -> Unit,
    onNavigateToLocation: () -> Unit,
    onNavigateToDrawing: () -> Unit,
    onNavigateToScreenStream: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    var isDisappearingMode by remember { mutableStateOf(false) }
    var isRecordingVoice by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableIntStateOf(0) }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Voice recording timer loop
    LaunchedEffect(isRecordingVoice) {
        if (isRecordingVoice) {
            recordingSeconds = 0
            while (isRecordingVoice) {
                delay(1000)
                recordingSeconds++
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBackground)
            .testTag("chat_screen")
    ) {
        // E2EE Info banner
        Surface(
            color = Color(0xFF0A1322),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Mesajlar uçtan uca AES-256-GCM ile korunmaktadır",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyanGlow,
                    fontSize = 11.sp
                )
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                MessageBubble(
                    message = message,
                    isMe = message.sender == currentRole,
                    onInspectCrypto = { onInspectMessageCrypto(message) },
                    onAddReaction = { emoji -> onAddReaction(message.id, emoji) },
                    onNavigateToLocation = onNavigateToLocation,
                    onNavigateToDrawing = onNavigateToDrawing
                )
            }
        }

        // Quick Shortcuts bar
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                QuickActionChip(
                    icon = Icons.Default.PinDrop,
                    label = "Konumumu Gönder",
                    tint = ElectricCyan,
                    onClick = onSendLocation,
                    tag = "quick_location_chip"
                )
            }
            item {
                QuickActionChip(
                    icon = Icons.Default.Palette,
                    label = "Ekrana Çizim Yap",
                    tint = NeonCoral,
                    onClick = onNavigateToDrawing,
                    tag = "quick_drawing_chip"
                )
            }
            item {
                QuickActionChip(
                    icon = Icons.Default.ScreenShare,
                    label = "Canlı Ekranı İzle",
                    tint = ElectricViolet,
                    onClick = onNavigateToScreenStream,
                    tag = "quick_screen_chip"
                )
            }
        }

        // Input composer bar
        Surface(
            color = DeepSurface,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                if (isDisappearingMode) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = NeonCoral,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Süreli mesaj modu aktif (okunduktan sonra imha edilir)",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonCoral,
                            fontSize = 11.sp
                        )
                    }
                }

                // If recording voice note
                if (isRecordingVoice) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF2A0F1E))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(NeonCoral)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ses Kaydediliyor... 00:${recordingSeconds.toString().padStart(2, '0')}",
                                color = NeonCoral,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Row {
                            TextButton(onClick = { isRecordingVoice = false }) {
                                Text("İptal", color = TextMuted)
                            }
                            IconButton(
                                onClick = {
                                    val duration = recordingSeconds.coerceAtLeast(1)
                                    isRecordingVoice = false
                                    onSendVoiceNote(duration)
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(NeonCoral)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = "Durdur ve Gönder", tint = Color.White)
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { isDisappearingMode = !isDisappearingMode },
                            modifier = Modifier.testTag("toggle_disappearing_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Süreli Mesaj",
                                tint = if (isDisappearingMode) NeonCoral else TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        IconButton(
                            onClick = { isRecordingVoice = true },
                            modifier = Modifier.testTag("record_voice_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Sesli Not",
                                tint = ElectricCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_field"),
                            placeholder = {
                                Text(
                                    "Uçtan uca şifreli mesaj yaz...",
                                    color = TextMuted,
                                    fontSize = 14.sp
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceCard,
                                unfocusedContainerColor = SurfaceCard,
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (inputText.isNotBlank()) {
                                        onSendMessage(inputText.trim(), isDisappearingMode)
                                        inputText = ""
                                    }
                                }
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(if (inputText.isNotBlank()) ElectricCyan else SurfaceCardElevated)
                                .clickable(enabled = inputText.isNotBlank()) {
                                    onSendMessage(inputText.trim(), isDisappearingMode)
                                    inputText = ""
                                }
                                .testTag("chat_send_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Gönder",
                                tint = if (inputText.isNotBlank()) Color.Black else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    isMe: Boolean,
    onInspectCrypto: () -> Unit,
    onAddReaction: (String) -> Unit,
    onNavigateToLocation: () -> Unit,
    onNavigateToDrawing: () -> Unit
) {
    var showReactionPicker by remember { mutableStateOf(false) }
    var isPlayingVoice by remember { mutableStateOf(false) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }

    val bubbleColor = if (isMe) Color(0xFF0F495C) else SurfaceCardElevated
    val textColor = TextPrimary

    androidx.compose.runtime.LaunchedEffect(isPlayingVoice) {
        if (isPlayingVoice) {
            try {
                val toneGen = android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 70)
                toneGen.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 400)
            } catch (_: Exception) {}
            kotlinx.coroutines.delay((message.voiceDurationSec.coerceAtLeast(1) * 1000L).coerceAtMost(6000L))
            isPlayingVoice = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("message_item_${message.id}"),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        if (!isMe) {
            Text(
                text = message.sender.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = ElectricViolet,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )
        }

        Box {
            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMe) 16.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(containerColor = bubbleColor),
                modifier = Modifier
                    .widthIn(max = 290.dp)
                    .clickable { showReactionPicker = !showReactionPicker }
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    // 1. Location Payload Card inside chat
                    if (message.locationPayload != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF091424),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PinDrop,
                                        contentDescription = null,
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = message.locationPayload.placeName,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                }
                                Text(
                                    text = message.locationPayload.streetAddress,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = onNavigateToLocation,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Navigation, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Haritada Gör", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 2. Voice Note Message
                    if (message.isVoiceNote) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { isPlayingVoice = !isPlayingVoice },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(ElectricCyan)
                            ) {
                                Icon(
                                    imageVector = if (isPlayingVoice) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))

                            // Animated simulated waveform
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(12, 24, 18, 28, 14, 22, 10, 26, 16, 20).forEach { h ->
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .height(h.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(if (isPlayingVoice) CyanGlow else TextSecondary.copy(alpha = 0.5f))
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${message.voiceDurationSec}s",
                                color = CyanGlow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // 3. Drawing Card
                    if (message.isDrawingCard) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E1032),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Palette, contentDescription = null, tint = NeonCoral, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ekran Çizim Bildirimi", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(message.drawingNote ?: "Yeni çizim", color = TextSecondary, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = onNavigateToDrawing,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCoral),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Çizimi Canlı İncele", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Plain text message
                    if (message.locationPayload == null && !message.isVoiceNote && !message.isDrawingCard) {
                        Text(
                            text = message.plainText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Tappable Lock badge indicating AES-256 E2EE proof
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Black.copy(alpha = 0.25f))
                                .clickable(onClick = onInspectCrypto)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                .testTag("inspect_crypto_button_${message.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Şifreli",
                                tint = CyanGlow,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "AES",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanGlow
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 10.sp
                        )

                        if (isMe) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Okundu",
                                tint = CyanGlow,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // Reaction pill if selected
        if (message.reaction != null) {
            Surface(
                shape = CircleShape,
                color = SurfaceCard,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .padding(top = 2.dp, end = if (isMe) 8.dp else 0.dp, start = if (!isMe) 8.dp else 0.dp)
                    .clickable { showReactionPicker = !showReactionPicker }
            ) {
                Text(
                    text = message.reaction,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontSize = 12.sp
                )
            }
        }

        // Quick Emoji Reaction Palette
        AnimatedVisibility(
            visible = showReactionPicker,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = DeepSurface,
                shadowElevation = 4.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)) {
                    listOf("❤️", "👍", "🔥", "😂", "📍").forEach { emoji ->
                        Text(
                            text = emoji,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    onAddReaction(emoji)
                                    showReactionPicker = false
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SurfaceCard,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
