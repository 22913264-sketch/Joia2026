package com.example.joia2026

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class JogoDetalheActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundResource(R.color.joia_background)
        }
        setContentView(ScrollView(this).apply { addView(root) })

        val jogoId = intent.getStringExtra(EXTRA_JOGO_ID)
        lifecycleScope.launch {
            val jogo = JoiaRepository.getJogos().find { it.id == jogoId }
            if (jogo == null) {
                Toast.makeText(this@JogoDetalheActivity, "Jogo nao encontrado", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }
            montarTela(root, jogo)
        }
    }

    private fun montarTela(root: LinearLayout, jogo: Jogo) {
        root.addView(TextView(this).apply {
            text = jogo.modalidade?.nome ?: "Detalhe do jogo"
            textSize = 26f
            setTextColor(getColor(R.color.joia_text))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })

        root.addView(Chip(this).apply {
            text = "${jogo.status ?: "AGENDADO"} · ${jogo.fase ?: "Fase"} · ${jogo.local ?: "Local"}"
            isClickable = false
            isCheckable = false
        })

        root.addView(card {
            addView(TextView(context).apply {
                text = "${jogo.cursoMandante()} ${jogo.placarMandanteTela()}  x  ${jogo.placarVisitanteTela()} ${jogo.cursoVisitante()}"
                gravity = Gravity.CENTER
                textSize = 30f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setTextColor(getColor(R.color.joia_text))
            })
            addView(TextView(context).apply {
                text = "${jogo.nomeMandante()} vs ${jogo.nomeVisitante()}\n${jogo.dataHoraTela()}"
                gravity = Gravity.CENTER
                setTextColor(getColor(R.color.joia_text_secondary))
            })
        })

        root.addView(actions(jogo))
        root.addView(section("Sumula", sumulaTexto(jogo)))
        root.addView(section("Escalacoes", escalacoesTexto(jogo)))
    }

    private fun actions(jogo: Jogo): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            addView(MaterialButton(context).apply {
                text = "Favoritar"
                setOnClickListener { Toast.makeText(context, "Jogo favoritado", Toast.LENGTH_SHORT).show() }
            })
            addView(MaterialButton(context).apply {
                text = "Compartilhar"
                setOnClickListener { Toast.makeText(context, "${jogo.nomeMandante()} x ${jogo.nomeVisitante()}", Toast.LENGTH_SHORT).show() }
            })
            addView(MaterialButton(context).apply {
                text = "Ver curso"
                setOnClickListener { Toast.makeText(context, jogo.cursoMandante(), Toast.LENGTH_SHORT).show() }
            })
        }
    }

    private fun section(titulo: String, conteudo: String): View {
        return card {
            addView(TextView(context).apply {
                text = titulo
                textSize = 18f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setTextColor(getColor(R.color.joia_text))
            })
            addView(TextView(context).apply {
                text = conteudo
                setTextColor(getColor(R.color.joia_text_secondary))
            })
        }
    }

    private fun card(block: LinearLayout.() -> Unit): MaterialCardView {
        return MaterialCardView(this).apply {
            radius = 18f
            cardElevation = 2f
            setCardBackgroundColor(getColor(R.color.joia_surface))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = 18
            }
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(24, 24, 24, 24)
                block()
            })
        }
    }

    private fun sumulaTexto(jogo: Jogo): String {
        if (jogo.cartoes.isEmpty()) return "Sem cartoes registrados."
        return jogo.cartoes.joinToString("\n") { "${it.tipo}: ${it.atleta?.nome ?: "Atleta"} - ${it.motivo ?: "Sem motivo"}" }
    }

    private fun escalacoesTexto(jogo: Jogo): String {
        val mandante = jogo.mandante?.atletas.orEmpty().joinToString("\n") { atletaLinha(it) }
        val visitante = jogo.visitante?.atletas.orEmpty().joinToString("\n") { atletaLinha(it) }
        return "${jogo.nomeMandante()}\n${mandante.ifBlank { "Escalacao nao informada" }}\n\n${jogo.nomeVisitante()}\n${visitante.ifBlank { "Escalacao nao informada" }}"
    }

    private fun atletaLinha(atleta: Atleta): String {
        val numero = atleta.numero?.let { "#$it " }.orEmpty()
        val capitao = if (atleta.capitao) " (capitao)" else ""
        return "$numero${atleta.nome}$capitao"
    }

    companion object {
        const val EXTRA_JOGO_ID = "jogo_id"
    }
}
