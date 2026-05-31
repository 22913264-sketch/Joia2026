package com.example.joia2026

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.launch

class FavoritosFragment : Fragment() {

    private lateinit var rvJogos: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var txtMensagem: TextView
    private lateinit var swipeRefresh: SwipeRefreshLayout

    private val adapter = JogoAdapter { jogo ->
        val intent = Intent(requireContext(), JogoDetalheActivity::class.java)
        intent.putExtra(JogoDetalheActivity.EXTRA_JOGO_ID, jogo.id)
        startActivity(intent)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_jogos, container, false)

        // Reutilizando o layout de jogos, mas ocultando o que não precisamos
        view.findViewById<TextView>(R.id.txtTituloJogos).text = "Meus Favoritos"
        view.findViewById<View>(R.id.scrollFiltros).visibility = View.GONE

        rvJogos = view.findViewById(R.id.rvJogos)
        progressBar = view.findViewById(R.id.progressBar)
        txtMensagem = view.findViewById(R.id.txtMensagem)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)

        rvJogos.layoutManager = LinearLayoutManager(context)
        rvJogos.adapter = adapter

        swipeRefresh.setOnRefreshListener {
            carregarFavoritos()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        carregarFavoritos()
    }

    private fun carregarFavoritos() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                if (!swipeRefresh.isRefreshing) progressBar.visibility = View.VISIBLE
                txtMensagem.visibility = View.GONE

                val favoritos = JoiaRepository.getFavoritos(requireContext())
                adapter.submitList(favoritos)

                if (favoritos.isEmpty()) {
                    txtMensagem.visibility = View.VISIBLE
                    txtMensagem.text = "Você ainda não favoritou nenhum jogo."
                }
            } catch (e: Exception) {
                txtMensagem.visibility = View.VISIBLE
                txtMensagem.text = "Erro ao carregar favoritos."
            } finally {
                progressBar.visibility = View.GONE
                swipeRefresh.isRefreshing = false
            }
        }
    }
}
