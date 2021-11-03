package com.example.runningtracker.di

import android.content.Context
import androidx.room.Room
import com.example.runningtracker.repository.RunnerRepository
import com.example.runningtracker.repository.datastore.DataStoreManager
import com.example.runningtracker.repository.room.RunnerDao
import com.example.runningtracker.repository.room.RunnerDatabase
import com.example.runningtracker.utils.Constants.DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Singleton
    @Provides
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStoreManager {
        return DataStoreManager(context)
    }

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ): RunnerDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            RunnerDatabase::class.java,
            DATABASE_NAME
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideDao(
        runnerDatabase: RunnerDatabase
    ) = runnerDatabase.runnerDao()

    @Singleton
    @Provides
    fun provideRepository(runnerDao: RunnerDao) = RunnerRepository(runnerDao)
}
