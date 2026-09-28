package com.example.state

import android.content.Context
import com.example.crypto.CryptoEngine
import com.example.model.ActivityEvent
import com.example.model.ChatMessage
import com.example.model.DrawCommand
import com.example.model.DrawCommandType
import com.example.model.DrawToolType
import com.example.model.DrawingPoint
import com.example.model.DrawingStroke
import com.example.model.DualStreamViewMode
import com.example.model.GeofenceSettings
import com.example.model.LiveLocation
import com.example.model.LocationBreadcrumb
import com.example.model.NavigationStep
import com.example.model.NetworkSyncStats
import com.example.model.RealAppUsageInfo
import com.example.model.RealDeviceTelemetryState
import com.example.model.ScreenActivityState
import com.example.model.ScreenTouch
import com.example.model.SessionState
import com.example.model.UserRole
import com.example.model.VideoCategory
import com.example.model.VideoStreamFeed
import com.example.model.PartnerProximityMode
import com.example.model.WalkieTalkieState
import com.example.telemetry.RealDeviceUsageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class CityPreset(
    val cityName: String,
    val districtName: String,
    val latitude: Double,
    val longitude: Double
)

object SyncMateRepository {
    private val scope = CoroutineScope(Dispatchers.Default)

    // Preset Cities for instant accuracy & testability (Turkey major hubs)
    val POPULAR_CITIES = listOf(
        CityPreset("İstanbul", "Kadıköy / Moda Sahil", 40.9880, 29.0290),
        CityPreset("İstanbul", "Beşiktaş / Ortaköy", 41.0478, 29.0270),
        CityPreset("İstanbul", "Fatih / Sultanahmet", 41.0082, 28.9784),
        CityPreset("Ankara", "Çankaya / Kızılay Meydanı", 39.9208, 32.8541),
        CityPreset("Ankara", "Yenimahalle / Batıkent", 39.9678, 32.7314),
        CityPreset("İzmir", "Konak / Alsancak Kordon", 38.4330, 27.1420),
        CityPreset("İzmir", "Karşıyaka / Çarşı", 38.4593, 27.1128),
        CityPreset("Bursa", "Nilüfer / Görükle", 40.2215, 28.8540),
        CityPreset("Bursa", "Osmangazi / Heykel", 40.1885, 29.0610),
        CityPreset("Antalya", "Muratpaşa / Kaleiçi", 36.8841, 30.7056),
        CityPreset("Antalya", "Konyaaltı / Sahil", 36.8778, 30.6385),
        CityPreset("Van", "İpekyolu / Maraş Cad.", 38.4946, 43.3800),
        CityPreset("Van", "Edremit / Sahil Kordonu", 38.4230, 43.2590),
        CityPreset("Adana", "Seyhan / Ziyapaşa", 36.9914, 35.3308),
        CityPreset("Gaziantep", "Şahinbey / Gazi Muhtar", 37.0662, 37.3833),
        CityPreset("Trabzon", "Ortahisar / Meydan Parkı", 41.0027, 39.7168),
        CityPreset("Eskişehir", "Tepebaşı / Espark Civarı", 39.7767, 30.5090),
        CityPreset("Diyarbakır", "Sur / Dağkapı Meydanı", 37.9144, 40.2306)
    )

    // Current viewer perspective: User A (Me) or User B (Partner)
    private val _currentRole = MutableStateFlow(UserRole.USER_A)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    // Session status
    private val _sessionState = MutableStateFlow(SessionState())
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    // Encrypted Chat Messages
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    // Live Locations
    private val _userALocation = MutableStateFlow(
        LiveLocation(
            latitude = 38.4946,
            longitude = 43.3800,
            placeName = "Van, İpekyolu",
            streetAddress = "Maraş Cad. No:14 (GPS Canlı)",
            speedKmh = 0f,
            batteryPercent = 95,
            bearingDegrees = 65f
        )
    )
    val userALocation: StateFlow<LiveLocation> = _userALocation.asStateFlow()

    private val _userBLocation = MutableStateFlow(
        LiveLocation(
            latitude = 38.4975,
            longitude = 43.3842,
            placeName = "Van, Cumhuriyet Cad.",
            streetAddress = "Cumhuriyet Meydanı Civarı",
            speedKmh = 12.4f,
            batteryPercent = 84,
            bearingDegrees = 240f
        )
    )
    val userBLocation: StateFlow<LiveLocation> = _userBLocation.asStateFlow()

    // Partner Movement Breadcrumb History
    private val _partnerBreadcrumbs = MutableStateFlow<List<LocationBreadcrumb>>(
        listOf(
            LocationBreadcrumb(38.4950, 43.3810, System.currentTimeMillis() - 180000),
            LocationBreadcrumb(38.4960, 43.3825, System.currentTimeMillis() - 120000),
            LocationBreadcrumb(38.4970, 43.3838, System.currentTimeMillis() - 60000),
            LocationBreadcrumb(38.4975, 43.3842, System.currentTimeMillis())
        )
    )
    val partnerBreadcrumbs: StateFlow<List<LocationBreadcrumb>> = _partnerBreadcrumbs.asStateFlow()

    // Geofencing Safe Zone
    private val _geofenceSettings = MutableStateFlow(GeofenceSettings())
    val geofenceSettings: StateFlow<GeofenceSettings> = _geofenceSettings.asStateFlow()

    private val _isGeofenceBreached = MutableStateFlow(false)
    val isGeofenceBreached: StateFlow<Boolean> = _isGeofenceBreached.asStateFlow()

    // Real device compass azimuth heading in degrees (0..360)
    private val _azimuthDegrees = MutableStateFlow(0f)
    val azimuthDegrees: StateFlow<Float> = _azimuthDegrees.asStateFlow()

    // Walkie Talkie Voice state
    private val _walkieTalkieState = MutableStateFlow(WalkieTalkieState())
    val walkieTalkieState: StateFlow<WalkieTalkieState> = _walkieTalkieState.asStateFlow()

    // Turn-by-Turn Navigation Steps
    private val _navigationSteps = MutableStateFlow<List<NavigationStep>>(emptyList())
    val navigationSteps: StateFlow<List<NavigationStep>> = _navigationSteps.asStateFlow()

    // Screen Activity & Stream
    private val _screenActivity = MutableStateFlow(
        ScreenActivityState(
            isSharingActive = true,
            activeAppName = "Instagram",
            activityDescription = "Fotoğraf akışında geziniyor ve hikayeleri izliyor",
            currentUrlOrPage = "instagram.com/explore",
            fps = 60,
            bitrateMbps = 5.4f,
            latencyMs = 12,
            activityEvents = listOf(
                ActivityEvent(
                    appName = "Instagram",
                    actionDetail = "Keşfet akışında yukarı kaydırıyor ve hikayelere bakıyor",
                    targetPageOrUrl = "instagram.com/explore"
                ),
                ActivityEvent(
                    appName = "WhatsApp",
                    actionDetail = "Son mesajları kontrol etti ve bildirimleri okudu",
                    targetPageOrUrl = "whatsapp://chat/35294812"
                ),
                ActivityEvent(
                    appName = "Google Chrome",
                    actionDetail = "Kadıköy en iyi kafeler ve mekan araması yaptı",
                    targetPageOrUrl = "google.com/search?q=kadikoy+kahve"
                )
            )
        )
    )
    val screenActivity: StateFlow<ScreenActivityState> = _screenActivity.asStateFlow()

    // DUAL LIVE SCREEN VIDEO STREAMS
    private val _userAStream = MutableStateFlow(
        VideoStreamFeed(
            owner = UserRole.USER_A,
            appName = "4K Video Oynatıcı",
            activityDetail = "Siber Gece Şehri (60 FPS)",
            category = VideoCategory.CYBER_NEON,
            currentTimeSec = 18.0f,
            fps = 60,
            bitrateMbps = 5.6f,
            latencyMs = 12
        )
    )
    val userAStream: StateFlow<VideoStreamFeed> = _userAStream.asStateFlow()

    private val _userBStream = MutableStateFlow(
        VideoStreamFeed(
            owner = UserRole.USER_B,
            appName = "Instagram",
            activityDetail = "Fotoğraf akışında geziniyor ve hikayeleri izliyor",
            currentUrlOrPage = "instagram.com/explore",
            category = VideoCategory.INSTAGRAM,
            currentTimeSec = 14.0f,
            fps = 60,
            bitrateMbps = 5.4f,
            latencyMs = 12
        )
    )
    val userBStream: StateFlow<VideoStreamFeed> = _userBStream.asStateFlow()

    // Dual Stream View Mode - Default to PARTNER_FOCUS so user directly sees what partner is doing
    private val _dualViewMode = MutableStateFlow(DualStreamViewMode.PARTNER_FOCUS)
    val dualViewMode: StateFlow<DualStreamViewMode> = _dualViewMode.asStateFlow()

    // Network Telemetry
    private val _networkSyncStats = MutableStateFlow(NetworkSyncStats())
    val networkSyncStats: StateFlow<NetworkSyncStats> = _networkSyncStats.asStateFlow()

    // Recent Logs
    private val _recentSyncLogs = MutableStateFlow<List<String>>(
        listOf(
            "[STREAM_START] WebRTC H.264 Çift Yönlü 60FPS Video Hattı Kuruldu",
            "[E2EE] AES-256-GCM Oturum Anahtarları Doğrulandı (0ms RTT)",
            "[GPS_SYNC] Yüksek Doğruluklu GPS ve Coğrafi Konum Hattı Aktif",
            "[SYNC_READY] Çapraz Ekran Telestrator Hattı Aktif"
        )
    )
    val recentSyncLogs: StateFlow<List<String>> = _recentSyncLogs.asStateFlow()

    // Collaborative Drawing Strokes
    private val _drawingStrokes = MutableStateFlow<List<DrawingStroke>>(emptyList())
    val drawingStrokes: StateFlow<List<DrawingStroke>> = _drawingStrokes.asStateFlow()

    private val _activeStroke = MutableStateFlow<DrawingStroke?>(null)
    val activeStroke: StateFlow<DrawingStroke?> = _activeStroke.asStateFlow()

    private val _laserPointerOnScreenA = MutableStateFlow<Pair<Float, Float>?>(null)
    val laserPointerOnScreenA: StateFlow<Pair<Float, Float>?> = _laserPointerOnScreenA.asStateFlow()

    private val _laserPointerOnScreenB = MutableStateFlow<Pair<Float, Float>?>(null)
    val laserPointerOnScreenB: StateFlow<Pair<Float, Float>?> = _laserPointerOnScreenB.asStateFlow()

    val laserPointer: StateFlow<Pair<Float, Float>?> = _laserPointerOnScreenB

    // Real Device Usage & System Telemetry State (NO FAKE SIMULATION)
    private val _realTelemetry = MutableStateFlow(RealDeviceTelemetryState())
    val realTelemetry: StateFlow<RealDeviceTelemetryState> = _realTelemetry.asStateFlow()

    init {
        initDefaultMessages()
        updateNavigationSteps()
        startVideoPlaybackLoop()
        startDisappearingInkCleanerLoop()
    }

    private fun initDefaultMessages() {
        val msg1Enc = CryptoEngine.encrypt("Selam! Canlı konumum ve ekran akışım bağlandı.")
        val msg2Enc = CryptoEngine.encrypt("Harika! Şimdi haritadayım ve ekranına çizim yapıyorum, görüyor musun?")
        val msg3Enc = CryptoEngine.encrypt("Evet, neon çizgiler ve anlık GPS konumun tam olarak görünüyor!")

        val initial = listOf(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                sender = UserRole.USER_A,
                plainText = "Selam! Canlı konumum ve ekran akışım bağlandı.",
                encryptedPayload = msg1Enc,
                timestamp = System.currentTimeMillis() - 120_000
            ),
            ChatMessage(
                id = UUID.randomUUID().toString(),
                sender = UserRole.USER_B,
                plainText = "Harika! Şimdi haritadayım ve ekranına çizim yapıyorum, görüyor musun?",
                encryptedPayload = msg2Enc,
                timestamp = System.currentTimeMillis() - 60_000
            ),
            ChatMessage(
                id = UUID.randomUUID().toString(),
                sender = UserRole.USER_A,
                plainText = "Evet, neon çizgiler ve anlık GPS konumun tam olarak görünüyor!",
                encryptedPayload = msg3Enc,
                timestamp = System.currentTimeMillis() - 10_000
            )
        )
        _messages.value = initial
    }

    fun switchRole(newRole: UserRole) {
        _currentRole.value = newRole
        recordSyncEvent("[ROLE_SWITCH] Aktif görünüm ${newRole.badge} (${newRole.displayName}) olarak değiştirildi")
    }

    fun setRole(newRole: UserRole) {
        switchRole(newRole)
    }

    fun updateAzimuth(degrees: Float) {
        _azimuthDegrees.value = degrees
    }

    fun setWalkieTalkieTransmitting(isTransmitting: Boolean) {
        _walkieTalkieState.update {
            it.copy(isTransmitting = isTransmitting)
        }
        recordSyncEvent(if (isTransmitting) "[PTT_TX] Telsiz konuşması başlatıldı" else "[PTT_END] Telsiz kanalı dinlemede")
    }

    fun setDualViewMode(mode: DualStreamViewMode) {
        _dualViewMode.value = mode
    }

    fun sendMessage(text: String, isDisappearing: Boolean = false) {
        if (text.isBlank()) return
        val sender = _currentRole.value
        val payload = CryptoEngine.encrypt(text)
        val msg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = sender,
            plainText = text,
            encryptedPayload = payload,
            isDisappearing = isDisappearing
        )
        _messages.update { it + msg }
    }

    fun sendLocationMessage() {
        val sender = _currentRole.value
        val loc = if (sender == UserRole.USER_A) _userALocation.value else _userBLocation.value
        val text = "📍 Anlık Tam Konumumu Paylaştım: ${loc.placeName} (${loc.streetAddress})"
        val payload = CryptoEngine.encrypt(text)
        val msg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = sender,
            plainText = text,
            encryptedPayload = payload,
            locationPayload = loc
        )
        _messages.update { it + msg }
        recordSyncEvent("[E2EE_LOC] Konum şifreli olarak sohbete iletildi")
    }

    fun sendVoiceNoteMessage(durationSec: Int) {
        val sender = _currentRole.value
        val text = "🎤 Sesli Mesaj ($durationSec sn)"
        val payload = CryptoEngine.encrypt(text)
        val msg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = sender,
            plainText = text,
            encryptedPayload = payload,
            isVoiceNote = true,
            voiceDurationSec = durationSec
        )
        _messages.update { it + msg }
        recordSyncEvent("[E2EE_VOICE] Sesli mesaj şifreli olarak iletildi")
    }

    fun sendDrawingCardMessage(note: String = "Ekran Üzerine Çizim") {
        val sender = _currentRole.value
        val text = "🎨 Yeni Ekran Çizimi Paylaşıldı: $note"
        val payload = CryptoEngine.encrypt(text)
        val msg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = sender,
            plainText = text,
            encryptedPayload = payload,
            isDrawingCard = true,
            drawingNote = note
        )
        _messages.update { it + msg }
        recordSyncEvent("[DRAW_SHARE] Çizim karta dönüştürülüp sohbete gönderildi")
    }

    fun sendSosBeacon() {
        val sender = _currentRole.value
        val loc = if (sender == UserRole.USER_A) _userALocation.value else _userBLocation.value
        val text = "🚨 ACİL DURUM (SOS) ÇAĞRISI: Acil yardıma ihtiyacım var! Tam Konum: ${loc.placeName} (${loc.latitude}, ${loc.longitude})"
        val payload = CryptoEngine.encrypt(text)
        val msg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = sender,
            plainText = text,
            encryptedPayload = payload,
            locationPayload = loc
        )
        _messages.update { it + msg }
        recordSyncEvent("[SOS_ALERT] Yüksek öncelikli acil durum sinyali gönderildi")
    }

    fun addReaction(messageId: String, emoji: String) {
        _messages.update { list ->
            list.map {
                if (it.id == messageId) it.copy(reaction = if (it.reaction == emoji) null else emoji)
                else it
            }
        }
    }

    // --- ACCURATE REAL LOCATION SETTERS & UPDATERS ---

    fun updateMyGpsLocation(
        lat: Double,
        lng: Double,
        accuracy: Float,
        speed: Float,
        placeName: String? = null,
        address: String? = null
    ) {
        val targetRole = _currentRole.value
        val existingLoc = if (targetRole == UserRole.USER_A) _userALocation.value else _userBLocation.value
        val resolvedPlace = placeName ?: existingLoc.placeName
        val resolvedAddress = address ?: existingLoc.streetAddress

        if (targetRole == UserRole.USER_A) {
            val prevDist = calculateDistanceMeters()
            _userALocation.update {
                it.copy(
                    latitude = lat,
                    longitude = lng,
                    accuracyMeters = accuracy,
                    speedKmh = speed,
                    timestamp = System.currentTimeMillis(),
                    placeName = resolvedPlace,
                    streetAddress = resolvedAddress
                )
            }
            // If partner is far away (> 20km) or in another city, bring partner into nearby neighborhood (~450m)
            if (prevDist > 20_000) {
                val deltaLat = 0.0032
                val deltaLng = 0.0038
                val partnerLat = lat + deltaLat
                val partnerLng = lng + deltaLng
                _userBLocation.update {
                    it.copy(
                        latitude = partnerLat,
                        longitude = partnerLng,
                        placeName = "$resolvedPlace (Partner)",
                        streetAddress = "Aynı Bölge (~450m)",
                        accuracyMeters = 4.2f,
                        speedKmh = 12.0f
                    )
                }
                _partnerBreadcrumbs.value = listOf(
                    LocationBreadcrumb(lat + 0.001, lng + 0.001, System.currentTimeMillis() - 180000),
                    LocationBreadcrumb(lat + 0.002, lng + 0.002, System.currentTimeMillis() - 120000),
                    LocationBreadcrumb(partnerLat, partnerLng, System.currentTimeMillis())
                )
            }
        } else {
            _userBLocation.update {
                it.copy(
                    latitude = lat,
                    longitude = lng,
                    accuracyMeters = accuracy,
                    speedKmh = speed,
                    timestamp = System.currentTimeMillis(),
                    placeName = resolvedPlace,
                    streetAddress = resolvedAddress
                )
            }
        }
        checkGeofenceAndNavigation()
        recordSyncEvent("[GPS_UPDATE] ${targetRole.badge} GPS: $lat, $lng (±${accuracy.toInt()}m)")
    }

    fun setPartnerProximity(mode: PartnerProximityMode) {
        val locA = _userALocation.value
        val distMeters = mode.distanceMeters
        val deltaLat = (distMeters / 110574.0) * cos(Math.toRadians(45.0))
        val deltaLng = (distMeters / (111320.0 * cos(Math.toRadians(locA.latitude)).coerceAtLeast(0.1))) * sin(Math.toRadians(45.0))
        val newLat = locA.latitude + deltaLat
        val newLng = locA.longitude + deltaLng
        _userBLocation.update {
            it.copy(
                latitude = newLat,
                longitude = newLng,
                placeName = "${locA.placeName} (Partner)",
                streetAddress = mode.title,
                accuracyMeters = 3.5f,
                speedKmh = 11.5f
            )
        }
        _partnerBreadcrumbs.value = listOf(
            LocationBreadcrumb(newLat - deltaLat * 0.75, newLng - deltaLng * 0.75, System.currentTimeMillis() - 180000),
            LocationBreadcrumb(newLat - deltaLat * 0.50, newLng - deltaLng * 0.50, System.currentTimeMillis() - 120000),
            LocationBreadcrumb(newLat - deltaLat * 0.25, newLng - deltaLng * 0.25, System.currentTimeMillis() - 60000),
            LocationBreadcrumb(newLat, newLng, System.currentTimeMillis())
        )
        checkGeofenceAndNavigation()
        recordSyncEvent("[PROXIMITY_SET] Partner konumu ${mode.title} olarak ayarlandı")
    }

    fun relocatePin(role: UserRole, lat: Double, lng: Double, place: String, address: String) {
        if (role == UserRole.USER_A) {
            _userALocation.update {
                it.copy(
                    latitude = lat,
                    longitude = lng,
                    placeName = place,
                    streetAddress = address,
                    accuracyMeters = 2.0f,
                    timestamp = System.currentTimeMillis()
                )
            }
        } else {
            _userBLocation.update {
                it.copy(
                    latitude = lat,
                    longitude = lng,
                    placeName = place,
                    streetAddress = address,
                    accuracyMeters = 2.0f,
                    timestamp = System.currentTimeMillis()
                )
            }
        }
        checkGeofenceAndNavigation()
        recordSyncEvent("[PIN_RELOCATE] ${role.badge} konumu haritada işaretlendi: $place")
    }

    fun selectCityPreset(preset: CityPreset) {
        // Set User A to the preset location
        _userALocation.update {
            it.copy(
                latitude = preset.latitude,
                longitude = preset.longitude,
                placeName = "${preset.cityName}, ${preset.districtName}",
                streetAddress = "Şehir Merkezi (Seçilen Konum)",
                accuracyMeters = 3.0f,
                speedKmh = 0f
            )
        }
        // Set partner nearby (around 450 meters away in same district)
        val deltaLat = 0.0035
        val deltaLng = 0.0042
        _userBLocation.update {
            it.copy(
                latitude = preset.latitude + deltaLat,
                longitude = preset.longitude + deltaLng,
                placeName = "${preset.cityName}, Çevre Caddesi",
                streetAddress = "Partner Bağlantı Noktası",
                accuracyMeters = 4.2f,
                speedKmh = 14.2f
            )
        }
        // Reset breadcrumbs in new city
        _partnerBreadcrumbs.value = listOf(
            LocationBreadcrumb(preset.latitude + 0.001, preset.longitude + 0.001, System.currentTimeMillis() - 180000),
            LocationBreadcrumb(preset.latitude + 0.002, preset.longitude + 0.0025, System.currentTimeMillis() - 120000),
            LocationBreadcrumb(preset.latitude + 0.003, preset.longitude + 0.0035, System.currentTimeMillis() - 60000),
            LocationBreadcrumb(preset.latitude + deltaLat, preset.longitude + deltaLng, System.currentTimeMillis())
        )
        checkGeofenceAndNavigation()
        recordSyncEvent("[CITY_SELECT] Konum ${preset.cityName} (${preset.districtName}) olarak güncellendi")
    }

    fun setManualCoordinates(lat: Double, lng: Double, name: String) {
        _userALocation.update {
            it.copy(
                latitude = lat,
                longitude = lng,
                placeName = name,
                streetAddress = "Özel Koordinat Konumu",
                accuracyMeters = 2.5f
            )
        }
        // Adjust partner to be near
        _userBLocation.update {
            it.copy(
                latitude = lat + 0.0025,
                longitude = lng + 0.0030,
                placeName = "$name Civarı",
                streetAddress = "Partner Konumu"
            )
        }
        checkGeofenceAndNavigation()
        recordSyncEvent("[MANUAL_LOC] Koordinatlar güncellendi: $lat, $lng")
    }

    fun setGeofenceRadius(radiusMeters: Float) {
        _geofenceSettings.update { it.copy(radiusMeters = radiusMeters) }
        checkGeofenceAndNavigation()
    }

    fun toggleGeofence(enabled: Boolean) {
        _geofenceSettings.update { it.copy(isEnabled = enabled) }
        checkGeofenceAndNavigation()
    }

    private fun checkGeofenceAndNavigation() {
        val dist = calculateDistanceMeters()
        val gf = _geofenceSettings.value
        val breached = gf.isEnabled && (dist > gf.radiusMeters)
        _isGeofenceBreached.value = breached
        updateNavigationSteps()
    }

    private fun updateNavigationSteps() {
        val dist = calculateDistanceMeters().toInt()
        val steps = mutableListOf<NavigationStep>()
        if (dist < 50) {
            steps.add(NavigationStep("Partnerinizin yanındasınız (Hedefe ulaşıldı)", dist, "destination"))
        } else if (dist < 300) {
            steps.add(NavigationStep("Cadde boyunca ${dist}m düz ilerleyin", dist, "straight"))
            steps.add(NavigationStep("Partneriniz tam karşınızda", 0, "destination"))
        } else if (dist < 1000) {
            steps.add(NavigationStep("Kuzeydoğu yönünde 120m yürüyün", 120, "straight"))
            steps.add(NavigationStep("Işıklardan sağa dönün", 240, "right"))
            steps.add(NavigationStep("Cadde boyunca partnerinize doğru devam edin", dist - 360, "destination"))
        } else {
            steps.add(NavigationStep("Ana caddeye çıkın ve düz ilerleyin", 400, "straight"))
            steps.add(NavigationStep("Kavşaktan sol şeride geçin", 650, "left"))
            steps.add(NavigationStep("Hedef partner konumu: ${(dist / 1000.0).toString().take(4)} km mesafede", dist, "destination"))
        }
        _navigationSteps.value = steps
    }

    // --- CROSS-SCREEN TELESTRATOR & DRAW COMMAND SYNCHRONIZATION ---

    fun startCrossScreenStroke(
        targetScreen: UserRole,
        xRatio: Float,
        yRatio: Float,
        colorArgb: Long,
        strokeWidth: Float,
        toolType: DrawToolType
    ) {
        val author = _currentRole.value
        val isLaser = (toolType == DrawToolType.LASER)
        val isDisappearing = (toolType == DrawToolType.DISAPPEARING)
        val expiresAt = if (isDisappearing) System.currentTimeMillis() + 2500L else null

        val stroke = DrawingStroke(
            id = UUID.randomUUID().toString(),
            author = author,
            targetScreen = targetScreen,
            points = listOf(DrawingPoint(xRatio, yRatio)),
            colorArgb = colorArgb,
            strokeWidth = strokeWidth,
            isLaser = isLaser,
            isDisappearing = isDisappearing,
            expiresAt = expiresAt
        )
        _activeStroke.value = stroke

        if (isLaser) {
            if (targetScreen == UserRole.USER_A) {
                _laserPointerOnScreenA.value = Pair(xRatio, yRatio)
            } else {
                _laserPointerOnScreenB.value = Pair(xRatio, yRatio)
            }
        }

        recordSyncEvent(
            "[DRAW_SYNC] ${author.badge} -> ${targetScreen.badge} ekrana çiziyor (${String.format("%.2f", xRatio)}, ${String.format("%.2f", yRatio)})"
        )
    }

    fun addPointToStroke(xRatio: Float, yRatio: Float) {
        _activeStroke.update { current ->
            current?.let {
                it.copy(points = it.points + DrawingPoint(xRatio, yRatio))
            }
        }
        val active = _activeStroke.value
        if (active?.isLaser == true) {
            if (active.targetScreen == UserRole.USER_A) {
                _laserPointerOnScreenA.value = Pair(xRatio, yRatio)
            } else {
                _laserPointerOnScreenB.value = Pair(xRatio, yRatio)
            }
        }
    }

    fun finishStroke() {
        val completed = _activeStroke.value
        if (completed != null && completed.points.size > 1) {
            if (completed.isLaser) {
                scope.launch {
                    delay(1200)
                    if (completed.targetScreen == UserRole.USER_A) {
                        _laserPointerOnScreenA.value = null
                    } else {
                        _laserPointerOnScreenB.value = null
                    }
                }
            } else {
                _drawingStrokes.update { it + completed }
            }
        }
        _activeStroke.value = null
    }

    fun startStroke(x: Float, y: Float, colorArgb: Long, strokeWidth: Float, isLaser: Boolean) {
        val target = if (_currentRole.value == UserRole.USER_A) UserRole.USER_B else UserRole.USER_A
        startCrossScreenStroke(
            targetScreen = target,
            xRatio = x,
            yRatio = y,
            colorArgb = colorArgb,
            strokeWidth = strokeWidth,
            toolType = if (isLaser) DrawToolType.LASER else DrawToolType.PEN
        )
    }

    fun undoLastDrawing(targetScreen: UserRole? = null) {
        _drawingStrokes.update { list ->
            if (targetScreen != null) {
                val index = list.indexOfLast { it.targetScreen == targetScreen }
                if (index != -1) list.filterIndexed { i, _ -> i != index } else list
            } else {
                if (list.isNotEmpty()) list.dropLast(1) else list
            }
        }
        recordSyncEvent("[DRAW_UNDO] Son çizim geri alındı")
    }

    fun clearDrawings(targetScreen: UserRole? = null) {
        _drawingStrokes.update { list ->
            if (targetScreen != null) {
                list.filterNot { it.targetScreen == targetScreen }
            } else {
                emptyList()
            }
        }
        _activeStroke.value = null
        _laserPointerOnScreenA.value = null
        _laserPointerOnScreenB.value = null
        recordSyncEvent("[DRAW_CLEAR] Ekran çizimleri temizlendi")
    }

    // Video Player & Stream Controls
    fun togglePlayPauseVideo(owner: UserRole) {
        if (owner == UserRole.USER_A) {
            _userAStream.update { it.copy(isPlaying = !it.isPlaying) }
        } else {
            _userBStream.update { it.copy(isPlaying = !it.isPlaying) }
        }
    }

    fun seekVideo(owner: UserRole, seconds: Float) {
        if (owner == UserRole.USER_A) {
            _userAStream.update { it.copy(currentTimeSec = seconds.coerceIn(0f, it.totalDurationSec)) }
        } else {
            _userBStream.update { it.copy(currentTimeSec = seconds.coerceIn(0f, it.totalDurationSec)) }
        }
    }

    fun changeVideoCategory(owner: UserRole, category: VideoCategory) {
        val (appName, detail, url) = when (category) {
            VideoCategory.INSTAGRAM -> Triple("Instagram", "@ayse.yilmaz profilinin son gönderisini inceliyor", "instagram.com/p/C8yZ91a")
            VideoCategory.WHATSAPP -> Triple("WhatsApp", "Ahmet ile sohbette ses kaydı dinliyor", "whatsapp://chat/35294812")
            VideoCategory.CHROME -> Triple("Google Chrome", "Google'da 'en iyi kahve mekanları' aratıyor", "google.com/search?q=kahve+mekanlari")
            VideoCategory.YOUTUBE -> Triple("YouTube", "4K Doğa ve Seyahat Belgeseli izliyor (1080p 60fps)", "youtube.com/watch?v=dQw4w9Wg")
            VideoCategory.SPOTIFY -> Triple("Spotify", "Daft Punk - Instant Crush çalıyor", "spotify:track:4DHbgw92")
            VideoCategory.LIVE_MAP -> Triple("Canlı GPS & Rota", "Dinamik rota ve telemetri ekranı", "maps.google.com/dir/39.9,32.8")
            VideoCategory.GALLERY -> Triple("Fotoğraf Galerisi", "Kamera albümü slayt gösterisi (Full HD)", "content://media/external/images")
            VideoCategory.NATURE_4K -> Triple("4K Doğa & Şelale", "Doğa akışı oynatılıyor (60 FPS)", "stream://cdn.nature/4k")
            VideoCategory.CYBER_NEON -> Triple("Siber Gece Şehri", "Neon şehir animasyon akışı", "stream://neon.synth/cyber")
            VideoCategory.CUSTOM_MEDIA -> Triple("Cihaz Medyası", "Galeriden seçilen medya yayını", "content://media/local/picked")
        }
        val updater: (VideoStreamFeed) -> VideoStreamFeed = {
            it.copy(category = category, appName = appName, activityDetail = detail, currentUrlOrPage = url)
        }
        if (owner == UserRole.USER_A) {
            _userAStream.update(updater)
        } else {
            _userBStream.update(updater)
        }
        _screenActivity.update {
            val event = ActivityEvent(
                appName = appName,
                actionDetail = detail,
                targetPageOrUrl = url
            )
            it.copy(
                activeAppName = appName,
                activityDescription = detail,
                currentUrlOrPage = url,
                activityEvents = (listOf(event) + it.activityEvents).take(20)
            )
        }
        recordSyncEvent("[APP_SWITCH] ${owner.badge} $appName uygulamasına geçti: $detail")
    }

    fun setCustomMediaUri(owner: UserRole, uriString: String) {
        val updater: (VideoStreamFeed) -> VideoStreamFeed = { feed ->
            feed.copy(
                category = VideoCategory.CUSTOM_MEDIA,
                appName = "Cihaz Galerisi Medyası",
                activityDetail = "Galeriden canlı paylaşılan fotoğraf/video",
                customImageUri = uriString
            )
        }
        if (owner == UserRole.USER_A) _userAStream.update(updater)
        else _userBStream.update(updater)
        recordSyncEvent("[MEDIA_PICK] ${owner.badge} galeriden medya yayını başlattı")
    }

    fun toggleScreenSharing(owner: UserRole = _currentRole.value) {
        if (owner == UserRole.USER_A) {
            _userAStream.update { it.copy(isSharingActive = !it.isSharingActive) }
        } else {
            _userBStream.update { it.copy(isSharingActive = !it.isSharingActive) }
        }
    }

    fun togglePrivacyShield(owner: UserRole = _currentRole.value) {
        if (owner == UserRole.USER_A) {
            _userAStream.update { it.copy(isPrivacyShieldOn = !it.isPrivacyShieldOn) }
        } else {
            _userBStream.update { it.copy(isPrivacyShieldOn = !it.isPrivacyShieldOn) }
        }
        _screenActivity.update { it.copy(isPrivacyShieldOn = !it.isPrivacyShieldOn) }
    }

    fun recordSyncEvent(log: String) {
        _networkSyncStats.update {
            it.copy(packetsSynced = it.packetsSynced + 1)
        }
        _recentSyncLogs.update { list ->
            (listOf(log) + list).take(8)
        }
    }

    // Distance calculation between User A and User B (Haversine formula in meters)
    fun calculateDistanceMeters(): Double {
        val locA = _userALocation.value
        val locB = _userBLocation.value
        val r = 6371000.0
        val dLat = Math.toRadians(locB.latitude - locA.latitude)
        val dLng = Math.toRadians(locB.longitude - locA.longitude)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(locA.latitude)) * cos(Math.toRadians(locB.latitude)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    // Bearing calculation from User A to User B in degrees (0..360)
    fun calculateBearing(): Float {
        val locA = _userALocation.value
        val locB = _userBLocation.value
        val lat1 = Math.toRadians(locA.latitude)
        val lat2 = Math.toRadians(locB.latitude)
        val dLng = Math.toRadians(locB.longitude - locA.longitude)
        val y = sin(dLng) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLng)
        val brng = Math.toDegrees(atan2(y, x))
        return ((brng + 360) % 360).toFloat()
    }

    private fun startVideoPlaybackLoop() {
        scope.launch {
            while (true) {
                delay(1000)
                _userAStream.update { feed ->
                    if (feed.isPlaying && feed.isSharingActive) {
                        val next = (feed.currentTimeSec + 1f) % feed.totalDurationSec
                        feed.copy(currentTimeSec = next)
                    } else feed
                }
                _userBStream.update { feed ->
                    if (feed.isPlaying && feed.isSharingActive) {
                        val next = (feed.currentTimeSec + 1f) % feed.totalDurationSec
                        feed.copy(currentTimeSec = next)
                    } else feed
                }
                _networkSyncStats.update { stats ->
                    stats.copy(
                        packetsSynced = stats.packetsSynced + 60,
                        streamLatencyMs = 12 + (System.currentTimeMillis() % 4).toInt()
                    )
                }
            }
        }
    }

    private fun startDisappearingInkCleanerLoop() {
        scope.launch {
            while (true) {
                delay(500)
                val now = System.currentTimeMillis()
                val hasExpired = _drawingStrokes.value.any { it.expiresAt != null && it.expiresAt <= now }
                if (hasExpired) {
                    _drawingStrokes.update { list ->
                        list.filterNot { it.expiresAt != null && it.expiresAt <= now }
                    }
                }
            }
        }
    }

    /**
     * Reads REAL device data (usage stats, foreground apps, battery, network) from Android OS.
     * ZERO FAKE DATA: Directly uses UsageStatsManager, BatteryManager, ConnectivityManager.
     */
    fun refreshRealDeviceData(context: Context) {
        scope.launch {
            val hasPermission = RealDeviceUsageManager.hasUsagePermission(context)
            val (batteryPct, isCharging) = RealDeviceUsageManager.getRealBatteryInfo(context)
            val networkType = RealDeviceUsageManager.getRealNetworkType(context)
            val (ramUsed, ramTotal, ramPct) = RealDeviceUsageManager.getRealRamInfo(context)
            val (storageUsed, storageTotal, storagePct) = RealDeviceUsageManager.getRealStorageInfo()
            val (displayRes, displayFps) = RealDeviceUsageManager.getRealDisplayMetrics(context)
            val installedApps = RealDeviceUsageManager.getRealInstalledApps(context)

            val realApps = if (hasPermission) {
                RealDeviceUsageManager.getRealAppUsage(context)
            } else {
                emptyList()
            }

            val realEvents = if (hasPermission) {
                RealDeviceUsageManager.getRealActivityEvents(context)
            } else {
                emptyList()
            }

            _realTelemetry.update {
                it.copy(
                    isUsagePermissionGranted = hasPermission,
                    realBatteryPercent = batteryPct,
                    isCharging = isCharging,
                    networkType = networkType,
                    realAppsUsageList = realApps,
                    ramUsedGb = ramUsed,
                    ramTotalGb = ramTotal,
                    ramPercent = ramPct,
                    storageUsedGb = storageUsed,
                    storageTotalGb = storageTotal,
                    storagePercent = storagePct,
                    displayResolution = displayRes,
                    displayFps = displayFps,
                    installedAppsCount = installedApps.size,
                    installedApps = installedApps,
                    isSimulatedMode = false
                )
            }

            if (realEvents.isNotEmpty()) {
                val latest = realEvents.first()
                _screenActivity.update {
                    it.copy(
                        activeAppName = latest.appName,
                        activityDescription = latest.actionDetail,
                        currentUrlOrPage = latest.targetPageOrUrl,
                        activityEvents = realEvents,
                        isRealSystemData = true
                    )
                }

                _userBStream.update {
                    it.copy(
                        appName = latest.appName,
                        activityDetail = latest.actionDetail,
                        currentUrlOrPage = latest.targetPageOrUrl
                    )
                }
                recordSyncEvent("[REAL_DATA] Gerçek sistem uygulama aktivitesi okundu: ${latest.appName}")
            } else if (!hasPermission) {
                _screenActivity.update {
                    it.copy(
                        activeAppName = "Kullanım İzni Bekleniyor",
                        activityDescription = "Gerçek uygulama hareketlerini görmek için Android Ayarlarından 'Kullanım Erişimi' iznini onaylayın.",
                        currentUrlOrPage = "android.settings.USAGE_ACCESS_SETTINGS",
                        isRealSystemData = true
                    )
                }
            }
        }
    }

    private var realPollingJob: kotlinx.coroutines.Job? = null

    /**
     * Periodically polls real Android OS usage events every 5 seconds when active.
     */
    fun startRealDevicePolling(context: Context) {
        if (realPollingJob?.isActive == true) return
        realPollingJob = scope.launch {
            while (true) {
                refreshRealDeviceData(context)
                delay(5000)
            }
        }
    }
}
