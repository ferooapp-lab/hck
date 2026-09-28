package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.GeofenceSettings
import com.example.model.LiveLocation
import com.example.model.LocationBreadcrumb
import com.example.model.LocationSearchResult
import com.example.model.NavigationStep
import com.example.model.PartnerProximityMode
import com.example.model.UserRole
import com.example.state.CityPreset
import com.example.state.SyncMateRepository
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
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LocationScreen(
    currentRole: UserRole,
    userALocation: LiveLocation,
    userBLocation: LiveLocation,
    formattedDistance: String,
    bearingDegrees: Float,
    azimuthDegrees: Float,
    partnerBreadcrumbs: List<LocationBreadcrumb>,
    geofenceSettings: GeofenceSettings,
    isGeofenceBreached: Boolean,
    navigationSteps: List<NavigationStep>,
    onStartGps: (Context) -> Unit,
    onSelectCityPreset: (CityPreset) -> Unit,
    onSetManualCoordinates: (Double, Double, String) -> Unit,
    onSetPartnerProximity: (PartnerProximityMode) -> Unit,
    onRelocatePin: (UserRole, Double, Double, String, String) -> Unit,
    onSearchLocation: (String, (List<LocationSearchResult>) -> Unit) -> Unit,
    onSetGeofenceRadius: (Float) -> Unit,
    onToggleGeofence: (Boolean) -> Unit,
    onSendLocationToChat: () -> Unit,
    onSendSosBeacon: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            hasLocationPermission = true
            onStartGps(context)
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            onStartGps(context)
        }
    }

    // Trigger subtle vibration on geofence breach
    LaunchedEffect(isGeofenceBreached) {
        if (isGeofenceBreached) {
            try {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(250, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(250)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    // Dialog state controllers
    var showCityPicker by remember { mutableStateOf(false) }
    var showGeofenceDialog by remember { mutableStateOf(false) }
    var showNavigationDrawer by remember { mutableStateOf(false) }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<LocationSearchResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    // Pin Placement Mode (tap map to relocate)
    var isPinDropMode by remember { mutableStateOf(false) }
    var pinDropTargetRole by remember { mutableStateOf(UserRole.USER_A) }

    // Map Pan and Zoom states
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // Radar sweep rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "radarTransition")
    val radarAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarAngle"
    )

    // Pulse animation for pins
    val pulseRatio by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseRatio"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBackground)
            .testTag("location_screen")
    ) {
        // Geofence Breach Banner Alert
        AnimatedVisibility(
            visible = isGeofenceBreached,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                color = NeonCoral,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DİKKAT: Partner belirlenen ${geofenceSettings.radiusMeters.toInt()}m güvenli alanın dışına çıktı!",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }

        // Top Telemetry & Controls Card
        Surface(
            color = SurfaceCard,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                // Address & City Quick Selector Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MintEmerald)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = userALocation.placeName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = userALocation.streetAddress,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Live Search Button
                        IconButton(
                            onClick = { isSearchExpanded = !isSearchExpanded },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSearchExpanded) Color(0xFF1E3A5F) else SurfaceCardElevated)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Konum Ara",
                                tint = CyanGlow,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Refresh GPS Fix Button
                        IconButton(
                            onClick = { onStartGps(context) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceCardElevated)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "GPS Yenile",
                                tint = MintEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Şehir Seçici Modal Butonu
                        OutlinedButton(
                            onClick = { showCityPicker = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("change_city_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationCity,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Şehir Seç", color = ElectricCyan, fontSize = 11.sp)
                        }
                    }
                }

                // Expandable Real-time Address & City Search Bar
                AnimatedVisibility(visible = isSearchExpanded) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { query ->
                                searchQuery = query
                                if (query.length >= 2) {
                                    isSearching = true
                                    onSearchLocation(query) { results ->
                                        searchResults = results
                                        isSearching = false
                                    }
                                } else {
                                    searchResults = emptyList()
                                }
                            },
                            placeholder = { Text("Şehir, ilçe veya semt ara (örn: Kadıköy, Kızılay, Van)...", fontSize = 12.sp) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = {
                                        searchQuery = ""
                                        searchResults = emptyList()
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Temizle", tint = TextMuted)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanGlow,
                                unfocusedBorderColor = Color(0xFF1E293B),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )

                        if (searchResults.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SurfaceCardElevated,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(4.dp)) {
                                    searchResults.take(4).forEach { res ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable {
                                                    onRelocatePin(
                                                        UserRole.USER_A,
                                                        res.latitude,
                                                        res.longitude,
                                                        res.title,
                                                        res.subtitle
                                                    )
                                                    isSearchExpanded = false
                                                    searchQuery = ""
                                                    searchResults = emptyList()
                                                    zoomScale = 1.0f
                                                    panOffset = Offset.Zero
                                                }
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(res.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                Text(res.subtitle, color = TextMuted, fontSize = 10.sp, maxLines = 1)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Telemetry Badges (Distance, Speed, Battery, Accuracy)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MetricBadge(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Navigation,
                        label = "Mesafe",
                        value = formattedDistance,
                        tint = ElectricCyan
                    )
                    MetricBadge(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Speed,
                        label = "Partner Hızı",
                        value = "${userBLocation.speedKmh.toInt()} km/s",
                        tint = ElectricViolet
                    )
                    MetricBadge(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.BatteryChargingFull,
                        label = "Partner Şarj",
                        value = "%${userBLocation.batteryPercent}",
                        tint = MintEmerald
                    )
                    MetricBadge(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.GpsFixed,
                        label = "Doğruluk",
                        value = "±${userALocation.accuracyMeters.toInt()}m",
                        tint = AmberWarning
                    )
                }

                // Partner Proximity Quick Selector Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Partner Konumunu Ayarla:",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(PartnerProximityMode.values()) { mode ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0F1E36),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        onSetPartnerProximity(mode)
                                        zoomScale = 1.0f
                                        panOffset = Offset.Zero
                                    }
                            ) {
                                Text(
                                    text = mode.title.split(" ").firstOrNull() ?: mode.title,
                                    color = CyanGlow,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live Pin Drop Mode banner if active
        if (isPinDropMode) {
            Surface(
                color = AmberWarning.copy(alpha = 0.2f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PushPin, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Haritaya dokunun: ${pinDropTargetRole.displayName} taşınacak",
                            color = AmberWarning,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Vazgeç",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { isPinDropMode = false }
                    )
                }
            }
        }

        // --- LIVE RADAR & ADAPTIVE GEODETIC COORDINATE MAP VIEW ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(0.2f, 10.0f)
                        panOffset += pan
                    }
                }
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(isPinDropMode, pinDropTargetRole) {
                        detectTapGestures { tapOffset ->
                            if (isPinDropMode) {
                                // Calculate tapped lat, lng from tapOffset
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                val center = Offset(canvasWidth / 2f + panOffset.x, canvasHeight / 2f + panOffset.y)

                                val minLat = minOf(userALocation.latitude, userBLocation.latitude)
                                val maxLat = maxOf(userALocation.latitude, userBLocation.latitude)
                                val minLng = minOf(userALocation.longitude, userBLocation.longitude)
                                val maxLng = maxOf(userALocation.longitude, userBLocation.longitude)
                                val centerLat = (userALocation.latitude + userBLocation.latitude) / 2.0
                                val centerLng = (userALocation.longitude + userBLocation.longitude) / 2.0

                                val deltaLatMeters = ((maxLat - minLat) * 110574.0).toFloat()
                                val deltaLngMeters = ((maxLng - minLng) * 111320.0 * cos(Math.toRadians(centerLat))).toFloat()
                                val spanMeters = maxOf(deltaLatMeters, deltaLngMeters, 250f)
                                val availableDim = minOf(canvasWidth, canvasHeight) * 0.65f
                                val baseMetersPerPx = (spanMeters / availableDim).coerceIn(0.2f, 10000f)
                                val metersPerPx = baseMetersPerPx / zoomScale

                                val xPx = tapOffset.x - center.x
                                val yPx = tapOffset.y - center.y
                                val xMeters = xPx * metersPerPx
                                val yMeters = -yPx * metersPerPx

                                val tappedLat = centerLat + (yMeters / 110574.0)
                                val tappedLng = centerLng + (xMeters / (111320.0 * cos(Math.toRadians(centerLat))))

                                onRelocatePin(
                                    pinDropTargetRole,
                                    tappedLat,
                                    tappedLng,
                                    "Seçilen Konum",
                                    "Haritada İşaretlendi (${String.format("%.4f", tappedLat)}, ${String.format("%.4f", tappedLng)})"
                                )
                                isPinDropMode = false
                            }
                        }
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val center = Offset(canvasWidth / 2f + panOffset.x, canvasHeight / 2f + panOffset.y)

                // 1. Draw modern city street grid
                val gridSize = 50.dp.toPx() * (zoomScale.coerceIn(0.5f, 2.0f))
                val startX = (center.x % gridSize)
                val startY = (center.y % gridSize)

                var x = startX
                while (x < canvasWidth) {
                    drawLine(
                        color = Color(0xFF1E293B).copy(alpha = 0.5f),
                        start = Offset(x, 0f),
                        end = Offset(x, canvasHeight),
                        strokeWidth = 1.dp.toPx()
                    )
                    x += gridSize
                }

                var y = startY
                while (y < canvasHeight) {
                    drawLine(
                        color = Color(0xFF1E293B).copy(alpha = 0.5f),
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1.dp.toPx()
                    )
                    y += gridSize
                }

                // 2. TRUE GEODETIC BOUNDING-BOX PROJECTION (Never clips, always auto-frames both pins!)
                val minLat = minOf(userALocation.latitude, userBLocation.latitude)
                val maxLat = maxOf(userALocation.latitude, userBLocation.latitude)
                val minLng = minOf(userALocation.longitude, userBLocation.longitude)
                val maxLng = maxOf(userALocation.longitude, userBLocation.longitude)
                val centerLat = (userALocation.latitude + userBLocation.latitude) / 2.0
                val centerLng = (userALocation.longitude + userBLocation.longitude) / 2.0

                val deltaLatMeters = ((maxLat - minLat) * 110574.0).toFloat()
                val deltaLngMeters = ((maxLng - minLng) * 111320.0 * cos(Math.toRadians(centerLat))).toFloat()
                val spanMeters = maxOf(deltaLatMeters, deltaLngMeters, 250f)
                val availableDim = minOf(canvasWidth, canvasHeight) * 0.65f
                val baseMetersPerPx = (spanMeters / availableDim).coerceIn(0.2f, 10000f)
                val metersPerPx = baseMetersPerPx / zoomScale

                fun getScreenOffset(lat: Double, lng: Double): Offset {
                    val xMeters = ((lng - centerLng) * 111320.0 * cos(Math.toRadians(centerLat))).toFloat()
                    val yMeters = -((lat - centerLat) * 110574.0).toFloat()
                    return Offset(
                        center.x + (xMeters / metersPerPx),
                        center.y + (yMeters / metersPerPx)
                    )
                }

                val userAPos = getScreenOffset(userALocation.latitude, userALocation.longitude)
                val userBPos = getScreenOffset(userBLocation.latitude, userBLocation.longitude)

                // 3. Draw Safe Zone / Geofence Circle around User A
                if (geofenceSettings.isEnabled) {
                    val radiusPx = (geofenceSettings.radiusMeters / metersPerPx)
                    drawCircle(
                        color = if (isGeofenceBreached) NeonCoral.copy(alpha = 0.15f) else MintEmerald.copy(alpha = 0.12f),
                        radius = radiusPx,
                        center = userAPos
                    )
                    drawCircle(
                        color = if (isGeofenceBreached) NeonCoral.copy(alpha = 0.6f) else MintEmerald.copy(alpha = 0.5f),
                        radius = radiusPx,
                        center = userAPos,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )
                    )
                }

                // 4. Draw Radar Concentric Rings around User A
                val maxRadius = (140.dp.toPx() * (zoomScale.coerceIn(0.7f, 1.6f)))
                listOf(0.33f, 0.66f, 1.0f).forEach { fraction ->
                    drawCircle(
                        color = ElectricCyan.copy(alpha = 0.14f),
                        radius = maxRadius * fraction,
                        center = userAPos,
                        style = Stroke(width = 1.2.dp.toPx())
                    )
                }

                // Rotating Radar Beam sweep from User A
                val rad = Math.toRadians(radarAngle.toDouble())
                val beamEnd = Offset(
                    (userAPos.x + maxRadius * cos(rad)).toFloat(),
                    (userAPos.y + maxRadius * sin(rad)).toFloat()
                )
                drawLine(
                    brush = Brush.radialGradient(
                        colors = listOf(ElectricCyan.copy(alpha = 0.5f), Color.Transparent),
                        center = userAPos,
                        radius = maxRadius
                    ),
                    start = userAPos,
                    end = beamEnd,
                    strokeWidth = 2.dp.toPx()
                )

                // 5. Draw Breadcrumb Trail for Partner
                if (partnerBreadcrumbs.size > 1) {
                    val trailPath = Path()
                    partnerBreadcrumbs.forEachIndexed { i, crumb ->
                        val pt = getScreenOffset(crumb.latitude, crumb.longitude)
                        if (i == 0) trailPath.moveTo(pt.x, pt.y) else trailPath.lineTo(pt.x, pt.y)
                        drawCircle(
                            color = ElectricViolet.copy(alpha = 0.4f),
                            radius = 3.dp.toPx(),
                            center = pt
                        )
                    }
                    drawPath(
                        path = trailPath,
                        color = ElectricViolet.copy(alpha = 0.5f),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                        )
                    )
                }

                // 6. Direct Trajectory Line between User A & User B
                drawLine(
                    color = CyanGlow.copy(alpha = 0.85f),
                    start = userAPos,
                    end = userBPos,
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f), 0f)
                )

                // 7. User A Pin ("Sen - Cihaz 1")
                drawCircle(
                    color = ElectricCyan.copy(alpha = 0.25f),
                    radius = (24.dp.toPx() * pulseRatio),
                    center = userAPos
                )
                drawCircle(
                    color = ElectricCyan,
                    radius = 9.dp.toPx(),
                    center = userAPos
                )
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = userAPos
                )

                // 8. User B Pin ("Partner - Cihaz 2")
                drawCircle(
                    color = ElectricViolet.copy(alpha = 0.35f),
                    radius = (28.dp.toPx() * pulseRatio),
                    center = userBPos
                )
                drawCircle(
                    color = ElectricViolet,
                    radius = 11.dp.toPx(),
                    center = userBPos
                )
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = userBPos
                )

                // 9. Distance Scale Bar on bottom corner
                val scaleBarPx = 80.dp.toPx()
                val scaleMeters = (scaleBarPx * metersPerPx).toInt()
                val scaleText = if (scaleMeters < 1000) "$scaleMeters m" else "${(scaleMeters / 1000.0).toString().take(4)} km"
                drawLine(
                    color = Color.White.copy(alpha = 0.7f),
                    start = Offset(20f, canvasHeight - 20f),
                    end = Offset(20f + scaleBarPx, canvasHeight - 20f),
                    strokeWidth = 3f
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.7f),
                    start = Offset(20f, canvasHeight - 26f),
                    end = Offset(20f, canvasHeight - 14f),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.7f),
                    start = Offset(20f + scaleBarPx, canvasHeight - 26f),
                    end = Offset(20f + scaleBarPx, canvasHeight - 14f),
                    strokeWidth = 2f
                )
            }

            // Legend & Dynamic Sensor Compass in Top-Left
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SurfaceCard.copy(alpha = 0.92f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sen", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(ElectricViolet)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Partner", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.width(10.dp))

                    // Compass Bearing Arrow with hardware azimuth heading integration
                    val relativeBearing = (bearingDegrees - azimuthDegrees + 360f) % 360f
                    Icon(
                        imageVector = Icons.Default.NearMe,
                        contentDescription = "Pusula",
                        tint = CyanGlow,
                        modifier = Modifier
                            .size(15.dp)
                            .rotate(relativeBearing)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${bearingDegrees.toInt()}° (${formattedDistance})",
                        color = CyanGlow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Map Quick Action Floating Buttons (Zoom In/Out, Recenter, Focus, Geofence, Google Maps, SOS)
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Zoom In
                IconButton(
                    onClick = { zoomScale = (zoomScale * 1.35f).coerceAtMost(10.0f) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceCardElevated)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Yakınlaştır", tint = Color.White)
                }

                // Zoom Out
                IconButton(
                    onClick = { zoomScale = (zoomScale / 1.35f).coerceAtLeast(0.2f) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceCardElevated)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Uzaklaştır", tint = Color.White)
                }

                // Auto-Fit / Recenter
                IconButton(
                    onClick = {
                        zoomScale = 1.0f
                        panOffset = Offset.Zero
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceCardElevated)
                        .testTag("recenter_map_button")
                ) {
                    Icon(Icons.Default.CenterFocusStrong, contentDescription = "Ortala", tint = ElectricCyan)
                }

                // Pin Placement Mode Toggle
                IconButton(
                    onClick = {
                        isPinDropMode = !isPinDropMode
                        pinDropTargetRole = if (currentRole == UserRole.USER_A) UserRole.USER_A else UserRole.USER_B
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isPinDropMode) AmberWarning else SurfaceCardElevated)
                ) {
                    Icon(
                        Icons.Default.PushPin,
                        contentDescription = "Pin Bırak",
                        tint = if (isPinDropMode) Color.Black else Color.White
                    )
                }

                // Geofence Settings
                IconButton(
                    onClick = { showGeofenceDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (geofenceSettings.isEnabled) MintEmerald.copy(alpha = 0.3f) else SurfaceCardElevated)
                        .testTag("geofence_button")
                ) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = "Güvenli Alan",
                        tint = if (geofenceSettings.isEnabled) MintEmerald else Color.White
                    )
                }

                // Launch External Google Maps App with Directions
                IconButton(
                    onClick = {
                        try {
                            val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=${userALocation.latitude},${userALocation.longitude}&destination=${userBLocation.latitude},${userBLocation.longitude}")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceCardElevated)
                ) {
                    Icon(Icons.Default.Map, contentDescription = "Google Haritalar", tint = CyanGlow)
                }

                // SOS Emergency Button
                IconButton(
                    onClick = onSendSosBeacon,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(NeonCoral)
                        .testTag("sos_button")
                ) {
                    Text(
                        text = "SOS",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Bottom Action Bar: Navigation Steps & Chat Location Share
        Surface(
            color = DeepSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Next Navigation Instruction Bar
                if (navigationSteps.isNotEmpty()) {
                    val nextStep = navigationSteps.first()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F1E36))
                            .clickable { showNavigationDrawer = !showNavigationDrawer }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Canlı Rota: ${nextStep.instruction}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary,
                                maxLines = 1
                            )
                            Text(
                                text = "Partner konumu: ${userBLocation.placeName} (${userBLocation.streetAddress})",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Directions,
                            contentDescription = "Yol Tarifi",
                            tint = CyanGlow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Action Buttons Row (Yol Tarifi & Sohbette Paylaş)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showNavigationDrawer = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("open_directions_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Yol Tarifi", color = ElectricCyan, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onSendLocationToChat,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_location_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShareLocation,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sohbete Gönder", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // --- CITY PICKER DIALOG ---
    if (showCityPicker) {
        var pickerSearchQuery by remember { mutableStateOf("") }
        var manualLatText by remember { mutableStateOf(userALocation.latitude.toString()) }
        var manualLngText by remember { mutableStateOf(userALocation.longitude.toString()) }
        var showManualInputs by remember { mutableStateOf(false) }

        val filteredCities = remember(pickerSearchQuery) {
            if (pickerSearchQuery.isBlank()) SyncMateRepository.POPULAR_CITIES
            else SyncMateRepository.POPULAR_CITIES.filter {
                it.cityName.contains(pickerSearchQuery, ignoreCase = true) ||
                it.districtName.contains(pickerSearchQuery, ignoreCase = true)
            }
        }

        AlertDialog(
            onDismissRequest = { showCityPicker = false },
            title = {
                Text(
                    text = "Konum & Şehir Seçici",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Doğru konumu seçerek haritayı ve partner mesafesini anında senkronize edebilirsiniz:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = pickerSearchQuery,
                        onValueChange = { pickerSearchQuery = it },
                        placeholder = { Text("Şehir veya ilçe ara...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (showManualInputs) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = manualLatText,
                                onValueChange = { manualLatText = it },
                                label = { Text("Enlem (Lat)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = manualLngText,
                                onValueChange = { manualLngText = it },
                                label = { Text("Boylam (Lng)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                        Button(
                            onClick = {
                                val lat = manualLatText.toDoubleOrNull() ?: userALocation.latitude
                                val lng = manualLngText.toDoubleOrNull() ?: userALocation.longitude
                                onSetManualCoordinates(lat, lng, "Özel Koordinat")
                                showCityPicker = false
                                zoomScale = 1.0f
                                panOffset = Offset.Zero
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanGlow)
                        ) {
                            Text("Koordinatları Uygula", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text(
                            text = "Manuel Koordinat Gir (Lat/Lng)",
                            color = CyanGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { showManualInputs = true }
                                .padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(modifier = Modifier.height(220.dp)) {
                        items(filteredCities) { city ->
                            Card(
                                onClick = {
                                    onSelectCityPreset(city)
                                    showCityPicker = false
                                    zoomScale = 1.0f
                                    panOffset = Offset.Zero
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceCardElevated),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = city.cityName,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = city.districtName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCityPicker = false }) {
                    Text("Kapat", color = ElectricCyan)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onStartGps(context)
                        showCityPicker = false
                    }
                ) {
                    Text("Gerçek GPS'i Al", color = MintEmerald)
                }
            },
            containerColor = SurfaceCard
        )
    }

    // --- GEOFENCE SETTINGS DIALOG ---
    if (showGeofenceDialog) {
        var radiusValue by remember { mutableFloatStateOf(geofenceSettings.radiusMeters) }
        var isEnabledValue by remember { mutableStateOf(geofenceSettings.isEnabled) }

        AlertDialog(
            onDismissRequest = { showGeofenceDialog = false },
            title = {
                Text(
                    text = "Güvenli Alan (Geofence) Ayarları",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Güvenli Alan Takibi",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        Switch(
                            checked = isEnabledValue,
                            onCheckedChange = { isEnabledValue = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = MintEmerald)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Uyarı Yarıçapı: ${radiusValue.toInt()} metre",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = ElectricCyan
                    )

                    Slider(
                        value = radiusValue,
                        onValueChange = { radiusValue = it },
                        valueRange = 100f..2500f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricCyan,
                            activeTrackColor = ElectricCyan
                        )
                    )

                    Text(
                        text = "Partner bu çemberin dışına çıktığında otomatik titreşim ve acil uyarı bildirimi verilir.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSetGeofenceRadius(radiusValue)
                        onToggleGeofence(isEnabledValue)
                        showGeofenceDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                ) {
                    Text("Kaydet", color = Color(0xFF090D16), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGeofenceDialog = false }) {
                    Text("İptal", color = TextMuted)
                }
            },
            containerColor = SurfaceCard
        )
    }

    // --- NAVIGATION STEPS DIALOG ---
    if (showNavigationDrawer) {
        AlertDialog(
            onDismissRequest = { showNavigationDrawer = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Directions,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Partnerine Canlı Rota",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }
            },
            text = {
                Column(modifier = Modifier.height(260.dp)) {
                    Text(
                        text = "Toplam Mesafe: $formattedDistance | Tahmini Süre: ~${((userBLocation.speedKmh.takeIf { it > 0 } ?: 4f) / 60 * 12).toInt().coerceAtLeast(3)} dk",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MintEmerald
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn {
                        items(navigationSteps) { step ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceCardElevated),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                                        contentDescription = null,
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = step.instruction,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = TextPrimary
                                    )
                                    if (step.distanceMeters > 0) {
                                        Text(
                                            text = "${step.distanceMeters} metre",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showNavigationDrawer = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                ) {
                    Text("Tamam", color = Color(0xFF090D16), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SurfaceCard
        )
    }
}

@Composable
private fun MetricBadge(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    tint: Color
) {
    Surface(
        modifier = modifier,
        color = Color(0xFF0C1424),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 9.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                fontSize = 12.sp
            )
        }
    }
}
