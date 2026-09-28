package com.example.model

import com.example.crypto.EncryptedMessagePayload
import java.util.UUID

enum class UserRole(val displayName: String, val badge: String) {
    USER_A("Sen (Cihazım)", "Cihaz 1"),
    USER_B("Partner (Karşı Taraf)", "Cihaz 2")
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: UserRole,
    val plainText: String,
    val encryptedPayload: EncryptedMessagePayload,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = true,
    val isDisappearing: Boolean = false,
    val reaction: String? = null,
    val locationPayload: LiveLocation? = null,
    val isVoiceNote: Boolean = false,
    val voiceDurationSec: Int = 0,
    val isDrawingCard: Boolean = false,
    val drawingNote: String? = null
)

data class LiveLocation(
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double = 42.0,
    val accuracyMeters: Float = 3.2f,
    val speedKmh: Float = 0.0f,
    val timestamp: Long = System.currentTimeMillis(),
    val placeName: String = "Merkez / Canlı",
    val streetAddress: String = "Bağlantı Aktif",
    val batteryPercent: Int = 88,
    val isLiveSharing: Boolean = true,
    val bearingDegrees: Float = 45f
)

data class LocationBreadcrumb(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis()
)

data class GeofenceSettings(
    val isEnabled: Boolean = true,
    val radiusMeters: Float = 400f,
    val label: String = "Güvenli Alan (400m Çemberi)"
)

data class NavigationStep(
    val instruction: String,
    val distanceMeters: Int,
    val turnIcon: String // "straight", "right", "left", "destination"
)

data class ScreenTouch(
    val id: Long = System.currentTimeMillis(),
    val xRatio: Float,
    val yRatio: Float,
    val durationMs: Long = 800
)

enum class VideoCategory(val title: String, val iconDescription: String) {
    INSTAGRAM("Instagram", "Hikayeler, Profil & Keşfet"),
    WHATSAPP("WhatsApp", "Sohbetler & Mesajlaşma"),
    CHROME("Google Chrome", "Webde Arama & Sayfa Gezintisi"),
    YOUTUBE("YouTube", "4K Video & Canlı Yayın"),
    SPOTIFY("Spotify Müzik", "Şarkı Listesi & Çalma"),
    LIVE_MAP("Haritalar & GPS", "Dinamik Rota & Navigasyon"),
    GALLERY("Fotoğraf Galerisi", "Kamera Albümü & Fotoğraflar"),
    NATURE_4K("4K Doğa & Şelale", "Canlı Manzara Akışı"),
    CYBER_NEON("Siber Gece Şehri", "Animasyonlu 60FPS Video"),
    CUSTOM_MEDIA("Cihaz Medyası", "Galeriden Aktarılan Medya")
}

data class ActivityEvent(
    val id: String = UUID.randomUUID().toString(),
    val appName: String,
    val actionDetail: String,
    val targetPageOrUrl: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRealEvent: Boolean = true
)

data class RealAppUsageInfo(
    val packageName: String,
    val appName: String,
    val lastTimeUsed: Long,
    val totalTimeInForegroundMs: Long,
    val isCurrentlyActive: Boolean = false
)

data class RealDeviceTelemetryState(
    val isUsagePermissionGranted: Boolean = false,
    val isRealScreenShareActive: Boolean = false,
    val realBatteryPercent: Int = 100,
    val isCharging: Boolean = false,
    val networkType: String = "Wi-Fi (Aktif)",
    val realAppsUsageList: List<RealAppUsageInfo> = emptyList(),
    val ramUsedGb: Float = 3.2f,
    val ramTotalGb: Float = 8.0f,
    val ramPercent: Int = 40,
    val storageUsedGb: Float = 45.0f,
    val storageTotalGb: Float = 128.0f,
    val storagePercent: Int = 35,
    val displayResolution: String = "1080x2400",
    val displayFps: Int = 60,
    val installedAppsCount: Int = 0,
    val installedApps: List<Pair<String, String>> = emptyList(),
    val isSimulatedMode: Boolean = false
)

data class VideoStreamFeed(
    val owner: UserRole,
    val appName: String = "Android Ekranı",
    val activityDetail: String = "Gerçek cihaz uygulama akışı",
    val currentUrlOrPage: String = "android.os.system",
    val isPlaying: Boolean = true,
    val currentTimeSec: Float = 0f,
    val totalDurationSec: Float = 3600f,
    val category: VideoCategory = VideoCategory.CHROME,
    val customImageUri: String? = null,
    val fps: Int = 60,
    val resolution: String = "1080 x 2400 (60fps)",
    val bitrateMbps: Float = 5.2f,
    val latencyMs: Int = 14,
    val isSharingActive: Boolean = true,
    val isPrivacyShieldOn: Boolean = false,
    val touches: List<ScreenTouch> = emptyList()
)

data class ScreenActivityState(
    val isSharingActive: Boolean = true,
    val activeAppName: String = "Android Cihaz",
    val activityDescription: String = "Gerçek cihaz kullanım verileri izleniyor",
    val currentUrlOrPage: String = "android.system.foreground",
    val fps: Int = 60,
    val bitrateMbps: Float = 4.8f,
    val latencyMs: Int = 14,
    val resolution: String = "1080 x 2400",
    val touches: List<ScreenTouch> = emptyList(),
    val isPrivacyShieldOn: Boolean = false,
    val simulatedScreenIndex: Int = 0,
    val activityEvents: List<ActivityEvent> = emptyList(),
    val isRealSystemData: Boolean = true
)

data class DrawingPoint(
    val x: Float,
    val y: Float
)

enum class DrawToolType(val label: String) {
    PEN("Neon Kalem"),
    HIGHLIGHTER("Vurgulayıcı"),
    LASER("Canlı Lazer"),
    DISAPPEARING("Uçucu Mürekkep"),
    ERASER("Silgi")
}

data class DrawingStroke(
    val id: String = UUID.randomUUID().toString(),
    val author: UserRole,
    val targetScreen: UserRole = UserRole.USER_B,
    val points: List<DrawingPoint>,
    val colorArgb: Long,
    val strokeWidth: Float,
    val isLaser: Boolean = false,
    val isDisappearing: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null
)

enum class DrawCommandType {
    STROKE_START,
    STROKE_POINT,
    STROKE_END,
    LASER_MOVE,
    LASER_STOP,
    CLEAR_ALL,
    UNDO
}

data class DrawCommand(
    val commandId: String = UUID.randomUUID().toString(),
    val type: DrawCommandType,
    val sender: UserRole,
    val targetScreen: UserRole,
    val xRatio: Float = 0f,
    val yRatio: Float = 0f,
    val colorArgb: Long = 0xFF06B6D4,
    val strokeWidth: Float = 8f,
    val toolType: DrawToolType = DrawToolType.PEN,
    val timestamp: Long = System.currentTimeMillis()
)

enum class DualStreamViewMode(val label: String) {
    SPLIT_DUAL("Yan Yana İkili Ekran"),
    PARTNER_FOCUS("Partner Ekranına Odaklan"),
    MY_FOCUS("Kendi Ekranıma Odaklan"),
    PIP_FLOAT("Resim İçinde Resim (PiP)")
}

data class NetworkSyncStats(
    val videoFps: Int = 60,
    val streamLatencyMs: Int = 14,
    val drawLatencyMs: Int = 4,
    val packetsSynced: Long = 1840,
    val encryptionInfo: String = "AES-256-GCM (Uçtan Uca Aktif)",
    val streamCodec: String = "WebRTC / H.264 Ultra-Low Latency"
)

data class SessionState(
    val pairingCode: String = "#SYNC-8921",
    val isConnected: Boolean = true,
    val encryptionAlgorithm: String = "AES-256-GCM / PBKDF2",
    val partnerDeviceName: String = "Pixel 8 Pro (Canlı Eşleşti)",
    val partnerOnline: Boolean = true,
    val pingMs: Int = 14
)

data class LocationSearchResult(
    val title: String,
    val subtitle: String,
    val latitude: Double,
    val longitude: Double
)

enum class PartnerProximityMode(
    val title: String,
    val distanceMeters: Double,
    val description: String
) {
    CLOSE_WALKING("Yakın (300m - Yürüyüş)", 300.0, "Partner yürüyüş mesafesinde yanınızda"),
    NEIGHBORHOOD("Mahalle (850m - Aynı Semt)", 850.0, "Partner aynı mahallede veya caddede"),
    DISTRICT("İlçe İçi (2.8 km - Araçla)", 2800.0, "Partner aynı ilçede araç mesafesinde"),
    CITY_WIDE("Şehir İçi (12 km - Metro/Araç)", 12000.0, "Partner kentin diğer ucunda")
}

data class WalkieTalkieState(
    val isTransmitting: Boolean = false,
    val audioLevel: Float = 0.0f,
    val channelName: String = "CH-01 (Uçtan Uca Kriptolu)",
    val lastTransmissionSec: Int = 0
)
