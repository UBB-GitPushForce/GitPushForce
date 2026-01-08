package com.example.budgeting.android.data.network

import android.util.Log
import com.example.budgeting.android.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private val BASE_URL: String = BuildConfig.BASE_URL

    init {
        Log.d("RetrofitClient", "Base URL: $BASE_URL")
    }

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    fun getMoshi(): Moshi = moshi
    
    private val moshiConverterFactory = MoshiConverterFactory.create(moshi)
        .asLenient()

    val authInstance: AuthApiService = run {
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

    val expenseInstance: ExpenseApiService = run {
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

    val groupInstance: GroupApiService = run {
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

    val userInstance: UserApiService = run {
        val client = OkHttpClient.Builder()
            .addInterceptor(TokenAuthInterceptor())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(moshiConverterFactory)
            .build()

        retrofit.create(UserApiService::class.java)
    }

    val expensePaymentInstance: ExpensePaymentApiService = run {
        val client = OkHttpClient.Builder()
            .addInterceptor(TokenAuthInterceptor())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(moshiConverterFactory)
            .build()

        retrofit.create(ExpensePaymentApiService::class.java)
    }

    val categoryInstance: CategoryApiService = run {
        val client = OkHttpClient.Builder()
            .addInterceptor(TokenAuthInterceptor())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(moshiConverterFactory)
            .build()

        retrofit.create(CategoryApiService::class.java)
    }

    val receiptInstance: ReceiptApiService = run {
        val client = OkHttpClient.Builder()
            .addInterceptor(TokenAuthInterceptor())
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(moshiConverterFactory)
            .build()

        retrofit.create(ReceiptApiService::class.java)
    }
}
