package com.example

import android.content.Context
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.ScreenShare
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.E2eeInfoDialog
import com.example.ui.components.PartnerSwitchHeader
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.DrawingScreen
import com.example.ui.screens.LocationScreen
import com.example.ui.screens.ScreenStreamScreen
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.DeepBackground
import com.example.ui.theme.DeepSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.SyncMateViewModel

enum class MainNavTab(
    val title: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
) {
    SCREEN_STREAM(
        title = "Ekran Yayını",
        selectedIcon = Icons.Filled.ScreenShare,
        unselectedIcon = Icons.Outlined.ScreenShare,
        testTag = "nav_screen_stream"
    ),
    LOCATION(
        title = "Canlı Konum",
        selectedIcon = Icons.Filled.LocationOn,
        unselectedIcon = Icons.Outlined.LocationOn,
        testTag = "nav_location"
    ),
    DRAWING(
        title = "Canlı Çizim",
        selectedIcon = Icons.Filled.Brush,
        unselectedIcon = Icons.Outlined.Brush,
        testTag = "nav_drawing"
    ),
    CHAT(
        title = "Şifreli Sohbet",
        selectedIcon = Icons.AutoMirrored.Filled.Chat,
        unselectedIcon = Icons.AutoMirrored.Outlined.Chat,
        testTag = "nav_chat"
    )
}

class MainActivity : ComponentActivity() {
    private val viewModel: SyncMateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SyncMateApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SyncMateApp(viewModel: SyncMateViewModel) {
    var selectedTab by remember { mutableStateOf(MainNavTab.SCREEN_STREAM) }

    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val userALoc by viewModel.userALocation.collectAsStateWithLifecycle()
    val userBLoc by viewModel.userBLocation.collectAsStateWithLifecycle()
    val partnerBreadcrumbs by viewModel.partnerBreadcrumbs.collectAsStateWithLifecycle()
    val geofenceSettings by viewModel.geofenceSettings.collectAsStateWithLifecycle()
    val isGeofenceBreached by viewModel.isGeofenceBreached.collectAsStateWithLifecycle()
    val navigationSteps by viewModel.navigationSteps.collectAsStateWithLifecycle()

    val screenActivity by viewModel.screenActivity.collectAsStateWithLifecycle()
    val azimuthDegrees by viewModel.azimuthDegrees.collectAsStateWithLifecycle()
    val walkieTalkieState by viewModel.walkieTalkieState.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Dual screen video streams & sync telemetry
    val userAStream by viewModel.userAStream.collectAsStateWithLifecycle()
    val userBStream by viewModel.userBStream.collectAsStateWithLifecycle()
    val dualViewMode by viewModel.dualViewMode.collectAsStateWithLifecycle()
    val networkSyncStats by viewModel.networkSyncStats.collectAsStateWithLifecycle()
    val recentSyncLogs by viewModel.recentSyncLogs.collectAsStateWithLifecycle()

    // Collaborative Drawings & Lasers
    val strokes by viewModel.drawingStrokes.collectAsStateWithLifecycle()
    val activeStroke by viewModel.activeStroke.collectAsStateWithLifecycle()
    val laserPointer by viewModel.laserPointer.collectAsStateWithLifecycle()
    val laserPointerA by viewModel.laserPointerOnScreenA.collectAsStateWithLifecycle()
    val laserPointerB by viewModel.laserPointerOnScreenB.collectAsStateWithLifecycle()
    val toolState by viewModel.toolState.collectAsStateWithLifecycle()

    val showE2eeDialog by viewModel.showE2eeDialog.collectAsStateWithLifecycle()
    val inspectedMessage by viewModel.inspectedMessage.collectAsStateWithLifecycle()
    val realTelemetry by viewModel.realTelemetry.collectAsStateWithLifecycle()

    val mediaProjectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val serviceIntent = android.content.Intent(context, com.example.service.ScreenCaptureService::class.java).apply {
                action = com.example.service.ScreenCaptureService.ACTION_START
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            viewModel.refreshRealDeviceData(context)
        }
    }

    // Auto-init GPS and real device usage telemetry polling on app launch
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.startGpsUpdates(context)
        viewModel.startRealDevicePolling(context)
        viewModel.refreshRealDeviceData(context)
    }

    // Back handling: If on secondary tab, return to Screen Stream tab
    BackHandler(enabled = selectedTab != MainNavTab.SCREEN_STREAM) {
        selectedTab = MainNavTab.SCREEN_STREAM
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            PartnerSwitchHeader(
                currentRole = currentRole,
                sessionState = sessionState,
                walkieTalkieState = walkieTalkieState,
                onToggleRole = { viewModel.toggleRole() },
                onOpenE2eeDetails = { viewModel.toggleE2eeDialog(true) },
                onToggleWalkieTalkie = { viewModel.setWalkieTalkieTransmitting(!walkieTalkieState.isTransmitting) }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .background(DeepSurface)
                    .testTag("main_bottom_nav"),
                containerColor = DeepSurface,
                tonalElevation = 6.dp
            ) {
                MainNavTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                tint = if (isSelected) CyanGlow else TextMuted
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                color = if (isSelected) TextPrimary else TextMuted
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFF0E3A4A),
                            selectedIconColor = CyanGlow,
                            unselectedIconColor = TextMuted,
                            selectedTextColor = TextPrimary,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DeepBackground)
        ) {
            when (selectedTab) {
                MainNavTab.SCREEN_STREAM -> {
                    ScreenStreamScreen(
                        currentRole = currentRole,
                        userAStream = userAStream,
                        userBStream = userBStream,
                        dualViewMode = dualViewMode,
                        networkSyncStats = networkSyncStats,
                        recentSyncLogs = recentSyncLogs,
                        strokes = strokes,
                        activeStroke = activeStroke,
                        laserPointerA = laserPointerA,
                        laserPointerB = laserPointerB,
                        toolState = toolState,
                        screenActivity = screenActivity,
                        realTelemetry = realTelemetry,
                        onRequestUsagePermission = {
                            try {
                                val intent = viewModel.getUsageAccessSettingsIntent()
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        onRefreshRealData = {
                            viewModel.refreshRealDeviceData(context)
                        },
                        onRequestScreenCapture = {
                            try {
                                val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
                                projectionManager?.let {
                                    mediaProjectionLauncher.launch(it.createScreenCaptureIntent())
                                }
                            } catch (_: Exception) {}
                        },
                        onSetDualViewMode = { mode -> viewModel.setDualViewMode(mode) },
                        onStartCrossDrawing = { target, x, y -> viewModel.startCrossDrawing(target, x, y) },
                        onContinueCrossDrawing = { x, y -> viewModel.continueCrossDrawing(x, y) },
                        onEndCrossDrawing = { viewModel.endCrossDrawing() },
                        onSetColor = { color -> viewModel.setDrawingColor(color) },
                        onSetStrokeWidth = { width -> viewModel.setStrokeWidth(width) },
                        onSetToolType = { type -> viewModel.setDrawToolType(type) },
                        onClearDrawings = { target -> viewModel.clearDrawings(target) },
                        onUndoDrawing = { target -> viewModel.undoDrawing(target) },
                        onToggleVideoPlayPause = { owner -> viewModel.toggleVideoPlayPause(owner) },
                        onSeekVideo = { owner, sec -> viewModel.seekVideo(owner, sec) },
                        onChangeVideoCategory = { owner, cat -> viewModel.changeVideoCategory(owner, cat) },
                        onToggleScreenSharing = { owner -> viewModel.toggleScreenSharing(owner) },
                        onTogglePrivacyShield = { owner -> viewModel.togglePrivacyShield(owner) }
                    )
                }
                MainNavTab.CHAT -> {
                    ChatScreen(
                        currentRole = currentRole,
                        messages = messages,
                        onSendMessage = { text, isDisappearing ->
                            viewModel.sendMessage(text, isDisappearing)
                        },
                        onSendLocation = {
                            viewModel.sendLocationMessage()
                        },
                        onSendVoiceNote = { duration ->
                            viewModel.sendVoiceNoteMessage(duration)
                        },
                        onAddReaction = { id, emoji ->
                            viewModel.addReaction(id, emoji)
                        },
                        onInspectMessageCrypto = { msg ->
                            viewModel.inspectMessageCrypto(msg)
                            viewModel.toggleE2eeDialog(true)
                        },
                        onNavigateToLocation = { selectedTab = MainNavTab.LOCATION },
                        onNavigateToDrawing = { selectedTab = MainNavTab.DRAWING },
                        onNavigateToScreenStream = { selectedTab = MainNavTab.SCREEN_STREAM }
                    )
                }
                MainNavTab.LOCATION -> {
                    LocationScreen(
                        currentRole = currentRole,
                        userALocation = userALoc,
                        userBLocation = userBLoc,
                        formattedDistance = viewModel.getFormattedDistance(),
                        bearingDegrees = viewModel.getBearingDegrees(),
                        azimuthDegrees = azimuthDegrees,
                        partnerBreadcrumbs = partnerBreadcrumbs,
                        geofenceSettings = geofenceSettings,
                        isGeofenceBreached = isGeofenceBreached,
                        navigationSteps = navigationSteps,
                        onStartGps = { ctx -> viewModel.startGpsUpdates(ctx) },
                        onSelectCityPreset = { city -> viewModel.selectCityPreset(city) },
                        onSetManualCoordinates = { lat, lng, name -> viewModel.setManualCoordinates(lat, lng, name) },
                        onSetPartnerProximity = { mode -> viewModel.setPartnerProximity(mode) },
                        onRelocatePin = { role, lat, lng, place, addr -> viewModel.relocatePin(role, lat, lng, place, addr) },
                        onSearchLocation = { query, onResult -> viewModel.searchLocation(context, query, onResult) },
                        onSetGeofenceRadius = { r -> viewModel.setGeofenceRadius(r) },
                        onToggleGeofence = { en -> viewModel.toggleGeofence(en) },
                        onSendLocationToChat = {
                            viewModel.sendLocationMessage()
                            selectedTab = MainNavTab.CHAT
                        },
                        onSendSosBeacon = {
                            viewModel.playSosEmergencySound()
                            viewModel.sendSosBeacon()
                            selectedTab = MainNavTab.CHAT
                        }
                    )
                }
                MainNavTab.DRAWING -> {
                    DrawingScreen(
                        currentRole = currentRole,
                        strokes = strokes,
                        activeStroke = activeStroke,
                        laserPointer = laserPointer,
                        toolState = toolState,
                        screenActivity = screenActivity,
                        partnerStream = if (currentRole == com.example.model.UserRole.USER_A) userBStream else userAStream,
                        onStartDrawing = { x, y -> viewModel.startDrawing(x, y) },
                        onContinueDrawing = { x, y -> viewModel.continueDrawing(x, y) },
                        onEndDrawing = { viewModel.endDrawing() },
                        onSetColor = { color -> viewModel.setDrawingColor(color) },
                        onSetStrokeWidth = { width -> viewModel.setStrokeWidth(width) },
                        onToggleLaserMode = { viewModel.toggleLaserMode() },
                        onToggleDrawOverScreen = { over -> viewModel.toggleDrawOverScreen(over) },
                        onClearDrawings = { viewModel.clearDrawings() },
                        onUndoDrawing = { viewModel.undoDrawing() },
                        onSendDrawingToChat = {
                            viewModel.sendDrawingCardMessage("Ekran Çizim Notu")
                            selectedTab = MainNavTab.CHAT
                        }
                    )
                }
            }
        }

        // End-to-End Cryptography Verification Dialog
        if (showE2eeDialog) {
            E2eeInfoDialog(
                inspectedMessage = inspectedMessage,
                onDismiss = {
                    viewModel.toggleE2eeDialog(false)
                    viewModel.inspectMessageCrypto(null)
                }
            )
        }
    }
}
