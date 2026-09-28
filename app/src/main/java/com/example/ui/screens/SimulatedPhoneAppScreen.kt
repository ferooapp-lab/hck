package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VideoCategory
import com.example.model.VideoStreamFeed
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.NeonCoral
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-fidelity Simulated Android Phone Operating System & Real App Screen
 * Renders real UI for WhatsApp, Instagram, Google Chrome, YouTube, Spotify, Maps, Gallery, etc.
 */
@Composable
fun SimulatedPhoneAppScreen(
    feed: VideoStreamFeed,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "simulatedScreenAnim")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Android Status Bar (Time, Wi-Fi, 5G, Battery 84%)
            AndroidStatusBar()

            // 2. Active Application Screen
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (feed.category) {
                    VideoCategory.WHATSAPP -> WhatsAppScreen(feed = feed, waveOffset = waveOffset)
                    VideoCategory.INSTAGRAM -> InstagramScreen(feed = feed, pulse = pulse)
                    VideoCategory.CHROME -> ChromeScreen(feed = feed, waveOffset = waveOffset)
                    VideoCategory.YOUTUBE -> YouTubeScreen(feed = feed, waveOffset = waveOffset)
                    VideoCategory.SPOTIFY -> SpotifyScreen(feed = feed, waveOffset = waveOffset)
                    VideoCategory.LIVE_MAP -> LiveMapNavScreen(feed = feed, waveOffset = waveOffset)
                    VideoCategory.GALLERY -> GalleryScreen(feed = feed)
                    VideoCategory.NATURE_4K -> Nature4KScreen(waveOffset = waveOffset)
                    VideoCategory.CYBER_NEON -> CyberNeonScreen(waveOffset = waveOffset)
                    VideoCategory.CUSTOM_MEDIA -> CustomMediaScreen(feed = feed)
                }
            }

            // 3. Android Navigation Bar Gesture Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(72.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.5f))
                )
            }
        }

        // 4. Live Finger Touch Ripples (Where the partner is touching right now)
        feed.touches.forEach { touch ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                val touchX = touch.xRatio
                val touchY = touch.yRatio

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val px = touchX * size.width
                    val py = touchY * size.height

                    // Expanding touch circle ring
                    drawCircle(
                        color = ElectricCyan.copy(alpha = 0.45f),
                        radius = 24.dp.toPx() * pulse,
                        center = Offset(px, py),
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                    // Inner glowing dot
                    drawCircle(
                        color = Color.White.copy(alpha = 0.9f),
                        radius = 8.dp.toPx(),
                        center = Offset(px, py)
                    )
                }

                // Partner Touch Indicator Tag
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.8f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan),
                    modifier = Modifier
                        .padding(
                            start = (touchX * 220).coerceAtLeast(10f).dp,
                            top = (touchY * 340).coerceAtLeast(24f).dp
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
                                .background(ElectricCyan)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Partner Dokundu",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// --- ANDROID SYSTEM STATUS BAR ---
@Composable
private fun AndroidStatusBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(22.dp)
            .background(Color.Black.copy(alpha = 0.9f))
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "14:38",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        // Camera hole notch in center
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E293B))
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(Icons.Default.Wifi, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
            Text("5G", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text("%84", color = MintEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// --- 1. WHATSAPP LIVE CHAT SCREEN ---
@Composable
private fun WhatsAppScreen(feed: VideoStreamFeed, waveOffset: Float) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B141A))
    ) {
        // WhatsApp Top Bar
        Surface(
            color = Color(0xFF1F2C34),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color(0xFF00A884), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00A884)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("AY", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Ahmet Yılmaz",
                            color = Color(0xFFE9EDEF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "çevrimiçi • yazıyor...",
                            color = Color(0xFF00A884),
                            fontSize = 10.sp
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Videocam, contentDescription = null, tint = Color(0xFF00A884), modifier = Modifier.size(16.dp))
                    Icon(Icons.Default.Call, contentDescription = null, tint = Color(0xFF00A884), modifier = Modifier.size(16.dp))
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color(0xFF8696A0), modifier = Modifier.size(16.dp))
                }
            }
        }

        // WhatsApp Chat Messages Area
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Date Pill
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Surface(
                    color = Color(0xFF182229),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("BUGÜN", color = Color(0xFF8696A0), fontSize = 9.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
            }

            // Incoming message bubble
            Surface(
                color = Color(0xFF202C33),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.widthIn(max = 240.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Selam Ferhat! Kadıköy'e geldin mi?", color = Color(0xFFE9EDEF), fontSize = 11.sp)
                    Text("14:32", color = Color(0xFF8696A0), fontSize = 9.sp, modifier = Modifier.align(Alignment.End))
                }
            }

            // Outgoing message bubble
            Surface(
                color = Color(0xFF005C4B),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .widthIn(max = 250.dp)
                    .align(Alignment.End)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Evet Moda'dayım, sahilde oturuyorum 📍", color = Color(0xFFE9EDEF), fontSize = 11.sp)
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("14:34", color = Color(0xFF8696A0), fontSize = 9.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(Icons.Default.DoneAll, contentDescription = null, tint = Color(0xFF53BDEB), modifier = Modifier.size(13.dp))
                    }
                }
            }

            // Incoming voice note bubble
            Surface(
                color = Color(0xFF202C33),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.widthIn(max = 240.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00A884)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            listOf(6, 12, 18, 14, 8, 16, 20, 10, 14, 8).forEach { h ->
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(h.dp)
                                        .background(Color(0xFF00A884))
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("0:14 • 14:35", color = Color(0xFF8696A0), fontSize = 9.sp)
                    }
                }
            }

            // Real-time "Ahmet yazıyor..." typing bubble
            Surface(
                color = Color(0xFF202C33).copy(alpha = 0.8f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ahmet yazıyor", color = Color(0xFF00A884), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        for (dot in 0..2) {
                            val dotAnim = (sin(waveOffset * 3f + dot * 1.5f).coerceAtLeast(0f) * 4f)
                            Box(
                                modifier = Modifier
                                    .size((3f + dotAnim).dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00A884))
                            )
                        }
                    }
                }
            }
        }

        // WhatsApp Bottom Input Bar
        Surface(
            color = Color(0xFF1F2C34),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFF2A3942),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tamamdır bekliyorum...", color = Color(0xFFE9EDEF), fontSize = 11.sp, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.Mic, contentDescription = null, tint = Color(0xFF8696A0), modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00A884)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// --- 2. INSTAGRAM LIVE REELS & FEED SCREEN ---
@Composable
private fun InstagramScreen(feed: VideoStreamFeed, pulse: Float) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Instagram Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Instagram",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Cursive
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                BadgedBox(
                    badge = {
                        Badge(containerColor = NeonCoral) {
                            Text("2", color = Color.White, fontSize = 8.sp)
                        }
                    }
                ) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Stories Bar
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val storyUsers = listOf("Senin Hikayen", "ayse.yilmaz", "mert_kaya", "deniz.ak", "burak65")
            items(storyUsers) { user ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .border(
                                2.dp,
                                Brush.linearGradient(listOf(Color(0xFFF58529), Color(0xFFDD2A7B), Color(0xFF8134AF))),
                                CircleShape
                            )
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(user.take(2).uppercase(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(user.take(8), color = Color.White, fontSize = 9.sp, maxLines = 1)
                }
            }
        }

        // Post Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                // Post Author Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDD2A7B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("AY", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("ayse.yilmaz", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Kadıköy, Moda Sahili", color = Color(0xFFA8A8A8), fontSize = 9.sp)
                        }
                    }
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }

                // Post Scenic Photo Simulation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Scenic sunset gradient
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(Color(0xFFFF7E5F), Color(0xFFFEB47B), Color.Transparent)
                            ),
                            radius = size.width * 0.35f,
                            center = Offset(size.width * 0.5f, size.height * 0.45f)
                        )
                        // Horizon sea
                        drawRect(
                            brush = Brush.verticalGradient(
                                listOf(Color(0xFF09203F), Color(0xFF537895))
                            ),
                            topLeft = Offset(0f, size.height * 0.6f),
                            size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.4f)
                        )
                    }

                    // Double-tap glowing red heart burst animation
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = NeonCoral.copy(alpha = 0.85f),
                        modifier = Modifier.size((48 * pulse).dp)
                    )
                }

                // Post Actions (Like, Comment, Share, Bookmark)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = NeonCoral, modifier = Modifier.size(18.dp))
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Icon(Icons.Default.BookmarkBorder, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }

                // Likes & Caption
                Column(modifier = Modifier.padding(horizontal = 10.dp)) {
                    Text("2.450 beğenme", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("ayse.yilmaz Hafta sonu kahvesi ve deniz havası gibisi yok ☕🌊 #kadikoy #moda", color = Color.White, fontSize = 10.sp)
                    Text("14 yorumun tümünü gör", color = Color(0xFFA8A8A8), fontSize = 9.sp)
                }
            }
        }
    }
}

// --- 3. GOOGLE CHROME BROWSER SCREEN ---
@Composable
private fun ChromeScreen(feed: VideoStreamFeed, waveOffset: Float) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1F1F1F))
    ) {
        // Chrome Top Bar & URL
        Surface(
            color = Color(0xFF2B2B2B),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFF1F1F1F),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MintEmerald, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = feed.currentUrlOrPage.ifEmpty { "google.com/search?q=kadikoy+kahve" },
                            color = Color(0xFFE8EAED),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .border(1.5.dp, Color.White, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("4", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Google Search Results Page
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Google Logo & Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Google", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Tümü", color = ElectricCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("Haritalar", color = Color(0xFF9AA0A6), fontSize = 10.sp)
                    Text("Görseller", color = Color(0xFF9AA0A6), fontSize = 10.sp)
                }
            }

            // Search Result Card 1
            Surface(
                color = Color(0xFF303134),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Moda Coffee Roasters • Kadıköy", color = Color(0xFF8AB4F8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("4.9 ★★★★★ (1.240 Yorum)", color = AmberWarning, fontSize = 10.sp)
                    Text("Taze kavrulmuş nitelikli kahve çekirdekleri, deniz manzarası ve sakin çalışma ortamı...", color = Color(0xFFBDC1C6), fontSize = 10.sp)
                }
            }

            // Search Result Card 2
            Surface(
                color = Color(0xFF303134),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Story Coffee Kadıköy • Özel Demleme", color = Color(0xFF8AB4F8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("4.8 ★★★★★ (890 Yorum)", color = AmberWarning, fontSize = 10.sp)
                    Text("Moda Caddesi üzerinde V60, Chemex filtre kahve çeşitleri ve taze kruvasanlar...", color = Color(0xFFBDC1C6), fontSize = 10.sp)
                }
            }

            // Search Result Card 3
            Surface(
                color = Color(0xFF303134),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Drip Coffeeist • Soğuk Demleme & Tatlı", color = Color(0xFF8AB4F8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("4.7 ★★★★★ (620 Yorum)", color = AmberWarning, fontSize = 10.sp)
                    Text("Soğuk demleme kahveler, cheesecake ve açık bahçe alanı...", color = Color(0xFFBDC1C6), fontSize = 10.sp)
                }
            }
        }
    }
}

// --- 4. YOUTUBE LIVE VIDEO SCREEN ---
@Composable
private fun YouTubeScreen(feed: VideoStreamFeed, waveOffset: Float) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
    ) {
        // Video Player Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Procedural scenic 4K city video
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF0A192F), Color(0xFF1E3A8A), Color(0xFF0284C7))
                    )
                )
                // Bridge silhouette
                val bridgeY = size.height * 0.7f
                drawLine(
                    color = Color.Black.copy(alpha = 0.8f),
                    start = Offset(0f, bridgeY),
                    end = Offset(size.width, bridgeY),
                    strokeWidth = 6f
                )
            }

            // 4K Badge
            Surface(
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
            ) {
                Text("4K 60FPS", color = CyanGlow, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
            }

            // Play/Pause center overlay
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }

            // Red YouTube progress bar at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Color.DarkGray)
                    .align(Alignment.BottomCenter)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.42f)
                        .fillMaxHeight()
                        .background(Color.Red)
                )
            }
        }

        // Video Details & Comments
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(
                text = "İstanbul Kadıköy Sokakları 4K 60FPS Canlı Yürüyüş Turu",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "348B görüntüleme • 2 gün önce",
                color = Color(0xFFAAAAAA),
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Channel row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("GK", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("Gezginler Kulübü", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("180B Abone", color = Color(0xFFAAAAAA), fontSize = 9.sp)
                    }
                }

                Surface(
                    color = Color.Red,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Abone Olundu", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ThumbUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("18B", color = Color.White, fontSize = 10.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Paylaş", color = Color.White, fontSize = 10.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.BookmarkBorder, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kaydet", color = Color.White, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Comments Box
            Surface(
                color = Color(0xFF272727),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Yorumlar (412)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("@gezginturk: Moda sahili her mevsim harika!", color = Color(0xFFDDDDDD), fontSize = 10.sp)
                }
            }
        }
    }
}

// --- 5. SPOTIFY LIVE MUSIC SCREEN ---
@Composable
private fun SpotifyScreen(feed: VideoStreamFeed, waveOffset: Float) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1E3A2F), Color(0xFF121212), Color(0xFF121212))
                )
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("ÇALMA LİSTESİNDEN ÇALIYOR", color = Color(0xFFB3B3B3), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text("Türkçe Rock & Alternatif", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        // Large Album Art with glowing rim
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF1DB954), Color(0xFF0A3A1A), Color(0xFF121212))
                    )
                )
                .border(2.dp, Color(0xFF1DB954), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Track Name & Artist
        Text(
            text = "Daft Punk - Instant Crush",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Julian Casablancas • Random Access Memories",
            color = Color(0xFFB3B3B3),
            fontSize = 10.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Spotify Scrubber Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF404040))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .fillMaxHeight()
                    .background(Color(0xFF1DB954))
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("02:45", color = Color(0xFFB3B3B3), fontSize = 9.sp)
            Text("05:37", color = Color(0xFFB3B3B3), fontSize = 9.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Playback Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.SkipPrevious, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1DB954)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Pause, contentDescription = null, tint = Color.Black, modifier = Modifier.size(24.dp))
            }
            Icon(Icons.Default.SkipNext, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Animated Equalizer Frequency Bars
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0..8) {
                val hAnim = (6f + sin(waveOffset * 4f + i * 1.2f).coerceAtLeast(0f) * 20f)
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(hAnim.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF1DB954))
                )
            }
        }
    }
}

// --- 6. LIVE MAP NAVIGATION SCREEN ---
@Composable
private fun LiveMapNavScreen(feed: VideoStreamFeed, waveOffset: Float) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1424))
    ) {
        // Navigation Top Banner
        Surface(
            color = Color(0xFF0B6623),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Navigation, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("200m sonra Moda Caddesi'ne sağa dönün", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("Kadıköy Sahil Yolu", color = Color.White.copy(alpha = 0.8f), fontSize = 9.sp)
                }
            }
        }

        // Live Dynamic Map Canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Road grid
                for (i in 0..4) {
                    val rx = w * (i / 4f)
                    drawLine(color = Color(0xFF1E293B), start = Offset(rx, 0f), end = Offset(rx, h), strokeWidth = 14f)
                }
                for (j in 0..5) {
                    val ry = h * (j / 5f)
                    drawLine(color = Color(0xFF1E293B), start = Offset(0f, ry), end = Offset(w, ry), strokeWidth = 12f)
                }

                // Route Path
                val p = Path()
                p.moveTo(w * 0.3f, h * 0.9f)
                p.lineTo(w * 0.3f, h * 0.4f)
                p.lineTo(w * 0.7f, h * 0.4f)
                p.lineTo(w * 0.7f, h * 0.15f)

                drawPath(path = p, color = ElectricCyan, style = Stroke(width = 8f, cap = StrokeCap.Round))

                // GPS Car Moving Point
                val carProg = (waveOffset / 6.283f)
                val cx = if (carProg < 0.5f) w * 0.3f else w * 0.3f + (carProg - 0.5f) * 2f * (w * 0.4f)
                val cy = if (carProg < 0.5f) h * 0.9f - carProg * 2f * (h * 0.5f) else h * 0.4f

                drawCircle(color = MintEmerald.copy(alpha = 0.4f), radius = 18f, center = Offset(cx, cy))
                drawCircle(color = Color.White, radius = 6f, center = Offset(cx, cy))
            }

            // Speedometer Badge
            Surface(
                color = Color.Black.copy(alpha = 0.8f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MintEmerald),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("42", color = MintEmerald, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("km/s", color = TextMuted, fontSize = 8.sp)
                }
            }
        }

        // ETA Bottom Bar
        Surface(
            color = Color(0xFF1E293B),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("14 dk (3.8 km)", color = MintEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Varış: 14:52 • En hızlı rota", color = Color(0xFFA0AEC0), fontSize = 9.sp)
                }
                Surface(
                    color = NeonCoral,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Çıkış", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }
        }
    }
}

// --- 7. GALLERY SCREEN ---
@Composable
private fun GalleryScreen(feed: VideoStreamFeed) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(10.dp)
    ) {
        Text("Fotoğraf Galerisi (284 Fotoğraf)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text("Albümler • Son Çekilenler", color = Color(0xFFA0AEC0), fontSize = 10.sp)

        Spacer(modifier = Modifier.height(10.dp))

        // Photo Grid
        val colors = listOf(
            listOf(Color(0xFF2563EB), Color(0xFF60A5FA)),
            listOf(Color(0xFFDB2777), Color(0xFFF472B6)),
            listOf(Color(0xFF059669), Color(0xFF34D399)),
            listOf(Color(0xFFD97706), Color(0xFFFBBF24)),
            listOf(Color(0xFF7C3AED), Color(0xFFA78BFA)),
            listOf(Color(0xFF0891B2), Color(0xFF38BDF8))
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f).height(75.dp).clip(RoundedCornerShape(8.dp)).background(Brush.linearGradient(colors[0])))
                Box(modifier = Modifier.weight(1f).height(75.dp).clip(RoundedCornerShape(8.dp)).background(Brush.linearGradient(colors[1])))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f).height(75.dp).clip(RoundedCornerShape(8.dp)).background(Brush.linearGradient(colors[2])))
                Box(modifier = Modifier.weight(1f).height(75.dp).clip(RoundedCornerShape(8.dp)).background(Brush.linearGradient(colors[3])))
            }
        }
    }
}

// --- 8. PROCEDURAL NATURE 4K SCREEN ---
@Composable
private fun Nature4KScreen(waveOffset: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF032B44), Color(0xFF054D68), Color(0xFF0F766E), Color(0xFF064E3B))
            )
        )

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
    }
}

// --- 9. PROCEDURAL CYBER NEON SCREEN ---
@Composable
private fun CyberNeonScreen(waveOffset: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF090A1A), Color(0xFF1B0A2A), Color(0xFF2C0A3E))
            )
        )

        val sunCenter = Offset(w * 0.5f, h * 0.35f)
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFFFF007F), Color(0xFF7928CA), Color.Transparent)
            ),
            radius = w * 0.32f,
            center = sunCenter
        )

        val horizonY = h * 0.52f
        drawLine(color = ElectricCyan, start = Offset(0f, horizonY), end = Offset(w, horizonY), strokeWidth = 2f)

        for (angle in 0..10) {
            val startX = w * (angle / 10f)
            drawLine(
                color = ElectricCyan.copy(alpha = 0.4f),
                start = Offset(w * 0.5f, horizonY),
                end = Offset(startX, h),
                strokeWidth = 1.8f
            )
        }
    }
}

// --- 10. CUSTOM MEDIA SCREEN ---
@Composable
private fun CustomMediaScreen(feed: VideoStreamFeed) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF0F172A))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Explore, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(42.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Cihaz Galerisi Medyası", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(feed.activityDetail, color = TextMuted, fontSize = 10.sp)
        }
    }
}
