package com.behavioralfirewall.domain.security

import com.behavioralfirewall.domain.repository.BehavioralRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureBackupManager @Inject constructor(
    private val secureStorageManager: SecureStorageManager,
    private val repository: BehavioralRepository
) {

    /**
     * Generates a secure, AES-encrypted, checksum-validated backup payload of user streaks and settings.
     */
    suspend fun generateBackupPayload(): String {
        return withContext(Dispatchers.IO) {
            try {
                val backupObj = JSONObject()
                val streak = repository.getStreak().firstOrNull()
                
                if (streak != null) {
                    val streakJson = JSONObject().apply {
                        put("streakStartDate", streak.streakStartDate)
                        put("currentDays", streak.currentDays)
                        put("longestDays", streak.longestDays)
                        put("lastRelapseAt", streak.lastRelapseAt)
                        put("recoveryScore", streak.recoveryScore)
                    }
                    backupObj.put("streak", streakJson)
                }

                backupObj.put("backupTimestamp", System.currentTimeMillis())
                val rawString = backupObj.toString()
                
                // Add an HMAC/checksum to verify integrity on restore
                val checksum = calculateSha256(rawString)
                val payloadObj = JSONObject().apply {
                    put("encryptedData", secureStorageManager.encrypt(rawString))
                    put("checksum", checksum)
                }
                
                payloadObj.toString()
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }
    }

    /**
     * Restores recovery configurations from an encrypted payload, validating data integrity.
     */
    suspend fun restoreBackupPayload(payload: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val payloadObj = JSONObject(payload)
                val encryptedData = payloadObj.getString("encryptedData")
                val expectedChecksum = payloadObj.getString("checksum")

                val decryptedData = secureStorageManager.decrypt(encryptedData)
                if (decryptedData.isEmpty()) return@withContext false

                // Validate integrity: ensure it is not corrupted or tampered with
                val calculatedChecksum = calculateSha256(decryptedData)
                if (calculatedChecksum != expectedChecksum) {
                    return@withContext false // Data tampered or corrupted
                }

                val dataObj = JSONObject(decryptedData)
                if (dataObj.has("streak")) {
                    val streakJson = dataObj.getJSONObject("streak")
                    val existingStreak = repository.getStreak().firstOrNull()
                    
                    if (existingStreak != null) {
                        val restoredStreak = existingStreak.copy(
                            streakStartDate = streakJson.getLong("streakStartDate"),
                            currentDays = streakJson.getInt("currentDays"),
                            longestDays = streakJson.getInt("longestDays"),
                            lastRelapseAt = streakJson.getLong("lastRelapseAt"),
                            recoveryScore = streakJson.getInt("recoveryScore"),
                            lastUpdated = System.currentTimeMillis()
                        )
                        repository.saveStreak(restoredStreak)
                    }
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    private fun calculateSha256(input: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }
}
