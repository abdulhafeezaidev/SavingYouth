package com.behavioralfirewall.domain.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureStorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val keyAlias = "BehavioralFirewallKeyAlias"
    private val providerName = "AndroidKeyStore"
    private val prefFileName = "secure_behavioral_prefs"
    
    private val sharedPreferences = context.getSharedPreferences(prefFileName, Context.MODE_PRIVATE)

    init {
        generateSecretKeyIfNeeded()
    }

    private fun generateSecretKeyIfNeeded() {
        try {
            val keyStore = KeyStore.getInstance(providerName).apply { load(null) }
            if (!keyStore.containsAlias(keyAlias)) {
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, providerName)
                val spec = KeyGenParameterSpec.Builder(
                    keyAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
                keyGenerator.init(spec)
                keyGenerator.generateKey()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(providerName).apply { load(null) }
        return (keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry).secretKey
    }

    fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return ""
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            
            // Combine IV and Encrypted Bytes: IV (12 bytes) + EncryptedBytes
            val combined = ByteArray(iv.size + encryptedBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)
            Base64.encodeToString(combined, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun decrypt(cipherText: String): String {
        if (cipherText.isEmpty()) return ""
        return try {
            val combined = Base64.decode(cipherText, Base64.DEFAULT)
            val ivSize = 12 // GCM default IV size
            val iv = ByteArray(ivSize)
            val encryptedBytes = ByteArray(combined.size - ivSize)
            
            System.arraycopy(combined, 0, iv, 0, ivSize)
            System.arraycopy(combined, ivSize, encryptedBytes, 0, encryptedBytes.size)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
            String(cipher.doFinal(encryptedBytes), Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun getDatabasePassphrase(): String {
        var encryptedKey = sharedPreferences.getString("db_encryption_key_v2", null)
        if (encryptedKey == null) {
            val bytes = ByteArray(32)
            SecureRandom().nextBytes(bytes)
            val rawPassphrase = Base64.encodeToString(bytes, Base64.NO_WRAP)
            encryptedKey = encrypt(rawPassphrase)
            sharedPreferences.edit().putString("db_encryption_key_v2", encryptedKey).apply()
            return rawPassphrase
        }
        val decrypted = decrypt(encryptedKey)
        if (decrypted.isEmpty()) {
            // Fallback if decryption fails
            val bytes = ByteArray(32)
            SecureRandom().nextBytes(bytes)
            val rawPassphrase = Base64.encodeToString(bytes, Base64.NO_WRAP)
            encryptedKey = encrypt(rawPassphrase)
            sharedPreferences.edit().putString("db_encryption_key_v2", encryptedKey).apply()
            return rawPassphrase
        }
        return decrypted
    }

    fun saveToken(key: String, token: String) {
        val encrypted = encrypt(token)
        sharedPreferences.edit().putString(key, encrypted).apply()
    }

    fun getToken(key: String): String? {
        val encrypted = sharedPreferences.getString(key, null) ?: return null
        return try {
            val decrypted = decrypt(encrypted)
            if (decrypted.isEmpty()) null else decrypted
        } catch (e: Exception) {
            null
        }
    }

    fun removeToken(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    fun saveBooleanSetting(key: String, value: Boolean) {
        val encrypted = encrypt(value.toString())
        sharedPreferences.edit().putString(key, encrypted).apply()
    }

    fun getBooleanSetting(key: String, default: Boolean): Boolean {
        val encrypted = sharedPreferences.getString(key, null) ?: return default
        return try {
            val decrypted = decrypt(encrypted)
            if (decrypted.isEmpty()) default else decrypted.toBoolean()
        } catch (e: Exception) {
            default
        }
    }
}
