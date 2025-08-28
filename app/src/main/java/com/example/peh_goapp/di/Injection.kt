package com.example.peh_goapp.di

import android.content.Context
import com.example.peh_goapp.data.local.TokenPreference
import com.example.peh_goapp.data.remote.api.ApiService
import com.example.peh_goapp.data.repository.UserRepository
import com.example.peh_goapp.ui.screen.login.LoginViewModel
import com.example.peh_goapp.util.ResourceProvider
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object Injection {

    private const val BASE_URL = "https://3aa27ba53912.ngrok-free.app/" // Ganti dengan URL API yang sebenarnya

    private fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()
    }

    private fun provideApiService(): ApiService {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(provideOkHttpClient())
            .build()

        return retrofit.create(ApiService::class.java)
    }

    private fun provideTokenPreference(context: Context): TokenPreference {
        return TokenPreference(context)
    }

    // Menambahkan provider untuk ResourceProvider
    private fun provideResourceProvider(context: Context): ResourceProvider {
        return ResourceProvider(context)
    }

    private fun provideUserRepository(context: Context): UserRepository {
        return UserRepository(
            apiService = provideApiService(),
            tokenPreference = provideTokenPreference(context)
        )
    }

    fun provideLoginViewModel(context: Context): LoginViewModel {
        return LoginViewModel(
            userRepository = provideUserRepository(context),
            tokenPreference = provideTokenPreference(context),
            resourceProvider = provideResourceProvider(context) // Menambahkan parameter ResourceProvider
        )
    }
}