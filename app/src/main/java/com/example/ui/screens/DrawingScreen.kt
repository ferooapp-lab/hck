package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DrawingStroke
import com.example.model.ScreenActivityState
import com.example.model.UserRole
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.DeepBackground
import com.example.ui.theme.DeepSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.NeonCoral
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.DrawingToolState

@Composable
fun DrawingScreen(
    currentRole: UserRole,
    strokes: List<DrawingStroke>,
    activeStroke: DrawingStroke?,
    laserPointer: Pair<Float, Float>?,
    toolState: DrawingToolState,
    screenActivity: ScreenActivityState,
    partnerStream: com.example.model.VideoStreamFeed? = null,
    onStartDrawing: (Float, Float) -> Unit,
    onContinueDrawing: (Float, Float) -> Unit,
    onEndDrawing: () -> Unit,
    onSetColor: (Long) -> Unit,
    onSetStrokeWidth: (Float) -> Unit,
    onToggleLaserMode: () -> Unit,
    onToggleDrawOverScreen: (Boolean) -> Unit,
    onClearDrawings: () -> Unit,
    onUndoDrawing: () -> Unit,
    onSendDrawingToChat: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "laserBeacon")
    val laserGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserGlow"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBackground)
            .testTag("drawing_screen")
    ) {
        // Top status & mode switcher
        Surface(
            color = SurfaceCard,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MintEmerald)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Eş Zamanlı Canlı Çizim",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Çizdikleriniz anında karşı tarafın ekranında belirir",
                            style = MaterialTheme.typography.bodySmall,
                            color = MintEmerald,
                            fontSize = 11.sp
                        )
                    }
                }

                // Mode toggle button: Screen Overlay vs Whiteboard
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0F172A),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onToggleDrawOverScreen(!toolState.isDrawOverScreen) }
                        .testTag("toggle_canvas_mode_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (toolState.isDrawOverScreen) Icons.Default.ScreenShare else Icons.Default.Layers,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (toolState.isDrawOverScreen) "Ekran Üzeri" else "Ortak Tahta",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = CyanGlow,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Live Drawing Interactive Canvas Area
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(if (toolState.isDrawOverScreen) Color(0xFF050811) else Color(0xFF0D1424))
        ) {
            val canvasW = constraints.maxWidth.toFloat().coerceAtLeast(1f)
            val canvasH = constraints.maxHeight.toFloat().coerceAtLeast(1f)

            // If "Draw Over Screen" is selected, render the partner's live phone screen background underneath!
            if (toolState.isDrawOverScreen) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0B132B).copy(alpha = 0.85f))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(20.dp))
                ) {
                    InteractiveVideoCanvas(
                        feed = partnerStream ?: com.example.model.VideoStreamFeed(
                            owner = if (currentRole == UserRole.USER_A) UserRole.USER_B else UserRole.USER_A,
                            category = com.example.model.VideoCategory.CYBER_NEON
                        ),
                        modifier = Modifier.fillMaxSize()
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Partner Ekranı (Canlı)",
                            color = CyanGlow,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Real-time Canvas detecting touches and rendering strokes
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(canvasW, canvasH) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val normX = (offset.x / canvasW).coerceIn(0f, 1f)
                                val normY = (offset.y / canvasH).coerceIn(0f, 1f)
                                onStartDrawing(normX, normY)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val normX = (change.position.x / canvasW).coerceIn(0f, 1f)
                                val normY = (change.position.y / canvasH).coerceIn(0f, 1f)
                                onContinueDrawing(normX, normY)
                            },
                            onDragEnd = {
                                onEndDrawing()
                            },
                            onDragCancel = {
                                onEndDrawing()
                            }
                        )
                    }
                    .testTag("collaborative_drawing_canvas")
            ) {
                val allStrokes = strokes + listOfNotNull(activeStroke)

                // Draw each stroke
                allStrokes.forEach { stroke ->
                    if (stroke.points.size > 1) {
                        val path = Path().apply {
                            val first = stroke.points.first()
                            moveTo(first.x * size.width, first.y * size.height)
                            for (i in 1 until stroke.points.size) {
                                val pt = stroke.points[i]
                                lineTo(pt.x * size.width, pt.y * size.height)
                            }
                        }

                        val strokeColor = Color(stroke.colorArgb)

                        if (stroke.isLaser) {
                            // Glowing Laser trail
                            drawPath(
                                path = path,
                                color = NeonCoral.copy(alpha = 0.4f),
                                style = Stroke(
                                    width = (stroke.strokeWidth * 2.5f).dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                            drawPath(
                                path = path,
                                color = Color.White,
                                style = Stroke(
                                    width = stroke.strokeWidth.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        } else {
                            // Standard Synchronous Pen Stroke
                            drawPath(
                                path = path,
                                color = strokeColor,
                                style = Stroke(
                                    width = stroke.strokeWidth.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }

                // Render dynamic Laser Pointer Beacon if active
                laserPointer?.let { (x, y) ->
                    val pointerCenter = Offset(x * size.width, y * size.height)
                    drawCircle(
                        color = NeonCoral.copy(alpha = 0.35f),
                        radius = (20.dp.toPx() * laserGlow),
                        center = pointerCenter
                    )
                    drawCircle(
                        color = NeonCoral,
                        radius = 8.dp.toPx(),
                        center = pointerCenter
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3.dp.toPx(),
                        center = pointerCenter
                    )
                }
            }

            // Sync Status Banner over Canvas
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SurfaceCard.copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(CyanGlow)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Senkron: ${strokes.size} çizim eşleşti",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Floating Bottom Tool Palette
        Surface(
            color = DeepSurface,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                // Color Picker Row & Stroke width & Laser
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 5 Colors
                    val colors = listOf(
                        0xFF06B6D4 to ElectricCyan,
                        0xFFF43F5E to NeonCoral,
                        0xFF10B981 to MintEmerald,
                        0xFFF59E0B to AmberWarning,
                        0xFF8B5CF6 to ElectricViolet,
                        0xFFFFFFFF to Color.White
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        colors.forEach { (colorLong, composeColor) ->
                            val isSelected = toolState.selectedColor == colorLong && !toolState.isLaserMode
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(composeColor)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { onSetColor(colorLong) }
                                    .testTag("color_picker_${colorLong}")
                            )
                        }
                    }

                    // Laser pointer mode toggle button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (toolState.isLaserMode) NeonCoral else SurfaceCard,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onToggleLaserMode)
                            .testTag("toggle_laser_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = "Lazer İşaretçi",
                                tint = if (toolState.isLaserMode) Color.White else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Lazer",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (toolState.isLaserMode) Color.White else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Brush thickness selector + Undo + Clear buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Stroke thickness chips
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(4f to "İnce", 8f to "Orta", 16f to "Kalın").forEach { (width, label) ->
                            val isSelected = toolState.strokeWidth == width
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) ElectricCyan.copy(alpha = 0.2f) else SurfaceCard,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan) else null,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSetStrokeWidth(width) }
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) CyanGlow else TextSecondary,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Undo and Clear Actions
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onUndoDrawing,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceCard)
                                .testTag("undo_drawing_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Undo,
                                contentDescription = "Geri Al",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onClearDrawings,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceCard)
                                .testTag("clear_drawings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Tümünü Temizle",
                                tint = NeonCoral,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onSendDrawingToChat,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ElectricCyan)
                                .testTag("send_drawing_to_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Sohbete Gönder",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
