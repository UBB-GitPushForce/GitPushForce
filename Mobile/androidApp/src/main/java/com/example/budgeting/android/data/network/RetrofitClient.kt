package com.example.budgeting.android.data.network

import com.example.budgeting.android.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitClient {

    // Extracts the base URL from the BuildConfig at build time.
    private const val BASE_URL = BuildConfig.BASE_URL

    // Moshi is a modern JSON library for parsing JSON into Kotlin objects.
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // A lazy delegate means the Retrofit instance is created only once, the first time it's needed.
    val authInstance: AuthApiService by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor(TokenAuthInterceptor())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        retrofit.create(AuthApiService::class.java)
    }

    val expenseInstance: ExpenseApiService by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor(TokenAuthInterceptor())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        retrofit.create(ExpenseApiService::class.java)
    }
}

