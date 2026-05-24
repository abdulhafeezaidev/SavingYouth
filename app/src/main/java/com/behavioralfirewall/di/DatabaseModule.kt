package com.behavioralfirewall.di

import android.content.Context
import androidx.room.Room
import com.behavioralfirewall.data.local.dao.BehavioralDao
import com.behavioralfirewall.data.local.database.AppDatabase
import com.behavioralfirewall.domain.security.SecureStorageManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        secureStorageManager: SecureStorageManager,
        performanceValidator: com.behavioralfirewall.domain.release.FinalPerformanceValidator
    ): AppDatabase {
        val start = System.currentTimeMillis()
        
        val passphrase = secureStorageManager.getDatabasePassphrase()
        val factory = SupportFactory(passphrase.toByteArray(Charsets.UTF_8))
        
        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "behavioral_firewall.db"
        )
        .openHelperFactory(factory)
        .fallbackToDestructiveMigration()
        .build()
        
        val duration = System.currentTimeMillis() - start
        performanceValidator.auditDatabaseLatency("Database Initialization", duration)
        
        return db
    }

    @Provides
    fun provideBehavioralDao(database: AppDatabase): BehavioralDao {
        return database.behavioralDao()
    }
}
