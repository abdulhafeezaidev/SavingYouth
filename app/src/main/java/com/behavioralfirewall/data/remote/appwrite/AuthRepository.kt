package com.behavioralfirewall.data.remote.appwrite

import android.content.Context
import com.behavioralfirewall.domain.security.SecureStorageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import io.appwrite.services.Account
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clientProvider: AppwriteClientProvider,
    private val secureStorageManager: SecureStorageManager
) {
    private val _isUserLoggedIn = MutableStateFlow(false)
    val isUserLoggedIn = _isUserLoggedIn.asStateFlow()

    init {
        checkSessionState()
    }

    fun checkSessionState() {
        val hasSession = secureStorageManager.getToken("appwrite_session_id") != null
        _isUserLoggedIn.value = hasSession
    }

    suspend fun loginAnonymously(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val client = clientProvider.provideClient(context)
                val account = Account(client)
                
                val session = account.createAnonymousSession()
                
                // Save session securely
                secureStorageManager.saveToken("appwrite_session_id", session.id)
                _isUserLoggedIn.value = true
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun logout(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val client = clientProvider.provideClient(context)
                val account = Account(client)
                
                account.deleteSession(sessionId = "current")
                
                secureStorageManager.removeToken("appwrite_session_id")
                _isUserLoggedIn.value = false
                true
            } catch (e: Exception) {
                e.printStackTrace()
                secureStorageManager.removeToken("appwrite_session_id")
                _isUserLoggedIn.value = false
                false
            }
        }
    }
}
