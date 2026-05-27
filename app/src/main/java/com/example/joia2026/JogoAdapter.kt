package com.example.joia2026

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class JogoAdapter(private val onJogoClick: (Jogo) -> Unit) : ListAdapter<Jogo, JogoAdapter.JogoViewHolder>(JogoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JogoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_jogo, parent, false)
        return JogoViewHolder(view, onJogoClick)
    }

    override fun onBindViewHolder(holder: JogoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class JogoViewHolder(itemView: View, private val onJogoClick: (Jogo) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val txtModalidade: TextView = itemView.findViewById(R.id.txtModalidade)
        private val txtFase: TextView = itemView.findViewById(R.id.txtFase)
        private val txtEquipeA: TextView = itemView.findViewById(R.id.txtEquipeA)
        private val txtEquipeB: TextView = itemView.findViewById(R.id.txtEquipeB)
        private val txtPlacarA: TextView = itemView.findViewById(R.id.txtPlacarA)
        private val txtPlacarB: TextView = itemView.findViewById(R.id.txtPlacarB)
        private val txtDataHora: TextView = itemView.findViewById(R.id.txtDataHora)
        private val txtLocal: TextView = itemView.findViewById(R.id.txtLocal)
        private val chipStatus: Chip = itemView.findViewById(R.id.chipStatus)

        fun bind(jogo: Jogo) {
            txtModalidade.text = jogo.modalidade?.nome?.uppercase() ?: "MODALIDADE"
            txtFase.text = formatarFase(jogo.fase)
            txtEquipeA.text = textoEquipe(jogo.nomeMandante(), jogo.cursoMandante())
            txtEquipeB.text = textoEquipe(jogo.nomeVisitante(), jogo.cursoVisitante())
            txtPlacarA.text = jogo.placarMandanteTela().toString()
            txtPlacarB.text = jogo.placarVisitanteTela().toString()
            txtDataHora.text = formatarData(jogo.dataHoraTela())
            txtLocal.text = jogo.local ?: "Local nao informado"
            
            configurarStatus(jogo.status)

            itemView.setOnClickListener { onJogoClick(jogo) }
        }

        private fun configurarStatus(status: String?) {
            when (status) {
                "EM_ANDAMENTO" -> {
                    chipStatus.text = "AO VIVO"
                    chipStatus.setChipBackgroundColorResource(R.color.status_live)
                    chipStatus.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white))
                }
                "FINALIZADO" -> {
                    chipStatus.text = "FINAL"
                    chipStatus.setChipBackgroundColorResource(R.color.status_finished)
                    chipStatus.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white))
                }
                "CANCELADO" -> {
                    chipStatus.text = "CANCELADO"
                    chipStatus.setChipBackgroundColorResource(R.color.status_live)
                    chipStatus.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white))
                }
                else -> { // AGENDADO
                    chipStatus.text = "AGENDADO"
                    chipStatus.setChipBackgroundColorResource(R.color.status_scheduled)
                    chipStatus.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white))
                }
            }
        }

        private fun formatarFase(fase: String?): String {
            return fase
                ?.replace("_", " ")
                ?.lowercase()
                ?.replaceFirstChar { it.titlecase() }
                ?: "Fase"
        }

        private fun textoEquipe(nome: String, curso: String): String {
            return if (curso.isBlank()) nome else "$nome\n$curso"
        }

        private fun formatarData(valor: String): String {
            if (valor.isBlank()) return "Data nao informada"

            return try {
                val formatter = DateTimeFormatter.ofPattern("dd/MM, HH:mm")
                    .withZone(ZoneId.systemDefault())
                formatter.format(Instant.parse(valor))
            } catch (_: Exception) {
                valor
            }
        }
    }

    class JogoDiffCallback : DiffUtil.ItemCallback<Jogo>() {
        override fun areItemsTheSame(oldItem: Jogo, newItem: Jogo): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Jogo, newItem: Jogo): Boolean = oldItem == newItem
    }
}
