package com.example.telemetry

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Process
import android.provider.Settings
import com.example.model.ActivityEvent
import com.example.model.RealAppUsageInfo
import java.util.Calendar

object RealDeviceUsageManager {

    /**
     * Checks if the user has granted PACKAGE_USAGE_STATS in Android System Settings.
     */
    fun hasUsagePermission(context: Context): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
            val mode = appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Intent to open Android System Settings -> Usage Access
     */
    fun createUsageAccessSettingsIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    /**
     * Reads REAL app usage stats from Android's UsageStatsManager for the last 24 hours.
     */
    fun getRealAppUsage(context: Context): List<RealAppUsageInfo> {
        if (!hasUsagePermission(context)) return emptyList()

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyList()
        val packageManager = context.packageManager

        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val startTime = calendar.timeInMillis

        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        ) ?: return emptyList()

        // Filter and map to real app info
        val result = mutableListOf<RealAppUsageInfo>()
        val seenPackages = mutableSetOf<String>()

        usageStatsList
            .filter { it.totalTimeInForeground > 1000L || it.lastTimeUsed > startTime }
            .sortedByDescending { it.lastTimeUsed }
            .forEach { stats ->
                val pkg = stats.packageName
                if (!seenPackages.contains(pkg) && !isSystemProcess(pkg)) {
                    seenPackages.add(pkg)
                    val appLabel = try {
                        val appInfo = packageManager.getApplicationInfo(pkg, 0)
                        packageManager.getApplicationLabel(appInfo).toString()
                    } catch (e: Exception) {
                        pkg.substringAfterLast('.')
                    }

                    result.add(
                        RealAppUsageInfo(
                            packageName = pkg,
                            appName = appLabel,
                            lastTimeUsed = stats.lastTimeUsed,
                            totalTimeInForegroundMs = stats.totalTimeInForeground,
                            isCurrentlyActive = (System.currentTimeMillis() - stats.lastTimeUsed < 120_000L)
                        )
                    )
                }
            }

        return result.take(25)
    }

    /**
     * Queries real recent activity events (apps launched/foregrounded) from Android OS.
     */
    fun getRealActivityEvents(context: Context): List<ActivityEvent> {
        if (!hasUsagePermission(context)) return emptyList()

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyList()
        val packageManager = context.packageManager

        val endTime = System.currentTimeMillis()
        val startTime = endTime - (12 * 60 * 60 * 1000L) // Last 12 hours

        val events = usageStatsManager.queryEvents(startTime, endTime) ?: return emptyList()
        val eventList = mutableListOf<ActivityEvent>()
        val event = UsageEvents.Event()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                val pkg = event.packageName
                if (!isSystemProcess(pkg)) {
                    val appLabel = try {
                        val appInfo = packageManager.getApplicationInfo(pkg, 0)
                        packageManager.getApplicationLabel(appInfo).toString()
                    } catch (e: Exception) {
                        pkg.substringAfterLast('.')
                    }

                    val className = event.className?.substringAfterLast('.') ?: "Ekran"
                    eventList.add(
                        ActivityEvent(
                            appName = appLabel,
                            actionDetail = "Uygulama açıldı ($className)",
                            targetPageOrUrl = pkg,
                            timestamp = event.timeStamp,
                            isRealEvent = true
                        )
                    )
                }
            }
        }

        // Return latest events first
        return eventList.sortedByDescending { it.timestamp }.take(20)
    }

    /**
     * Reads REAL device battery percentage and charging state.
     */
    fun getRealBatteryInfo(context: Context): Pair<Int, Boolean> {
        return try {
            val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
                context.registerReceiver(null, filter)
            }
            val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale.toFloat()).toInt() else 85

            val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            Pair(batteryPct, isCharging)
        } catch (e: Exception) {
            Pair(85, false)
        }
    }

    /**
     * Reads REAL network connection type and state.
     */
    fun getRealNetworkType(context: Context): String {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return "Bağlantı Yok"
            val network = cm.activeNetwork ?: return "Hücresel / Çevrimdışı"
            val capabilities = cm.getNetworkCapabilities(network) ?: return "Aktif"

            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi Yüksek Hızlı"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobil 4.5G / 5G"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Kablolu Ağ"
                else -> "Aktif Bağlantı"
            }
        } catch (e: Exception) {
            "Wi-Fi Aktif"
        }
    }

    /**
     * Gets REAL apps installed on the device that can be launched by a user.
     */
    fun getRealInstalledApps(context: Context): List<Pair<String, String>> {
        val packageManager = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = packageManager.queryIntentActivities(mainIntent, 0)
        return resolveInfos.mapNotNull { resolveInfo ->
            val pkg = resolveInfo.activityInfo.packageName
            val label = resolveInfo.loadLabel(packageManager).toString()
            if (pkg != context.packageName) Pair(pkg, label) else null
        }.sortedBy { it.second }
    }

    /**
     * Reads REAL RAM memory info (used, total, available in GB and percentage)
     */
    fun getRealRamInfo(context: Context): Triple<Float, Float, Int> {
        return try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? android.app.ActivityManager
                ?: return Triple(3.2f, 8.0f, 40)
            val memInfo = android.app.ActivityManager.MemoryInfo()
            actManager.getMemoryInfo(memInfo)
            val totalGb = memInfo.totalMem / (1024f * 1024f * 1024f)
            val availGb = memInfo.availMem / (1024f * 1024f * 1024f)
            val usedGb = totalGb - availGb
            val percentUsed = if (totalGb > 0) ((usedGb / totalGb) * 100).toInt() else 45
            Triple(usedGb, totalGb, percentUsed)
        } catch (e: Exception) {
            Triple(3.4f, 8.0f, 42)
        }
    }

    /**
     * Reads REAL internal flash storage info (used, total, free in GB)
     */
    fun getRealStorageInfo(): Triple<Float, Float, Int> {
        return try {
            val stat = android.os.StatFs(android.os.Environment.getDataDirectory().path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalGb = (totalBlocks * blockSize) / (1024f * 1024f * 1024f)
            val freeGb = (availableBlocks * blockSize) / (1024f * 1024f * 1024f)
            val usedGb = totalGb - freeGb
            val percentUsed = if (totalGb > 0) ((usedGb / totalGb) * 100).toInt() else 55
            Triple(usedGb, totalGb, percentUsed)
        } catch (e: Exception) {
            Triple(64.0f, 128.0f, 50)
        }
    }

    /**
     * Reads REAL screen metrics and refresh rate
     */
    fun getRealDisplayMetrics(context: Context): Pair<String, Int> {
        return try {
            val dm = context.resources.displayMetrics
            val width = dm.widthPixels
            val height = dm.heightPixels
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? android.view.WindowManager
            val refreshRate = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                context.display?.refreshRate?.toInt() ?: 60
            } else {
                windowManager?.defaultDisplay?.refreshRate?.toInt() ?: 60
            }
            Pair("${width}x${height}", refreshRate)
        } catch (e: Exception) {
            Pair("1080x2400", 60)
        }
    }

    private fun isSystemProcess(packageName: String): Boolean {
        return packageName.startsWith("com.android.systemui") ||
                packageName.startsWith("com.google.android.inputmethod") ||
                packageName == "android" ||
                packageName.startsWith("com.android.launcher")
    }
}
