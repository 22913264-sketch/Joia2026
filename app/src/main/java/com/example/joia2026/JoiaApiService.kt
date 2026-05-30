package com.example.joia2026

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface JoiaApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @GET("users/me")
    suspend fun getMe(@Header("Authorization") authorization: String): Response<User>

    @PATCH("users/me")
    suspend fun updateMe(
        @Header("Authorization") authorization: String,
        @Body request: UpdateProfileRequest
    ): Response<User>

    @GET("jogos")
    suspend fun getJogos(
        @Query("status") status: String? = null,
        @Query("modalidadeId") modalidadeId: String? = null,
        @Query("cursoId") cursoId: String? = null
    ): Response<List<Jogo>>

    @GET("jogos/{id}")
    suspend fun getJogo(@Path("id") id: String): Response<Jogo>

    @GET("cursos")
    suspend fun getCursos(): Response<List<Curso>>

    @GET("cursos/{id}")
    suspend fun getCurso(@Path("id") id: String): Response<Curso>

    @GET("modalidades")
    suspend fun getModalidades(): Response<List<Modalidade>>

    @GET("modalidades/{id}")
    suspend fun getModalidade(@Path("id") id: String): Response<Modalidade>

    @GET("equipes")
    suspend fun getEquipes(
        @Query("cursoId") cursoId: String? = null,
        @Query("modalidadeId") modalidadeId: String? = null,
        @Query("genero") genero: String? = null
    ): Response<List<Equipe>>

    @GET("equipes/{id}")
    suspend fun getEquipe(@Path("id") id: String): Response<Equipe>

    @POST("equipes")
    suspend fun createEquipe(@Body request: CreateEquipeRequest): Response<Equipe>

    @DELETE("equipes/{id}")
    suspend fun deleteEquipe(@Path("id") id: String): Response<Unit>

    @GET("ranking/geral")
    suspend fun getRankingGeral(): Response<List<RankingGeralItem>>

    @GET("ranking/modalidade/{id}")
    suspend fun getRankingModalidade(@Path("id") id: String): Response<List<RankingModalidadeItem>>

    @GET("favoritos")
    suspend fun getFavoritos(@Header("Authorization") authorization: String): Response<List<Jogo>>

    @POST("favoritos/{jogoId}")
    suspend fun addFavorito(
        @Header("Authorization") authorization: String,
        @Path("jogoId") jogoId: String
    ): Response<Unit>

    @DELETE("favoritos/{jogoId}")
    suspend fun deleteFavorito(
        @Header("Authorization") authorization: String,
        @Path("jogoId") jogoId: String
    ): Response<Unit>

    @PATCH("jogos/{id}/placar")
    suspend fun updatePlacar(@Path("id") id: String, @Body request: ScoreRequest): Response<Jogo>

    @POST("jogos/{id}/cartoes")
    suspend fun addCartao(@Path("id") id: String, @Body request: CartaoRequest): Response<Cartao>

    @POST("jogos/{id}/iniciar")
    suspend fun iniciarJogo(@Path("id") id: String): Response<Jogo>

    @POST("jogos/{id}/finalizar")
    suspend fun finalizarJogo(@Path("id") id: String): Response<Jogo>
}

object RetrofitClient {
    private const val BASE_URL = "https://utf60vh8hyb7y44yzsmiw0n1.187.127.5.61.sslip.io/"

    val instance: JoiaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(JoiaApiService::class.java)
    }
}
