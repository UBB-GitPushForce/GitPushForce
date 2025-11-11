package com.example.budgeting.android.data.network

import com.example.budgeting.android.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitClient {
    
    // Load environment variables from .env file using dotenv
    private val BASE_URL: String = BuildConfig.BASE_URL

    // Moshi is a modern JSON library for parsing JSON into Kotlin objects.
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    
    // Create Moshi converter factory
    private val moshiConverterFactory = MoshiConverterFactory.create(moshi)

    // A lazy delegate means the Retrofit instance is created only once, the first time it's needed.
    val authInstance: AuthApiService by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor(TokenAuthInterceptor())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(moshiConverterFactory)
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
            .addConverterFactory(moshiConverterFactory)
            .build()

        retrofit.create(ExpenseApiService::class.java)
    }

    val groupInstance: GroupApiService by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor(TokenAuthInterceptor())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(moshiConverterFactory)
            .build()

        retrofit.create(GroupApiService::class.java)
    }

}

