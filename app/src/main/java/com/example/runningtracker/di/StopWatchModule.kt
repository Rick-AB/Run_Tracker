package com.example.runningtracker.di

import com.example.runningtracker.utils.StopWatchOrchestrator
import com.example.runningtracker.utils.StopWatchStateHolder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier

@InstallIn(SingletonComponent::class)
@Module
object StopWatchModule {

    @DefaultDispatcher
    @Provides
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    fun provideStopWatchOrchestrator(
        stateHolder: StopWatchStateHolder,
        @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher
    ): StopWatchOrchestrator {
        return StopWatchOrchestrator(
            stateHolder,
            CoroutineScope(SupervisorJob() + coroutineDispatcher)
        )
    }
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher