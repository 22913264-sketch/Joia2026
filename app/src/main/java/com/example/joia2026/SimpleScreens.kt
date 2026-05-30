package com.example.joia2026

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class RankingFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = screenRoot()
        root.addHeader("Ranking", "Podio geral e tabela da modalidade escolhida")

        val modalidadeChooser = root.addModalidadeChooser("Carregando modalidades...")
        val modalidadeTitulo = TextView(requireContext()).apply {
            text = "Ranking da modalidade"
            textSize = 18f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(context.getColor(R.color.joia_text))
            layoutParams = LinearLayout.LayoutParams(match(), wrap()).withTop(18)
        }
        root.addView(modalidadeTitulo)
        val modalidade = root.addSection("Classificacao")
        val geral = root.addSection("Trofeu Rotativo")

        viewLifecycleOwner.lifecycleScope.launch {
            val modalidades = JoiaRepository.getModalidades()
            val primeiraModalidade = modalidades.firstOrNull()
            modalidadeChooser.text.text = primeiraModalidade?.nome ?: "Nenhuma modalidade"
            modalidadeTitulo.text = "Ranking de ${primeiraModalidade?.nome ?: "modalidade"}"
            renderGeral(geral)
            renderModalidade(modalidade, primeiraModalidade?.id)

            modalidadeChooser.card.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Escolha a modalidade")
                    .setItems(modalidades.map { it.nome }.toTypedArray()) { _, position ->
                        val selecionada = modalidades[position]
                        modalidadeChooser.text.text = selecionada.nome
                        modalidadeTitulo.text = "Ranking de ${selecionada.nome}"
                        viewLifecycleOwner.lifecycleScope.launch { renderModalidade(modalidade, selecionada.id) }
                    }
                    .show()
            }
        }
        return scroll(root)
    }

    private suspend fun renderGeral(container: LinearLayout) {
        container.removeAllViews()
        JoiaRepository.getRankingGeral().forEach {
            container.addRankingCard(
                position = it.posicao,
                title = it.sigla ?: it.curso?.sigla ?: "-",
                subtitle = it.nome ?: it.curso?.nome ?: "Curso",
                score = "${it.pontos} pts",
                featured = it.posicao <= 3,
                stats = listOf("Trofeu Rotativo", "Geral")
            )
        }
    }

    private suspend fun renderModalidade(container: LinearLayout, modalidadeId: String?) {
        container.removeAllViews()
        if (modalidadeId == null) return
        JoiaRepository.getRankingModalidade(modalidadeId).forEach {
            val equipe = it.equipe?.nome ?: "Equipe"
            container.addRankingCard(
                position = it.posicao,
                title = equipe,
                subtitle = it.curso?.sigla ?: it.equipe?.cursoSigla() ?: "Curso",
                score = "${it.pontos} pts",
                featured = it.posicao <= 3,
                stats = listOf("J ${it.jogos}", "V ${it.vitorias}", "E ${it.empates}", "D ${it.derrotas}", "GP ${it.golsPro}", "GC ${it.golsContra}", "SG ${it.saldo}")
            )
        }
    }
}

class ModalidadesFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = screenRoot()
        root.addHeader("Modalidades", "Regulamentos, equipes inscritas e jogos")
        viewLifecycleOwner.lifecycleScope.launch {
            JoiaRepository.getModalidades().forEach { modalidade ->
                root.addModalidadeCard(modalidade)
            }
        }
        return scroll(root)
    }
}

class EquipesFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = screenRoot()
        root.addHeader("Equipes", "Inscricoes por curso, modalidade e genero")
        val lista = root.addSection("Lista de equipes")
        viewLifecycleOwner.lifecycleScope.launch {
            JoiaRepository.getEquipes().forEach { equipe ->
                lista.addEquipeCard(equipe)
            }
        }
        return scroll(root)
    }
}

class CursosFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = screenRoot()
        root.addHeader("Cursos", "Pontuacao, posicao e equipes vinculadas")
        viewLifecycleOwner.lifecycleScope.launch {
            JoiaRepository.getRankingGeral().forEach { item ->
                root.addCursoCard(item)
            }
        }
        return scroll(root)
    }
}

class SobreFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = screenRoot()
        root.addHeader("Regulamento", "Informacoes gerais do JOIA 2026")
        root.addTextCard("JOIA 2026", JoiaRepository.regulamentoResumo(), "GERAL")
        root.addTextCard("Comissao organizadora", "Datas, locais e contatos oficiais podem ser atualizados aqui sem depender de uma tela nova.", "CONTATO")
        return scroll(root)
    }
}

class AdminFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = screenRoot()
        if (!UserSession.isAdmin(requireContext())) {
            root.addHeader("Acesso restrito", "Somente administradores podem cadastrar times e alterar placares")
            root.addTextCard("Perfil visualizador", "Seu usuario consegue ver jogos, placares, rankings, modalidades e equipes normalmente, mas nao acessa operacoes administrativas.", "VIEWER")
            return scroll(root)
        }

        root.addHeader("Painel ADM", "Cadastre times, remova equipes e atualize placares")
        viewLifecycleOwner.lifecycleScope.launch {
            renderAdmin(root)
        }
        return scroll(root)
    }

    private suspend fun renderAdmin(root: LinearLayout) {
        val cursos = JoiaRepository.getCursos()
        val modalidades = JoiaRepository.getModalidades()
        val equipes = JoiaRepository.getEquipes()
        val jogos = JoiaRepository.getJogos("EM_ANDAMENTO") + JoiaRepository.getJogos("AGENDADO")

        var cursoSelecionado = cursos.firstOrNull()
        var modalidadeSelecionada = modalidades.firstOrNull()
        var generoSelecionado = "MASCULINO"

        val createSection = root.addSection("Cadastrar time")
        val nomeEquipe = TextInputEditText(requireContext()).apply {
            hint = "Nome do time"
            setSingleLine(true)
            setTextColor(context.getColor(R.color.joia_text))
            setHintTextColor(context.getColor(R.color.joia_text_secondary))
            background = roundedStroke(context.getColor(R.color.joia_surface), context.getColor(R.color.joia_outline), dp(1), dp(8).toFloat())
            setPadding(dp(14), dp(12), dp(14), dp(12))
            layoutParams = LinearLayout.LayoutParams(match(), wrap()).withTop(8)
        }

        val cursoChooser = createSection.addAdminChooser("Curso", cursoSelecionado?.nome ?: "Selecione")
        val modalidadeChooser = createSection.addAdminChooser("Modalidade", modalidadeSelecionada?.nome ?: "Selecione")
        val generoChooser = createSection.addAdminChooser("Genero", generoSelecionado)
        createSection.addView(nomeEquipe)

        cursoChooser.card.setOnClickListener {
            showChoice("Escolha o curso", cursos.map { it.nome }) { position ->
                cursoSelecionado = cursos[position]
                cursoChooser.text.text = cursos[position].nome
            }
        }
        modalidadeChooser.card.setOnClickListener {
            showChoice("Escolha a modalidade", modalidades.map { it.nome }) { position ->
                modalidadeSelecionada = modalidades[position]
                modalidadeChooser.text.text = modalidades[position].nome
            }
        }
        generoChooser.card.setOnClickListener {
            val generos = listOf("MASCULINO", "FEMININO", "MISTO")
            showChoice("Escolha o genero", generos) { position ->
                generoSelecionado = generos[position]
                generoChooser.text.text = generos[position]
            }
        }

        createSection.addView(MaterialButton(requireContext()).apply {
            text = "Criar time"
            setTextColor(context.getColor(R.color.joia_button_text))
            backgroundTintList = android.content.res.ColorStateList.valueOf(context.getColor(R.color.primary))
            layoutParams = LinearLayout.LayoutParams(match(), dp(52)).withTop(12)
            setOnClickListener {
                val nome = nomeEquipe.text?.toString()?.trim().orEmpty()
                val curso = cursoSelecionado
                val modalidade = modalidadeSelecionada
                if (nome.isBlank() || curso == null || modalidade == null) {
                    Toast.makeText(requireContext(), "Preencha nome, curso e modalidade", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val jaExisteNaMesmaModalidade = equipes.any {
                    it.nome.equals(nome, ignoreCase = true) && it.modalidade?.id == modalidade.id
                }
                if (jaExisteNaMesmaModalidade) {
                    Toast.makeText(requireContext(), "Ja existe time com esse nome nessa modalidade", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                viewLifecycleOwner.lifecycleScope.launch {
                    JoiaRepository.createEquipe(nome, curso.id, modalidade.id, generoSelecionado)
                    Toast.makeText(requireContext(), "Time criado em ${modalidade.nome}", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.beginTransaction().replace(R.id.fragmentContainer, AdminFragment()).commit()
                }
            }
        })

        val equipesSection = root.addSection("Times cadastrados")
        equipes.forEach { equipe ->
            equipesSection.addAdminTeamCard(equipe) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Excluir time")
                    .setMessage("Excluir ${equipe.nome} de ${equipe.modalidade?.nome ?: "modalidade"}?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Excluir") { _, _ ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            JoiaRepository.deleteEquipe(equipe.id)
                            Toast.makeText(requireContext(), "Time excluido", Toast.LENGTH_SHORT).show()
                            parentFragmentManager.beginTransaction().replace(R.id.fragmentContainer, AdminFragment()).commit()
                        }
                    }
                    .show()
            }
        }

        val placarSection = root.addSection("Alterar placares")
        if (jogos.isEmpty()) {
            placarSection.addTextCard("Sem jogos editaveis", "Quando houver jogos agendados ou em andamento, eles aparecem aqui para o ADM lancar placar.", "PLACAR")
        } else {
            jogos.forEach { jogo ->
                placarSection.addScoreAdminCard(jogo) { mandante, visitante ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        JoiaRepository.updatePlacar(jogo.id, mandante, visitante)
                        Toast.makeText(requireContext(), "Placar salvo", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.beginTransaction().replace(R.id.fragmentContainer, AdminFragment()).commit()
                    }
                }
            }
        }
    }

    private fun showChoice(title: String, items: List<String>, onSelect: (Int) -> Unit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setItems(items.toTypedArray()) { _, position -> onSelect(position) }
            .show()
    }
}

private fun Fragment.screenRoot(): LinearLayout {
    return LinearLayout(requireContext()).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(20), dp(18), dp(20), dp(28))
        setBackgroundResource(R.color.joia_background)
    }
}

private fun Fragment.scroll(root: LinearLayout): ScrollView {
    return ScrollView(requireContext()).apply {
        isFillViewport = true
        addView(root)
    }
}

private fun LinearLayout.addHeader(title: String, subtitle: String) {
    addView(TextView(context).apply {
        text = title
        textSize = 28f
        setTypeface(typeface, Typeface.BOLD)
        setTextColor(context.getColor(R.color.joia_text))
    })
    addView(TextView(context).apply {
        text = subtitle
        textSize = 14f
        setTextColor(context.getColor(R.color.joia_text_secondary))
        layoutParams = LinearLayout.LayoutParams(match(), wrap()).withTop(2)
    })
}

private data class ModalidadeChooser(val card: View, val text: TextView)

private fun LinearLayout.addModalidadeChooser(initialText: String): ModalidadeChooser {
    val selectedText = TextView(context).apply {
        text = initialText
        textSize = 18f
        setTypeface(typeface, Typeface.BOLD)
        setTextColor(context.getColor(R.color.joia_text))
    }

    val card = MaterialCardView(context).apply {
        radius = dp(8).toFloat()
        cardElevation = 3f
        strokeWidth = dp(1)
        strokeColor = context.getColor(R.color.primaryVariant)
        setCardBackgroundColor(context.getColor(R.color.joia_surface))
        layoutParams = LinearLayout.LayoutParams(match(), wrap()).withTop(18)
        isClickable = true
        isFocusable = true
        addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, wrap(), 1f)
                addView(TextView(context).apply {
                    text = "Modalidade selecionada"
                    textSize = 12f
                    setTypeface(typeface, Typeface.BOLD)
                    setTextColor(context.getColor(R.color.primaryVariant))
                })
                addView(selectedText)
            })
            addView(TextView(context).apply {
                text = "Ver lista"
                gravity = Gravity.CENTER
                textSize = 13f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(context.getColor(R.color.joia_button_text))
                background = rounded(context.getColor(R.color.primary), dp(18).toFloat())
                setPadding(dp(12), dp(7), dp(12), dp(7))
            })
        })
    }

    addView(card)
    return ModalidadeChooser(card, selectedText)
}

private fun LinearLayout.addSection(title: String): LinearLayout {
    val content = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(match(), wrap()).withTop(10)
    }

    addView(MaterialCardView(context).apply {
        radius = dp(8).toFloat()
        cardElevation = 1.5f
        strokeWidth = dp(1)
        strokeColor = context.getColor(R.color.joia_outline)
        setCardBackgroundColor(context.getColor(R.color.joia_surface))
        layoutParams = LinearLayout.LayoutParams(match(), wrap()).withTop(18)
        addView(LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(14))
            addView(TextView(context).apply {
                text = title
                textSize = 17f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(context.getColor(R.color.joia_text))
            })
            addView(content)
        })
    })

    return content
}

private fun LinearLayout.addRankingCard(
    position: Int,
    title: String,
    subtitle: String,
    score: String,
    featured: Boolean = false,
    stats: List<String> = emptyList()
) {
    val accent = when (position) {
        1 -> 0xFFFFB000.toInt()
        2 -> 0xFFC0C7D2.toInt()
        3 -> 0xFFCD7F32.toInt()
        else -> context.getColor(R.color.joia_outline)
    }
    val podiumIcon = when (position) {
        1 -> "👑"
        2 -> "🥈"
        3 -> "🥉"
        else -> "${position}o"
    }
    val podiumLabel = when (position) {
        1 -> "👑 TOP 1 · coroa"
        2 -> "🥈 TOP 2 · prata"
        3 -> "🥉 TOP 3 · bronze"
        else -> null
    }
    val detailText = when (position) {
        1 -> "Campeao em destaque com efeito de fogo"
        2 -> "Vice-lider com medalha de prata"
        3 -> "Terceiro lugar com medalha de bronze"
        else -> null
    }

    val row = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(12), dp(12), dp(12), dp(12))
    }

    row.addView(TextView(context).apply {
        text = podiumIcon
        gravity = Gravity.CENTER
        textSize = if (position <= 3) 24f else 18f
        setTypeface(typeface, Typeface.BOLD)
        setTextColor(if (position == 1) context.getColor(R.color.joia_button_text) else context.getColor(R.color.joia_text))
        background = rounded(accent, dp(8).toFloat())
        layoutParams = LinearLayout.LayoutParams(dp(54), dp(54))
    })

    row.addView(LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(12), 0, dp(8), 0)
        layoutParams = LinearLayout.LayoutParams(0, wrap(), 1f)
        if (podiumLabel != null) {
            addView(context.label(podiumLabel).apply {
                layoutParams = LinearLayout.LayoutParams(wrap(), wrap()).apply { bottomMargin = dp(4) }
            })
        }
        addView(TextView(context).apply {
            text = title
            textSize = if (featured) 19f else 16f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(context.getColor(R.color.joia_text))
        })
        addView(TextView(context).apply {
            text = subtitle
            textSize = 13f
            setTextColor(context.getColor(R.color.joia_text_secondary))
        })
        if (stats.isNotEmpty()) {
            addView(context.chipRow(stats))
        }
        if (detailText != null) {
            addView(TextView(context).apply {
                text = detailText
                textSize = 12f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(
                    when (position) {
                        1 -> 0xFFB42318.toInt()
                        2 -> 0xFF475467.toInt()
                        else -> 0xFF9A5B13.toInt()
                    }
                )
                layoutParams = LinearLayout.LayoutParams(match(), wrap()).withTop(6)
            })
        }
    })

    row.addView(TextView(context).apply {
        text = score
        gravity = Gravity.CENTER
        textSize = 15f
        setTypeface(typeface, Typeface.BOLD)
        setTextColor(context.getColor(R.color.joia_button_text))
        background = rounded(if (position == 2) 0xFFC0C7D2.toInt() else if (position == 3) 0xFFCD7F32.toInt() else context.getColor(R.color.primary), dp(18).toFloat())
        setPadding(dp(12), dp(6), dp(12), dp(6))
    })

    val content = if (position == 1) {
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = roundedStroke(0xFFFFFBEB.toInt(), 0xFFFF6B00.toInt(), dp(2), dp(8).toFloat())
            addView(TextView(context).apply {
                text = "🔥🔥🔥 CAMPEAO DA MODALIDADE 🔥🔥🔥"
                gravity = Gravity.CENTER
                textSize = 12f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(0xFFB42318.toInt())
                setPadding(0, dp(8), 0, 0)
            })
            addView(row)
            addView(TextView(context).apply {
                text = "🔥 efeito de fogo ao redor do lider 🔥"
                gravity = Gravity.CENTER
                textSize = 11f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(0xFFFF6B00.toInt())
                setPadding(0, 0, 0, dp(8))
            })
        }
    } else {
        row
    }

    addView(rowCard(content, featured, position))
}

private fun LinearLayout.addEquipeCard(equipe: Equipe) {
    val totalAtletas = equipe.atletas.orEmpty().size
    val content = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(14), dp(14), dp(14), dp(14))
        addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(TextView(context).apply {
                text = equipe.cursoSigla().take(3).uppercase()
                gravity = Gravity.CENTER
                textSize = 15f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(context.getColor(R.color.joia_button_text))
                background = rounded(context.getColor(R.color.primary), dp(10).toFloat())
                layoutParams = LinearLayout.LayoutParams(dp(52), dp(52))
            })
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(12), 0, 0, 0)
                layoutParams = LinearLayout.LayoutParams(0, wrap(), 1f)
                addView(TextView(context).apply {
                    text = equipe.nome
                    textSize = 17f
                    setTypeface(typeface, Typeface.BOLD)
                    setTextColor(context.getColor(R.color.joia_text))
                })
                addView(TextView(context).apply {
                    text = equipe.curso?.nome ?: "Curso nao informado"
                    textSize = 13f
                    setTextColor(context.getColor(R.color.joia_text_secondary))
                })
            })
        })
        addView(context.chipRow(listOf("🏆 ${equipe.modalidade?.nome ?: "Modalidade"}", equipe.genero ?: "Genero", "$totalAtletas atletas")))
    }
    addView(rowCard(content))
}

private fun LinearLayout.addModalidadeCard(modalidade: Modalidade) {
    val content = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16), dp(16), dp(16), dp(16))
        addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(TextView(context).apply {
                text = modalidade.nome.take(1).uppercase()
                gravity = Gravity.CENTER
                textSize = 22f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(context.getColor(R.color.joia_header_text))
                background = rounded(context.getColor(R.color.joia_header), dp(12).toFloat())
                layoutParams = LinearLayout.LayoutParams(dp(54), dp(54))
            })
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(12), 0, 0, 0)
                layoutParams = LinearLayout.LayoutParams(0, wrap(), 1f)
                addView(TextView(context).apply {
                    text = modalidade.nome
                    textSize = 18f
                    setTypeface(typeface, Typeface.BOLD)
                    setTextColor(context.getColor(R.color.joia_text))
                })
                addView(TextView(context).apply {
                    text = modalidade.regulamentoTecnico ?: modalidade.descricao ?: "Regulamento tecnico em atualizacao."
                    textSize = 13f
                    maxLines = 2
                    setTextColor(context.getColor(R.color.joia_text_secondary))
                })
            })
        })
        addView(context.chipRow(listOf(modalidade.tipo ?: "Modalidade", "${modalidade.equipesInscritas ?: 0} equipes", "${modalidade.jogos ?: 0} jogos")))
    }
    addView(rowCard(content))
}

private fun LinearLayout.addCursoCard(item: RankingGeralItem) {
    val sigla = item.sigla ?: item.curso?.sigla ?: "-"
    val nome = item.nome ?: item.curso?.nome ?: "Curso"
    addRankingCard(
        position = item.posicao,
        title = sigla,
        subtitle = nome,
        score = "${item.pontos} pts",
        featured = item.posicao <= 3,
        stats = listOf("Ranking geral", "Equipes vinculadas")
    )
}

private fun LinearLayout.addTextCard(title: String, body: String, label: String) {
    val content = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16), dp(16), dp(16), dp(16))
        addView(context.label(label))
        addView(TextView(context).apply {
            text = title
            textSize = 19f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(context.getColor(R.color.joia_text))
            layoutParams = LinearLayout.LayoutParams(match(), wrap()).withTop(8)
        })
        addView(TextView(context).apply {
            text = body
            textSize = 14f
            setTextColor(context.getColor(R.color.joia_text_secondary))
            layoutParams = LinearLayout.LayoutParams(match(), wrap()).withTop(6)
        })
    }
    addView(rowCard(content))
}

private fun LinearLayout.addAdminAction(title: String, subtitle: String, endpoint: String) {
    val content = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(14), dp(14), dp(14), dp(14))
        addView(TextView(context).apply {
            text = "+"
            gravity = Gravity.CENTER
            textSize = 24f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(context.getColor(R.color.joia_button_text))
            background = rounded(context.getColor(R.color.primary), dp(10).toFloat())
            layoutParams = LinearLayout.LayoutParams(dp(46), dp(46))
        })
        addView(LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
            layoutParams = LinearLayout.LayoutParams(0, wrap(), 1f)
            addView(TextView(context).apply {
                text = title
                textSize = 16f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(context.getColor(R.color.joia_text))
            })
            addView(TextView(context).apply {
                text = "$subtitle\n$endpoint"
                textSize = 12f
                setTextColor(context.getColor(R.color.joia_text_secondary))
            })
        })
    }
    addView(rowCard(content).apply {
        setOnClickListener { Toast.makeText(context, title, Toast.LENGTH_SHORT).show() }
    })
}

private fun LinearLayout.addAdminChooser(label: String, initialText: String): ModalidadeChooser {
    val selectedText = TextView(context).apply {
        text = initialText
        textSize = 15f
        setTypeface(typeface, Typeface.BOLD)
        setTextColor(context.getColor(R.color.joia_text))
    }

    val card = MaterialCardView(context).apply {
        radius = dp(8).toFloat()
        cardElevation = 1.5f
        strokeWidth = dp(1)
        strokeColor = context.getColor(R.color.joia_outline)
        setCardBackgroundColor(context.getColor(R.color.joia_surface_variant))
        isClickable = true
        isFocusable = true
        layoutParams = LinearLayout.LayoutParams(match(), wrap()).withTop(8)
        addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(10))
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, wrap(), 1f)
                addView(TextView(context).apply {
                    text = label
                    textSize = 11f
                    setTypeface(typeface, Typeface.BOLD)
                    setTextColor(context.getColor(R.color.joia_text_secondary))
                })
                addView(selectedText)
            })
            addView(TextView(context).apply {
                text = "Escolher"
                textSize = 12f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(context.getColor(R.color.joia_button_text))
                background = rounded(context.getColor(R.color.primary), dp(16).toFloat())
                setPadding(dp(10), dp(6), dp(10), dp(6))
            })
        })
    }

    addView(card)
    return ModalidadeChooser(card, selectedText)
}

private fun LinearLayout.addAdminTeamCard(equipe: Equipe, onDelete: () -> Unit) {
    val content = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(14), dp(14), dp(14), dp(14))
        addView(TextView(context).apply {
            text = equipe.cursoSigla().take(3).uppercase()
            gravity = Gravity.CENTER
            textSize = 15f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(context.getColor(R.color.joia_button_text))
            background = rounded(context.getColor(R.color.primary), dp(10).toFloat())
            layoutParams = LinearLayout.LayoutParams(dp(52), dp(52))
        })
        addView(LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, dp(8), 0)
            layoutParams = LinearLayout.LayoutParams(0, wrap(), 1f)
            addView(TextView(context).apply {
                text = equipe.nome
                textSize = 16f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(context.getColor(R.color.joia_text))
            })
            addView(TextView(context).apply {
                text = "${equipe.modalidade?.nome ?: "Modalidade"} · ${equipe.genero ?: "Genero"} · ${equipe.curso?.nome ?: "Curso"}"
                textSize = 12f
                setTextColor(context.getColor(R.color.joia_text_secondary))
            })
        })
        addView(MaterialButton(context).apply {
            text = "Excluir"
            textSize = 12f
            setTextColor(context.getColor(R.color.white))
            backgroundTintList = android.content.res.ColorStateList.valueOf(context.getColor(R.color.joia_error))
            layoutParams = LinearLayout.LayoutParams(wrap(), dp(42))
            setOnClickListener { onDelete() }
        })
    }
    addView(rowCard(content))
}

private fun LinearLayout.addScoreAdminCard(jogo: Jogo, onSave: (Int, Int) -> Unit) {
    var mandante = jogo.placarMandanteTela()
    var visitante = jogo.placarVisitanteTela()

    lateinit var txtMandante: TextView
    lateinit var txtVisitante: TextView

    fun refreshScore() {
        txtMandante.text = mandante.toString()
        txtVisitante.text = visitante.toString()
    }

    val content = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(14), dp(14), dp(14), dp(14))
        addView(TextView(context).apply {
            text = jogo.modalidade?.nome ?: "Jogo"
            textSize = 12f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(context.getColor(R.color.primaryVariant))
        })
        addView(TextView(context).apply {
            text = "${jogo.nomeMandante()} x ${jogo.nomeVisitante()}"
            textSize = 16f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(context.getColor(R.color.joia_text))
        })
        addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, dp(8))

            addScoreStepper(jogo.cursoMandante(), { mandante = (mandante - 1).coerceAtLeast(0); refreshScore() }, { mandante++; refreshScore() }) {
                txtMandante = it
            }
            addView(TextView(context).apply {
                text = "x"
                gravity = Gravity.CENTER
                textSize = 18f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(context.getColor(R.color.joia_text_secondary))
                layoutParams = LinearLayout.LayoutParams(dp(28), wrap())
            })
            addScoreStepper(jogo.cursoVisitante(), { visitante = (visitante - 1).coerceAtLeast(0); refreshScore() }, { visitante++; refreshScore() }) {
                txtVisitante = it
            }
        })
        addView(MaterialButton(context).apply {
            text = "Salvar placar"
            setTextColor(context.getColor(R.color.joia_button_text))
            backgroundTintList = android.content.res.ColorStateList.valueOf(context.getColor(R.color.primary))
            layoutParams = LinearLayout.LayoutParams(match(), dp(48)).withTop(8)
            setOnClickListener { onSave(mandante, visitante) }
        })
    }

    refreshScore()
    addView(rowCard(content))
}

private fun LinearLayout.addScoreStepper(label: String, onMinus: () -> Unit, onPlus: () -> Unit, bindScore: (TextView) -> Unit) {
    addView(LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        layoutParams = LinearLayout.LayoutParams(0, wrap(), 1f)
        addView(TextView(context).apply {
            text = label.ifBlank { "Time" }
            gravity = Gravity.CENTER
            textSize = 12f
            setTextColor(context.getColor(R.color.joia_text_secondary))
        })
        addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            addView(MaterialButton(context).apply {
                text = "-"
                layoutParams = LinearLayout.LayoutParams(dp(42), dp(42))
                setOnClickListener { onMinus() }
            })
            addView(TextView(context).apply {
                bindScore(this)
                gravity = Gravity.CENTER
                textSize = 24f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(context.getColor(R.color.joia_text))
                layoutParams = LinearLayout.LayoutParams(dp(46), dp(42))
            })
            addView(MaterialButton(context).apply {
                text = "+"
                layoutParams = LinearLayout.LayoutParams(dp(42), dp(42))
                setOnClickListener { onPlus() }
            })
        })
    })
}

private fun Context.chipRow(labels: List<String>): LinearLayout {
    return LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.START
        layoutParams = LinearLayout.LayoutParams(match(), wrap()).withTop(10)
        labels.forEach { text ->
            addView(Chip(context).apply {
                this.text = text
                isClickable = false
                isCheckable = false
                textSize = 11f
                setTextColor(context.getColor(R.color.joia_text))
                chipBackgroundColor = android.content.res.ColorStateList.valueOf(context.getColor(R.color.joia_surface_variant))
                layoutParams = LinearLayout.LayoutParams(wrap(), dp(34)).apply { marginEnd = dp(6) }
            })
        }
    }
}

private fun Context.label(text: String): TextView {
    return TextView(this).apply {
        this.text = text
        textSize = 11f
        setTypeface(typeface, Typeface.BOLD)
        setTextColor(getColor(R.color.joia_button_text))
        background = rounded(getColor(R.color.primary), dp(12).toFloat())
        setPadding(dp(10), dp(5), dp(10), dp(5))
    }
}

private fun LinearLayout.rowCard(content: View, featured: Boolean = false, position: Int? = null): MaterialCardView {
    return MaterialCardView(context).apply {
        radius = dp(8).toFloat()
        cardElevation = when (position) {
            1 -> 8f
            2, 3 -> 4f
            else -> if (featured) 3f else 1.5f
        }
        strokeWidth = if (position == 1) dp(2) else dp(1)
        strokeColor = when (position) {
            1 -> 0xFFFF6B00.toInt()
            2 -> 0xFFA7B0C0.toInt()
            3 -> 0xFFC47A2C.toInt()
            else -> if (featured) context.getColor(R.color.primaryVariant) else context.getColor(R.color.joia_outline)
        }
        setCardBackgroundColor(
            when (position) {
                1 -> 0xFFFFF7ED.toInt()
                2 -> 0xFFF8FAFC.toInt()
                3 -> 0xFFFFF4E6.toInt()
                else -> context.getColor(R.color.joia_surface)
            }
        )
        layoutParams = LinearLayout.LayoutParams(match(), wrap()).withTop(10)
        addView(content)
    }
}

private fun rounded(color: Int, radius: Float): GradientDrawable {
    return GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius
    }
}

private fun roundedStroke(color: Int, strokeColor: Int, strokeWidth: Int, radius: Float): GradientDrawable {
    return GradientDrawable().apply {
        setColor(color)
        setStroke(strokeWidth, strokeColor)
        cornerRadius = radius
    }
}

private fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
private fun View.dp(value: Int): Int = context.dp(value)

private fun match(): Int = ViewGroup.LayoutParams.MATCH_PARENT
private fun wrap(): Int = ViewGroup.LayoutParams.WRAP_CONTENT

private fun LinearLayout.LayoutParams.withTop(value: Int): LinearLayout.LayoutParams {
    topMargin = value
    return this
}
