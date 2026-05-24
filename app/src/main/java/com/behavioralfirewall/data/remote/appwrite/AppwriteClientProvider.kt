package com.behavioralfirewall.data.remote.appwrite

import android.content.Context
import com.behavioralfirewall.BuildConfig
import io.appwrite.Client
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppwriteClientProvider @Inject constructor() {
    
    fun provideClient(context: Context): Client {
        return Client(context)
            .setEndpoint("https://cloud.appwrite.io/v1") // TODO: use environment variable
            .setProject(BuildConfig.APPWRITE_PROJECT_ID)
    }
}
