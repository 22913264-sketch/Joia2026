package com.example.joia2026

import android.content.Context

object JoiaRepository {
    private val cursosDemo = listOf(
        Curso("1", "Sistemas de Informacao", "SI", 42, 1),
        Curso("2", "Direito", "DIR", 35, 2),
        Curso("3", "Enfermagem", "ENF", 28, 3),
        Curso("4", "Administracao", "ADM", 24, 4)
    )

    private val modalidadesDemo = listOf(
        Modalidade("1", "Futsal", "Coletiva", "Disputa em quadra com duas equipes.", "Dois tempos de 20 minutos.", 1, 8, 16),
        Modalidade("2", "Voleibol", "Coletiva", "Partidas em melhor de tres sets.", "Sets de 25 pontos.", 1, 6, 10),
        Modalidade("3", "Xadrez", "Individual", "Sistema suiço entre atletas inscritos.", "Ritmo rapido.", 4, 14, 24)
    )

    private val equipesDemo = mutableListOf(
        Equipe("1", "SI Alpha", cursosDemo[0], modalidadesDemo[0], "MASCULINO", atletasDemo("SI", true)),
        Equipe("2", "Direito FC", cursosDemo[1], modalidadesDemo[0], "MASCULINO", atletasDemo("DIR", false)),
        Equipe("3", "Enfermagem Volei", cursosDemo[2], modalidadesDemo[1], "FEMININO", atletasDemo("ENF", true)),
        Equipe("4", "ADM Volei", cursosDemo[3], modalidadesDemo[1], "FEMININO", atletasDemo("ADM", false))
    )

    private val jogosDemo = mutableListOf(
        Jogo("1", iniciaEm = "2026-06-01T18:00:00Z", local = "Ginasio principal", fase = "CLASSIFICATORIA", status = "EM_ANDAMENTO", placarMandante = 2, placarVisitante = 1, mandante = equipesDemo[0], visitante = equipesDemo[1], modalidade = modalidadesDemo[0], cartoes = listOf(Cartao("1", "AMARELO", equipesDemo[0].atletas.first(), "Reclamacao"))),
        Jogo("2", iniciaEm = "2026-06-01T20:00:00Z", local = "Quadra 2", fase = "CLASSIFICATORIA", status = "AGENDADO", mandante = equipesDemo[2], visitante = equipesDemo[3], modalidade = modalidadesDemo[1]),
        Jogo("3", iniciaEm = "2026-06-02T19:00:00Z", local = "Sala multiuso", fase = "SEMI_FINAL", status = "AGENDADO", mandante = equipesDemo[0], visitante = equipesDemo[3], modalidade = modalidadesDemo[2]),
        Jogo("4", iniciaEm = "2026-05-30T14:00:00Z", local = "Ginasio principal", fase = "CLASSIFICATORIA", status = "FINALIZADO", placarMandante = 3, placarVisitante = 2, mandante = equipesDemo[1], visitante = equipesDemo[0], modalidade = modalidadesDemo[0])
    )

    suspend fun getJogos(status: String? = null, modalidadeId: String? = null, cursoId: String? = null): List<Jogo> {
        return try {
            val response = RetrofitClient.instance.getJogos(status, modalidadeId, cursoId)
            if (response.isSuccessful) response.body().orEmpty() else fallbackJogos(status, modalidadeId, cursoId)
        } catch (_: Exception) {
            fallbackJogos(status, modalidadeId, cursoId)
        }
    }

    suspend fun getCursos(): List<Curso> {
        return try {
            val response = RetrofitClient.instance.getCursos()
            if (response.isSuccessful) response.body().orEmpty() else cursosDemo
        } catch (_: Exception) {
            cursosDemo
        }
    }

    suspend fun getModalidades(): List<Modalidade> {
        return try {
            val response = RetrofitClient.instance.getModalidades()
            if (response.isSuccessful) response.body().orEmpty() else modalidadesDemo
        } catch (_: Exception) {
            modalidadesDemo
        }
    }

    suspend fun getEquipes(cursoId: String? = null, modalidadeId: String? = null, genero: String? = null): List<Equipe> {
        return try {
            val response = RetrofitClient.instance.getEquipes(cursoId, modalidadeId, genero)
            if (response.isSuccessful) {
                mergeEquipes(response.body().orEmpty()).filter { equipe ->
                    (cursoId == null || equipe.curso?.id == cursoId) &&
                        (modalidadeId == null || equipe.modalidade?.id == modalidadeId) &&
                        (genero == null || equipe.genero == genero)
                }
            } else {
                fallbackEquipes(cursoId, modalidadeId, genero)
            }
        } catch (_: Exception) {
            fallbackEquipes(cursoId, modalidadeId, genero)
        }
    }

    suspend fun createEquipe(nome: String, cursoId: String, modalidadeId: String, genero: String?): Equipe {
        val curso = cursosDemo.find { it.id == cursoId } ?: cursosDemo.first()
        val modalidade = modalidadesDemo.find { it.id == modalidadeId } ?: modalidadesDemo.first()
        val equipe = Equipe(
            id = "local-${System.currentTimeMillis()}",
            nome = nome,
            curso = curso,
            modalidade = modalidade,
            genero = genero,
            atletas = emptyList()
        )

        equipesDemo.add(equipe)

        try {
            val response = RetrofitClient.instance.createEquipe(
                CreateEquipeRequest(nome = nome, cursoId = cursoId, modalidadeId = modalidadeId, genero = genero)
            )
            if (response.isSuccessful) {
                response.body()?.let { apiEquipe ->
                    equipesDemo.removeAll { it.id == equipe.id }
                    equipesDemo.add(apiEquipe)
                    return apiEquipe
                }
            }
        } catch (_: Exception) {
        }

        return equipe
    }

    suspend fun deleteEquipe(equipeId: String): Boolean {
        equipesDemo.removeAll { it.id == equipeId }
        return try {
            val response = RetrofitClient.instance.deleteEquipe(equipeId)
            response.isSuccessful
        } catch (_: Exception) {
            true
        }
    }

    suspend fun updatePlacar(jogoId: String, mandante: Int, visitante: Int): Jogo? {
        val index = jogosDemo.indexOfFirst { it.id == jogoId }
        if (index >= 0) {
            jogosDemo[index] = jogosDemo[index].copy(
                placarMandante = mandante,
                placarVisitante = visitante,
                status = "EM_ANDAMENTO"
            )
        }

        return try {
            val response = RetrofitClient.instance.updatePlacar(jogoId, ScoreRequest(mandante, visitante))
            if (response.isSuccessful) response.body() else jogosDemo.getOrNull(index)
        } catch (_: Exception) {
            jogosDemo.getOrNull(index)
        }
    }

    suspend fun getRankingGeral(): List<RankingGeralItem> {
        return try {
            val response = RetrofitClient.instance.getRankingGeral()
            if (response.isSuccessful) response.body().orEmpty() else rankingDemo()
        } catch (_: Exception) {
            rankingDemo()
        }
    }

    suspend fun getRankingModalidade(modalidadeId: String): List<RankingModalidadeItem> {
        return try {
            val response = RetrofitClient.instance.getRankingModalidade(modalidadeId)
            if (response.isSuccessful) response.body().orEmpty() else rankingModalidadeDemo(modalidadeId)
        } catch (_: Exception) {
            rankingModalidadeDemo(modalidadeId)
        }
    }

    suspend fun getFavoritos(context: Context): List<Jogo> {
        val token = UserSession.getToken(context)
        if (!token.isNullOrBlank()) {
            try {
                val response = RetrofitClient.instance.getFavoritos("Bearer $token")
                if (response.isSuccessful) return response.body().orEmpty()
            } catch (_: Exception) {
            }
        }
        return jogosDemo.take(2)
    }

    fun regulamentoResumo(): String {
        return "JOIA 2026 reune os cursos em disputas coletivas e individuais. A pontuacao geral alimenta o Trofeu Rotativo, com jogos por fase classificatoria, semifinais e finais. Cartoes, sumulas, escalações e favoritos acompanham cada partida."
    }

    private fun fallbackJogos(status: String?, modalidadeId: String?, cursoId: String?): List<Jogo> {
        return jogosDemo.filter { jogo ->
            (status == null || jogo.status == status) &&
                (modalidadeId == null || jogo.modalidade?.id == modalidadeId) &&
                (cursoId == null || jogo.mandante?.curso?.id == cursoId || jogo.visitante?.curso?.id == cursoId)
        }
    }

    private fun fallbackEquipes(cursoId: String?, modalidadeId: String?, genero: String?): List<Equipe> {
        return equipesDemo.filter { equipe ->
            (cursoId == null || equipe.curso?.id == cursoId) &&
                (modalidadeId == null || equipe.modalidade?.id == modalidadeId) &&
                (genero == null || equipe.genero == genero)
        }
    }

    private fun mergeEquipes(apiEquipes: List<Equipe>): List<Equipe> {
        val apiIds = apiEquipes.map { it.id }.toSet()
        return apiEquipes + equipesDemo.filter { it.id !in apiIds }
    }

    private fun rankingDemo(): List<RankingGeralItem> {
        return cursosDemo.sortedBy { it.posicao }.map {
            RankingGeralItem(it.posicao ?: 0, it, it.nome, it.sigla, it.pontos ?: 0)
        }
    }

    private fun rankingModalidadeDemo(modalidadeId: String): List<RankingModalidadeItem> {
        return equipesDemo.filter { it.modalidade?.id == modalidadeId }.mapIndexed { index, equipe ->
            RankingModalidadeItem(index + 1, equipe, equipe.curso, jogos = 3, vitorias = 2 - index.coerceAtMost(1), empates = index, derrotas = index, golsPro = 7 - index, golsContra = 3 + index, saldo = 4 - index, pontos = 6 - index)
        }
    }

    private fun atletasDemo(prefixo: String, primeiroCapitao: Boolean): List<Atleta> {
        return listOf(
            Atleta("$prefixo-1", "$prefixo Capitao", 10, primeiroCapitao),
            Atleta("$prefixo-2", "$prefixo Ala", 7, false),
            Atleta("$prefixo-3", "$prefixo Defesa", 4, false)
        )
    }
}
