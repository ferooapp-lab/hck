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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DrawToolType
import com.example.model.DrawingPoint
import com.example.model.DrawingStroke
import com.example.model.DualStreamViewMode
import com.example.model.NetworkSyncStats
import com.example.model.UserRole
import com.example.model.VideoCategory
import com.example.model.VideoStreamFeed
import com.example.ui.theme.AmberWarning
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
import kotlin.math.cos
import kotlin.math.sin
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.DrawingToolState
import kotlin.math.cos
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import com.example.model.RealAppUsageInfo
import com.example.model.RealDeviceTelemetryState
import com.example.model.ScreenActivityState

@Composable
fun ScreenStreamScreen(
    currentRole: UserRole,
    userAStream: VideoStreamFeed,
    userBStream: VideoStreamFeed,
    dualViewMode: DualStreamViewMode,
    networkSyncStats: NetworkSyncStats,
    recentSyncLogs: List<String>,
    strokes: List<DrawingStroke>,
    activeStroke: DrawingStroke?,
    laserPointerA: Pair<Float, Float>?,
    laserPointerB: Pair<Float, Float>?,
    toolState: DrawingToolState,
    screenActivity: ScreenActivityState = ScreenActivityState(),
    realTelemetry: RealDeviceTelemetryState = RealDeviceTelemetryState(),
    onRequestUsagePermission: () -> Unit = {},
    onRefreshRealData: () -> Unit = {},
    onRequestScreenCapture: () -> Unit = {},
    onSetDualViewMode: (DualStreamViewMode) -> Unit,
    onStartCrossDrawing: (targetScreen: UserRole, xRatio: Float, yRatio: Float) -> Unit,
    onContinueCrossDrawing: (xRatio: Float, yRatio: Float) -> Unit,
    onEndCrossDrawing: () -> Unit,
    onSetColor: (Long) -> Unit,
    onSetStrokeWidth: (Float) -> Unit,
    onSetToolType: (DrawToolType) -> Unit,
    onClearDrawings: (targetScreen: UserRole?) -> Unit,
    onUndoDrawing: (targetScreen: UserRole?) -> Unit,
    onToggleVideoPlayPause: (owner: UserRole) -> Unit,
    onSeekVideo: (owner: UserRole, seconds: Float) -> Unit,
    onChangeVideoCategory: (owner: UserRole, category: VideoCategory) -> Unit,
    onToggleScreenSharing: (owner: UserRole) -> Unit,
    onTogglePrivacyShield: (owner: UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    var showLogsSheet by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulseLive")
    val liveAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "liveAlpha"
    )

    val laserGlow by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserGlow"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBackground)
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
            .testTag("screen_stream_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. TOP HEADER & TELEMETRY PANEL ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(NeonCoral.copy(alpha = liveAlpha))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CANLI EŞ ZAMANLI İKİLİ EKRAN & ÇİZİM",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = NeonCoral
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF14243B),
                        modifier = Modifier.clickable { showLogsSheet = !showLogsSheet }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = CyanGlow,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${networkSyncStats.videoFps} FPS • ${networkSyncStats.streamLatencyMs}ms",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = CyanGlow,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Telemetry summary badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StreamMetricBadge(
                        title = "Çizim Senkronu",
                        value = "< ${networkSyncStats.drawLatencyMs} ms",
                        color = MintEmerald,
                        modifier = Modifier.weight(1f)
                    )
                    StreamMetricBadge(
                        title = "Paket Sayısı",
                        value = "${networkSyncStats.packetsSynced}",
                        color = ElectricCyan,
                        modifier = Modifier.weight(1f)
                    )
                    StreamMetricBadge(
                        title = "Kripto / Güvenlik",
                        value = "AES-256 E2EE",
                        color = ElectricViolet,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Expandable Sync Logs
                AnimatedVisibility(visible = showLogsSheet) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF090E17))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Canlı Senkronizasyon Olay Kaydı",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = CyanGlow
                            )
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Kapat",
                                tint = TextMuted,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { showLogsSheet = false }
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        recentSyncLogs.forEach { log ->
                            Text(
                                text = log,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- 2. VIEW MODE SELECTOR (Split, Partner Focus, My Screen Focus, PiP) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DeepSurface)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DualStreamViewMode.values().forEach { mode ->
                val isSelected = dualViewMode == mode
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) Color(0xFF0E3A4A) else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSetDualViewMode(mode) }
                        .testTag("mode_${mode.name.lowercase()}")
                ) {
                    Text(
                        text = when (mode) {
                            DualStreamViewMode.SPLIT_DUAL -> "İkili Ekran"
                            DualStreamViewMode.PARTNER_FOCUS -> "Partner"
                            DualStreamViewMode.MY_FOCUS -> "Cihazım"
                            DualStreamViewMode.PIP_FLOAT -> "PiP"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isSelected) CyanGlow else TextMuted,
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- PARTNER REAL-TIME ACTIVITY INSPECTOR (Nelerle Uğraşıyor, Nereye Girdi) ---
        PartnerActivityInspectorCard(
            stream = userBStream,
            screenActivity = screenActivity,
            realTelemetry = realTelemetry,
            onRequestUsagePermission = onRequestUsagePermission,
            onRefreshRealData = onRefreshRealData,
            onRequestScreenCapture = onRequestScreenCapture,
            onChangeCategory = { cat -> onChangeVideoCategory(UserRole.USER_B, cat) }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // --- 3. LIVE SCREENS AREA ---
        when (dualViewMode) {
            DualStreamViewMode.SPLIT_DUAL -> {
                // Two screens side-by-side or stacked
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // SCREEN 1: User A's Device
                    LivePhoneScreenContainer(
                        title = "Cihaz 1 (Sen / Ferhat) • Canlı Yayın",
                        feed = userAStream,
                        role = UserRole.USER_A,
                        isCurrentActor = (currentRole == UserRole.USER_A),
                        strokes = strokes.filter { it.targetScreen == UserRole.USER_A },
                        activeStroke = if (activeStroke?.targetScreen == UserRole.USER_A) activeStroke else null,
                        laserPointer = laserPointerA,
                        laserGlow = laserGlow,
                        toolState = toolState,
                        onDrawStart = { x, y -> onStartCrossDrawing(UserRole.USER_A, x, y) },
                        onDrawMove = { x, y -> onContinueCrossDrawing(x, y) },
                        onDrawEnd = { onEndCrossDrawing() },
                        onTogglePlayPause = { onToggleVideoPlayPause(UserRole.USER_A) },
                        onSeek = { sec -> onSeekVideo(UserRole.USER_A, sec) },
                        onChangeCategory = { cat -> onChangeVideoCategory(UserRole.USER_A, cat) },
                        onToggleSharing = { onToggleScreenSharing(UserRole.USER_A) },
                        onTogglePrivacy = { onTogglePrivacyShield(UserRole.USER_A) },
                        testTagPrefix = "screen_user_a"
                    )

                    // SCREEN 2: User B's Device (Partner)
                    LivePhoneScreenContainer(
                        title = "Cihaz 2 (Partner) • Canlı Yayın",
                        feed = userBStream,
                        role = UserRole.USER_B,
                        isCurrentActor = (currentRole == UserRole.USER_B),
                        strokes = strokes.filter { it.targetScreen == UserRole.USER_B },
                        activeStroke = if (activeStroke?.targetScreen == UserRole.USER_B) activeStroke else null,
                        laserPointer = laserPointerB,
                        laserGlow = laserGlow,
                        toolState = toolState,
                        onDrawStart = { x, y -> onStartCrossDrawing(UserRole.USER_B, x, y) },
                        onDrawMove = { x, y -> onContinueCrossDrawing(x, y) },
                        onDrawEnd = { onEndCrossDrawing() },
                        onTogglePlayPause = { onToggleVideoPlayPause(UserRole.USER_B) },
                        onSeek = { sec -> onSeekVideo(UserRole.USER_B, sec) },
                        onChangeCategory = { cat -> onChangeVideoCategory(UserRole.USER_B, cat) },
                        onToggleSharing = { onToggleScreenSharing(UserRole.USER_B) },
                        onTogglePrivacy = { onTogglePrivacyShield(UserRole.USER_B) },
                        testTagPrefix = "screen_user_b"
                    )
                }
            }

            DualStreamViewMode.PARTNER_FOCUS -> {
                // Focus on partner's screen, draw directly on it!
                LivePhoneScreenContainer(
                    title = "Partner'ın Canlı Ekranı (Üzerine Çizim Yapın)",
                    feed = userBStream,
                    role = UserRole.USER_B,
                    isCurrentActor = (currentRole == UserRole.USER_B),
                    strokes = strokes.filter { it.targetScreen == UserRole.USER_B },
                    activeStroke = if (activeStroke?.targetScreen == UserRole.USER_B) activeStroke else null,
                    laserPointer = laserPointerB,
                    laserGlow = laserGlow,
                    toolState = toolState,
                    onDrawStart = { x, y -> onStartCrossDrawing(UserRole.USER_B, x, y) },
                    onDrawMove = { x, y -> onContinueCrossDrawing(x, y) },
                    onDrawEnd = { onEndCrossDrawing() },
                    onTogglePlayPause = { onToggleVideoPlayPause(UserRole.USER_B) },
                    onSeek = { sec -> onSeekVideo(UserRole.USER_B, sec) },
                    onChangeCategory = { cat -> onChangeVideoCategory(UserRole.USER_B, cat) },
                    onToggleSharing = { onToggleScreenSharing(UserRole.USER_B) },
                    onTogglePrivacy = { onTogglePrivacyShield(UserRole.USER_B) },
                    isLargeView = true,
                    testTagPrefix = "screen_partner_focus"
                )
            }

            DualStreamViewMode.MY_FOCUS -> {
                // Focus on my screen, watch partner draw on it!
                LivePhoneScreenContainer(
                    title = "Kendi Canlı Ekranım (Partner Çizimleri Görünür)",
                    feed = userAStream,
                    role = UserRole.USER_A,
                    isCurrentActor = (currentRole == UserRole.USER_A),
                    strokes = strokes.filter { it.targetScreen == UserRole.USER_A },
                    activeStroke = if (activeStroke?.targetScreen == UserRole.USER_A) activeStroke else null,
                    laserPointer = laserPointerA,
                    laserGlow = laserGlow,
                    toolState = toolState,
                    onDrawStart = { x, y -> onStartCrossDrawing(UserRole.USER_A, x, y) },
                    onDrawMove = { x, y -> onContinueCrossDrawing(x, y) },
                    onDrawEnd = { onEndCrossDrawing() },
                    onTogglePlayPause = { onToggleVideoPlayPause(UserRole.USER_A) },
                    onSeek = { sec -> onSeekVideo(UserRole.USER_A, sec) },
                    onChangeCategory = { cat -> onChangeVideoCategory(UserRole.USER_A, cat) },
                    onToggleSharing = { onToggleScreenSharing(UserRole.USER_A) },
                    onTogglePrivacy = { onTogglePrivacyShield(UserRole.USER_A) },
                    isLargeView = true,
                    testTagPrefix = "screen_my_focus"
                )
            }

            DualStreamViewMode.PIP_FLOAT -> {
                // Main partner screen with miniature floating feed of my screen
                Box(modifier = Modifier.fillMaxWidth()) {
                    LivePhoneScreenContainer(
                        title = "Partner Ekranı (Ana Yayın)",
                        feed = userBStream,
                        role = UserRole.USER_B,
                        isCurrentActor = (currentRole == UserRole.USER_B),
                        strokes = strokes.filter { it.targetScreen == UserRole.USER_B },
                        activeStroke = if (activeStroke?.targetScreen == UserRole.USER_B) activeStroke else null,
                        laserPointer = laserPointerB,
                        laserGlow = laserGlow,
                        toolState = toolState,
                        onDrawStart = { x, y -> onStartCrossDrawing(UserRole.USER_B, x, y) },
                        onDrawMove = { x, y -> onContinueCrossDrawing(x, y) },
                        onDrawEnd = { onEndCrossDrawing() },
                        onTogglePlayPause = { onToggleVideoPlayPause(UserRole.USER_B) },
                        onSeek = { sec -> onSeekVideo(UserRole.USER_B, sec) },
                        onChangeCategory = { cat -> onChangeVideoCategory(UserRole.USER_B, cat) },
                        onToggleSharing = { onToggleScreenSharing(UserRole.USER_B) },
                        onTogglePrivacy = { onTogglePrivacyShield(UserRole.USER_B) },
                        isLargeView = true,
                        testTagPrefix = "screen_pip_main"
                    )

                    // Miniature PiP corner window
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF090E17),
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 12.dp, bottom = 12.dp)
                            .width(130.dp)
                            .aspectRatio(9f / 16f)
                            .border(2.dp, CyanGlow, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            SimulatedPhoneAppScreen(
                                feed = userAStream,
                                modifier = Modifier.fillMaxSize()
                            )
                            Surface(
                                color = Color.Black.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(bottomStart = 8.dp),
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Text(
                                    text = "Sen (PiP)",
                                    color = ElectricCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- 4. INTEGRATED TELESTRATOR & DRAWING CONTROLS ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Brush,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Eş Zamanlı Çizim & Lazer Kontrolü",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }

                    Row {
                        IconButton(
                            onClick = { onUndoDrawing(null) },
                            modifier = Modifier.size(32.dp).testTag("action_undo_drawing")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Undo,
                                contentDescription = "Geri Al",
                                tint = CyanGlow,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = { onClearDrawings(null) },
                            modifier = Modifier.size(32.dp).testTag("action_clear_drawing")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Tümünü Temizle",
                                tint = NeonCoral,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tool selection chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DrawToolType.values().forEach { toolType ->
                        val isSelected = toolState.toolType == toolType
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFF0F2B3B) else Color(0xFF131D2E),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, CyanGlow) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSetToolType(toolType) }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = when (toolType) {
                                        DrawToolType.PEN -> Icons.Default.Brush
                                        DrawToolType.HIGHLIGHTER -> Icons.Default.Movie
                                        DrawToolType.LASER -> Icons.Default.FlashOn
                                        DrawToolType.DISAPPEARING -> Icons.Default.AutoAwesome
                                        DrawToolType.ERASER -> Icons.Default.DeleteSweep
                                    },
                                    contentDescription = null,
                                    tint = if (isSelected) CyanGlow else TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = toolType.label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) TextPrimary else TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Color picker palette
                val colors = listOf(
                    0xFF06B6D4 to ElectricCyan,
                    0xFF8B5CF6 to ElectricViolet,
                    0xFF10B981 to MintEmerald,
                    0xFFF43F5E to NeonCoral,
                    0xFFF59E0B to AmberWarning,
                    0xFFFFFFFF to Color.White
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Renk:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        colors.forEach { (colorLong, composeColor) ->
                            val isChosen = toolState.selectedColor == colorLong
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(composeColor)
                                    .clickable { onSetColor(colorLong) }
                                    .then(
                                        if (isChosen) Modifier.border(2.5.dp, Color.White, CircleShape)
                                        else Modifier
                                    )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Stroke width slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kalınlık: ${toolState.strokeWidth.toInt()}px",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        modifier = Modifier.width(90.dp)
                    )
                    Slider(
                        value = toolState.strokeWidth,
                        onValueChange = { onSetStrokeWidth(it) },
                        valueRange = 4f..28f,
                        colors = SliderDefaults.colors(
                            thumbColor = CyanGlow,
                            activeTrackColor = ElectricCyan,
                            inactiveTrackColor = Color(0xFF1E293B)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun LivePhoneScreenContainer(
    title: String,
    feed: VideoStreamFeed,
    role: UserRole,
    isCurrentActor: Boolean,
    strokes: List<DrawingStroke>,
    activeStroke: DrawingStroke?,
    laserPointer: Pair<Float, Float>?,
    laserGlow: Float,
    toolState: DrawingToolState,
    onDrawStart: (Float, Float) -> Unit,
    onDrawMove: (Float, Float) -> Unit,
    onDrawEnd: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onChangeCategory: (VideoCategory) -> Unit,
    onToggleSharing: () -> Unit,
    onTogglePrivacy: () -> Unit,
    isLargeView: Boolean = false,
    testTagPrefix: String = "screen"
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Container Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = if (role == UserRole.USER_A) ElectricCyan.copy(alpha = 0.2f) else ElectricViolet.copy(alpha = 0.2f),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (role == UserRole.USER_A) "1" else "2",
                                color = if (role == UserRole.USER_A) ElectricCyan else ElectricViolet,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "${feed.appName} • ${feed.activityDetail}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Privacy Shield toggle
                    IconButton(
                        onClick = onTogglePrivacy,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = if (feed.isPrivacyShieldOn) Icons.Default.Shield else Icons.Default.Security,
                            contentDescription = "Gizlilik Kalkanı",
                            tint = if (feed.isPrivacyShieldOn) AmberWarning else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    // Screen Sharing On/Off
                    IconButton(
                        onClick = onToggleSharing,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = if (feed.isSharingActive) Icons.Default.Videocam else Icons.Default.Close,
                            contentDescription = "Yayın Durumu",
                            tint = if (feed.isSharingActive) MintEmerald else NeonCoral,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // The Phone Screen Frame with interactive drawing & video stream
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(if (isLargeView) 10f / 16f else 12f / 16f)
                    .clip(RoundedCornerShape(22.dp))
                    .border(
                        2.5.dp,
                        Brush.linearGradient(
                            if (role == UserRole.USER_A) listOf(ElectricCyan, Color(0xFF0F766E))
                            else listOf(ElectricViolet, NeonCoral)
                        ),
                        RoundedCornerShape(22.dp)
                    )
                    .background(Color(0xFF070B14))
                    .testTag("${testTagPrefix}_frame")
            ) {
                if (!feed.isSharingActive) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ekran Yayını Duraklatıldı",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    // 1. Live Animated Video Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (feed.isPrivacyShieldOn) Modifier.blur(16.dp) else Modifier)
                    ) {
                        SimulatedPhoneAppScreen(
                            feed = feed,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // 2. Cross-Screen Telestration Canvas (where touches occur and draw strokes appear)
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val canvasW = constraints.maxWidth.toFloat().coerceAtLeast(1f)
                        val canvasH = constraints.maxHeight.toFloat().coerceAtLeast(1f)

                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(canvasW, canvasH, role) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            val normX = (offset.x / canvasW).coerceIn(0f, 1f)
                                            val normY = (offset.y / canvasH).coerceIn(0f, 1f)
                                            onDrawStart(normX, normY)
                                        },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            val normX = (change.position.x / canvasW).coerceIn(0f, 1f)
                                            val normY = (change.position.y / canvasH).coerceIn(0f, 1f)
                                            onDrawMove(normX, normY)
                                        },
                                        onDragEnd = { onDrawEnd() },
                                        onDragCancel = { onDrawEnd() }
                                    )
                                }
                                .testTag("${testTagPrefix}_canvas")
                        ) {
                            val allStrokes = strokes + listOfNotNull(activeStroke)

                            allStrokes.forEach { stroke ->
                                if (stroke.points.size > 1) {
                                    val strokePath = Path()
                                    val first = stroke.points.first()
                                    strokePath.moveTo(first.x * size.width, first.y * size.height)

                                    for (i in 1 until stroke.points.size) {
                                        val pt = stroke.points[i]
                                        strokePath.lineTo(pt.x * size.width, pt.y * size.height)
                                    }

                                    val baseColor = Color(stroke.colorArgb)
                                    val alpha = if (stroke.isDisappearing) 0.85f else 1.0f

                                    // Outer neon glow
                                    drawPath(
                                        path = strokePath,
                                        color = baseColor.copy(alpha = 0.35f * alpha),
                                        style = Stroke(
                                            width = stroke.strokeWidth * 1.8f,
                                            cap = StrokeCap.Round,
                                            join = StrokeJoin.Round
                                        )
                                    )

                                    // Inner core crisp line
                                    drawPath(
                                        path = strokePath,
                                        color = baseColor.copy(alpha = alpha),
                                        style = Stroke(
                                            width = stroke.strokeWidth,
                                            cap = StrokeCap.Round,
                                            join = StrokeJoin.Round
                                        )
                                    )
                                }
                            }

                            // Render Live Laser Pointer Beacon if active on this screen
                            laserPointer?.let { (lx, ly) ->
                                val px = lx * size.width
                                val py = ly * size.height

                                // Outer expanding glow rings
                                drawCircle(
                                    color = NeonCoral.copy(alpha = 0.35f),
                                    radius = 28f * laserGlow,
                                    center = Offset(px, py)
                                )
                                drawCircle(
                                    color = AmberWarning.copy(alpha = 0.65f),
                                    radius = 16f * laserGlow,
                                    center = Offset(px, py)
                                )
                                // Bright core center dot
                                drawCircle(
                                    color = Color.White,
                                    radius = 7f,
                                    center = Offset(px, py)
                                )
                            }
                        }

                        // Drawer name indicator tag following laser pointer or active stroke
                        laserPointer?.let { (lx, ly) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Black.copy(alpha = 0.75f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCoral),
                                modifier = Modifier
                                    .padding(
                                        start = (lx * 200).coerceAtLeast(10f).dp,
                                        top = (ly * 280).coerceAtLeast(10f).dp
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(NeonCoral)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Canlı Lazer İşaretçi",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Watermark & Resolution Indicator
                    Surface(
                        shape = RoundedCornerShape(bottomStart = 10.dp),
                        color = Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = "${feed.resolution} • ${feed.latencyMs}ms",
                            color = CyanGlow,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (feed.isPrivacyShieldOn) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Black.copy(alpha = 0.85f),
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = AmberWarning,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Gizlilik Kalkanı Aktif",
                                    color = AmberWarning,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Video Stream Playback & Category Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Play/Pause button
                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier.size(32.dp).testTag("${testTagPrefix}_play_pause")
                ) {
                    Icon(
                        imageVector = if (feed.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Oynat / Duraklat",
                        tint = CyanGlow,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Video time progress slider
                Slider(
                    value = feed.currentTimeSec,
                    onValueChange = onSeek,
                    valueRange = 0f..feed.totalDurationSec,
                    colors = SliderDefaults.colors(
                        thumbColor = CyanGlow,
                        activeTrackColor = ElectricCyan,
                        inactiveTrackColor = Color(0xFF1E293B)
                    ),
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )

                Text(
                    text = String.format("%02d:%02d", (feed.currentTimeSec / 60).toInt(), (feed.currentTimeSec % 60).toInt()),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            // Quick Category & App Switcher
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(VideoCategory.values()) { cat ->
                    val isCatSelected = feed.category == cat
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isCatSelected) Color(0xFF112E40) else Color(0xFF0E1726),
                        border = if (isCatSelected) androidx.compose.foundation.BorderStroke(1.dp, CyanGlow) else null,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onChangeCategory(cat) }
                    ) {
                        Text(
                            text = when (cat) {
                                VideoCategory.WHATSAPP -> "WhatsApp"
                                VideoCategory.INSTAGRAM -> "Instagram"
                                VideoCategory.CHROME -> "Chrome"
                                VideoCategory.YOUTUBE -> "YouTube"
                                VideoCategory.SPOTIFY -> "Spotify"
                                VideoCategory.LIVE_MAP -> "GPS Rota"
                                VideoCategory.GALLERY -> "Galeri"
                                VideoCategory.NATURE_4K -> "Doğa 4K"
                                VideoCategory.CYBER_NEON -> "Siber Neon"
                                VideoCategory.CUSTOM_MEDIA -> "Cihaz Medyası"
                            },
                            color = if (isCatSelected) CyanGlow else TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 60 FPS Procedural Animated Video Canvas simulating rich live video streams (4K Nature, Cyberpunk City, GPS Route, Gallery)
 */
@Composable
fun InteractiveVideoCanvas(
    feed: VideoStreamFeed,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "videoCanvasAnim")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        when (feed.category) {
            VideoCategory.NATURE_4K -> {
                // Sky & Waterfall Gradient
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF032B44),
                            Color(0xFF054D68),
                            Color(0xFF0F766E),
                            Color(0xFF064E3B)
                        )
                    )
                )

                // Waterfall Flow lines
                for (i in 0..12) {
                    val xPos = (w * 0.35f) + (i * 12f)
                    val yAnim = ((waveOffset * 80f + i * 25f) % h)
                    drawLine(
                        color = Color(0xFFA5F3FC).copy(alpha = 0.5f),
                        start = Offset(xPos, yAnim),
                        end = Offset(xPos, (yAnim + 70f).coerceAtMost(h)),
                        strokeWidth = 3f
                    )
                }

                // Gentle Mist Waves at Bottom
                for (row in 0..2) {
                    val wavePath = Path()
                    wavePath.moveTo(0f, h - 80f + row * 25f)
                    for (x in 0..w.toInt() step 20) {
                        val y = h - 80f + row * 25f + sin(x * 0.03f + waveOffset + row).toFloat() * 12f
                        wavePath.lineTo(x.toFloat(), y)
                    }
                    wavePath.lineTo(w, h)
                    wavePath.lineTo(0f, h)
                    wavePath.close()

                    drawPath(
                        path = wavePath,
                        color = Color(0xFF0891B2).copy(alpha = 0.35f + row * 0.15f)
                    )
                }
            }

            VideoCategory.CYBER_NEON -> {
                // Deep Cyberpunk Space
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF090A1A), Color(0xFF1B0A2A), Color(0xFF2C0A3E))
                    )
                )

                // Synthwave Neon Sun
                val sunCenter = Offset(w * 0.5f, h * 0.35f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFF007F), Color(0xFF7928CA), Color.Transparent)
                    ),
                    radius = w * 0.32f,
                    center = sunCenter
                )

                // Perspective Neon Grid Floor
                val horizonY = h * 0.52f
                drawLine(
                    color = ElectricCyan,
                    start = Offset(0f, horizonY),
                    end = Offset(w, horizonY),
                    strokeWidth = 2f
                )

                // Radial perspective lines
                for (angle in 0..10) {
                    val startX = w * (angle / 10f)
                    drawLine(
                        color = ElectricCyan.copy(alpha = 0.4f),
                        start = Offset(w * 0.5f, horizonY),
                        end = Offset(startX, h),
                        strokeWidth = 1.8f
                    )
                }

                // Moving horizontal grid bars
                for (bar in 0..6) {
                    val progress = ((waveOffset * 0.2f + bar / 6f) % 1f)
                    val barY = horizonY + (progress * progress) * (h - horizonY)
                    drawLine(
                        color = NeonCoral.copy(alpha = progress * 0.8f),
                        start = Offset(0f, barY),
                        end = Offset(w, barY),
                        strokeWidth = 2.5f
                    )
                }
            }

            VideoCategory.LIVE_MAP -> {
                // Dark Modern Street Map Grid
                drawRect(color = Color(0xFF0D1424))

                // Roads and Intersections
                for (xStep in 0..5) {
                    val rx = w * (xStep / 5f)
                    drawLine(
                        color = Color(0xFF1E293B),
                        start = Offset(rx, 0f),
                        end = Offset(rx, h),
                        strokeWidth = 12f
                    )
                }
                for (yStep in 0..7) {
                    val ry = h * (yStep / 7f)
                    drawLine(
                        color = Color(0xFF1E293B),
                        start = Offset(0f, ry),
                        end = Offset(w, ry),
                        strokeWidth = 10f
                    )
                }

                // Dynamic Navigation Path & Moving GPS Car Dot
                val routePath = Path()
                routePath.moveTo(w * 0.2f, h * 0.85f)
                routePath.lineTo(w * 0.2f, h * 0.5f)
                routePath.lineTo(w * 0.6f, h * 0.5f)
                routePath.lineTo(w * 0.6f, h * 0.2f)

                drawPath(
                    path = routePath,
                    color = ElectricCyan,
                    style = Stroke(width = 6f, cap = StrokeCap.Round)
                )

                // Animated vehicle point along route
                val carT = (waveOffset / 6.283f)
                val (carX, carY) = if (carT < 0.33f) {
                    val seg = carT / 0.33f
                    Pair(w * 0.2f, h * 0.85f - seg * (h * 0.35f))
                } else if (carT < 0.66f) {
                    val seg = (carT - 0.33f) / 0.33f
                    Pair(w * 0.2f + seg * (w * 0.4f), h * 0.5f)
                } else {
                    val seg = (carT - 0.66f) / 0.34f
                    Pair(w * 0.6f, h * 0.5f - seg * (h * 0.3f))
                }

                drawCircle(
                    color = MintEmerald.copy(alpha = 0.4f),
                    radius = 16f,
                    center = Offset(carX, carY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 6f,
                    center = Offset(carX, carY)
                )
            }

            VideoCategory.GALLERY -> {
                // High-res Gallery Photo Collage Simulation
                drawRect(color = Color(0xFF080D1A))

                // Photos Grid
                val tileW = (w - 24f) / 2f
                val tileH = (h - 32f) / 3f

                val photoPalettes = listOf(
                    listOf(Color(0xFF2563EB), Color(0xFF60A5FA)),
                    listOf(Color(0xFFDB2777), Color(0xFFF472B6)),
                    listOf(Color(0xFF059669), Color(0xFF34D399)),
                    listOf(Color(0xFFD97706), Color(0xFFFBBF24)),
                    listOf(Color(0xFF7C3AED), Color(0xFFA78BFA)),
                    listOf(Color(0xFF0891B2), Color(0xFF38BDF8))
                )

                for (idx in 0..5) {
                    val col = idx % 2
                    val row = idx / 2
                    val tx = 8f + col * (tileW + 8f)
                    val ty = 8f + row * (tileH + 8f)

                    drawRoundRect(
                        brush = Brush.linearGradient(photoPalettes[idx]),
                        topLeft = Offset(tx, ty),
                        size = androidx.compose.ui.geometry.Size(tileW, tileH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f)
                    )
                }
            }

            VideoCategory.CUSTOM_MEDIA -> {
                // Device Media Stream Canvas
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF0F172A))
                    )
                )
                // Center media icon / glow
                drawCircle(
                    color = ElectricCyan.copy(alpha = 0.2f),
                    radius = 50f,
                    center = Offset(w / 2f, h / 2f)
                )
                drawCircle(
                    color = ElectricCyan,
                    radius = 12f,
                    center = Offset(w / 2f, h / 2f)
                )
            }

            VideoCategory.INSTAGRAM -> {
                // Instagram Dark UI
                drawRect(color = Color(0xFF0A0A0A))
                // Top header bar
                drawRect(
                    color = Color(0xFF18181B),
                    topLeft = Offset(0f, 0f),
                    size = androidx.compose.ui.geometry.Size(w, 40f)
                )
                // Story circles row
                for (s in 0..4) {
                    val scx = 24f + s * 44f
                    if (scx < w - 20f) {
                        drawCircle(
                            brush = Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFEC4899), Color(0xFF8B5CF6))),
                            radius = 16f,
                            center = Offset(scx, 64f),
                            style = Stroke(width = 2.5f)
                        )
                        drawCircle(
                            color = Color(0xFF27272A),
                            radius = 13f,
                            center = Offset(scx, 64f)
                        )
                    }
                }
                // Post card container
                drawRoundRect(
                    color = Color(0xFF18181B),
                    topLeft = Offset(16f, 96f),
                    size = androidx.compose.ui.geometry.Size(w - 32f, (h - 130f).coerceAtLeast(80f)),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f)
                )
                // Simulated photo content gradient
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(Color(0xFF3B82F6).copy(alpha = 0.4f), Color(0xFF8B5CF6).copy(alpha = 0.5f))),
                    topLeft = Offset(24f, 130f),
                    size = androidx.compose.ui.geometry.Size(w - 48f, (h - 200f).coerceAtLeast(60f)),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                )
            }

            VideoCategory.WHATSAPP -> {
                // WhatsApp Dark UI
                drawRect(color = Color(0xFF0B141A))
                // WhatsApp Header
                drawRect(
                    color = Color(0xFF1F2C34),
                    topLeft = Offset(0f, 0f),
                    size = androidx.compose.ui.geometry.Size(w, 44f)
                )
                // Chat bubbles
                // Bubble 1 (Partner - left)
                drawRoundRect(
                    color = Color(0xFF1F2C34),
                    topLeft = Offset(16f, 60f),
                    size = androidx.compose.ui.geometry.Size((w * 0.65f).coerceAtMost(280f), 48f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                )
                // Bubble 2 (User - right)
                drawRoundRect(
                    color = Color(0xFF005D4B),
                    topLeft = Offset(w - (w * 0.65f).coerceAtMost(280f) - 16f, 120f),
                    size = androidx.compose.ui.geometry.Size((w * 0.65f).coerceAtMost(280f), 52f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                )
                // Bubble 3 (Partner - left)
                drawRoundRect(
                    color = Color(0xFF1F2C34),
                    topLeft = Offset(16f, 185f),
                    size = androidx.compose.ui.geometry.Size((w * 0.55f).coerceAtMost(240f), 42f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                )
            }

            VideoCategory.CHROME -> {
                // Chrome Browser
                drawRect(color = Color(0xFF1E1F22))
                // Search/Omnibox
                drawRoundRect(
                    color = Color(0xFF2B2D31),
                    topLeft = Offset(16f, 8f),
                    size = androidx.compose.ui.geometry.Size(w - 32f, 36f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f)
                )
                // Web Page content card
                drawRoundRect(
                    color = Color(0xFF2B2D31).copy(alpha = 0.6f),
                    topLeft = Offset(16f, 56f),
                    size = androidx.compose.ui.geometry.Size(w - 32f, (h - 76f).coerceAtLeast(80f)),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                )
            }

            VideoCategory.YOUTUBE -> {
                // YouTube Dark Red/Black
                drawRect(color = Color(0xFF0F0F0F))
                // Video thumbnail player
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(Color(0xFF7F1D1D), Color(0xFF18181B))),
                    topLeft = Offset(12f, 8f),
                    size = androidx.compose.ui.geometry.Size(w - 24f, (h * 0.55f).coerceAtLeast(100f)),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                )
                // Red play indicator
                drawCircle(
                    color = Color(0xFFEF4444),
                    radius = 22f,
                    center = Offset(w / 2f, (h * 0.28f).coerceAtLeast(50f))
                )
            }

            VideoCategory.SPOTIFY -> {
                // Spotify Dark Emerald
                drawRect(
                    brush = Brush.verticalGradient(listOf(Color(0xFF065F46), Color(0xFF121212)))
                )
                // Album artwork
                val artSize = (w * 0.55f).coerceAtMost(160f)
                drawRoundRect(
                    brush = Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF047857))),
                    topLeft = Offset((w - artSize) / 2f, 30f),
                    size = androidx.compose.ui.geometry.Size(artSize, artSize),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
                )
                // Progress line
                drawRoundRect(
                    color = Color(0xFF10B981),
                    topLeft = Offset(24f, 40f + artSize),
                    size = androidx.compose.ui.geometry.Size((w - 48f) * 0.6f, 4f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                )
            }

            else -> {
                // Fallback Cyber stream
                drawRect(
                    brush = Brush.verticalGradient(listOf(Color(0xFF0B132B), Color(0xFF1C2541)))
                )
            }
        }

        // Live Audio Equalizer Bars at bottom right
        val barCount = 6
        val barWidth = 4f
        val gap = 3f
        val startX = w - 48f
        for (b in 0 until barCount) {
            val barH = 6f + (sin(waveOffset * 3f + b * 1.2f).toFloat().coerceAtLeast(0f) * 18f)
            drawRoundRect(
                color = CyanGlow.copy(alpha = 0.75f),
                topLeft = Offset(startX + b * (barWidth + gap), h - 14f - barH),
                size = androidx.compose.ui.geometry.Size(barWidth, barH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
            )
        }
    }
}

@Composable
fun StreamMetricBadge(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF0F172A),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = color,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun PartnerActivityInspectorCard(
    stream: VideoStreamFeed,
    screenActivity: ScreenActivityState,
    realTelemetry: RealDeviceTelemetryState,
    onRequestUsagePermission: () -> Unit,
    onRefreshRealData: () -> Unit,
    onRequestScreenCapture: () -> Unit,
    onChangeCategory: (VideoCategory) -> Unit
) {
    var showTimelineHistory by remember { mutableStateOf(false) }
    var showAllAppsUsage by remember { mutableStateOf(true) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("partner_activity_inspector_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header with pulsing indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    Text(
                        text = "GERÇEK CİHAZ AKTİVİTESİ & UYGULAMA TAKİBİ",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MintEmerald
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onRefreshRealData,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Yenile",
                            tint = CyanGlow,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF14243B),
                        modifier = Modifier.clickable { showTimelineHistory = !showTimelineHistory }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = CyanGlow,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (showTimelineHistory) "Gizle" else "Geçmiş (${screenActivity.activityEvents.size})",
                                color = CyanGlow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- REAL TELEMETRY STATUS / PERMISSION BANNER ---
            if (!realTelemetry.isUsagePermissionGranted) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF2D1600),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberWarning),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = AmberWarning,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gerçek Uygulama Verileri İçin İzin Gerekli",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = AmberWarning
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Simülasyon yerine cihazın gerçekte hangi uygulamaya girdiğini, ne kadar süre kullandığını ve gerçek zamanlı hareketlerini görmek için Android sistem 'Kullanım Erişimi' iznini onaylayın.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFDE68A),
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AmberWarning,
                                modifier = Modifier
                                    .clickable { onRequestUsagePermission() }
                            ) {
                                Text(
                                    text = "Ayarlardan İzni Aç",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF3E2300),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AmberWarning.copy(alpha = 0.5f)),
                                modifier = Modifier.clickable { onRefreshRealData() }
                            ) {
                                Text(
                                    text = "İzni Kontrol Et",
                                    color = AmberWarning,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF06281E),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MintEmerald),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MintEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Gerçek Sistem Verileri Aktif",
                                color = MintEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Pil: %${realTelemetry.realBatteryPercent} • ${realTelemetry.networkType}",
                                color = Color(0xFFA7F3D0),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Active App & Current Action details
            Surface(
                color = Color(0xFF090E17),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (stream.category) {
                                    VideoCategory.WHATSAPP -> Color(0xFF00A884)
                                    VideoCategory.INSTAGRAM -> Color(0xFFDD2A7B)
                                    VideoCategory.CHROME -> Color(0xFF4285F4)
                                    VideoCategory.YOUTUBE -> Color(0xFFFF0000)
                                    VideoCategory.SPOTIFY -> Color(0xFF1DB954)
                                    VideoCategory.LIVE_MAP -> Color(0xFF0F766E)
                                    else -> ElectricCyan
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = when (stream.category) {
                                            VideoCategory.WHATSAPP -> Icons.Default.ChatBubbleOutline
                                            VideoCategory.INSTAGRAM -> Icons.Default.Favorite
                                            VideoCategory.CHROME -> Icons.Default.Search
                                            VideoCategory.YOUTUBE -> Icons.Default.PlayArrow
                                            VideoCategory.SPOTIFY -> Icons.Default.MusicNote
                                            VideoCategory.LIVE_MAP -> Icons.Default.Navigation
                                            else -> Icons.Default.Devices
                                        },
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Girdiği Uygulama: ${screenActivity.activeAppName}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Paket/Sayfa: ${screenActivity.currentUrlOrPage}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanGlow,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF1A2634)
                        ) {
                            Text(
                                text = "CANLI SİSTEM",
                                color = MintEmerald,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = screenActivity.activityDescription,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFFE2E8F0)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- REAL DEVICE APPS USAGE LIST (UsageStatsManager Data) ---
            if (realTelemetry.realAppsUsageList.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cihazda Bugün Kullanılan Gerçek Uygulamalar (${realTelemetry.realAppsUsageList.size}):",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = CyanGlow
                    )
                    Text(
                        text = if (showAllAppsUsage) "Kapat" else "Genişlet",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { showAllAppsUsage = !showAllAppsUsage }
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))

                AnimatedVisibility(visible = showAllAppsUsage) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF090E17))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        realTelemetry.realAppsUsageList.take(6).forEach { appInfo ->
                            val minutes = (appInfo.totalTimeInForegroundMs / 60000).toInt()
                            val seconds = ((appInfo.totalTimeInForegroundMs % 60000) / 1000).toInt()
                            val durationStr = if (minutes > 0) "$minutes dk $seconds sn" else "$seconds sn"

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF131D2E),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = appInfo.appName,
                                            color = TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = appInfo.packageName,
                                            color = TextMuted,
                                            fontSize = 9.sp
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = durationStr,
                                            color = if (minutes > 10) NeonCoral else ElectricCyan,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (appInfo.isCurrentlyActive) {
                                            Text(
                                                text = "ŞU AN AÇIK",
                                                color = MintEmerald,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // --- REAL DEVICE HARDWARE TELEMETRY PANEL (RAM, Depolama, Ekran) ---
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F172A),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "CİHAZ DONANIM & SİSTEM TELEMETRİSİ",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ElectricCyan,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // RAM Box
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(text = "RAM Bellek", color = TextMuted, fontSize = 9.sp)
                                Text(
                                    text = "%.1f / %.1f GB".format(realTelemetry.ramUsedGb, realTelemetry.ramTotalGb),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "%${realTelemetry.ramPercent} Kullanımda",
                                    color = if (realTelemetry.ramPercent > 80) AmberWarning else MintEmerald,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Storage Box
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(text = "Dahili Hafıza", color = TextMuted, fontSize = 9.sp)
                                Text(
                                    text = "%.0f / %.0f GB".format(realTelemetry.storageUsedGb, realTelemetry.storageTotalGb),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "%${realTelemetry.storagePercent} Dolu",
                                    color = CyanGlow,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Screen Metrics Box
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(text = "Ekran & Panel", color = TextMuted, fontSize = 9.sp)
                                Text(
                                    text = realTelemetry.displayResolution,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${realTelemetry.displayFps} Hz Yenileme",
                                    color = MintEmerald,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Real Screen Capture Launcher Button
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF172554),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRequestScreenCapture() }
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Devices,
                        contentDescription = null,
                        tint = CyanGlow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Gerçek Ekran Paylaşımını Başlat (MediaProjection)",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Expandable Chronological Timeline (Nereye Girdi, Neler Yaptı)
            AnimatedVisibility(visible = showTimelineHistory) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF080C14))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Gerçek Sistem Hareket Kaydı (Kronolojik Olaylar)",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    if (screenActivity.activityEvents.isEmpty()) {
                        Text(
                            text = "Henüz kaydedilmiş sistem olayı yok. Kullanım izni verildikten sonra açılan uygulamalar burada listelenir.",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    } else {
                        screenActivity.activityEvents.take(8).forEach { ev ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(ElectricCyan)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "${ev.appName} • ${ev.actionDetail}",
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = ev.targetPageOrUrl,
                                        color = TextMuted,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

