package com.example.joia2026

data class Jogo(
    val id: String,
    val dataHora: String? = null,
    val iniciaEm: String? = null,
    val local: String? = null,
    val fase: String? = null,
    val status: String? = null,
    val placarA: Int? = 0,
    val placarB: Int? = 0,
    val placarMandante: Int? = null,
    val placarVisitante: Int? = null,
    val equipeA: Equipe? = null,
    val equipeB: Equipe? = null,
    val mandante: Equipe? = null,
    val visitante: Equipe? = null,
    val modalidade: Modalidade? = null,
    val cartoes: List<Cartao> = emptyList()
) {
    fun nomeMandante(): String = mandante?.nome ?: equipeA?.nome ?: "TBD"
    fun nomeVisitante(): String = visitante?.nome ?: equipeB?.nome ?: "TBD"
    fun cursoMandante(): String = mandante?.curso?.sigla ?: mandante?.curso?.nome ?: equipeA?.curso?.sigla ?: equipeA?.curso?.nome ?: ""
    fun cursoVisitante(): String = visitante?.curso?.sigla ?: visitante?.curso?.nome ?: equipeB?.curso?.sigla ?: equipeB?.curso?.nome ?: ""
    fun placarMandanteTela(): Int = placarMandante ?: placarA ?: 0
    fun placarVisitanteTela(): Int = placarVisitante ?: placarB ?: 0
    fun dataHoraTela(): String = iniciaEm ?: dataHora ?: ""
}

data class Equipe(
    val id: String,
    val nome: String,
    val curso: Curso? = null,
    val modalidade: Modalidade? = null,
    val genero: String? = null,
    val atletas: List<Atleta> = emptyList()
) {
    fun cursoSigla(): String = curso?.sigla ?: curso?.nome ?: "-"
}

data class Curso(
    val id: String,
    val nome: String,
    val sigla: String? = null,
    val pontos: Int? = null,
    val posicao: Int? = null
)

data class Modalidade(
    val id: String,
    val nome: String,
    val tipo: String? = null,
    val descricao: String? = null,
    val regulamentoTecnico: String? = null,
    val limitePorCurso: Int? = null,
    val equipesInscritas: Int? = null,
    val jogos: Int? = null
)

data class Atleta(
    val id: String,
    val nome: String,
    val numero: Int? = null,
    val capitao: Boolean = false
)

data class Cartao(
    val id: String? = null,
    val tipo: String,
    val atleta: Atleta? = null,
    val motivo: String? = null
)

data class RankingGeralItem(
    val posicao: Int,
    val curso: Curso? = null,
    val nome: String? = null,
    val sigla: String? = null,
    val pontos: Int = 0
)

data class RankingModalidadeItem(
    val posicao: Int,
    val equipe: Equipe? = null,
    val curso: Curso? = null,
    val jogos: Int = 0,
    val vitorias: Int = 0,
    val empates: Int = 0,
    val derrotas: Int = 0,
    val golsPro: Int = 0,
    val golsContra: Int = 0,
    val saldo: Int = 0,
    val pontos: Int = 0
)

// --- AUTH MODELS ---
data class LoginRequest(
    val email: String,
    val senha: String
)

data class RegisterRequest(
    val nome: String,
    val email: String,
    val senha: String,
    val cpf: String?,
    val telefone: String?,
    val cursoId: String
)

data class UpdateProfileRequest(
    val nome: String,
    val cpf: String?,
    val telefone: String?,
    val cursoId: String?
)

data class ScoreRequest(val placarMandante: Int, val placarVisitante: Int)
data class CartaoRequest(val tipo: String, val atletaId: String, val motivo: String?)

data class AuthResponse(
    val user: User,
    val token: String
)

data class User(
    val id: String,
    val nome: String,
    val email: String,
    val role: String,
    val cursoId: String? = null,
    val curso: Curso? = null,
    val cpf: String? = null,
    val telefone: String? = null
)
