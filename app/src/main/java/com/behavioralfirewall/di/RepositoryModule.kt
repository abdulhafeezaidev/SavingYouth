package com.behavioralfirewall.di

import com.behavioralfirewall.data.repository.BehavioralRepositoryImpl
import com.behavioralfirewall.domain.repository.BehavioralRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBehavioralRepository(
        behavioralRepositoryImpl: BehavioralRepositoryImpl
    ): BehavioralRepository
}
