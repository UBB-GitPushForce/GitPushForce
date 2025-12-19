package com.example.budgeting.android.data.network

import com.example.budgeting.android.data.model.ReceiptProcessResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ReceiptApiService {
    @Multipart
    @POST("/receipt/process-receipt")
    suspend fun processReceipt(@Part image: MultipartBody.Part): Response<ReceiptProcessResponse>
}