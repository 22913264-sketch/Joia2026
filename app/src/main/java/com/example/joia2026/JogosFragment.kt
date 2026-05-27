package com.example.joia2026

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch

class JogosFragment : Fragment() {

    private lateinit var rvJogos: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var txtMensagem: TextView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var chipGroupStatus: ChipGroup
    private var filtroStatus: String? = null

    private val adapter = JogoAdapter { jogo ->
        Toast.makeText(context, "Selecionado: ${jogo.nomeMandante()} x ${jogo.nomeVisitante()}", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_jogos, container, false)

        rvJogos = view.findViewById(R.id.rvJogos)
        progressBar = view.findViewById(R.id.progressBar)
        txtMensagem = view.findViewById(R.id.txtMensagem)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        chipGroupStatus = view.findViewById(R.id.chipGroupStatus)

        rvJogos.layoutManager = LinearLayoutManager(context)
        rvJogos.adapter = adapter

        chipGroupStatus.setOnCheckedStateChangeListener { _, checkedIds ->
            filtroStatus = when (checkedIds.firstOrNull()) {
                R.id.chipAoVivo -> "EM_ANDAMENTO"
                R.id.chipAgendado -> "AGENDADO"
                R.id.chipFinalizado -> "FINALIZADO"
                else -> null
            }
            carregarJogos()
        }

        swipeRefresh.setOnRefreshListener {
            carregarJogos()
        }

        carregarJogos()

        return view
    }

    private fun carregarJogos() {
        lifecycleScope.launch {
            try {
                if (!swipeRefresh.isRefreshing) exibirCarregando(true)
                val response = RetrofitClient.instance.getJogos(status = filtroStatus)

                if (response.isSuccessful) {
                    val jogos = response.body() ?: emptyList()
                    adapter.submitList(jogos)

                    if (jogos.isEmpty()) {
                        txtMensagem.visibility = View.VISIBLE
                        txtMensagem.text = "Nenhum jogo disponivel no momento."
                    } else {
                        txtMensagem.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(context, "Erro ao carregar jogos", Toast.LENGTH_SHORT).show()
                    txtMensagem.visibility = View.VISIBLE
                    txtMensagem.text = "Falha na conexao com o servidor."
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro de conexao: ${e.message}", Toast.LENGTH_SHORT).show()
                txtMensagem.visibility = View.VISIBLE
                txtMensagem.text = "Verifique sua conexao com a internet."
            } finally {
                exibirCarregando(false)
                swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun exibirCarregando(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        if (isLoading) txtMensagem.visibility = View.GONE
    }
}
