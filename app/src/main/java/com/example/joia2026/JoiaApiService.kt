package com.example.joia2026

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface JoiaApiService {
    @GET("jogos")
    suspend fun getJogos(
        @Query("status") status: String? = null,
        @Query("modalidadeId") modalidadeId: String? = null
    ): Response<List<Jogo>>

    @GET("ranking/geral")
    suspend fun getRankingGeral(): Response<List<Map<String, Any>>>
}

object RetrofitClient {
    private const val BASE_URL = "https://utf60vh8hyb7y44yzsmiw0n1.187.127.5.61.sslip.io/"

    val instance: JoiaApiService by lazy {
        Retrofit.Builder( )
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(JoiaApiService::class.java)
    }
}