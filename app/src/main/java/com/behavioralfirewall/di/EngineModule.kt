package com.behavioralfirewall.di

import com.behavioralfirewall.domain.engine.AppMonitoringManager
import com.behavioralfirewall.domain.engine.AppMonitoringManagerImpl
import com.behavioralfirewall.domain.engine.RiskEngine
import com.behavioralfirewall.domain.engine.RiskEngineImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class EngineModule {

    @Binds
    @Singleton
    abstract fun bindRiskEngine(
        riskEngineImpl: RiskEngineImpl
    ): RiskEngine

    @Binds
    @Singleton
    abstract fun bindAppMonitoringManager(
        appMonitoringManagerImpl: AppMonitoringManagerImpl
    ): AppMonitoringManager
}
