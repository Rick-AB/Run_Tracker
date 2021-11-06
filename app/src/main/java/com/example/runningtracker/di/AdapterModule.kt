package com.example.runningtracker.di

import android.content.Context
import com.example.runningtracker.ui.adapters.HomeRecyclerViewAdapter
import com.example.runningtracker.utils.TimestampMillisecondsFormatter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped


@InstallIn(ActivityComponent::class)
@Module
object AdapterModule {

    @ActivityScoped
    @Provides
    fun provideAdapter(
        @ApplicationContext context: Context,
        timestampMillisecondsFormatter: TimestampMillisecondsFormatter
    ) = HomeRecyclerViewAdapter(context, timestampMillisecondsFormatter)
}