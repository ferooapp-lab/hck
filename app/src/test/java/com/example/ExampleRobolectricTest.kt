package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.crypto.CryptoEngine
import com.example.model.DrawToolType
import com.example.model.DualStreamViewMode
import com.example.model.UserRole
import com.example.model.VideoCategory
import com.example.state.SyncMateRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SyncMate", appName)
  }

  @Test
  fun `verify aes gcm encryption and decryption`() {
    val secretMessage = "Merhaba! Bu uçtan uca şifreli bir mesajdır."
    val encryptedPayload = CryptoEngine.encrypt(secretMessage)

    assertNotNull(encryptedPayload.cipherText)
    assertNotEquals(secretMessage, encryptedPayload.cipherText)

    val decrypted = CryptoEngine.decrypt(encryptedPayload.cipherText, encryptedPayload.iv)
    assertEquals(secretMessage, decrypted)
  }

  @Test
  fun `verify cross-screen drawing synchronization`() {
    val repo = SyncMateRepository
    repo.clearDrawings()

    // User A draws on User B's screen
    repo.setRole(UserRole.USER_A)
    repo.startCrossScreenStroke(
        targetScreen = UserRole.USER_B,
        xRatio = 0.25f,
        yRatio = 0.50f,
        colorArgb = 0xFF06B6D4,
        strokeWidth = 8f,
        toolType = DrawToolType.PEN
    )
    repo.addPointToStroke(0.30f, 0.55f)
    repo.finishStroke()

    val strokes = repo.drawingStrokes.value
    assertTrue("At least one synchronized stroke must exist", strokes.isNotEmpty())
    val last = strokes.last()
    assertEquals(UserRole.USER_A, last.author)
    assertEquals(UserRole.USER_B, last.targetScreen)
    assertEquals(2, last.points.size)
  }

  @Test
  fun `verify video stream controls and category sync`() {
    val repo = SyncMateRepository

    // Change video category on Device 1
    repo.changeVideoCategory(UserRole.USER_A, VideoCategory.CYBER_NEON)
    assertEquals(VideoCategory.CYBER_NEON, repo.userAStream.value.category)

    // Seek video position
    repo.seekVideo(UserRole.USER_A, 45.0f)
    assertEquals(45.0f, repo.userAStream.value.currentTimeSec, 0.1f)

    // Change dual view mode
    repo.setDualViewMode(DualStreamViewMode.SPLIT_DUAL)
    assertEquals(DualStreamViewMode.SPLIT_DUAL, repo.dualViewMode.value)
  }

  @Test
  fun `verify partner proximity adjustment and pin relocation`() {
    val repo = SyncMateRepository

    // Set User A location to Istanbul Kadikoy
    repo.relocatePin(UserRole.USER_A, 40.9880, 29.0290, "İstanbul, Kadıköy", "Moda Sahil")
    assertEquals(40.9880, repo.userALocation.value.latitude, 0.0001)
    assertEquals(29.0290, repo.userALocation.value.longitude, 0.0001)

    // Set partner proximity to CLOSE_WALKING (300m)
    repo.setPartnerProximity(com.example.model.PartnerProximityMode.CLOSE_WALKING)
    val distance = repo.calculateDistanceMeters()
    assertTrue("Partner distance should be approximately 300 meters", distance in 250.0..350.0)

    // Test Walkie-Talkie transmission state
    repo.setWalkieTalkieTransmitting(true)
    assertTrue("Walkie talkie should be transmitting", repo.walkieTalkieState.value.isTransmitting)
    repo.setWalkieTalkieTransmitting(false)
    assertTrue("Walkie talkie should be idle", !repo.walkieTalkieState.value.isTransmitting)
  }

  @Test
  fun `verify real device telemetry and usage stats manager integration`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = SyncMateRepository
    repo.refreshRealDeviceData(context)
    val telemetry = repo.realTelemetry.value
    assertNotNull(telemetry)
    assertTrue("Battery percent should be between 0 and 100", telemetry.realBatteryPercent in 0..100)
    assertNotNull(telemetry.networkType)
  }
}
