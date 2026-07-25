package com.batdaulaptrinh.freeudemycoupons.di

import android.content.Context
import com.batdaulaptrinh.freeudemycoupons.util.NetworkUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UtilModule {
    @Provides
    @Singleton
    fun provideNetworkUtils(@ApplicationContext context: Context): NetworkUtil {
        return NetworkUtil(context)
    }
}
