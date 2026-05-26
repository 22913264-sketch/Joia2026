package com.example.joia2026

    data class Jogo(
        val id: String,
        val dataHora: String,
        val local: String,
        val fase: String,
        val status: String, // "AGENDADO", "EM_ANDAMENTO", "FINALIZADO"
        val placarA: Int? = 0,
        val placarB: Int? = 0,
        val equipeA: Equipe? = null,
        val equipeB: Equipe? = null,
        val modalidade: Modalidade? = null
    )

data class Equipe(val id: String, val nome: String, val curso: Curso? = null)
data class Curso(val id: String, val nome: String, val sigla: String? = null)
data class Modalidade(val id: String, val nome: String)

