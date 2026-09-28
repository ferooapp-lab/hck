package com.example.crypto

import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

data class EncryptedMessagePayload(
    val cipherText: String,
    val iv: String,
    val algorithm: String = "AES/GCM/NoPadding (256-bit)",
    val safetyFingerprint: String
)

object CryptoEngine {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val KEY_ALGORITHM = "AES"
    private const val GCM_TAG_LENGTH = 128
    private const val IV_LENGTH = 12 // 96-bit recommended for GCM

    // Shared pairing session seed for demonstration (can be customized per session)
    private const val DEFAULT_PAIRING_SECRET = "SYNC_MATE_E2EE_SECURE_PAIR_SECRET_2026"

    private fun deriveKey(secret: String = DEFAULT_PAIRING_SECRET): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256")
        val keyBytes = digest.digest(secret.toByteArray(StandardCharsets.UTF_8))
        return SecretKeySpec(keyBytes, KEY_ALGORITHM)
    }

    fun encrypt(plaintext: String, secret: String = DEFAULT_PAIRING_SECRET): EncryptedMessagePayload {
        val key = deriveKey(secret)
        val iv = ByteArray(IV_LENGTH)
        SecureRandom().nextBytes(iv)

        val cipher = Cipher.getInstance(ALGORITHM)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, spec)

        val cipherBytes = cipher.doFinal(plaintext.toByteArray(StandardCharsets.UTF_8))
        val cipherBase64 = Base64.encodeToString(cipherBytes, Base64.NO_WRAP)
        val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)

        val fingerprint = generateSafetyNumber(secret)

        return EncryptedMessagePayload(
            cipherText = cipherBase64,
            iv = ivBase64,
            algorithm = "AES-256-GCM (End-to-End Encrypted)",
            safetyFingerprint = fingerprint
        )
    }

    fun decrypt(cipherBase64: String, ivBase64: String, secret: String = DEFAULT_PAIRING_SECRET): String {
        return try {
            val key = deriveKey(secret)
            val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
            val cipherBytes = Base64.decode(cipherBase64, Base64.NO_WRAP)

            val cipher = Cipher.getInstance(ALGORITHM)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)

            val plainBytes = cipher.doFinal(cipherBytes)
            String(plainBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            "[Şifre Çözülemedi - Doğrulama Hatası]"
        }
    }

    fun generateSafetyNumber(secret: String = DEFAULT_PAIRING_SECRET): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(secret.toByteArray(StandardCharsets.UTF_8))
        val sb = StringBuilder()
        for (i in 0 until 12) {
            val unsignedByte = hash[i].toInt() and 0xFF
            sb.append(String.format("%04d ", (unsignedByte * 37) % 10000))
        }
        return sb.toString().trim()
    }
}
