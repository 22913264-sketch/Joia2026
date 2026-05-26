package com.example.joia2026

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var rvJogos: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var txtMensagem: TextView
    private val adapter = JogoAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Configuração do preenchimento para Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicialização das Views
        rvJogos = findViewById(R.id.rvJogos)
        progressBar = findViewById(R.id.progressBar)
        txtMensagem = findViewById(R.id.txtMensagem)

        // Configuração do RecyclerView
        rvJogos.layoutManager = LinearLayoutManager(this)
        rvJogos.adapter = adapter

        // Carregar dados
        carregarJogos()
    }

    private fun carregarJogos() {
        lifecycleScope.launch {
            try {
                exibirCarregando(true)
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
                    Toast.makeText(this@MainActivity, "Erro ao carregar jogos", Toast.LENGTH_SHORT).show()
                    txtMensagem.visibility = View.VISIBLE
                    txtMensagem.text = "Falha na conexão com o servidor."
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Erro de conexão: ${e.message}", Toast.LENGTH_SHORT).show()
                txtMensagem.visibility = View.VISIBLE
                txtMensagem.text = "Verifique sua conexão com a internet."
            } finally {
                exibirCarregando(false)
            }
        }
    }

    private fun exibirCarregando(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        if (isLoading) txtMensagem.visibility = View.GONE
    }
}
