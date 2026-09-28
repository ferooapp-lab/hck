package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.state.SyncMateRepository

class ScreenCaptureService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START_SCREEN_CAPTURE"
        const val ACTION_STOP = "ACTION_STOP_SCREEN_CAPTURE"
        private const val CHANNEL_ID = "screen_share_channel"
        private const val NOTIFICATION_ID = 2024
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val notification = buildNotification()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
                    )
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
                SyncMateRepository.recordSyncEvent("[MEDIA_PROJECTION] Canlı ekran yakalama servisi başlatıldı (Aktif)")
            }
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                SyncMateRepository.recordSyncEvent("[MEDIA_PROJECTION] Canlı ekran yakalama servisi durduruldu")
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Ekran Paylaşımı",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ekran paylaşımı sırasında aktif olan bildirim kanalı"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SyncMate Ekran Paylaşımı")
            .setContentText("Cihaz ekranı canlı ve şifreli olarak paylaşılıyor")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()
    }
}
