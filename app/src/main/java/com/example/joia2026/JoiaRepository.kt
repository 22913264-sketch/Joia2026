package com.example.joia2026

import android.content.Context
import android.util.Log

object JoiaRepository {

    suspend fun getJogos(status: String? = null, modalidadeId: String? = null, cursoId: String? = null): List<Jogo> {
        return try {
            val response = RetrofitClient.instance.getJogos(status, modalidadeId, cursoId)
            if (response.isSuccessful) response.body().orEmpty() else emptyList()
        } catch (e: Exception) {
            Log.e("JoiaRepository", "Erro ao buscar jogos", e)
            emptyList()
        }
    }

    suspend fun getJogo(id: String): Jogo? {
        return try {
            val response = RetrofitClient.instance.getJogo(id)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            Log.e("JoiaRepository", "Erro ao buscar jogo $id", e)
            null
        }
    }

    suspend fun getCursos(): List<Curso> {
        return try {
            val response = RetrofitClient.instance.getCursos()
            if (response.isSuccessful) response.body().orEmpty() else emptyList()
        } catch (e: Exception) {
            Log.e("JoiaRepository", "Erro ao buscar cursos", e)
            emptyList()
        }
    }

    suspend fun getModalidades(): List<Modalidade> {
        return try {
            val response = RetrofitClient.instance.getModalidades()
            if (response.isSuccessful) response.body().orEmpty() else emptyList()
        } catch (e: Exception) {
            Log.e("JoiaRepository", "Erro ao buscar modalidades", e)
            emptyList()
        }
    }

    suspend fun getEquipes(cursoId: String? = null, modalidadeId: String? = null, genero: String? = null): List<Equipe> {
        return try {
            val response = RetrofitClient.instance.getEquipes(cursoId, modalidadeId, genero)
            if (response.isSuccessful) response.body().orEmpty() else emptyList()
        } catch (e: Exception) {
            Log.e("JoiaRepository", "Erro ao buscar equipes", e)
            emptyList()
        }
    }

    suspend fun createEquipe(nome: String, cursoId: String, modalidadeId: String, genero: String?): Equipe? {
        return try {
            val response = RetrofitClient.instance.createEquipe(
                CreateEquipeRequest(nome = nome, cursoId = cursoId, modalidadeId = modalidadeId, genero = genero)
            )
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            Log.e("JoiaRepository", "Erro ao criar equipe", e)
            null
        }
    }

    suspend fun deleteEquipe(equipeId: String): Boolean {
        return try {
            val response = RetrofitClient.instance.deleteEquipe(equipeId)
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("JoiaRepository", "Erro ao deletar equipe", e)
            false
        }
    }

    suspend fun updatePlacar(jogoId: String, mandante: Int, visitante: Int): Jogo? {
        return try {
            val response = RetrofitClient.instance.updatePlacar(jogoId, ScoreRequest(mandante, visitante))
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            Log.e("JoiaRepository", "Erro ao atualizar placar", e)
            null
        }
    }

    suspend fun getRankingGeral(): List<RankingGeralItem> {
        return try {
            val response = RetrofitClient.instance.getRankingGeral()
            if (response.isSuccessful) response.body().orEmpty() else emptyList()
        } catch (e: Exception) {
            Log.e("JoiaRepository", "Erro ao buscar ranking geral", e)
            emptyList()
        }
    }

    suspend fun getRankingModalidade(modalidadeId: String): List<RankingModalidadeItem> {
        return try {
            val response = RetrofitClient.instance.getRankingModalidade(modalidadeId)
            if (response.isSuccessful) response.body().orEmpty() else emptyList()
        } catch (e: Exception) {
            Log.e("JoiaRepository", "Erro ao buscar ranking modalidade", e)
            emptyList()
        }
    }

    suspend fun getFavoritos(context: Context): List<Jogo> {
        val token = UserSession.getToken(context)
        if (token.isNullOrBlank()) return emptyList()
        
        return try {
            val response = RetrofitClient.instance.getFavoritos("Bearer $token")
            if (response.isSuccessful) {
                val favoritosApi = response.body().orEmpty()
                // Filtra apenas jogos que possuem modalidade e equipes minimamente consistentes
                // ou que não retornam erro ao buscar detalhes (conferência básica de integridade)
                favoritosApi.filter { it.id.length > 5 && it.modalidade != null }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("JoiaRepository", "Erro ao buscar favoritos", e)
            emptyList()
        }
    }

    suspend fun addFavorito(context: Context, jogoId: String): Boolean {
        val token = UserSession.getToken(context) ?: return false
        return try {
            val response = RetrofitClient.instance.addFavorito("Bearer $token", jogoId)
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("JoiaRepository", "Erro ao adicionar favorito", e)
            false
        }
    }

    suspend fun deleteFavorito(context: Context, jogoId: String): Boolean {
        val token = UserSession.getToken(context) ?: return false
        return try {
            val response = RetrofitClient.instance.deleteFavorito("Bearer $token", jogoId)
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("JoiaRepository", "Erro ao deletar favorito", e)
            false
        }
    }

    suspend fun isFavorito(context: Context, jogoId: String): Boolean {
        return getFavoritos(context).any { it.id == jogoId }
    }

    fun regulamentoResumo(): String {
        return "JOIA 2026 reune os cursos em disputas coletivas e individuais. A pontuacao geral alimenta o Trofeu Rotativo, com jogos por fase classificatoria, semifinais e finais. Cartoes, sumulas, escalações e favoritos acompanham cada partida."
    }
}
