package com.example.data.storage

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecureApiKeyStorage(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    init {
        ensureMasterKey()
    }

    private fun ensureMasterKey() {
        try {
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE
                )
                val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
                keyGenerator.init(keyGenParameterSpec)
                keyGenerator.generateKey()
            }
        } catch (_: Exception) {
            // Graceful fallback handled in encrypt/decrypt
        }
    }

    private fun getSecretKey(): SecretKey {
        return keyStore.getKey(KEY_ALIAS, null) as? SecretKey
            ?: throw IllegalStateException("Keystore SecretKey unavailable")
    }

    private fun encrypt(plainText: String): String {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val secretKey = getSecretKey()
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val combined = ByteArray(iv.size + encryptedBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)
            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (_: Exception) {
            // Obfuscated fallback if Keystore fails on older emulator/device
            Base64.encodeToString(plainText.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        }
    }

    private fun decrypt(encryptedData: String): String {
        return try {
            val combined = Base64.decode(encryptedData, Base64.NO_WRAP)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val secretKey = getSecretKey()
            val gcmSpec = GCMParameterSpec(128, combined, 0, 12)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
            val decryptedBytes = cipher.doFinal(combined, 12, combined.size - 12)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (_: Exception) {
            // Try base64 fallback
            try {
                String(Base64.decode(encryptedData, Base64.NO_WRAP), Charsets.UTF_8)
            } catch (_: Exception) {
                ""
            }
        }
    }

    fun saveApiKey(apiKey: String, isValidated: Boolean = true) {
        val trimmed = apiKey.trim()
        if (trimmed.isEmpty()) return
        val encrypted = encrypt(trimmed)
        prefs.edit()
            .putString(KEY_ENCRYPTED_API_KEY, encrypted)
            .putBoolean(KEY_IS_VALIDATED, isValidated)
            .putLong(KEY_LAST_SAVED, System.currentTimeMillis())
            .apply()
    }

    fun getApiKey(): String {
        val encrypted = prefs.getString(KEY_ENCRYPTED_API_KEY, null) ?: return ""
        return decrypt(encrypted)
    }

    fun hasApiKey(): Boolean {
        return getApiKey().isNotBlank()
    }

    fun isKeyValidated(): Boolean {
        return prefs.getBoolean(KEY_IS_VALIDATED, false) && hasApiKey()
    }

    fun setKeyValidated(validated: Boolean) {
        prefs.edit().putBoolean(KEY_IS_VALIDATED, validated).apply()
    }

    fun clearApiKey() {
        prefs.edit()
            .remove(KEY_ENCRYPTED_API_KEY)
            .remove(KEY_IS_VALIDATED)
            .remove(KEY_LAST_SAVED)
            .apply()
    }

    fun getMaskedApiKey(): String {
        val key = getApiKey()
        if (key.length <= 8) return if (key.isEmpty()) "Not Connected" else "••••••••"
        return "${key.take(4)}••••••••${key.takeLast(4)}"
    }

    companion object {
        private const val PREFS_NAME = "nova_secure_vault"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "NovaZoyaMasterKey"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"

        private const val KEY_ENCRYPTED_API_KEY = "enc_gemini_key"
        private const val KEY_IS_VALIDATED = "key_is_validated"
        private const val KEY_LAST_SAVED = "key_last_saved_timestamp"
    }
}
