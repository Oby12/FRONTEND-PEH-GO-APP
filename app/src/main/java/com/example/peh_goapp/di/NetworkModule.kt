package com.example.peh_goapp.di

import com.example.peh_goapp.data.local.TokenPreference
import com.example.peh_goapp.data.remote.api.ApiConfig
import com.example.peh_goapp.data.remote.api.ApiService
import com.example.peh_goapp.data.remote.api.Base64ImageApi
import com.example.peh_goapp.data.remote.api.Base64ImageService
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Menggunakan BASE_URL dari ApiConfig untuk konsistensi
    private val BASE_URL = ApiConfig.BASE_URL

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            // Set ke BODY untuk melihat semua detail request dan response
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            // Tingkatkan timeout untuk stabilitas koneksi
            .connectTimeout(180, TimeUnit.SECONDS)  // 3 menit
            .readTimeout(180, TimeUnit.SECONDS)     // 3 menit
            .writeTimeout(180, TimeUnit.SECONDS)    // 3 menit
            .retryOnConnectionFailure(true)         // Retry jika koneksi gagal
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    // Tambahan untuk API Base64
    @Provides
    @Singleton
    fun provideBase64ImageApi(retrofit: Retrofit): Base64ImageApi {
        return retrofit.create(Base64ImageApi::class.java)
    }

    // Service untuk gambar Base64
    @Provides
    @Singleton
    fun provideBase64ImageService(api: Base64ImageApi, tokenPreference: TokenPreference): Base64ImageService {
        return Base64ImageService(api, tokenPreference)
    }
}