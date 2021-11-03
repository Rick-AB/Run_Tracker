package com.example.runningtracker.di

import com.example.runningtracker.utils.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object StopWatchModule {

    @Singleton
    @DefaultDispatcher
    @Provides
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Singleton
    @Provides
    fun provideStopWatchOrchestrator(
        stateHolder: StopWatchStateHolder,
        @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher
    ): StopWatchOrchestrator {
        return StopWatchOrchestrator(
            stateHolder,
            CoroutineScope(coroutineDispatcher)
        )
    }

    @Singleton
    @Provides
    fun provideTimeStampProvider() = TimeStampProvider()

    @Singleton
    @Provides
    fun provideStopWatchStateCalculator(
        timeStampProvider: TimeStampProvider,
        elapsedTimeCalculator: ElapsedTimeCalculator
    ) = StopWatchStateCalculator(timeStampProvider, elapsedTimeCalculator)

    @Singleton
    @Provides
    fun provideStopWatchStateHolder(
        stopWatchStateCalculator: StopWatchStateCalculator,
        elapsedTimeCalculator: ElapsedTimeCalculator,
        timestampMillisecondsFormatter: TimestampMillisecondsFormatter
    ) = StopWatchStateHolder(
        stopWatchStateCalculator,
        elapsedTimeCalculator,
        timestampMillisecondsFormatter
    )

    @Singleton
    @Provides
    fun provideTimeStampMillisecondsFormatter() = TimestampMillisecondsFormatter()

    @Singleton
    @Provides
    fun provideElapsedTimeCalculator(
        timeStampProvider: TimeStampProvider
    ) = ElapsedTimeCalculator(timeStampProvider)
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher