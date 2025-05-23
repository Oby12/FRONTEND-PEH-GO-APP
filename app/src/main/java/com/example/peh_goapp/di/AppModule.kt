package com.example.peh_goapp.di

import android.content.Context
import com.example.peh_goapp.data.local.TokenPreference
import com.example.peh_goapp.data.remote.api.ApiService
import com.example.peh_goapp.util.ResourceProvider
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = "https://7110-114-10-98-181.ngrok-free.app/"

//    @Provides
//    @Singleton
//    fun provideOkHttpClient(): OkHttpClient {
//        return OkHttpClient.Builder()
//            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
//            .connectTimeout(120, TimeUnit.SECONDS)
//            .readTimeout(120, TimeUnit.SECONDS)
//            .build()
//    }
//
//    @Provides
//    @Singleton
//    fun provideApiService(okHttpClient: OkHttpClient): ApiService {
//        val gson = GsonBuilder()
//            .setLenient()
//            .create()
//
//        val retrofit = Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create(gson))
//            .client(okHttpClient)
//            .build()
//
//        return retrofit.create(ApiService::class.java)
//    }

    @Provides
    @Singleton
    fun provideTokenPreference(@ApplicationContext context: Context): TokenPreference {
        return TokenPreference(context)
    }

    @Provides
    @Singleton
    fun provideResourceProvider(@ApplicationContext context: Context): ResourceProvider {
        return ResourceProvider(context)
    }
}