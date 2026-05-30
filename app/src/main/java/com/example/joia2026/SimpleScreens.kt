package com.example.joia2026

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class RankingFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = screenRoot()
        root.addTitle("Ranking")
        root.addSubtitle("Trofeu Rotativo e classificacao por modalidade")
        val modalidadeSelect = MaterialAutoCompleteTextView(requireContext())
        val layout = TextInputLayout(requireContext()).apply {
            hint = "Modalidade"
            addView(modalidadeSelect)
        }
        root.addView(layout)
        val geral = root.addSection("Geral")
        val modalidade = root.addSection("Por modalidade")

        viewLifecycleOwner.lifecycleScope.launch {
            val modalidades = JoiaRepository.getModalidades()
            modalidadeSelect.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, modalidades.map { it.nome }))
            modalidadeSelect.setText(modalidades.firstOrNull()?.nome.orEmpty(), false)
            renderGeral(geral)
            renderModalidade(modalidade, modalidades.firstOrNull()?.id)
            modalidadeSelect.setOnItemClickListener { _, _, position, _ ->
                viewLifecycleOwner.lifecycleScope.launch { renderModalidade(modalidade, modalidades[position].id) }
            }
        }
        return ScrollView(requireContext()).apply { addView(root) }
    }

    private suspend fun renderGeral(container: LinearLayout) {
        container.removeAllViews()
        JoiaRepository.getRankingGeral().forEach {
            container.addLine("${it.posicao}o  ${it.sigla ?: it.curso?.sigla ?: "-"} - ${it.nome ?: it.curso?.nome ?: "Curso"}", "${it.pontos} pts")
        }
    }

    private suspend fun renderModalidade(container: LinearLayout, modalidadeId: String?) {
        container.removeAllViews()
        if (modalidadeId == null) return
        JoiaRepository.getRankingModalidade(modalidadeId).forEach {
            val equipe = it.equipe?.nome ?: "Equipe"
            container.addLine("${it.posicao}o $equipe", "J ${it.jogos} | V ${it.vitorias} | E ${it.empates} | D ${it.derrotas} | SG ${it.saldo} | ${it.pontos} pts")
        }
    }
}

class ModalidadesFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = screenRoot()
        root.addTitle("Modalidades")
        root.addSubtitle("Regulamentos, equipes inscritas e jogos")
        viewLifecycleOwner.lifecycleScope.launch {
            JoiaRepository.getModalidades().forEach { modalidade ->
                root.addCard(modalidade.nome, "${modalidade.tipo ?: "Modalidade"} · ${modalidade.equipesInscritas ?: 0} equipes · ${modalidade.jogos ?: 0} jogos\n${modalidade.regulamentoTecnico ?: modalidade.descricao ?: "Regulamento tecnico em atualizacao."}")
            }
        }
        return ScrollView(requireContext()).apply { addView(root) }
    }
}

class EquipesFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = screenRoot()
        root.addTitle("Equipes")
        root.addSubtitle("Inscricoes por curso, modalidade e genero")
        val lista = root.addSection("Lista")
        viewLifecycleOwner.lifecycleScope.launch {
            JoiaRepository.getEquipes().forEach { equipe ->
                lista.addLine("${equipe.nome} · ${equipe.cursoSigla()}", "${equipe.modalidade?.nome ?: "-"} · ${equipe.genero ?: "-"} · ${equipe.atletas.size} atletas")
            }
        }
        return ScrollView(requireContext()).apply { addView(root) }
    }
}

class CursosFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = screenRoot()
        root.addTitle("Cursos")
        root.addSubtitle("Pontuacao, posicao e equipes vinculadas")
        viewLifecycleOwner.lifecycleScope.launch {
            val ranking = JoiaRepository.getRankingGeral()
            ranking.forEach { item ->
                root.addCard("${item.posicao}o · ${item.sigla ?: item.curso?.sigla ?: "-"}", "${item.nome ?: item.curso?.nome ?: "Curso"}\n${item.pontos} pontos no ranking geral")
            }
        }
        return ScrollView(requireContext()).apply { addView(root) }
    }
}

class SobreFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = screenRoot()
        root.addTitle("Regulamento")
        root.addCard("JOIA 2026", JoiaRepository.regulamentoResumo())
        root.addCard("Comissao organizadora", "Datas, locais e contatos oficiais podem ser atualizados aqui sem depender de uma tela nova.")
        return ScrollView(requireContext()).apply { addView(root) }
    }
}

class AdminFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = screenRoot()
        root.addTitle("Admin")
        root.addSubtitle("Atalhos para jogos do dia")
        val section = root.addSection("Acoes")
        section.addLine("Iniciar partida", "POST /jogos/:id/iniciar")
        section.addLine("Lancar placar", "PATCH /jogos/:id/placar")
        section.addLine("Adicionar cartao", "POST /jogos/:id/cartoes")
        section.addLine("Finalizar jogo", "POST /jogos/:id/finalizar")
        return ScrollView(requireContext()).apply { addView(root) }
    }
}

private fun Fragment.screenRoot(): LinearLayout {
    return LinearLayout(requireContext()).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(28, 24, 28, 28)
        setBackgroundResource(R.color.joia_background)
    }
}

private fun LinearLayout.addTitle(text: String) {
    addView(TextView(context).apply {
        this.text = text
        textSize = 26f
        setTypeface(typeface, android.graphics.Typeface.BOLD)
        setTextColor(context.getColor(R.color.joia_text))
    })
}

private fun LinearLayout.addSubtitle(text: String) {
    addView(TextView(context).apply {
        this.text = text
        textSize = 14f
        setTextColor(context.getColor(R.color.joia_text_secondary))
    })
}

private fun LinearLayout.addSection(title: String): LinearLayout {
    return LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        addCard(title, "", this)
    }
}

private fun LinearLayout.addCard(title: String, body: String, innerOverride: LinearLayout? = null) {
    val inner = innerOverride ?: LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    inner.addView(TextView(context).apply {
        text = title
        textSize = 18f
        setTypeface(typeface, android.graphics.Typeface.BOLD)
        setTextColor(context.getColor(R.color.joia_text))
    }, 0)
    if (body.isNotBlank()) {
        inner.addView(TextView(context).apply {
            text = body
            textSize = 14f
            setTextColor(context.getColor(R.color.joia_text_secondary))
        })
    }
    addView(MaterialCardView(context).apply {
        radius = 18f
        cardElevation = 2f
        setCardBackgroundColor(context.getColor(R.color.joia_surface))
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            topMargin = 18
        }
        addView(inner.apply { setPadding(22, 20, 22, 20) })
        setOnClickListener { Toast.makeText(context, title, Toast.LENGTH_SHORT).show() }
    })
}

private fun LinearLayout.addLine(left: String, right: String) {
    addView(TextView(context).apply {
        text = "$left\n$right"
        textSize = 14f
        setPadding(0, 10, 0, 10)
        setTextColor(context.getColor(R.color.joia_text))
    })
}
