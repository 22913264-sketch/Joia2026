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
import kotlinx.coroutines.launch

class JogosFragment : Fragment() {

    private lateinit var rvJogos: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var txtMensagem: TextView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    
    // Inicializando o adapter com um listener de clique
    private val adapter = JogoAdapter { jogo ->
        // Ação ao clicar no jogo (ex: abrir detalhes)
        Toast.makeText(context, "Selecionado: ${jogo.equipeA?.nome} x ${jogo.equipeB?.nome}", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_jogos, container, false)

        rvJogos = view.findViewById(R.id.rvJogos)
        progressBar = view.findViewById(R.id.progressBar)
        txtMensagem = view.findViewById(R.id.txtMensagem)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)

        rvJogos.layoutManager = LinearLayoutManager(context)
        rvJogos.adapter = adapter

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
                val response = RetrofitClient.instance.getJogos()
                
                if (response.isSuccessful) {
                    val jogos = response.body() ?: emptyList()
                    adapter.submitList(jogos)
                    
                    if (jogos.isEmpty()) {
                        txtMensagem.visibility = View.VISIBLE
                        txtMensagem.text = "Nenhum jogo disponível no momento."
                    } else {
                        txtMensagem.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(context, "Erro ao carregar jogos", Toast.LENGTH_SHORT).show()
                    txtMensagem.visibility = View.VISIBLE
                    txtMensagem.text = "Falha na conexão com o servidor."
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro de conexão: ${e.message}", Toast.LENGTH_SHORT).show()
                txtMensagem.visibility = View.VISIBLE
                txtMensagem.text = "Verifique sua conexão com a internet."
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
