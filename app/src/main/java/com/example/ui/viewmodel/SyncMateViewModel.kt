package com.example.ui.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.ChatMessage
import com.example.model.DrawToolType
import com.example.model.DrawingStroke
import com.example.model.DualStreamViewMode
import com.example.model.GeofenceSettings
import com.example.model.LiveLocation
import com.example.model.LocationBreadcrumb
import com.example.model.LocationSearchResult
import com.example.model.NavigationStep
import com.example.model.NetworkSyncStats
import com.example.model.PartnerProximityMode
import com.example.model.RealDeviceTelemetryState
import com.example.model.ScreenActivityState
import com.example.model.SessionState
import com.example.model.UserRole
import com.example.model.VideoCategory
import com.example.model.VideoStreamFeed
import com.example.model.WalkieTalkieState
import com.example.state.CityPreset
import com.example.state.SyncMateRepository
import com.example.telemetry.RealDeviceUsageManager
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

data class TelestratorToolState(
    val selectedColor: Long = 0xFF06B6D4, // Electric Cyan
    val strokeWidth: Float = 10f,
    val toolType: DrawToolType = DrawToolType.PEN,
    val isLaserMode: Boolean = false,
    val isDrawOverScreen: Boolean = true,
    val targetScreen: UserRole = UserRole.USER_B
)

typealias DrawingToolState = TelestratorToolState

class SyncMateViewModel(
    private val repo: SyncMateRepository = SyncMateRepository
) : ViewModel() {

    val currentRole: StateFlow<UserRole> = repo.currentRole
    val sessionState: StateFlow<SessionState> = repo.sessionState
    val messages: StateFlow<List<ChatMessage>> = repo.messages

    val userALocation: StateFlow<LiveLocation> = repo.userALocation
    val userBLocation: StateFlow<LiveLocation> = repo.userBLocation
    val partnerBreadcrumbs: StateFlow<List<LocationBreadcrumb>> = repo.partnerBreadcrumbs
    val geofenceSettings: StateFlow<GeofenceSettings> = repo.geofenceSettings
    val isGeofenceBreached: StateFlow<Boolean> = repo.isGeofenceBreached
    val navigationSteps: StateFlow<List<NavigationStep>> = repo.navigationSteps
    val azimuthDegrees: StateFlow<Float> = repo.azimuthDegrees
    val walkieTalkieState: StateFlow<WalkieTalkieState> = repo.walkieTalkieState

    val screenActivity: StateFlow<ScreenActivityState> = repo.screenActivity
    val userAStream: StateFlow<VideoStreamFeed> = repo.userAStream
    val userBStream: StateFlow<VideoStreamFeed> = repo.userBStream
    val dualViewMode: StateFlow<DualStreamViewMode> = repo.dualViewMode
    val networkSyncStats: StateFlow<NetworkSyncStats> = repo.networkSyncStats
    val recentSyncLogs: StateFlow<List<String>> = repo.recentSyncLogs

    // Real Device Usage & System Telemetry (NO FAKE DATA)
    val realTelemetry: StateFlow<RealDeviceTelemetryState> = repo.realTelemetry

    fun refreshRealDeviceData(context: Context) {
        repo.refreshRealDeviceData(context)
    }

    fun startRealDevicePolling(context: Context) {
        repo.startRealDevicePolling(context)
    }

    fun hasUsagePermission(context: Context): Boolean {
        return RealDeviceUsageManager.hasUsagePermission(context)
    }

    fun getUsageAccessSettingsIntent(): android.content.Intent {
        return RealDeviceUsageManager.createUsageAccessSettingsIntent()
    }

    val drawingStrokes: StateFlow<List<DrawingStroke>> = repo.drawingStrokes
    val activeStroke: StateFlow<DrawingStroke?> = repo.activeStroke
    val laserPointerOnScreenA: StateFlow<Pair<Float, Float>?> = repo.laserPointerOnScreenA
    val laserPointerOnScreenB: StateFlow<Pair<Float, Float>?> = repo.laserPointerOnScreenB
    val laserPointer: StateFlow<Pair<Float, Float>?> = repo.laserPointerOnScreenB

    private val _toolState = MutableStateFlow(TelestratorToolState())
    val toolState: StateFlow<TelestratorToolState> = _toolState.asStateFlow()

    private val _showE2eeDialog = MutableStateFlow(false)
    val showE2eeDialog: StateFlow<Boolean> = _showE2eeDialog.asStateFlow()

    private val _inspectedMessage = MutableStateFlow<ChatMessage?>(null)
    val inspectedMessage: StateFlow<ChatMessage?> = _inspectedMessage.asStateFlow()

    fun switchRole(role: UserRole) {
        repo.switchRole(role)
        val newTarget = if (role == UserRole.USER_A) UserRole.USER_B else UserRole.USER_A
        _toolState.value = _toolState.value.copy(targetScreen = newTarget)
    }

    fun toggleRole() {
        switchRole(if (currentRole.value == UserRole.USER_A) UserRole.USER_B else UserRole.USER_A)
    }

    fun toggleE2eeDialog(show: Boolean) {
        _showE2eeDialog.value = show
    }

    fun inspectMessageCrypto(msg: ChatMessage?) {
        _inspectedMessage.value = msg
    }

    fun setDualViewMode(mode: DualStreamViewMode) {
        repo.setDualViewMode(mode)
    }

    fun sendMessage(text: String, isDisappearing: Boolean = false) {
        repo.sendMessage(text, isDisappearing)
    }

    fun sendLocationMessage() {
        repo.sendLocationMessage()
    }

    fun sendVoiceNoteMessage(durationSec: Int) {
        repo.sendVoiceNoteMessage(durationSec)
    }

    fun sendDrawingCardMessage(note: String = "Ekran Üzerine Çizim") {
        repo.sendDrawingCardMessage(note)
    }

    fun sendSosBeacon() {
        repo.sendSosBeacon()
    }

    fun addReaction(messageId: String, emoji: String) {
        repo.addReaction(messageId, emoji)
    }

    fun setDrawingColor(color: Long) {
        _toolState.value = _toolState.value.copy(
            selectedColor = color,
            toolType = if (_toolState.value.toolType == DrawToolType.LASER) DrawToolType.PEN else _toolState.value.toolType,
            isLaserMode = false
        )
    }

    fun setStrokeWidth(width: Float) {
        _toolState.value = _toolState.value.copy(strokeWidth = width)
    }

    fun setDrawToolType(type: DrawToolType) {
        _toolState.value = _toolState.value.copy(
            toolType = type,
            isLaserMode = (type == DrawToolType.LASER)
        )
    }

    fun toggleLaserMode() {
        val willBeLaser = !_toolState.value.isLaserMode
        _toolState.value = _toolState.value.copy(
            isLaserMode = willBeLaser,
            toolType = if (willBeLaser) DrawToolType.LASER else DrawToolType.PEN
        )
    }

    fun toggleDrawOverScreen(overScreen: Boolean) {
        _toolState.value = _toolState.value.copy(isDrawOverScreen = overScreen)
    }

    fun setTargetScreen(target: UserRole) {
        _toolState.value = _toolState.value.copy(targetScreen = target)
    }

    fun startCrossDrawing(targetScreen: UserRole, xRatio: Float, yRatio: Float) {
        val tool = _toolState.value
        repo.startCrossScreenStroke(
            targetScreen = targetScreen,
            xRatio = xRatio,
            yRatio = yRatio,
            colorArgb = if (tool.toolType == DrawToolType.ERASER) 0x00000000 else tool.selectedColor,
            strokeWidth = if (tool.toolType == DrawToolType.HIGHLIGHTER) tool.strokeWidth * 2.2f else tool.strokeWidth,
            toolType = tool.toolType
        )
    }

    fun continueCrossDrawing(xRatio: Float, yRatio: Float) {
        repo.addPointToStroke(xRatio, yRatio)
    }

    fun endCrossDrawing() {
        repo.finishStroke()
    }

    // Backward-compatible drawing calls
    fun startDrawing(x: Float, y: Float) {
        val target = _toolState.value.targetScreen
        startCrossDrawing(target, x, y)
    }

    fun continueDrawing(x: Float, y: Float) {
        continueCrossDrawing(x, y)
    }

    fun endDrawing() {
        endCrossDrawing()
    }

    fun clearDrawings(targetScreen: UserRole? = null) {
        repo.clearDrawings(targetScreen)
    }

    fun undoDrawing(targetScreen: UserRole? = null) {
        repo.undoLastDrawing(targetScreen)
    }

    fun toggleVideoPlayPause(owner: UserRole) {
        repo.togglePlayPauseVideo(owner)
    }

    fun seekVideo(owner: UserRole, seconds: Float) {
        repo.seekVideo(owner, seconds)
    }

    fun changeVideoCategory(owner: UserRole, category: VideoCategory) {
        repo.changeVideoCategory(owner, category)
    }

    fun setCustomMediaUri(owner: UserRole, uriString: String) {
        repo.setCustomMediaUri(owner, uriString)
    }

    fun toggleScreenSharing(owner: UserRole = currentRole.value) {
        repo.toggleScreenSharing(owner)
    }

    fun togglePrivacyShield(owner: UserRole = currentRole.value) {
        repo.togglePrivacyShield(owner)
    }

    fun selectCityPreset(preset: CityPreset) {
        repo.selectCityPreset(preset)
    }

    fun setManualCoordinates(lat: Double, lng: Double, name: String) {
        repo.setManualCoordinates(lat, lng, name)
    }

    fun setGeofenceRadius(radiusMeters: Float) {
        repo.setGeofenceRadius(radiusMeters)
    }

    fun toggleGeofence(enabled: Boolean) {
        repo.toggleGeofence(enabled)
    }

    fun getFormattedDistance(): String {
        val meters = repo.calculateDistanceMeters()
        return if (meters < 1000) {
            "${meters.toInt()} m"
        } else {
            String.format(Locale.getDefault(), "%.1f km", meters / 1000)
        }
    }

    fun getBearingDegrees(): Float {
        return repo.calculateBearing()
    }

    fun setPartnerProximity(mode: PartnerProximityMode) {
        repo.setPartnerProximity(mode)
    }

    fun relocatePin(role: UserRole, lat: Double, lng: Double, place: String, address: String) {
        repo.relocatePin(role, lat, lng, place, address)
    }

    private var sensorManager: SensorManager? = null
    private var rotationSensor: Sensor? = null
    private var sensorListener: SensorEventListener? = null

    fun startCompassSensor(context: Context) {
        if (sensorManager != null) return
        try {
            sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
                ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ORIENTATION)

            sensorListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                        val rotationMatrix = FloatArray(9)
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(rotationMatrix, orientation)
                        val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                        repo.updateAzimuth((azimuth + 360f) % 360f)
                    } else if (event.sensor.type == Sensor.TYPE_ORIENTATION) {
                        repo.updateAzimuth((event.values[0] + 360f) % 360f)
                    }
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }
            rotationSensor?.let {
                sensorManager?.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_UI)
            }
        } catch (_: Exception) {}
    }

    fun searchLocation(context: Context, query: String, onResult: (List<LocationSearchResult>) -> Unit) {
        if (query.isBlank()) {
            onResult(emptyList())
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val results = mutableListOf<LocationSearchResult>()
            try {
                val geocoder = Geocoder(context, Locale("tr", "TR"))
                val addresses = geocoder.getFromLocationName(query, 6)
                if (!addresses.isNullOrEmpty()) {
                    for (addr in addresses) {
                        val title = addr.featureName ?: addr.subLocality ?: addr.locality ?: query
                        val parts = listOfNotNull(
                            addr.thoroughfare,
                            addr.subLocality,
                            addr.locality,
                            addr.adminArea,
                            addr.countryName
                        ).distinct()
                        val subtitle = if (parts.isNotEmpty()) parts.joinToString(", ") else "Koordinat: ${addr.latitude}, ${addr.longitude}"
                        results.add(
                            LocationSearchResult(
                                title = title,
                                subtitle = subtitle,
                                latitude = addr.latitude,
                                longitude = addr.longitude
                            )
                        )
                    }
                }
            } catch (_: Exception) {}

            withContext(Dispatchers.Main) {
                onResult(results)
            }
        }
    }

    private var toneGenerator: ToneGenerator? = null

    fun playWalkieTalkieChirp(isStart: Boolean) {
        try {
            if (toneGenerator == null) {
                toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
            }
            if (isStart) {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
            } else {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 120)
            }
        } catch (_: Exception) {}
    }

    fun setWalkieTalkieTransmitting(isTransmitting: Boolean) {
        repo.setWalkieTalkieTransmitting(isTransmitting)
        playWalkieTalkieChirp(isTransmitting)
    }

    fun playSosEmergencySound() {
        try {
            if (toneGenerator == null) {
                toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            }
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 1500)
        } catch (_: Exception) {}
    }

    @SuppressLint("MissingPermission")
    fun startGpsUpdates(context: Context) {
        startCompassSensor(context)
        viewModelScope.launch {
            try {
                val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                fusedClient.lastLocation.addOnSuccessListener { loc: Location? ->
                    loc?.let { reverseGeocodeAndUpdate(context, it) }
                }
                try {
                    fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { loc: Location? ->
                            loc?.let { reverseGeocodeAndUpdate(context, it) }
                        }
                } catch (_: Exception) {}

                val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
                    .setMinUpdateIntervalMillis(1000L)
                    .setMinUpdateDistanceMeters(0.5f)
                    .build()

                val callback = object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        result.lastLocation?.let { loc ->
                            reverseGeocodeAndUpdate(context, loc)
                        }
                    }
                }
                fusedClient.requestLocationUpdates(req, callback, Looper.getMainLooper())
            } catch (_: Exception) {
                startLegacyLocationManager(context)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLegacyLocationManager(context: Context) {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (locationManager != null) {
                val providers = listOf(
                    LocationManager.GPS_PROVIDER,
                    LocationManager.NETWORK_PROVIDER,
                    LocationManager.PASSIVE_PROVIDER
                )
                var bestLocation: Location? = null
                for (provider in providers) {
                    try {
                        val loc = locationManager.getLastKnownLocation(provider)
                        if (loc != null) {
                            if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                                bestLocation = loc
                            }
                        }
                    } catch (_: Exception) {}
                }

                bestLocation?.let { loc ->
                    reverseGeocodeAndUpdate(context, loc)
                }

                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        reverseGeocodeAndUpdate(context, location)
                    }
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }

                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 1f, listener)
                }
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000L, 1f, listener)
                }
            }
        } catch (_: Exception) {}
    }

    private fun reverseGeocodeAndUpdate(context: Context, location: Location) {
        viewModelScope.launch(Dispatchers.IO) {
            var placeName = "Anlık GPS Konumu"
            var streetAddress = "Hassas GPS (±${location.accuracy.toInt()}m)"
            try {
                val geocoder = Geocoder(context, Locale("tr", "TR"))
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val district = addr.subLocality ?: addr.subAdminArea ?: ""
                    val city = addr.locality ?: addr.adminArea ?: ""
                    val thoroughfare = addr.thoroughfare ?: addr.featureName ?: ""
                    placeName = if (city.isNotEmpty() && district.isNotEmpty()) "$city, $district" else (city.ifEmpty { district.ifEmpty { "Anlık GPS" } })
                    streetAddress = if (thoroughfare.isNotEmpty()) "$thoroughfare (Doğrulandı)" else "Cadde Doğrulandı"
                }
            } catch (_: Exception) {}

            withContext(Dispatchers.Main) {
                repo.updateMyGpsLocation(
                    lat = location.latitude,
                    lng = location.longitude,
                    accuracy = location.accuracy,
                    speed = if (location.hasSpeed()) location.speed * 3.6f else 0f,
                    placeName = placeName,
                    address = streetAddress
                )
            }
        }
    }
}
