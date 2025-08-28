package com.example.peh_goapp.di

import android.content.ContentResolver
import android.content.Context
import com.example.peh_goapp.data.local.TokenPreference
import com.example.peh_goapp.data.remote.api.ApiService
import com.example.peh_goapp.data.remote.api.OtherApiService
import com.example.peh_goapp.data.repository.DestinationRepository
import com.example.peh_goapp.data.repository.FavoriteRepository
import com.example.peh_goapp.data.repository.OtherRepository
import com.example.peh_goapp.data.repository.StatsRepository
import com.example.peh_goapp.data.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

//    @Provides
//    @Singleton
//    fun provideTokenPreference(@ApplicationContext context: Context): TokenPreference {
//        return TokenPreference(context)
//    }

    @Provides
    @Singleton
    fun provideUserRepository(
        apiService: ApiService,
        tokenPreference: TokenPreference
    ): UserRepository {
        return UserRepository(apiService, tokenPreference)
    }

    @Provides
    @Singleton
    fun provideDestinationRepository(
        apiService: ApiService,
        tokenPreference: TokenPreference,
        @ApplicationContext context: Context // Tambahkan Context ke parameter
    ): DestinationRepository {
        return DestinationRepository(apiService, tokenPreference, context)
    }

    @Provides
    @Singleton
    fun provideStatsRepository(
        apiService: ApiService,
        tokenPreference: TokenPreference
    ): StatsRepository {
        return StatsRepository(apiService, tokenPreference)
    }

    @Provides
    @Singleton
    fun provideFavoriteRepository(
        apiService: ApiService,
        tokenPreference: TokenPreference
    ): FavoriteRepository {
        return FavoriteRepository(apiService, tokenPreference)
    }

    // ADD OTHER REPOSITORY
    @Provides
    @Singleton
    fun provideOtherRepository(
        apiService: ApiService, // Use main ApiService
        tokenPreference: TokenPreference,
        @ApplicationContext context: Context
    ): OtherRepository {
        return OtherRepository(
            apiService, // Changed from otherApiService to apiService
            tokenPreference,
            context.contentResolver
        )
    }

    // ADD CONTENT RESOLVER PROVIDER
    @Provides
    @Singleton
    fun provideContentResolver(
        @ApplicationContext context: Context
    ): ContentResolver {
        return context.contentResolver
    }
}