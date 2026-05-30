package com.example.joia2026

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var etEmail: TextInputEditText
    private lateinit var etSenha: TextInputEditText
    private lateinit var cardUltimoCadastro: View
    private lateinit var txtNomeCadastrado: TextView
    private lateinit var txtEmailCadastrado: TextView
    private lateinit var txtCpfCadastrado: TextView
    private lateinit var txtTelefoneCadastrado: TextView
    private lateinit var txtCursoCadastrado: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etSenha = findViewById(R.id.etSenha)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnIrCadastro = findViewById<TextView>(R.id.btnIrCadastro)
        cardUltimoCadastro = findViewById(R.id.cardUltimoCadastro)
        txtNomeCadastrado = findViewById(R.id.txtNomeCadastrado)
        txtEmailCadastrado = findViewById(R.id.txtEmailCadastrado)
        txtCpfCadastrado = findViewById(R.id.txtCpfCadastrado)
        txtTelefoneCadastrado = findViewById(R.id.txtTelefoneCadastrado)
        txtCursoCadastrado = findViewById(R.id.txtCursoCadastrado)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val senha = etSenha.text.toString().trim()

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val demoUser = demoLogin(email, senha)
            if (demoUser != null) {
                UserSession.saveToken(this@LoginActivity, "demo-token-${demoUser.role.lowercase()}")
                UserSession.saveLoggedUser(this@LoginActivity, demoUser)
                UserSession.markLoggedIn(this@LoginActivity)
                Toast.makeText(this@LoginActivity, "Bem-vindo, ${demoUser.nome}!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
                return@setOnClickListener
            }

            // Chamada real da API usando Coroutines
            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.instance.login(LoginRequest(email, senha))
                    
                    if (response.isSuccessful) {
                        val authResponse = response.body()
                        val token = authResponse?.token
                        
                        UserSession.saveToken(this@LoginActivity, token)
                        UserSession.saveLoggedUser(this@LoginActivity, authResponse?.user)
                        UserSession.markLoggedIn(this@LoginActivity)

                        Toast.makeText(this@LoginActivity, "Bem-vindo, ${authResponse?.user?.nome}!", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Email ou senha incorretos", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "Erro de conexão: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnIrCadastro.setOnClickListener {
            val intent = Intent(this, CadastroActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        atualizarDadosCadastrados()
    }

    private fun atualizarDadosCadastrados() {
        val userData = UserSession.getUserData(this)
        if (userData.email.isNullOrBlank()) {
            cardUltimoCadastro.visibility = View.GONE
            return
        }

        cardUltimoCadastro.visibility = View.VISIBLE
        if (etEmail.text.isNullOrBlank()) {
            etEmail.setText(userData.email)
        }
        txtNomeCadastrado.text = userData.nome.orEmpty()
        txtEmailCadastrado.text = userData.email
        txtCpfCadastrado.text = "CPF: ${userData.cpf.orEmpty()}"
        txtTelefoneCadastrado.text = "Telefone: ${userData.telefone.orEmpty()}"
        txtCursoCadastrado.text = "Curso: ${userData.cursoNome.orEmpty()}"
    }

    private fun demoLogin(email: String, senha: String): User? {
        if (email == "admin@joia.com" && senha == "admin123") {
            return User(
                id = "demo-admin",
                nome = "Administrador JOIA",
                email = email,
                role = "ADMIN",
                cursoId = "1",
                curso = Curso("1", "Sistemas de Informacao", "SI")
            )
        }

        if (email == "viewer@joia.com" && senha == "viewer123") {
            return User(
                id = "demo-viewer",
                nome = "Visualizador JOIA",
                email = email,
                role = "VIEWER",
                cursoId = "2",
                curso = Curso("2", "Direito", "DIR")
            )
        }

        return null
    }
}
