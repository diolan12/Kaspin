package com.syscode.kaspin.data.network.service

import android.util.Log
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.syscode.kaspin.data.database.model.Product
import com.syscode.kaspin.data.network.dto.ProductDTO
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

const val FAKE_STORE_BASE_URL = "https://fakestoreapi.com/"

interface FakeStoreService {
    @GET("products?limit=10")
    suspend fun asyncGetAllProducts(): ArrayList<ProductDTO>

    companion object {
        operator fun invoke(): FakeStoreService {
            val requestInterceptor = Interceptor {chain ->
                val url = chain.request().url()
                val request = chain.request()
                    .newBuilder()
//                    .addHeader("Authorization", "Bearer ${credentialManager.get().jwt}")
                    .url(url)
                    .build()
                return@Interceptor chain.proceed(request).also {
                    Log.i("PostService", it.toString())
                }
            }
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(requestInterceptor)
                .build()
            return Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(FAKE_STORE_BASE_URL)
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(FakeStoreService::class.java)
        }
    }
    sealed class State {
        object Idle: State()
        object Running: State()
        data class Success(val data: ArrayList<ProductDTO>): State()
        data class Error(val exception: Exception): State()
    }
}