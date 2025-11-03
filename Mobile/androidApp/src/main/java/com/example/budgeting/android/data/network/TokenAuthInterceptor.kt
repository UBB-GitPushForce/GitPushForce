package com.example.budgeting.android.data.network

import com.example.budgeting.android.data.auth.TokenHolder
import okhttp3.Interceptor
import okhttp3.Response

class TokenAuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = TokenHolder.token
        val request = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }
        return chain.proceed(request)
    }
}


