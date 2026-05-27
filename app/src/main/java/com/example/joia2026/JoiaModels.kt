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
    val modalidade: Modalidade? = null
) {
    fun nomeMandante(): String = mandante?.nome ?: equipeA?.nome ?: "TBD"
    fun nomeVisitante(): String = visitante?.nome ?: equipeB?.nome ?: "TBD"
    fun cursoMandante(): String = mandante?.curso?.sigla ?: mandante?.curso?.nome ?: equipeA?.curso?.sigla ?: equipeA?.curso?.nome ?: ""
    fun cursoVisitante(): String = visitante?.curso?.sigla ?: visitante?.curso?.nome ?: equipeB?.curso?.sigla ?: equipeB?.curso?.nome ?: ""
    fun placarMandanteTela(): Int = placarMandante ?: placarA ?: 0
    fun placarVisitanteTela(): Int = placarVisitante ?: placarB ?: 0
    fun dataHoraTela(): String = iniciaEm ?: dataHora ?: ""
}

data class Equipe(val id: String, val nome: String, val curso: Curso? = null)
data class Curso(val id: String, val nome: String, val sigla: String? = null)
data class Modalidade(val id: String, val nome: String)

// --- AUTH MODELS ---
data class LoginRequest(
    val email: String,
    val senha: String
)

data class RegisterRequest(
    val nome: String,
    val email: String,
    val senha: String,
    val cpf: String,
    val telefone: String,
    val cursoId: String
)

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
    val cpf: String? = null,
    val telefone: String? = null
)
