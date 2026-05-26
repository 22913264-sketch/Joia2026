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

class JogoAdapter : ListAdapter<Jogo, JogoAdapter.JogoViewHolder>(JogoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JogoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_jogo, parent, false)
        return JogoViewHolder(view)
    }

    override fun onBindViewHolder(holder: JogoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class JogoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
            txtModalidade.text = jogo.modalidade?.nome ?: "Modalidade"
            txtFase.text = jogo.fase
            txtEquipeA.text = jogo.equipeA?.nome ?: "TBD"
            txtEquipeB.text = jogo.equipeB?.nome ?: "TBD"
            txtPlacarA.text = (jogo.placarA ?: 0).toString()
            txtPlacarB.text = (jogo.placarB ?: 0).toString()
            txtDataHora.text = jogo.dataHora // Idealmente formatar aqui
            txtLocal.text = jogo.local
            
            configurarStatus(jogo.status)
        }

        private fun configurarStatus(status: String) {
            chipStatus.text = status
            when (status) {
                "EM_ANDAMENTO" -> {
                    chipStatus.text = "AO VIVO"
                    chipStatus.setChipBackgroundColorResource(android.R.color.holo_red_light)
                    chipStatus.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white))
                }
                "FINALIZADO" -> {
                    chipStatus.setChipBackgroundColorResource(android.R.color.darker_gray)
                    chipStatus.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white))
                }
                else -> { // AGENDADO
                    chipStatus.setChipBackgroundColorResource(android.R.color.holo_blue_light)
                    chipStatus.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white))
                }
            }
        }
    }

    class JogoDiffCallback : DiffUtil.ItemCallback<Jogo>() {
        override fun areItemsTheSame(oldItem: Jogo, newItem: Jogo): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Jogo, newItem: Jogo): Boolean = oldItem == newItem
    }
}
